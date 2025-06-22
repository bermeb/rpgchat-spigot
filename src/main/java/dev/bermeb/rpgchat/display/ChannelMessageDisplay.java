package dev.bermeb.rpgchat.display;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.channel.ChannelManager;
import dev.bermeb.rpgchat.config.ChatConfig;
import dev.bermeb.rpgchat.server.IWharStand;
import dev.bermeb.rpgchat.server.NMSHandler;
import dev.bermeb.rpgchat.sound.SoundManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ChannelMessageDisplay implements MessageDisplayStrategy {
    
    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private static final long CHAT_TASK_DELAY = 0L;
    private static final long CHAT_TASK_PERIOD = 1L;
    private static final long TICKS_PER_SECOND = 20L;
    private static final int SHOW_TIME_OFFSET = 10;
    private static final int MESSAGE_COMPLETE_OFFSET = 1;
    
    private final ChatConfig config;
    private final SoundManager soundManager;
    private final NMSHandler nmsHandler;
    private final ChannelManager channelManager;
    private final List<IWharStand> wharStands = new ArrayList<>();
    
    public ChannelMessageDisplay(ChatConfig config, SoundManager soundManager, NMSHandler nmsHandler, ChannelManager channelManager) {
        this.config = config;
        this.soundManager = soundManager;
        this.nmsHandler = nmsHandler;
        this.channelManager = channelManager;
    }
    
    public void displayChannelMessage(Player player, String message, String channelName) {
        if (player == null || message == null || channelName == null) {
            PLUGIN.getLogger().warning("Invalid parameters for displayChannelMessage: player=" + player + ", message=" + message + ", channelName=" + channelName);
            return;
        }
        
        ChannelManager.ChannelConfig channelConfig = channelManager.getChannel(channelName);
        if (channelConfig == null) {
            PLUGIN.getLogger().warning("Channel configuration not found: " + channelName);
            return;
        }
        
        List<Player> channelPlayers = channelManager.getChannelPlayers(player, channelName, channelConfig.range());
        if (channelPlayers == null || channelPlayers.isEmpty()) {
            PLUGIN.getLogger().info("No players found in channel: " + channelName);
            return;
        }
        
        IWharStand wharStand = createChannelWharStand(player, channelPlayers, channelConfig);
        if (wharStand == null) {
            PLUGIN.getLogger().warning("Failed to create WharStand for player: " + player.getName());
            return;
        }

        new BukkitRunnable() {
            int i = 0;
            
            @Override
            public void run() {
                try {
                    if (!player.isOnline()) {
                        cleanup(wharStand);
                        cancel();
                        return;
                    }
                    
                    wharStand.teleport(player.getEyeLocation().add(0, config.height(), 0));
                    
                    if (i < message.length()) {
                        wharStand.appendToCustomName(String.valueOf(message.charAt(i)));
                        wharStand.reloadEntity(); // Needs to be reloaded to update the name and location with new packets
                        i++;
                    } else if (i == message.length() + MESSAGE_COMPLETE_OFFSET) {
                        soundManager.playChannelChatSound(player.getLocation(), channelPlayers, config.normal().sound());
                        i++;
                    } else if (i == getMinShowTime(message) * TICKS_PER_SECOND + SHOW_TIME_OFFSET) {
                        cleanup(wharStand);
                        cancel();
                    } else {
                        i++;
                    }
                } catch (Exception e) {
                    PLUGIN.getLogger().warning("Error in chat display task for player " + player.getName() + ": " + e.getMessage());
                    cleanup(wharStand);
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, CHAT_TASK_DELAY, CHAT_TASK_PERIOD);
    }
    
    @Override
    public void displayMessage(Player player, String message) {
        String channelName = channelManager.getPlayerChannel(player);
        displayChannelMessage(player, message, channelName);
    }
    
    private IWharStand createChannelWharStand(Player player, List<Player> channelPlayers, ChannelManager.ChannelConfig channelConfig) {
        IWharStand wharStand = nmsHandler.getWharStand(
            player.getEyeLocation().add(0, config.height(), 0),
            channelPlayers
        );
        
        String customName = ChatColor.translateAlternateColorCodes('&', channelConfig.color());
        wharStand.setName(customName);
        
        wharStands.add(wharStand);
        return wharStand;
    }
    
    private int getMinShowTime(String message) {
        return (int) Math.ceil(message.length() / (double) TICKS_PER_SECOND) + config.duration();
    }
    
    private void cleanup(IWharStand wharStand) {
        wharStands.remove(wharStand);
        wharStand.destroyEntity();
    }
    
    @Override
    public void cleanup() {
        wharStands.forEach(stand -> {
            try {
                stand.destroyEntity();
            } catch (Exception e) {
                PLUGIN.getLogger().warning("Error destroying WharStand: " + e.getMessage());
            }
        });
        wharStands.clear();
    }
}