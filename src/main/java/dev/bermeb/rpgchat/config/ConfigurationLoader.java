package dev.bermeb.rpgchat.config;

import dev.bermeb.rpgchat.sound.SoundManager;
import dev.bermeb.rpgchat.utils.ChatOptions;

public class ConfigurationLoader {

    public static ChatConfig loadFromChatOptions(ChatOptions chatOptions) {
        return new ChatConfig(
                loadNormalConfig(chatOptions),
                loadWhisperedConfig(chatOptions),
                loadBehaviorConfig(chatOptions),
                loadCompatibilityConfig(chatOptions),
                chatOptions.getInteger("Chat.Duration"),
                chatOptions.getDouble("Chat.Height"),
                chatOptions.getInteger("Chat.MaxLength"),
                chatOptions.getBoolean("Chat.CustomPrefixes"),
                chatOptions.getBoolean("Chat.ChannelBeta"),
                chatOptions.getBoolean("Chat.NewMessagePriority"),
                chatOptions.getStringList("Chat.Prefixes"),
                chatOptions.getStringList("Chat.Channels"),
                chatOptions.getStringList("Chat.Worlds")
        );
    }

    private static ChatConfig.NormalChatConfig loadNormalConfig(ChatOptions chatOptions) {
        return new ChatConfig.NormalChatConfig(
                chatOptions.getString("Normal.Color"),
                chatOptions.getString("Normal.Symbol"),
                new ChatConfig.SoundConfig(
                        SoundManager.parseSound(chatOptions.getString("Normal.Sound.Name")),
                        chatOptions.getFloat("Normal.Sound.Volume"),
                        chatOptions.getFloat("Normal.Sound.Pitch")
                ),
                chatOptions.getBoolean("Chat.HideBaseChat")
        );
    }

    private static ChatConfig.WhisperedChatConfig loadWhisperedConfig(ChatOptions chatOptions) {
        return new ChatConfig.WhisperedChatConfig(
                chatOptions.getString("Whispered.Color"),
                chatOptions.getString("Whispered.Symbol"),
                chatOptions.getDouble("Whispered.Range"),
                new ChatConfig.SoundConfig(
                        SoundManager.parseSound(chatOptions.getString("Whispered.Sound.Name")),
                        chatOptions.getFloat("Whispered.Sound.Volume"),
                        chatOptions.getFloat("Whispered.Sound.Pitch")
                ),
                chatOptions.getBoolean("Chat.HideWhisperedBaseChat")
        );
    }

    private static ChatConfig.ChatBehaviorConfig loadBehaviorConfig(ChatOptions chatOptions) {
        return new ChatConfig.ChatBehaviorConfig(
                new ChatConfig.FilterConfig(
                        chatOptions.getString("Chat.Behavior.Filter.CensorSymbol"),
                        chatOptions.getStringList("Chat.Behavior.Filter.BadWords")
                ),
                chatOptions.getInteger("Chat.Behavior.Cooldown"),
                chatOptions.getBoolean("Chat.Behavior.AntiRepeat")
        );
    }

    private static ChatConfig.CompatibilityConfig loadCompatibilityConfig(ChatOptions chatOptions) {
        return new ChatConfig.CompatibilityConfig(
                new ChatConfig.PlaceholderAPIConfig(
                        chatOptions.getBoolean("Compatibility.PlaceholderAPI.enabled")
                ),
                new ChatConfig.ChatControlConfig(
                        chatOptions.getBoolean("Compatibility.ChatControl.enabled"),
                        chatOptions.getStringList("Compatibility.ChatControl.enabled_channels")
                )
        );
    }
}