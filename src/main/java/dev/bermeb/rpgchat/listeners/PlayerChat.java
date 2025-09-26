package dev.bermeb.rpgchat.listeners;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.utils.ChatUtils;
import dev.bermeb.rpgchat.config.ChatConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.chatcontrol.SenderCache;
import org.mineacademy.chatcontrol.model.Channel;
import org.mineacademy.chatcontrol.model.db.PlayerCache;

import java.util.*;

public class PlayerChat implements Listener {

    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private static final long TICKS_PER_SECOND = 20L;
    private final ChatUtils chatUtils;
    private final ChatConfig config;
    private final List<Player> cooldownList;
    private final Map<Player, String> lastMessage;
    private final int chatCooldown;

    public PlayerChat(ChatUtils chatUtils) {
        this.chatUtils = chatUtils;
        this.lastMessage = new HashMap<>();
        this.cooldownList = new ArrayList<>();
        this.config = chatUtils.getConfig();
        this.chatCooldown = Optional.of(config.behavior().cooldown()).orElse(-1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || !event.isAsynchronous() || event.getMessage().isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (cooldownList.contains(player) || isRepeatedMessage(player, message)) {
            event.setCancelled(true);
            return;
        }
        if (!isWhitelistedWorld(event.getPlayer().getWorld().getName()))
            return;

        boolean whispered = isWhisperedMessage(message);
        boolean normal = isNormalMessage(message);

        // Since you can define a symbol for normal chat, we need to check if the message starts with it,
        // if it does not, we won't process the message
        if (!normal && !whispered) {
            return;
        }

        handleCooldown(player);
        handleMessageCancellation(event, normal, whispered);
        handleAntiRepeat(player, message);

        if (handleChatControlCompatibility(player)) {
            return;
        }

        chatUtils.addToChatQueue(event.getPlayer(), message, whispered);
    }

    private boolean isWhitelistedWorld(String worldName) {
        return config.worlds().contains(worldName);
    }

    private boolean isWhisperedMessage(String message) {
        return message.startsWith(config.whispered().symbol());
    }

    private boolean isNormalMessage(String message) {
        return config.normal().symbol().isEmpty() || message.startsWith(config.normal().symbol());
    }

    private boolean handleChatControlCompatibility(Player player) {
        if (config.compatibility().chatControl().enabled()
                && PLUGIN.getServer().getPluginManager().getPlugin("ChatControl") != null) {
            final String targetName = SenderCache.from(player).getConversingPlayerName();
            if (!Objects.isNull(targetName)) {
                return true;
            }

            // Check if player is in an enabled channel for ChatControl compatibility
            return checkChatControlChannelCompatibility(player);
        }
        return false;
    }

    private void handleCooldown(Player player) {
        if (this.chatCooldown > 0 && !player.hasPermission("RPGChat.Bypass")) {
            cooldownList.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    cooldownList.remove(player);
                }
            }.runTaskLaterAsynchronously(PLUGIN, TICKS_PER_SECOND * chatCooldown);
        }
    }

    private void handleMessageCancellation(AsyncPlayerChatEvent event, boolean normal, boolean whispered) {
        if (whispered && config.whispered().hideBaseChat()) {
            event.setCancelled(true);
        }
        if (normal && config.normal().hideBaseChat()) {
            event.setCancelled(true);
        }
    }

    // If anti-repeat is enabled, store the last message sent by the player and check
    private void handleAntiRepeat(Player player, String message) {
        lastMessage.remove(player);
        if (config.behavior().antiRepeat() && !player.hasPermission("RPGChat.Bypass")) {
            lastMessage.put(player, message);
        }
    }

    // Check if the message is a repeated message by comparing it with the last message sent by the player
    private boolean isRepeatedMessage(Player player, String message) {
        return lastMessage.get(player) != null && message.equalsIgnoreCase(lastMessage.get(player));
    }

    private boolean checkChatControlChannelCompatibility(Player player) {
        try {
            final PlayerCache playerCache = PlayerCache.fromCached(player);
            final Channel writeChannel = playerCache.getWriteChannel();

            if (writeChannel != null) {
                final String channelName = writeChannel.getName();
                final List<String> enabledChannels = config.compatibility().chatControl().enabledChannels();

                // If player is in a channel that's not in the enabled_channels list, don't show RPGChat bubble
                if (!enabledChannels.contains(channelName)) {
                    return true; // Return true to skip RPGChat processing
                }
            }
        } catch (Exception e) {
            // If there's any error with ChatControl integration, log it and continue
            PLUGIN.getLogger().warning("Error checking ChatControl channel compatibility: " + e.getMessage());
        }

        return false; // Allow RPGChat to continue processing
    }
}