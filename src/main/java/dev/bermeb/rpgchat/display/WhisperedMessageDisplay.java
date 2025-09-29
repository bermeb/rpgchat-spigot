package dev.bermeb.rpgchat.display;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.config.ChatConfig;
import dev.bermeb.rpgchat.server.IWharStand;
import dev.bermeb.rpgchat.server.NMSHandler;
import dev.bermeb.rpgchat.sound.SoundManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WhisperedMessageDisplay implements MessageDisplayStrategy {

    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private static final long CHAT_TASK_DELAY = 0L;
    private static final long CHAT_TASK_PERIOD = 1L;
    private static final long TICKS_PER_SECOND = 20L;
    private static final int SHOW_TIME_OFFSET = 10;

    private ChatConfig config;

    private final SoundManager soundManager;
    private final NMSHandler nmsHandler;
    private final ConcurrentHashMap<Player, IWharStand> playerWharStands = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, Integer> displayProgress = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, BukkitRunnable> activeTasks = new ConcurrentHashMap<>();

    public WhisperedMessageDisplay(ChatConfig config, SoundManager soundManager, NMSHandler nmsHandler) {
        this.config = config;
        this.soundManager = soundManager;
        this.nmsHandler = nmsHandler;
    }

    @Override
    public void displayMessage(Player player, String message) {
        List<Player> playersInRange = getPlayersInRange(player, config.whispered().range());
        IWharStand wharStand = createWharStand(player, playersInRange);

        displayProgress.put(player, 0);

        BukkitRunnable displayTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Teleport the WharStand to the player's eye location with the specified height
                wharStand.teleport(player.getEyeLocation().add(0, config.height(), 0));

                Integer currentIndex = displayProgress.get(player);
                if (currentIndex != null) {
                    int i = currentIndex;

                    if (i < message.length()) {
                        wharStand.appendToCustomName(String.valueOf(message.charAt(i)));
                    }

                    wharStand.reloadEntity(); // Needs to be reloaded to update the name and location with new packets

                    i++;

                    if (i == (getMinShowTime(message) * TICKS_PER_SECOND + SHOW_TIME_OFFSET)) {
                        playerWharStands.remove(player);
                        wharStand.destroyEntity();
                        displayProgress.remove(player);
                        activeTasks.remove(player);
                        cancel();
                        return;
                    } else if (i == message.length() + 1) {
                        soundManager.playWhisperedChatSound(player.getLocation(), playersInRange, config.whispered().sound());
                    }
                    displayProgress.put(player, i);
                }
            }
        };

        activeTasks.put(player, displayTask);
        displayTask.runTaskTimer(PLUGIN, CHAT_TASK_DELAY, CHAT_TASK_PERIOD);
    }

    private IWharStand createWharStand(Player player, List<Player> playersInRange) {
        // Cancel any existing task for this player
        BukkitRunnable existingTask = activeTasks.get(player);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Remove existing whar stand for this player if it exists
        IWharStand existingStand = playerWharStands.get(player);
        if (existingStand != null) {
            existingStand.destroyEntity();
        }

        IWharStand wharStand = nmsHandler.getWharStand(
                player.getEyeLocation().add(0, config.height(), 0),
                playersInRange
        );

        String customName = config.customPrefixes() ?
                getPrefix(player) : config.whispered().color();

        wharStand.setName(customName);

        playerWharStands.put(player, wharStand);
        return wharStand;
    }

    private List<Player> getPlayersInRange(Player player, double range) {
        List<Player> players = player.getNearbyEntities(range, range, range)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
        players.add(player);
        return players;
    }

    private String getPrefix(Player player) {
        List<String> prefixes = config.prefixes();
        if (prefixes == null || prefixes.isEmpty()) {
            return config.whispered().color();
        }

        return prefixes.stream()
                .map(s -> s.split("\\|"))
                .filter(rank -> rank.length >= 2 && player.hasPermission("RPGChat.Prefix." + rank[0]))
                .map(rank -> rank[1])
                .findFirst()
                .orElse(config.whispered().color());
    }

    private int getMinShowTime(String message) {
        return (int) Math.ceil(message.length() / (double) TICKS_PER_SECOND) + config.duration();
    }

    @Override
    public void reloadConfig(ChatConfig newConfig) {
        this.config = newConfig;
    }

    @Override
    public void cleanup() {
        activeTasks.values().forEach(BukkitRunnable::cancel);
        activeTasks.clear();
        playerWharStands.values().forEach(IWharStand::destroyEntity);
        playerWharStands.clear();
        displayProgress.clear();
    }

    @Override
    public void cleanupPlayer(Player player) {
        BukkitRunnable task = activeTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
        displayProgress.remove(player);
        IWharStand wharStand = playerWharStands.remove(player);
        if (wharStand != null) {
            wharStand.destroyEntity();
        }
    }
}