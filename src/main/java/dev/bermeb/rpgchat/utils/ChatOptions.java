package dev.bermeb.rpgchat.utils;

import dev.bermeb.rpgchat.RPGChat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ChatOptions {

    private static final String CONFIG_STRING = "Options.";
    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);

    private final Map<String, Object> optionMap;

    public ChatOptions() {
        this.optionMap = new HashMap<>();
        PLUGIN.getLogger().info("Configuration " + (loadOptions() ? "successfully" : "not successfully") + " loaded");
    }

    public boolean loadOptions() {
        try {
            Arrays.asList(
                    new Option("Normal.Color", OptionType.STRING),
                    new Option("Normal.Symbol", OptionType.STRING),
                    new Option("Normal.Sound.Name", OptionType.STRING),
                    new Option("Normal.Sound.Volume", OptionType.DOUBLE),
                    new Option("Normal.Sound.Pitch", OptionType.DOUBLE),
                    new Option("Whispered.Color", OptionType.STRING),
                    new Option("Whispered.Symbol", OptionType.STRING),
                    new Option("Whispered.Range", OptionType.DOUBLE),
                    new Option("Whispered.Sound.Name", OptionType.STRING),
                    new Option("Whispered.Sound.Volume", OptionType.DOUBLE),
                    new Option("Whispered.Sound.Pitch", OptionType.DOUBLE),
                    new Option("Chat.HideBaseChat", OptionType.BOOLEAN),
                    new Option("Chat.HideWhisperedBaseChat", OptionType.BOOLEAN),
                    new Option("Chat.Duration", OptionType.INT),
                    new Option("Chat.Height", OptionType.DOUBLE),
                    new Option("Chat.MaxLength", OptionType.INT),
                    new Option("Chat.CustomPrefixes", OptionType.BOOLEAN),
                    new Option("Chat.Prefixes", OptionType.STRING_LIST),
                    new Option("Chat.ChannelBeta", OptionType.BOOLEAN),
                    new Option("Chat.Channels", OptionType.STRING_LIST),
                    new Option("Chat.Worlds", OptionType.STRING_LIST),
                    new Option("Chat.NewMessagePriority", OptionType.BOOLEAN),
                    new Option("Chat.Behavior.Filter.CensorSymbol", OptionType.STRING),
                    new Option("Chat.Behavior.Filter.BadWords", OptionType.STRING_LIST),
                    new Option("Chat.Behavior.Cooldown", OptionType.INT),
                    new Option("Chat.Behavior.AntiRepeat", OptionType.BOOLEAN),
                    new Option("Compatibility.PlaceholderAPI.enabled", OptionType.BOOLEAN),
                    new Option("Compatibility.ChatControl.enabled", OptionType.BOOLEAN),
                    new Option("Compatibility.ChatControl.enabled_channels", OptionType.STRING_LIST)
            ).forEach(this::addOption);
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    public void reloadOptions() {
        optionMap.clear();
        PLUGIN.getLogger().info("[RPGChat] Configuration " + (loadOptions() ? "successfully" : "not successfully") + " reloaded");
    }

    public String getString(String key) {
        return (String) optionMap.get(key);
    }

    public int getInteger(String key) {
        return (Integer) optionMap.get(key);
    }

    public double getDouble(String key) {
        return (Double) optionMap.get(key);
    }

    public float getFloat(String key) {
        return Float.parseFloat(String.valueOf(getDouble(key)));
    }

    public boolean getBoolean(String key) {
        return (Boolean) optionMap.get(key);
    }

    // Returns a list of strings for the given key without any Unsafe cast
    // or CLassCastException risk while ensuring that no can be NPE thrown
    public List<String> getStringList(String key) {
        Object value = optionMap.get(key);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>();
    }

    private void addOption(Option option) {
        String key = option.key();
        OptionType type = option.type();
        switch (type) {
            case STRING -> optionMap.put(key, PLUGIN.getConfig().getString(CONFIG_STRING + key));
            case INT -> optionMap.put(key, PLUGIN.getConfig().getInt(CONFIG_STRING + key));
            case DOUBLE -> optionMap.put(key, PLUGIN.getConfig().getDouble(CONFIG_STRING + key));
            case BOOLEAN -> optionMap.put(key, PLUGIN.getConfig().getBoolean(CONFIG_STRING + key));
            case STRING_LIST -> optionMap.put(key, PLUGIN.getConfig().getStringList(CONFIG_STRING + key));
            default -> throw new IllegalStateException("Unexpected type: " + type);
        }
    }

    private record Option(String key, OptionType type) {
    }

    private enum OptionType {
        STRING, INT, DOUBLE, BOOLEAN, STRING_LIST
    }
}
