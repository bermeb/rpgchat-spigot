package dev.bermeb.rpgchat.config;

import org.bukkit.Sound;

import java.util.List;

public record ChatConfig(
    NormalChatConfig normal,
    WhisperedChatConfig whispered,
    ChatBehaviorConfig behavior,
    CompatibilityConfig compatibility,
    int duration,
    double height,
    int maxLength,
    boolean customPrefixes,
    boolean channelBeta,
    List<String> prefixes,
    List<String> channels,
    List<String> worlds
) {
    public record NormalChatConfig(
        String color,
        String symbol,
        SoundConfig sound,
        boolean hideBaseChat
    ) {}

    public record WhisperedChatConfig(
        String color,
        String symbol,
        double range,
        SoundConfig sound,
        boolean hideBaseChat
    ) {}

    public record SoundConfig(
        Sound sound,
        float volume,
        float pitch
    ) {}

    public record ChatBehaviorConfig(
        FilterConfig filter,
        int cooldown,
        boolean antiRepeat
    ) {}

    public record FilterConfig(
        String censorSymbol,
        List<String> badWords
    ) {}

    public record CompatibilityConfig(
        ChatControlConfig chatControl
    ) {}

    public record ChatControlConfig(
        boolean enabled,
        List<String> enabledChannels
    ) {}
}