package dev.bermeb.rpgchat.nms.v1_21_R6;

import dev.bermeb.rpgchat.server.IWharStand;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_21_R6.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WharStand extends ArmorStand implements IWharStand {

    private final List<Player> players;
    // Needed for teleportation and position updates
    private Set<Relative> tmpRelatives;
    private PositionMoveRotation tmpPosMoveRot;

    public WharStand(Location loc, List<Player> players) {
        this(loc, ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle(), players);
    }

    public WharStand(Location loc, ServerLevel notchWorld, List<Player> players) {
        super(EntityType.ARMOR_STAND, notchWorld);

        setPos(loc.getX(), loc.getY(), loc.getZ());
        doModifiers();
        this.players = players;
        this.tmpRelatives = Set.of(); // Can also be null, but we initialize it to avoid null checks later
        this.tmpPosMoveRot = PositionMoveRotation.of(this); // Can also be null, but we initialize it to avoid null checks later

        sendPacket(players, new ClientboundAddEntityPacket(this, 0, this.blockPosition()));
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
        // Using TeleportTransition to handle teleportation correctly
        TeleportTransition teleporttransition = new TeleportTransition(((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle(), CraftLocation.toVec3D(loc), Vec3.ZERO,
                loc.getPitch(), loc.getYaw(), Set.of(), TeleportTransition.DO_NOTHING, PlayerTeleportEvent.TeleportCause.PLUGIN);

        this.tmpPosMoveRot = PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of(this), PositionMoveRotation.of(teleporttransition), teleporttransition.relatives());
        this.tmpRelatives = teleporttransition.relatives();

        this.teleport(teleporttransition);
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
        // Should never be null, but we check just in case
        if(this.tmpPosMoveRot == null || this.tmpRelatives == null) {
            return;
        }
        // TeleportPacket got changed in 1.21.2, so we need to use the new PositionMoveRotation
        sendPacket(players, new ClientboundTeleportEntityPacket(
                this.getId(),
                this.tmpPosMoveRot,
                this.tmpRelatives,
                false
        ));
        sendPacket(players, new ClientboundSetEntityDataPacket(
                this.getId(),
                this.getEntityData().getNonDefaultValues()
        ));
    }

    private void sendPacket(List<Player> players, Packet<? extends PacketListener> packet) {
        for (Player p : players)
            ((CraftPlayer) p).getHandle().connection.send(packet);
    }
}