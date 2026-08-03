package net.nightcube.staffchat;

import org.bukkit.plugin.java.JavaPlugin;

public final class Staffchat extends JavaPlugin {

    private DiscordManager discordManager;
    private NicknameManager nicknameManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        nicknameManager = new NicknameManager(this);

        discordManager = new DiscordManager(this);
        discordManager.connect();

        if (getCommand("sc") != null) {
            getCommand("sc").setExecutor(new StaffChatCommand(this));
        }
        if (getCommand("setnick") != null) {
            getCommand("setnick").setExecutor(new SetNickCommand(this));
        }
        if (getCommand("screload") != null) {
            getCommand("screload").setExecutor(new ReloadCommand(this));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (discordManager != null) {
            discordManager.disconnect();
        }
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public NicknameManager getNicknameManager() {
        return nicknameManager;
    }
}
