package net.nightcube.staffchat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

public class DiscordManager extends ListenerAdapter {

    private final Staffchat plugin;
    private JDA jda;
    private String channelId;
    private String serverName;
    private boolean discordHandler;
    private WebhookClient webhookClient;
    private String webhookUrl;

    public DiscordManager(Staffchat plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String token = plugin.getConfig().getString("discord.token");
        channelId = plugin.getConfig().getString("discord.channel-id");
        serverName = plugin.getConfig().getString("server-name", "Server");
        discordHandler = plugin.getConfig().getBoolean("discord-handler", false);

        if (token == null || token.isEmpty()) {
            plugin.getLogger().warning("Discord token not set in config.yml!");
            return;
        }

        plugin.getLogger().info("Discord Handler Status: " + discordHandler);

        // Run connection asynchronously to avoid blocking the main thread
        new Thread(() -> {
            try {
                jda = JDABuilder.createDefault(token)
                        .enableIntents(EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                        .addEventListeners(this)
                        .build();
                jda.awaitReady();
                plugin.getLogger().info("Connected to Discord!");

                setupWebhook();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to connect to Discord: " + e.getMessage());
            }
        }).start();
    }

    private void setupWebhook() {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null) {
            plugin.getLogger().warning("Channel not found!");
            return;
        }

        channel.retrieveWebhooks().queue(webhooks -> {
            for (Webhook webhook : webhooks) {
                if (webhook.getName().equalsIgnoreCase("StaffChatNightCube")) {
                    webhookUrl = webhook.getUrl();
                    webhookClient = WebhookClient.withUrl(webhookUrl);
                    plugin.getLogger().info("Found existing webhook.");
                    return;
                }
            }

            // Create new webhook if not found
            channel.createWebhook("StaffChatNightCube").queue(webhook -> {
                webhookUrl = webhook.getUrl();
                webhookClient = WebhookClient.withUrl(webhookUrl);
                plugin.getLogger().info("Created new webhook.");
            });
        });
    }

    public void disconnect() {
        if (webhookClient != null) {
            webhookClient.close();
        }
        if (jda != null) {
            jda.shutdown();
        }
    }

    public void sendToDiscord(String username, java.util.UUID uuid, String message) {
        if (webhookClient == null) {
            plugin.getLogger().warning("WebhookClient is null! Message not sent to Discord.");
            return;
        }

        String displayName = username;
        String avatarUrl = "https://cdn.discordapp.com/embed/avatars/0.png";

        if (uuid != null) {
            String nick = plugin.getNicknameManager().getNickname(uuid);
            if (nick != null) {
                displayName = nick;
            }
            avatarUrl = "https://api.mcheads.org/head/" + displayName + "?overlay";
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(displayName + " | " + serverName);
        builder.setAvatarUrl(avatarUrl);
        builder.setContent(message);

        webhookClient.send(builder.build());
    }

    public void sendSyncMessage(String id, String nick) {
        if (webhookClient == null) return;
        // Send a hidden sync message
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("StaffChatSystem");
        builder.setAvatarUrl("https://cdn.discordapp.com/embed/avatars/0.png");
        builder.setContent("||[SyncNick] " + id + ":" + nick + "||"); // Spoiler to make it less obtrusive if seen
        webhookClient.send(builder.build());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (channelId == null || !event.getChannel().getId().equals(channelId)) return;

        String content = event.getMessage().getContentDisplay();
        String authorName = event.getAuthor().getName();

        // Handle Webhook messages (Cross-Server Chat)
        if (event.isWebhookMessage()) {
            // Handle SyncNick messages
            if (content.contains("[SyncNick] ")) {
                String cleanContent = content.replace("||", "").trim();
                if (cleanContent.startsWith("[SyncNick] ")) {
                    String data = cleanContent.substring(11);
                    String[] parts = data.split(":", 2);
                    if (parts.length == 2) {
                        String id = parts[0];
                        String nick = parts[1];
                        plugin.getNicknameManager().setNickname(id, nick);
                        plugin.getLogger().info("Synced nickname for " + id + " to " + nick);

                        // If we are the handler, delete this system message to keep chat clean
                        if (discordHandler) {
                            try {
                                event.getMessage().delete().queueAfter(2, java.util.concurrent.TimeUnit.SECONDS);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                    }
                }
                return;
            }

            // Check if username contains " | " separator
            if (authorName.contains(" | ")) {
                String[] parts = authorName.split(" \\| ", 2);
                String user = parts[0];
                String originServer = parts[1];

                // Ignore messages from this server
                if (serverName != null && serverName.equals(originServer)) {
                    return;
                }

                String format = plugin.getConfig().getString("messages.cross-server-format", "&8[&cStaffChat&8] &e[%server%] &f%player%: &7%message%");
                String formattedMessage = ChatColor.translateAlternateColorCodes('&', format
                        .replace("%server%", originServer)
                        .replace("%player%", user)
                        .replace("%message%", content));

                broadcastToStaff(formattedMessage);
                plugin.getLogger().info("[StaffChat] [" + originServer + "] " + user + ": " + content);
                return;
            }
        }

        // Handle normal Discord user messages (ignore bots to prevent loops/spam)
        if (event.getAuthor().isBot()) return;

        // Only handle Discord interactions if this server is the designated handler
        if (discordHandler) {
            // Handle /setnick command
            if (content.startsWith("/setnick ")) {
                String newNick = content.substring(9).trim();
                if (!newNick.isEmpty()) {
                    // Send sync message instead of just local set
                    sendSyncMessage(event.getAuthor().getId(), newNick);

                    try {
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("Nickname set to: " + newNick).queue(msg -> msg.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to delete message or send reply: " + e.getMessage());
                    }
                }
                return;
            }

            // Replace user message with Webhook message
            try {
                event.getMessage().delete().queue();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete user message: " + e.getMessage());
            }

            String displayName = authorName;
            String nick = plugin.getNicknameManager().getNickname(event.getAuthor().getId());
            if (nick != null) {
                displayName = nick;
            }
            String avatarUrl = event.getAuthor().getAvatarUrl();
            if (avatarUrl == null) avatarUrl = event.getAuthor().getDefaultAvatarUrl();

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            // Use "DC" instead of "Discord" to avoid invalid username error
            builder.setUsername(displayName + " | DC");
            builder.setAvatarUrl(avatarUrl);
            builder.setContent(content);

            if (webhookClient != null) {
                webhookClient.send(builder.build());
            } else {
                plugin.getLogger().warning("WebhookClient is null! Cannot replace user message.");
            }
            // Return if none of the above conditions matched --- Random debugging safeguard
            return;
        }
    }

    private void broadcastToStaff(String message) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("staffchat.see")) {
                    p.sendMessage(message);
                }
            }
        });
    }
}

