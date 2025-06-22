package dev.bermeb.rpgchat.server;

import org.bukkit.Location;

public interface IWharStand {
    void doModifiers();
    void teleport(Location loc);
    void setName(String name);
    void appendToCustomName(String append);
    void destroyEntity();
    void reloadEntity();
}
