package dev.bermeb.rpgchat.display;

import org.bukkit.entity.Player;

public interface MessageDisplayStrategy {
    void displayMessage(Player player, String message);
    void cleanup();
}