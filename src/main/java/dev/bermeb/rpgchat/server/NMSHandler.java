package dev.bermeb.rpgchat.server;

import dev.bermeb.rpgchat.RPGChat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NMSHandler {

    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private final static String PACKAGE_NAME = "dev.bermeb.rpgchat.nms.";

    private final String version;

    public NMSHandler() {
        this.version = getServerVersion();
    }

    // This method retrieves the server version and maps it to the NMS version
    private String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        // Since 1.20 the package name can be in a different format we check if it contains "craftbukkit"
        // If yes, we need a different approach to get the NMS version
        if(packageName.substring(packageName.lastIndexOf('.') + 1).equals("craftbukkit")){
            return getVersionByBukkitVersion(Bukkit.getServer().getBukkitVersion());
        }
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * Returns the NMS version used by the server.
     *
     * @return The NMS version as a string.
     */
    public IWharStand getWharStand(Location loc, List<Player> playerList) {
        String className = PACKAGE_NAME + version + ".WharStand";
        try {
            Class<?> clazz = Class.forName(className);
            if (IWharStand.class.isAssignableFrom(clazz)) {
                return (IWharStand) clazz.getConstructor(Location.class, List.class).newInstance(loc, playerList);
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException | IllegalAccessException exception) {
            PLUGIN.logSevereError("Could not find entity.", exception, version);
        }
        return null;
    }

    /**
     * Returns the entity register for the current NMS version.
     *
     * @return An instance of IEntityRegister for the current NMS version, or null if it could not be created.
     */
    public IEntityRegister getEntityRegister() {
        String className = PACKAGE_NAME + version + ".EntityRegister";
        try {
            Class<?> clazz = Class.forName(className);
            if (IEntityRegister.class.isAssignableFrom(clazz)) {
                return (IEntityRegister) clazz.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException | IllegalAccessException exception) {
            PLUGIN.logSevereError("Could not register entity.", exception, version);
        }
        return null;
    }

    // Since the change in 1.20+ versions where the version is not in the format of "v1_20_R1",
    // we need to map the bukkit version to the NMS version manually
    private String getVersionByBukkitVersion(String bukkitVersion) {
        return switch (bukkitVersion) {
            case "1.20-R0.1-SNAPSHOT", "1.20.1-R0.1-SNAPSHOT" -> "v1_20_R1";
            case "1.20.2-R0.1-SNAPSHOT" -> "v1_20_R2";
            case "1.20.3-R0.1-SNAPSHOT", "1.20.4-R0.1-SNAPSHOT" -> "v1_20_R3";
            case "1.20.5-R0.1-SNAPSHOT", "1.20.6-R0.1-SNAPSHOT" -> "v1_20_R4";
            case "1.21.1-R0.1-SNAPSHOT" -> "v1_21_R1";
            case "1.21.3-R0.1-SNAPSHOT" -> "v1_21_R2";
            case "1.21.4-R0.1-SNAPSHOT" -> "v1_21_R3";
            case "1.21.5-R0.1-SNAPSHOT" -> "v1_21_R4";
            default -> "unidentified Bukkit version: " + bukkitVersion;
        };
    }
}
