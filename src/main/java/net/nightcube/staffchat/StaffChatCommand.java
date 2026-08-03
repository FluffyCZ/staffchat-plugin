package net.nightcube.staffchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class StaffChatCommand implements CommandExecutor {

    private final Staffchat plugin;

    public StaffChatCommand(Staffchat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staffchat.use")) {
            String noPerm = plugin.getConfig().getString("messages.no-permission");
            if (noPerm != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPerm));
            }
            return true;
        }

        if (args.length == 0) {
            String usage = plugin.getConfig().getString("messages.usage");
            if (usage != null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
            }
            return true;
        }

        String message = String.join(" ", args);
        String format = plugin.getConfig().getString("messages.staffchat-format");
        if (format == null) format = "&8[&cStaffChat&8] &f%player%: &7%message%";

        String playerName = sender instanceof Player ? sender.getName() : "Console";

        String formattedMessage = ChatColor.translateAlternateColorCodes('&', format
                .replace("%player%", playerName)
                .replace("%message%", message));

        // Send to all players with permission
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("staffchat.see")) {
                p.sendMessage(formattedMessage);
            }
        }
        // Send to console
        Bukkit.getConsoleSender().sendMessage(formattedMessage);

        // Send to Discord
        if (sender instanceof Player) {
            Player player = (Player) sender;
            plugin.getDiscordManager().sendToDiscord(player.getName(), player.getUniqueId(), message);
        } else {
            plugin.getDiscordManager().sendToDiscord("Console", null, message);
        }

        return true;
    }
}

