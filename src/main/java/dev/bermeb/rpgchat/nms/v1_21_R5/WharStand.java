package dev.bermeb.rpgchat.nms.v1_21_R5;

import dev.bermeb.rpgchat.server.IWharStand;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class WharStand extends ArmorStand implements IWharStand {

    private final List<Player> players;

    public WharStand(Location loc, List<Player> players) {
        this(loc, ((CraftWorld) loc.getWorld()).getHandle(), players);
    }

    public WharStand(Location loc, ServerLevel notchWorld, List<Player> players) {
        super(EntityType.ARMOR_STAND, notchWorld);

        setPos(loc.getX(), loc.getY(), loc.getZ());
        doModifiers();

        var serverEntity = new ServerEntity(notchWorld, this, 0, false, packet -> {
        }, (packet, uuids) -> {}, Set.of());

        sendPacket(players, getAddEntityPacket(serverEntity));
        sendPacket(players, new ClientboundSetEntityDataPacket(this.getId(), this.getEntityData().getNonDefaultValues()));
        this.players = players;
    }

    @Override
    public void doModifiers() {
        this.setInvisible(true);
        this.setInvulnerable(true);
        this.setNoGravity(true);
        this.setCustomNameVisible(true);
        this.collides = false;
    }

    public void teleport(Location loc) {
        this.getBukkitEntity().teleport(loc);
    }

    @Override
    public void setName(String name) {
        this.setCustomName(CraftChatMessage.fromString(ChatColor.translateAlternateColorCodes('&', name), false)[0]);
    }

    @Override
    public void appendToCustomName(String append) {
        this.setCustomName(CraftChatMessage.fromString(CraftChatMessage.fromComponent(getCustomName())
                + ChatColor.translateAlternateColorCodes('&', append), false)[0]);
    }

    @Override
    public void destroyEntity() {
        sendPacket(players, new ClientboundRemoveEntitiesPacket(this.getId()));
        this.getBukkitEntity().remove();
    }

    @Override
    public void reloadEntity() {
        sendPacket(players, new ClientboundTeleportEntityPacket(this.getId(), PositionMoveRotation.of(this), Relative.ALL, false));
        sendPacket(players, new ClientboundSetEntityDataPacket(this.getId(), this.getEntityData().getNonDefaultValues()));
    }

    public void sendPacket(List<Player> players, Packet<? extends PacketListener> packet) {
        for (Player p : players)
            ((CraftPlayer) p).getHandle().connection.send(packet);
    }
}