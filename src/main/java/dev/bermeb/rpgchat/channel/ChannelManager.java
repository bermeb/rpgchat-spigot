package dev.bermeb.rpgchat.channel;

import dev.bermeb.rpgchat.RPGChat;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChannelManager {
    
    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private final ConcurrentHashMap<String, ChannelConfig> channels = new ConcurrentHashMap<>();

    /**
     * Loads channel configurations from a list of strings.
     * Each string should be in the format: "name|range|color".
     * If the configuration is invalid, it logs a warning and skips that entry.
     *
     * @param channelConfigs List of channel configuration strings.
     */
    public void loadChannels(List<String> channelConfigs) {
        channels.clear();
        if (channelConfigs == null || channelConfigs.isEmpty()) {
            PLUGIN.getLogger().warning("No channel configurations provided");
            return;
        }
        
        channelConfigs.forEach(config -> {
            if (config == null || config.trim().isEmpty()) {
                PLUGIN.getLogger().warning("Skipping null or empty channel configuration");
                return;
            }
            
            String[] parts = config.split("\\|");
            if (parts.length >= 3) {
                try {
                    String name = parts[0].trim();
                    if (name.isEmpty()) {
                        PLUGIN.getLogger().warning("Channel name cannot be empty: " + config);
                        return;
                    }
                    
                    double range = Double.parseDouble(parts[1].trim());
                    String color = parts[2].trim();
                    
                    if (color.isEmpty()) {
                        PLUGIN.getLogger().warning("Channel color cannot be empty: " + config);
                        return;
                    }
                    
                    channels.put(name, new ChannelConfig(name, range, color));
                } catch (NumberFormatException e) {
                    PLUGIN.getLogger().warning("Invalid range value in channel configuration: " + config);
                } catch (Exception e) {
                    PLUGIN.getLogger().warning("Error parsing channel configuration: " + config + " - " + e.getMessage());
                }
            } else {
                PLUGIN.getLogger().warning("Invalid channel configuration format (expected: name|range|color): " + config);
            }
        });
    }

    /**
     * Checks if a channel with the specified name exists.
     *
     * @param channelName The name of the channel to check.
     * @return true if the channel exists, false otherwise.
     */
    public boolean channelExists(String channelName) {
        return channels.containsKey(channelName);
    }

    /**
     * Gets the ChannelConfig for the specified channel name.
     *
     * @param channelName The name of the channel to retrieve.
     * @return The ChannelConfig for the specified channel, or null if it does not exist.
     */
    public ChannelConfig getChannel(String channelName) {
        return channels.get(channelName);
    }

    /**
     * Gets the player's current channel, defaulting to "normal" if not set.
     *
     * @param player The player whose channel is being retrieved.
     * @return The name of the player's current channel, or "normal" if not set.
     */
    public String getPlayerChannel(Player player) {
        return player.getPersistentDataContainer().getOrDefault(
            PLUGIN.getChannelKey(), PersistentDataType.STRING, "normal"
        );
    }

    /**
     * Sets the player's channel to the specified channel if it exists.
     * If the channel does not exist, it logs a warning.
     *
     * @param player  The player whose channel is being set.
     * @param channel The name of the channel to set for the player.
     */
    public void setPlayerChannel(Player player, String channel) {
        if (player == null) {
            PLUGIN.getLogger().warning("Cannot set channel for null player");
            return;
        }
        if (channel == null || channel.trim().isEmpty()) {
            PLUGIN.getLogger().warning("Cannot set null or empty channel for player: " + player.getName());
            return;
        }
        
        if (channelExists(channel)) {
            player.getPersistentDataContainer().set(
                PLUGIN.getChannelKey(), PersistentDataType.STRING, channel
            );
        } else {
            PLUGIN.getLogger().warning("Attempted to set non-existent channel '" + channel + "' for player: " + player.getName());
        }
    }

    /**
     * Gets a list of players in the specified channel within the given range.
     * If range is -1, it returns all players in the global channel.
     *
     * @param sourcePlayer The player requesting the channel players.
     * @param channelName  The name of the channel to check.
     * @param range        The range to search for players, or -1 for global.
     * @return A list of players in the specified channel within the given range.
     */
    public List<Player> getChannelPlayers(Player sourcePlayer, String channelName, double range) {
        if (sourcePlayer == null) {
            PLUGIN.getLogger().warning("Cannot get channel players for null source player");
            return List.of();
        }
        if (channelName == null || channelName.trim().isEmpty()) {
            PLUGIN.getLogger().warning("Cannot get channel players for null or empty channel name");
            return List.of();
        }
        
        ChannelConfig config = getChannel(channelName);
        if (config == null) {
            return List.of();
        }
        
        try {
            if (range == -1) {
                // Global channel
                return sourcePlayer.getWorld().getPlayers().stream()
                    .filter(player -> player != null && hasPlayerChannel(player, channelName))
                    .collect(Collectors.toList());
            } else {
                // Range-based channel
                return sourcePlayer.getNearbyEntities(range, range, range).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .filter(player -> hasPlayerChannel(player, channelName))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            PLUGIN.getLogger().warning("Error getting channel players for channel '" + channelName + "': " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Checks if the player is in the specified channel.
     *
     * @param player      The player to check.
     * @param channelName The name of the channel to check against.
     * @return true if the player is in the specified channel, false otherwise.
     */
    private boolean hasPlayerChannel(Player player, String channelName) {
        return Objects.equals(getPlayerChannel(player), channelName);
    }
    
    public record ChannelConfig(String name, double range, String color) {}
}