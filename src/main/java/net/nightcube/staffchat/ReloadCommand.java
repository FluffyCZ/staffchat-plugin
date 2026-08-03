package net.nightcube.staffchat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final Staffchat plugin;

    public ReloadCommand(Staffchat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("staffchat.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        plugin.reloadConfig();
        plugin.getNicknameManager().load();

        // Reconnect Discord
        plugin.getDiscordManager().disconnect();
        plugin.getDiscordManager().connect();

        String message = plugin.getConfig().getString("messages.reload-success", "&aConfiguration reloaded!");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return true;
    }
}

