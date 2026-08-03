package net.nightcube.staffchat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetNickCommand implements CommandExecutor {

    private final Staffchat plugin;

    public SetNickCommand(Staffchat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("staffchat.setnick")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /setnick <nickname>");
            return true;
        }

        String nickname = args[0];
        plugin.getNicknameManager().setNickname(player.getUniqueId(), nickname);
        sender.sendMessage(ChatColor.GREEN + "Your staffchat nickname has been set to: " + nickname);

        // Optionally display the nickname in console for verification
        plugin.getNicknameManager().displayNickname(player.getUniqueId(), nickname);

        return true;
    }
}

