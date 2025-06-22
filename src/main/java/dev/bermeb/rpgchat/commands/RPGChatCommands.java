package dev.bermeb.rpgchat.commands;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.server.IWharStand;
import dev.bermeb.rpgchat.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RPGChatCommands implements CommandExecutor {

    private static final RPGChat PLUGIN = JavaPlugin.getPlugin(RPGChat.class);
    private static final double ENTITY_SEARCH_RANGE = 2.5;
    private final ChatUtils chatUtils;

    public RPGChatCommands(ChatUtils chatUtils) {
        this.chatUtils = chatUtils;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Wrong command usage!");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReloadCommand(sender);
            case "fix":
                return handleFixCommand(sender);
            case "channel":
                if (args.length == 2) {
                    return handleChannelCommand(sender, args[1].toLowerCase());
                } else {
                    sender.sendMessage("Wrong command usage!");
                    return false;
                }
            default:
                sender.sendMessage("Wrong command usage!");
                return false;
        }
    }

    /**
     * Handles the reload command to reload the plugin's configuration.
     *
     * @param sender The command sender (should have permission).
     * @return true if the command was successful, false otherwise.
     */
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("RPGChat.Reload")) {
            sender.sendMessage("No Permissions!");
            return false;
        }
        PLUGIN.reloadConfig();
        PLUGIN.saveConfig();
        PLUGIN.getChatOptions().reloadOptions();
        sender.sendMessage("Reloaded RPGChat-Config");
        return true;
    }

    /**
     * Handles the fix command to remove nearby ArmorStands or IWharStands.
     *
     * @param sender The command sender (should be a player).
     * @return true if the command was successful, false otherwise.
     */
    private boolean handleFixCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a Player!");
            return false;
        }
        if (!sender.hasPermission("RPGChat.Fix")) {
            sender.sendMessage("No Permissions!");
            return false;
        }
        for (Entity entity : player.getNearbyEntities(ENTITY_SEARCH_RANGE, ENTITY_SEARCH_RANGE, ENTITY_SEARCH_RANGE)) {
            if (entity instanceof ArmorStand || entity instanceof IWharStand) {
                sender.sendMessage("An entity got removed!");
                entity.remove();
            }
        }
        return true;
    }

    /**
     * Handles the channel command to switch the player's chat channel.
     *
     * @param sender  The command sender (should be a player).
     * @param channel The channel to switch to.
     * @return true if the command was successful, false otherwise.
     */
    private boolean handleChannelCommand(CommandSender sender, String channel) {
        if (!PLUGIN.getChatOptions().getBoolean("Chat.ChannelBeta")) {
            sender.sendMessage("Channels are disabled!");
            return false;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Must be a player!");
            return false;
        }
        if (chatUtils.getChannelManager().channelExists(channel)) {
            chatUtils.getChannelManager().setPlayerChannel(player, channel);
            sender.sendMessage("Successfully switched channel to: " + channel);
            return true;
        } else {
            sender.sendMessage("This channel doesn't exist!");
            return false;
        }
    }
}