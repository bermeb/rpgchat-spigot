package dev.bermeb.rpgchat.utils;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.channel.ChannelManager;
import dev.bermeb.rpgchat.config.ChatConfig;
import dev.bermeb.rpgchat.config.ConfigurationLoader;
import dev.bermeb.rpgchat.display.*;
import dev.bermeb.rpgchat.queue.ChatQueueManager;
import dev.bermeb.rpgchat.server.IEntityRegister;
import dev.bermeb.rpgchat.server.NMSHandler;
import dev.bermeb.rpgchat.sound.SoundManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class ChatUtils implements Listener {

    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);

    private ChatConfig config;

    private final ChannelManager channelManager;
    private final ChatQueueManager queueManager;

    private final NormalMessageDisplay normalDisplay;
    private final WhisperedMessageDisplay whisperedDisplay;
    private final ChannelMessageDisplay channelDisplay;

    private volatile Pattern[] compiledPatterns = new Pattern[0];

    public ChatUtils() {
        // Load configuration
        this.config = ConfigurationLoader.loadFromChatOptions(PLUGIN.getChatOptions());

        // Initialize managers
        this.channelManager = new ChannelManager();
        this.queueManager = new ChatQueueManager();
        final NMSHandler nmsHandler = new NMSHandler();
        final SoundManager soundManager = new SoundManager();

        // Initialize display strategies
        this.normalDisplay = new NormalMessageDisplay(config, soundManager);
        this.whisperedDisplay = new WhisperedMessageDisplay(config, soundManager, nmsHandler);
        this.channelDisplay = new ChannelMessageDisplay(config, soundManager, nmsHandler, channelManager);

        // Load channels
        if (config.channels() != null) {
            channelManager.loadChannels(config.channels());
        }

        // Register NMS entity
        IEntityRegister entityRegister = nmsHandler.getEntityRegister();
        if (entityRegister != null) {
            PLUGIN.getLogger().log(Level.INFO, "Entity " +
                    (entityRegister.registerEntity() ? "successfully" : "not successfully") + " registered!");
        } else {
            PLUGIN.getLogger().log(Level.SEVERE, "Failed to get EntityRegister - Wrong or unsupported version!");
        }

        // Precompile regex patterns
        precompilePatterns();

        // Start chat processing
        new ChatRunnable(this);
    }

    public void addToChatQueue(Player player, String message, boolean whispered) {
        String processedMessage = processMessage(message);

        // Check regex, mostly for interaction with different Chat plugins
        if(shouldHideMessage(processedMessage)) {
            return;
        }

        if (config.channelBeta() && !whispered) {
            String channel = channelManager.getPlayerChannel(player);
            queueManager.addToChannelQueue(player, channel, processedMessage);
        } else if (whispered) {
            String whisperedMessage = removeSymbolPrefix(processedMessage, config.whispered().symbol());
            queueManager.addToWhisperedQueue(player, whisperedMessage);
        } else {
            String normalMessage = removeSymbolPrefix(processedMessage, config.normal().symbol());
            queueManager.addToNormalQueue(player, normalMessage);
        }
    }

    private String processMessage(String message) {
        // Apply max length limit
        int maxLength = config.maxLength();
        if (maxLength > 0 && message.length() > maxLength) {
            message = message.substring(0, maxLength) + "...";
        }

        // Apply censoring
        String censorSymbol = config.behavior().filter().censorSymbol();
        if (censorSymbol != null && !censorSymbol.isEmpty()) {
            List<String> badWords = config.behavior().filter().badWords();
            if (badWords != null) {
                for (String badWord : badWords) {
                    message = censorBadWords(message, badWord, censorSymbol);
                }
            }
        }

        return message;
    }

    private String removeSymbolPrefix(String message, String symbol) {
        if (symbol != null && !symbol.isEmpty() && message.startsWith(symbol)) {
            return message.substring(symbol.length());
        }
        return message;
    }

    private String censorBadWords(String message, String badWord, String censorSymbol) {
        return Arrays.stream(message.split(" "))
                .map(word -> word.toLowerCase().contains(badWord.toLowerCase()) ?
                        censorSymbol.repeat(word.length()) : word)
                .collect(Collectors.joining(" "));
    }

    private boolean shouldHideMessage(String message) {
        Pattern[] patterns = compiledPatterns; // Local reference for thread safety
        for (Pattern pattern : patterns) {
            if (pattern.matcher(message).find()) {
                return true;
            }
        }
        return false;
    }

    private void precompilePatterns() {
        List<String> regexList = config.behavior().filter().wontShowRegex();
        if (regexList == null || regexList.isEmpty()) {
            compiledPatterns = new Pattern[0];
            return;
        }

        compiledPatterns = regexList.stream()
            .map(this::safeCompile)
            .filter(Objects::nonNull)
            .toArray(Pattern[]::new);
    }

    private Pattern safeCompile(String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            PLUGIN.getLogger().warning("Invalid regex pattern: " + regex + " - " + e.getMessage());
            return null;
        }
    }

    public void displayNormalMessage(Player player, String message) {
        normalDisplay.displayMessage(player, message);
    }

    public void displayWhisperedMessage(Player player, String message) {
        whisperedDisplay.displayMessage(player, message);
    }

    public void displayChannelMessage(Player player, String message, String channel) {
        channelDisplay.displayChannelMessage(player, message, channel);
    }

    public void removeStands() {
        normalDisplay.cleanup();
        whisperedDisplay.cleanup();
        channelDisplay.cleanup();
    }

    public void reloadConfig() {
        this.config = ConfigurationLoader.loadFromChatOptions(PLUGIN.getChatOptions());
        channelManager.loadChannels(config.channels());
        normalDisplay.reloadConfig(config);
        whisperedDisplay.reloadConfig(config);
        channelDisplay.reloadConfig(config);
        precompilePatterns();
    }

    public ChatQueueManager getQueueManager() {
        return queueManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ChatConfig getConfig() {
        return config;
    }

    public int getMinShowTime(String message) {
        return (int) Math.ceil(message.length() / 20.0) + config.duration();
    }
}