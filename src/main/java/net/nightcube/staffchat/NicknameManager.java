package net.nightcube.staffchat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NicknameManager {

    private final Staffchat plugin;
    private File file;
    private FileConfiguration config;

    public NicknameManager(Staffchat plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "nicknames.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create nicknames.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save nicknames.yml!");
        }
    }

    public void displayNickname(UUID uuid, String nickname) {
        System.out.println("Nickname for " + uuid.toString() + " is set to " + nickname);
    }

    public void setNickname(UUID uuid, String nickname) {
        setNickname(uuid.toString(), nickname);
    }

    public void setNickname(String id, String nickname) {
        config.set(id, nickname);
        save();
    }

    public String getNickname(UUID uuid) {
        return getNickname(uuid.toString());
    }

    public String getNickname(String id) {
        return config.getString(id);
    }
}

