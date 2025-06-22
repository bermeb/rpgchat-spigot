package dev.bermeb.rpgchat.sound;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.config.ChatConfig;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

// TODO: Change deprecated Sound API to the new one in 1.20+
public class SoundManager {
    
    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    
    public void playNormalChatSound(Player player, ChatConfig.SoundConfig soundConfig) {
        if (soundConfig.sound() != null) {
            player.getWorld().playSound(player.getLocation(), soundConfig.sound(), 
                soundConfig.volume(), soundConfig.pitch());
        }
    }
    
    public void playWhisperedChatSound(Location location, List<Player> players, ChatConfig.SoundConfig soundConfig) {
        if (soundConfig.sound() != null) {
            players.forEach(player -> 
                player.playSound(location, soundConfig.sound(), soundConfig.volume(), soundConfig.pitch())
            );
        }
    }
    
    public void playChannelChatSound(Location location, List<Player> players, ChatConfig.SoundConfig soundConfig) {
        if (soundConfig.sound() != null) {
            players.forEach(player -> 
                player.playSound(location, soundConfig.sound(), soundConfig.volume(), soundConfig.pitch())
            );
        }
    }

    /**
     * Parses a sound name from a string and returns the corresponding Sound enum.
     * If the sound name is invalid, it logs a warning and returns null.
     *
     * @param soundName The name of the sound to parse.
     * @return The corresponding Sound enum, or null if the name is invalid.
     */
    public static Sound parseSound(String soundName) {
        if (soundName == null || soundName.isEmpty()) {
            return null;
        }
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            PLUGIN.getLogger().warning("Invalid sound name: " + soundName);
            return null;
        }
    }
}