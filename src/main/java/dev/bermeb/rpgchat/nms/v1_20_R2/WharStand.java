package dev.bermeb.rpgchat.nms.v1_20_R2;

import dev.bermeb.rpgchat.server.IWharStand;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class WharStand extends ArmorStand implements IWharStand {

    private final List<Player> players;

    public WharStand(Location loc, List<Player> players) { this(loc, ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle(), players); }

    public WharStand(Location loc, Level notchWorld, List<Player> players) {
        super(EntityType.ARMOR_STAND, notchWorld);

        setPos(loc.getX(), loc.getY(), loc.getZ());
        doModifiers();
        this.players = players;

        sendPacket(players, this.getAddEntityPacket());
        sendPacket(players, new ClientboundSetEntityDataPacket(this.getId(), this.getEntityData().getNonDefaultValues()));
    }

    @Override
    public void doModifiers() {
        this.setCustomName(CraftChatMessage.fromStringOrNull(""));
        this.setCustomNameVisible(true);
        this.setInvisible(true);
        this.setMarker(true);
        this.collides = false;
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    @Override
    public void teleport(Location loc) {
        this.getBukkitEntity().teleport(loc);
    }

    @Override
    public void setName(String name) {
        final String formattedName = ChatColor.translateAlternateColorCodes('&', name);
        this.setCustomName(CraftChatMessage.fromStringOrNull(formattedName));
    }

    @Override
    public void appendToCustomName(String append) {
        String formattedAppend = ChatColor.translateAlternateColorCodes('&', append);

        String currentText = this.getCustomName() != null ?
                CraftChatMessage.fromComponent(this.getCustomName()) : "";

        String newText = currentText + formattedAppend;

        this.setCustomName(CraftChatMessage.fromStringOrNull(newText));
    }

    @Override
    public void destroyEntity() {
        sendPacket(players, new ClientboundRemoveEntitiesPacket(this.getId()));
        this.discard();
    }

    @Override
    public void reloadEntity() {
        sendPacket(players, new ClientboundTeleportEntityPacket(this));
        sendPacket(players, new ClientboundSetEntityDataPacket(this.getId(), this.getEntityData().getNonDefaultValues()));
    }

    private void sendPacket(List<Player> players, Packet<? extends PacketListener> packet) {
        for (Player p : players)
            ((CraftPlayer) p).getHandle().connection.send(packet);
    }
}