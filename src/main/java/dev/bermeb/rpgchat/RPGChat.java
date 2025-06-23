package dev.bermeb.rpgchat;

import dev.bermeb.rpgchat.commands.RPGChatCommands;
import dev.bermeb.rpgchat.listeners.PlayerChat;
import dev.bermeb.rpgchat.utils.ChatOptions;
import dev.bermeb.rpgchat.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPGChat extends JavaPlugin {

    private Logger logger;
    private ChatUtils chatUtils;
    private ChatOptions chatOptions;
    private NamespacedKey namespacedKey;

    public void onEnable() {
        loadConfig();
        this.logger = this.getLogger();
        this.chatOptions = new ChatOptions();

        this.chatUtils = new ChatUtils();

        // Only needed for channel management via PersistentDataContainers
        if(this.getConfig().getBoolean(("Options.Chat.ChannelBeta"))) {
            this.namespacedKey = new NamespacedKey(this, "rpgchat_channel");
        }

        this.getCommand("rpgchat").setExecutor(new RPGChatCommands(chatUtils));
        Bukkit.getPluginManager().registerEvents(new PlayerChat(chatUtils), this);
    }

    // Remove all chat stands and save the configuration
    public void onDisable() {
        // If there is a problem with custom entity registration,
        // removeStands() will throw an exception
        try {
            chatUtils.removeStands();
        } catch (Exception ignored) {}
        reloadConfig();
        saveConfig();
    }

    // Initializes the configuration with default values on first startup
    private void loadConfig() {
        getConfig().options().setHeader(Collections.singletonList("#RPGChat by SirZontax - Version " + this.getDescription().getVersion()));
        getConfig().addDefault("Options.Normal.Color", "&f");
        getConfig().addDefault("Options.Normal.Symbol", "");
        getConfig().addDefault("Options.Normal.Sound.Name", "UI_BUTTON_CLICK");
        getConfig().addDefault("Options.Normal.Sound.Volume", 1.0);
        getConfig().addDefault("Options.Normal.Sound.Pitch", 1.0);
        getConfig().addDefault("Options.Whispered.Color", "&f&o");
        getConfig().addDefault("Options.Whispered.Symbol", "#");
        getConfig().addDefault("Options.Whispered.Range", 5.0D);
        getConfig().addDefault("Options.Whispered.Sound.Name", "UI_BUTTON_CLICK");
        getConfig().addDefault("Options.Whispered.Sound.Volume", 1.0);
        getConfig().addDefault("Options.Whispered.Sound.Pitch", 1.0);
        getConfig().addDefault("Options.Chat.HideBaseChat", true);
        getConfig().addDefault("Options.Chat.HideWhisperedBaseChat", true);
        getConfig().addDefault("Options.Compatibility.PlaceholderAPI.enabled", false);
        getConfig().addDefault("Options.Compatibility.ChatControl.enabled", false);
        getConfig().addDefault("Options.Compatibility.ChatControl.enabled_channels", List.of("global"));
        getConfig().addDefault("Options.Chat.Duration", 5);
        getConfig().addDefault("Options.Chat.Height", 0.0);
        getConfig().addDefault("Options.Chat.MaxLength", -1);
        getConfig().addDefault("Options.Chat.CustomPrefixes", false);
        getConfig().addDefault("Options.Chat.ChannelBeta", false);
        getConfig().addDefault("Options.Chat.Prefixes", Arrays.asList("Admin|&cAdmin&7: ", "Mod|&1Mod&7: ", "Player|&7Player: "));
        getConfig().addDefault("Options.Chat.Channels", Arrays.asList("admin|10|&c", "normal|-1|&f"));
        getConfig().addDefault("Options.Chat.Worlds", Arrays.asList("world", "world_nether", "world_the_end"));
        getConfig().addDefault("Options.Chat.Behavior.Filter.CensorSymbol", "*");
        getConfig().addDefault("Options.Chat.Behavior.Filter.BadWords", Arrays.asList("bad", "words"));
        getConfig().addDefault("Options.Chat.Behavior.Cooldown", 0);
        getConfig().addDefault("Options.Chat.Behavior.AntiRepeat", true);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void logSevereError(List<String> messages, Exception exception, String version) {
        messages.forEach(message -> logger.log(Level.SEVERE, message));
        logException(version, exception);
    }

    public void logSevereError(String message, Exception exception, String version) {
        logger.log(Level.SEVERE, message);
        logException(version, exception);
    }

    /**
     * Logs an exception with a message indicating that the version may not be supported
     * and disables the plugin with formatting.
     *
     * @param version The version of the plugin or Minecraft that may not be supported.
     * @param e The exception to log.
     */
    private void logException(String version, Exception e) {
        logger.log(Level.SEVERE, "----------------------------------------------------------");
        logger.log(Level.SEVERE, "Version '" + version + "' may not be supported at this time!");
        logger.log(Level.SEVERE, "----------------------------------------------------------");
        logger.log(Level.SEVERE, "Exception: " + e);
        logger.log(Level.SEVERE, "----------------------------------------------------------");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        saveConfig();
        chatOptions.reloadOptions();
        chatUtils.reloadConfig();
    }

    public ChatOptions getChatOptions() { return chatOptions; }

    // Needed for channel management via PersistentDataContainers
    public NamespacedKey getChannelKey() { return namespacedKey; }
}
