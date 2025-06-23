package dev.bermeb.rpgchat.display;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.config.ChatConfig;
import dev.bermeb.rpgchat.sound.SoundManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NormalMessageDisplay implements MessageDisplayStrategy {
    
    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private static final long CHAT_TASK_DELAY = 0L;
    private static final long CHAT_TASK_PERIOD = 1L;
    private static final long TICKS_PER_SECOND = 20L;
    private static final int SHOW_TIME_OFFSET = 10;
    
    private ChatConfig config;

    private final SoundManager soundManager;
    private final List<ArmorStand> armorStands = new ArrayList<>();
    private final ConcurrentHashMap<Player, Integer> displayProgress = new ConcurrentHashMap<>();
    
    public NormalMessageDisplay(ChatConfig config, SoundManager soundManager) {
        this.config = config;
        this.soundManager = soundManager;
    }
    
    @Override
    public void displayMessage(Player player, String message) {
        ArmorStand armorStand = createArmorStand(player);
        displayProgress.put(player, 0);
        
        new BukkitRunnable() {
            private final StringBuilder currentName = new StringBuilder();

            @Override
            public void run() {

                armorStand.teleport(player.getEyeLocation().add(0, config.height(), 0));
                
                Integer currentIndex = displayProgress.get(player);
                if (currentIndex != null) {
                    int i = currentIndex;
                    
                    if (i < message.length()) {
                        currentName.append(message.charAt(i));
                        updateArmorStandName(armorStand, currentName.toString());
                    }
                    
                    i++;
                    
                    if (i == (getMinShowTime(message) * TICKS_PER_SECOND + SHOW_TIME_OFFSET)) {
                        armorStands.remove(armorStand);
                        armorStand.remove();
                        displayProgress.remove(player);
                        cancel();
                        return;
                    } else if (i == (message.length() + 1)) {
                        soundManager.playNormalChatSound(player, config.normal().sound());
                    }
                    displayProgress.put(player, i);
                }
            }
        }.runTaskTimer(PLUGIN, CHAT_TASK_DELAY, CHAT_TASK_PERIOD);
    }
    
    private ArmorStand createArmorStand(Player player) {
        ArmorStand armorStand = player.getWorld()
                .spawn(player.getEyeLocation().add(0, config.height(), 0), ArmorStand.class);

        armorStand.setAI(false);
        armorStand.setMarker(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setVisible(false);
        armorStand.setCollidable(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomNameVisible(true);

        String initialName = ChatColor.translateAlternateColorCodes('&', config.normal().color());

        armorStand.setCustomName(initialName);

        armorStands.add(armorStand);
        return armorStand;
    }

    private void updateArmorStandName(ArmorStand armorStand, String newText) {
        String colorCode = config.normal().color();
        String fullName = colorCode + newText;
        String processedName = ChatColor.translateAlternateColorCodes('&', fullName);

        armorStand.setCustomName(processedName);
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
        armorStands.forEach(ArmorStand::remove);
        armorStands.clear();
        displayProgress.clear();
    }
}