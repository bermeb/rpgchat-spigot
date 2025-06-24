package dev.bermeb.rpgchat.display;

import dev.bermeb.rpgchat.config.ChatConfig;
import org.bukkit.entity.Player;

public interface MessageDisplayStrategy {
    void displayMessage(Player player, String message);
    void cleanup();
    void reloadConfig(ChatConfig config);
}