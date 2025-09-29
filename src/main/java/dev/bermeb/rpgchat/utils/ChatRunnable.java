package dev.bermeb.rpgchat.utils;

import com.mojang.datafixers.util.Pair;
import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.queue.ChatQueueManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ChatRunnable extends BukkitRunnable {

    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);

    private final ChatUtils chatUtils;
    private final ChatQueueManager queueManager;

    //The Pair has as first value the current time in seconds and as second value the minimum showtime in seconds
    //So it is like this: Pair<CurrentTime, MinShowTime>
    private final ConcurrentHashMap<Player, Pair<Integer, Integer>> chatMap = new ConcurrentHashMap<>();

    public ChatRunnable(ChatUtils chatUtils) {
        this.chatUtils = chatUtils;
        this.queueManager = chatUtils.getQueueManager();
        runTaskTimer(PLUGIN, 0L, 20L);
    }

    @Override
    public void run() {
        try {
            processChatQueues();
            cleanUpChatMap();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void processChatQueues() {
        processNormalQueue();
        processWhisperedQueue();
        processChannelQueue();
    }

    private void processNormalQueue() {
        queueManager.getAllNormalQueues().forEach((player, messageList) -> {
            if (player == null || !player.isOnline()) {
                return;
            }

            boolean canProcess = !chatMap.containsKey(player);
            boolean hasNewMessagePriority = chatUtils.getConfig().newMessagePriority();

            if ((canProcess || hasNewMessagePriority) && !messageList.isEmpty()) {
                if (hasNewMessagePriority && chatMap.containsKey(player)) {
                    chatUtils.removePlayerStands(player);
                }

                String message = messageList.get(0); // Using .get(0) for Java 17 Support
                chatUtils.displayNormalMessage(player, message);
                messageList.remove(0); // Using .remove(0) for Java 17 Support

                if (messageList.isEmpty()) {
                    queueManager.getAllNormalQueues().remove(player);
                }

                chatMap.put(player, Pair.of(0, chatUtils.getMinShowTime(message)));
            }
        });
    }

    private void processWhisperedQueue() {
        queueManager.getAllWhisperedQueues().forEach((player, messageList) -> {
            if (player == null || !player.isOnline()) {
                return;
            }

            boolean canProcess = !chatMap.containsKey(player);
            boolean hasNewMessagePriority = chatUtils.getConfig().newMessagePriority();

            if ((canProcess || hasNewMessagePriority) && !messageList.isEmpty()) {
                if (hasNewMessagePriority && chatMap.containsKey(player)) {
                    chatUtils.removePlayerStands(player);
                }

                String message = messageList.get(0); // Using .get(0) for Java 17 Support
                chatUtils.displayWhisperedMessage(player, message);
                messageList.remove(0); // Using .remove(0) for Java 17 Support

                if (messageList.isEmpty()) {
                    queueManager.getAllWhisperedQueues().remove(player);
                }

                chatMap.put(player, Pair.of(0, chatUtils.getMinShowTime(message)));
            }
        });
    }

    private void processChannelQueue() {
        queueManager.getAllChannelQueues().forEach((player, channelMap) -> {
            if (player == null || !player.isOnline()) {
                return;
            }

            boolean canProcess = !chatMap.containsKey(player);
            boolean hasNewMessagePriority = chatUtils.getConfig().newMessagePriority();

            if (canProcess || hasNewMessagePriority) {
                if (hasNewMessagePriority && chatMap.containsKey(player)) {
                    chatUtils.removePlayerStands(player);
                }

                channelMap.forEach((channel, messageList) -> {
                    if (!messageList.isEmpty()) {
                        String message = messageList.get(0); // Using .get(0) for Java 17 Support
                        chatUtils.displayChannelMessage(player, message, channel);
                        messageList.remove(0); // Using .remove(0) for Java 17 Support

                        if (messageList.isEmpty()) {
                            channelMap.remove(channel);
                        }

                        chatMap.put(player, Pair.of(0, chatUtils.getMinShowTime(message)));
                    }
                });

                // Remove empty channel map
                if (channelMap.isEmpty()) {
                    queueManager.getAllChannelQueues().remove(player);
                }
            }
        });
    }

    private void cleanUpChatMap() {
        chatMap.entrySet().removeIf(entry -> {
            Player player = entry.getKey();
            if (player == null || !player.isOnline()) {
                return true;
            }

            Pair<Integer, Integer> pair = entry.getValue();
            int currentTime = pair.getFirst();
            int minShowTime = pair.getSecond();

            if (currentTime < minShowTime) {
                chatMap.put(player, Pair.of(currentTime + 1, minShowTime));
                return false;
            }

            return true;
        });
    }
}