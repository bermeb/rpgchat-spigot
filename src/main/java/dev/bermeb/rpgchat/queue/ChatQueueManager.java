package dev.bermeb.rpgchat.queue;

import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatQueueManager {

    private final ConcurrentHashMap<Player, CopyOnWriteArrayList<String>> normalChatQueue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, CopyOnWriteArrayList<String>> whisperedChatQueue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> channelChatQueue = new ConcurrentHashMap<>();

    public void addToNormalQueue(Player player, String message) {
        CopyOnWriteArrayList<String> queue = normalChatQueue.computeIfAbsent(player, k -> new CopyOnWriteArrayList<>());
        queue.add(message);
    }

    public void addToWhisperedQueue(Player player, String message) {
        CopyOnWriteArrayList<String> queue = whisperedChatQueue.computeIfAbsent(player, k -> new CopyOnWriteArrayList<>());
        queue.add(message);
    }

    public void addToChannelQueue(Player player, String channel, String message) {
        ConcurrentHashMap<String, CopyOnWriteArrayList<String>> playerChannels =
                channelChatQueue.computeIfAbsent(player, k -> new ConcurrentHashMap<>());
        CopyOnWriteArrayList<String> queue = playerChannels.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>());
        queue.add(message);
    }

    public ConcurrentHashMap<Player, CopyOnWriteArrayList<String>> getAllNormalQueues() {
        return normalChatQueue;
    }

    public ConcurrentHashMap<Player, CopyOnWriteArrayList<String>> getAllWhisperedQueues() {
        return whisperedChatQueue;
    }

    public ConcurrentHashMap<Player, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> getAllChannelQueues() {
        return channelChatQueue;
    }
}