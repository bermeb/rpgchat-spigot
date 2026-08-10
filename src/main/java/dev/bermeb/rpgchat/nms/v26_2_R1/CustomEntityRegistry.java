package dev.bermeb.rpgchat.nms.v26_2_R1;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.*;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.*;
import net.minecraft.world.entity.animal.feline.*;
import net.minecraft.world.entity.animal.fish.*;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.*;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.cubemob.*;
import net.minecraft.world.entity.monster.illager.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.*;
import net.minecraft.world.entity.monster.spider.*;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.*;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.*;
import net.minecraft.world.entity.vehicle.minecart.*;
import org.bukkit.craftbukkit.entity.boat.*;
import org.bukkit.entity.Hoglin;

import java.util.*;

public class CustomEntityRegistry<T> extends DefaultedMappedRegistry {

    private final BiMap<Identifier, T> entities = HashBiMap.create();
    private final BiMap<T, Identifier> entityClasses = this.entities.inverse();
    private final Map<EntityType<?>, Integer> entityIds = Maps.newHashMap();
    private final MappedRegistry<Holder.Reference<T>> wrapped;

    public CustomEntityRegistry(DefaultedMappedRegistry<Holder.Reference<T>> original) {
        super(original.getDefaultKey().getNamespace(), null, null, false);
        this.wrapped = original;
    }

    @Override
    public int getId(Object key) {
        if (entityIds.containsKey(key))
            return entityIds.get(key);
        return wrapped.getId((Holder.Reference<T>) key);
    }

    @Override
    public Optional<Holder.Reference<Holder.Reference<T>>> getRandom(RandomSource paramRandom) {
        return wrapped.getRandom(paramRandom);
    }

    public EntityType<?> findType(Class<?> search) {
        return minecraftClassMap.inverse().get(search);
    }

    @Override
    public T byId(int var0) {
        return (T) Objects.requireNonNull(this.wrapped.byId(var0));
    }

    @Override
    public T getValue(Identifier key) {
        if (entities.containsKey(key))
            return entities.get(key);
        return (T) Objects.requireNonNull(wrapped.get(key));
    }

    @Override
    public Identifier getKey(Object value) {
        if (entityClasses.containsKey(value))
            return entityClasses.get(value);
        return Objects.requireNonNull(wrapped.getKey((Holder.Reference<T>) value));
    }

    @Override
    public Optional<T> getOptional(Identifier var0) {
        if (entities.containsKey(var0))
            return Optional.of(entities.get(var0));
        return (Optional<T>) this.wrapped.getOptional(var0);
    }

    public MappedRegistry<Holder.Reference<T>> getWrapped() {
        return wrapped;
    }

    @Override
    public Iterator<Object> iterator() {
        return (Iterator) wrapped.iterator();
    }

    @Override
    public Set<Object> keySet() {
        return (Set) wrapped.keySet();
    }

    public void put(int entityId, Identifier key, EntityType<?> entityClass) {
        entities.put(key, (T)entityClass);
        entityIds.put(entityClass, entityId);
    }

    private static final BiMap<EntityType<?>, Class<?>> minecraftClassMap = HashBiMap.create();

    static {
        minecraftClassMap.put(EntityTypes.ACACIA_BOAT, CraftAcaciaBoat.class);
        minecraftClassMap.put(EntityTypes.ACACIA_CHEST_BOAT, CraftAcaciaChestBoat.class);
        minecraftClassMap.put(EntityTypes.ALLAY, Allay.class);
        minecraftClassMap.put(EntityTypes.AREA_EFFECT_CLOUD, AreaEffectCloud.class);
        minecraftClassMap.put(EntityTypes.ARMADILLO, Armadillo.class);
        minecraftClassMap.put(EntityTypes.ARMOR_STAND, ArmorStand.class);
        minecraftClassMap.put(EntityTypes.ARROW, Arrow.class);
        minecraftClassMap.put(EntityTypes.AXOLOTL, Axolotl.class);
        minecraftClassMap.put(EntityTypes.BAMBOO_CHEST_RAFT, CraftBambooChestRaft.class);
        minecraftClassMap.put(EntityTypes.BAMBOO_RAFT, CraftBambooRaft.class);
        minecraftClassMap.put(EntityTypes.BAT, Bat.class);
        minecraftClassMap.put(EntityTypes.BEE, Bee.class);
        minecraftClassMap.put(EntityTypes.BIRCH_BOAT, CraftBirchBoat.class);
        minecraftClassMap.put(EntityTypes.BIRCH_CHEST_BOAT, CraftBirchChestBoat.class);
        minecraftClassMap.put(EntityTypes.BLAZE, Blaze.class);
        minecraftClassMap.put(EntityTypes.BLOCK_DISPLAY, Display.BlockDisplay.class);
        minecraftClassMap.put(EntityTypes.BOGGED, Bogged.class);
        minecraftClassMap.put(EntityTypes.BREEZE, Breeze.class);
        minecraftClassMap.put(EntityTypes.BREEZE_WIND_CHARGE, BreezeWindCharge.class);
        minecraftClassMap.put(EntityTypes.CAMEL, Camel.class);
        minecraftClassMap.put(EntityTypes.CAMEL_HUSK, CamelHusk.class);
        minecraftClassMap.put(EntityTypes.CAT, Cat.class);
        minecraftClassMap.put(EntityTypes.CAVE_SPIDER, CaveSpider.class);
        minecraftClassMap.put(EntityTypes.CHERRY_BOAT, CraftCherryBoat.class);
        minecraftClassMap.put(EntityTypes.CHERRY_CHEST_BOAT, CraftCherryChestBoat.class);
        minecraftClassMap.put(EntityTypes.CHEST_MINECART, MinecartChest.class);
        minecraftClassMap.put(EntityTypes.CHICKEN, Chicken.class);
        minecraftClassMap.put(EntityTypes.COD, Cod.class);
        minecraftClassMap.put(EntityTypes.COPPER_GOLEM, CopperGolem.class);
        minecraftClassMap.put(EntityTypes.COMMAND_BLOCK_MINECART, MinecartCommandBlock.class);
        minecraftClassMap.put(EntityTypes.COW, Cow.class);
        minecraftClassMap.put(EntityTypes.CREAKING, Creaking.class);
        minecraftClassMap.put(EntityTypes.CREEPER, Creeper.class);
        minecraftClassMap.put(EntityTypes.DARK_OAK_BOAT, CraftDarkOakBoat.class);
        minecraftClassMap.put(EntityTypes.DARK_OAK_CHEST_BOAT, CraftDarkOakChestBoat.class);
        minecraftClassMap.put(EntityTypes.DOLPHIN, Dolphin.class);
        minecraftClassMap.put(EntityTypes.DONKEY, Donkey.class);
        minecraftClassMap.put(EntityTypes.DRAGON_FIREBALL, DragonFireball.class);
        minecraftClassMap.put(EntityTypes.DROWNED, Drowned.class);
        minecraftClassMap.put(EntityTypes.EGG, ThrownEgg.class);
        minecraftClassMap.put(EntityTypes.ELDER_GUARDIAN, ElderGuardian.class);
        minecraftClassMap.put(EntityTypes.ENDERMAN, EnderMan.class);
        minecraftClassMap.put(EntityTypes.ENDERMITE, Endermite.class);
        minecraftClassMap.put(EntityTypes.ENDER_DRAGON, EnderDragon.class);
        minecraftClassMap.put(EntityTypes.ENDER_PEARL, ThrownEnderpearl.class);
        minecraftClassMap.put(EntityTypes.END_CRYSTAL, EndCrystal.class);
        minecraftClassMap.put(EntityTypes.EVOKER, Evoker.class);
        minecraftClassMap.put(EntityTypes.EVOKER_FANGS, EvokerFangs.class);
        minecraftClassMap.put(EntityTypes.EXPERIENCE_BOTTLE, ThrownExperienceBottle.class);
        minecraftClassMap.put(EntityTypes.EXPERIENCE_ORB, ExperienceOrb.class);
        minecraftClassMap.put(EntityTypes.EYE_OF_ENDER, EyeOfEnder.class);
        minecraftClassMap.put(EntityTypes.FALLING_BLOCK, FallingBlockEntity.class);
        minecraftClassMap.put(EntityTypes.FIREBALL, LargeFireball.class);
        minecraftClassMap.put(EntityTypes.FIREWORK_ROCKET, FireworkRocketEntity.class);
        minecraftClassMap.put(EntityTypes.FOX, Fox.class);
        minecraftClassMap.put(EntityTypes.FROG, Frog.class);
        minecraftClassMap.put(EntityTypes.FURNACE_MINECART, MinecartFurnace.class);
        minecraftClassMap.put(EntityTypes.GHAST, Ghast.class);
        minecraftClassMap.put(EntityTypes.HAPPY_GHAST, HappyGhast.class);
        minecraftClassMap.put(EntityTypes.GIANT, Giant.class);
        minecraftClassMap.put(EntityTypes.GLOW_ITEM_FRAME, GlowItemFrame.class);
        minecraftClassMap.put(EntityTypes.GLOW_SQUID, GlowSquid.class);
        minecraftClassMap.put(EntityTypes.GOAT, Goat.class);
        minecraftClassMap.put(EntityTypes.GUARDIAN, Guardian.class);
        minecraftClassMap.put(EntityTypes.HOGLIN, Hoglin.class);
        minecraftClassMap.put(EntityTypes.HOPPER_MINECART, MinecartHopper.class);
        minecraftClassMap.put(EntityTypes.HORSE, Horse.class);
        minecraftClassMap.put(EntityTypes.HUSK, Husk.class);
        minecraftClassMap.put(EntityTypes.ILLUSIONER, Illusioner.class);
        minecraftClassMap.put(EntityTypes.INTERACTION, Interaction.class);
        minecraftClassMap.put(EntityTypes.IRON_GOLEM, IronGolem.class);
        minecraftClassMap.put(EntityTypes.ITEM, ItemEntity.class);
        minecraftClassMap.put(EntityTypes.ITEM_DISPLAY, Display.ItemDisplay.class);
        minecraftClassMap.put(EntityTypes.ITEM_FRAME, ItemFrame.class);
        minecraftClassMap.put(EntityTypes.JUNGLE_BOAT, CraftJungleBoat.class);
        minecraftClassMap.put(EntityTypes.JUNGLE_CHEST_BOAT, CraftJungleChestBoat.class);
        minecraftClassMap.put(EntityTypes.LEASH_KNOT, LeashFenceKnotEntity.class);
        minecraftClassMap.put(EntityTypes.LIGHTNING_BOLT, LightningBolt.class);
        minecraftClassMap.put(EntityTypes.LLAMA, Llama.class);
        minecraftClassMap.put(EntityTypes.LLAMA_SPIT, LlamaSpit.class);
        minecraftClassMap.put(EntityTypes.MAGMA_CUBE, MagmaCube.class);
        minecraftClassMap.put(EntityTypes.MANGROVE_BOAT, CraftMangroveBoat.class);
        minecraftClassMap.put(EntityTypes.MANGROVE_CHEST_BOAT, CraftMangroveChestBoat.class);
        minecraftClassMap.put(EntityTypes.MANNEQUIN, Mannequin.class);
        minecraftClassMap.put(EntityTypes.MARKER, Marker.class);
        minecraftClassMap.put(EntityTypes.MINECART, Minecart.class);
        minecraftClassMap.put(EntityTypes.MOOSHROOM, MushroomCow.class);
        minecraftClassMap.put(EntityTypes.MULE, Mule.class);
        minecraftClassMap.put(EntityTypes.NAUTILUS, Nautilus.class);
        minecraftClassMap.put(EntityTypes.OAK_BOAT, CraftOakBoat.class);
        minecraftClassMap.put(EntityTypes.OAK_CHEST_BOAT, CraftOakChestBoat.class);
        minecraftClassMap.put(EntityTypes.OCELOT, Ocelot.class);
        minecraftClassMap.put(EntityTypes.OMINOUS_ITEM_SPAWNER, OminousItemSpawner.class);
        minecraftClassMap.put(EntityTypes.PAINTING, Painting.class);
        minecraftClassMap.put(EntityTypes.PALE_OAK_BOAT, CraftPaleOakBoat.class);
        minecraftClassMap.put(EntityTypes.PALE_OAK_CHEST_BOAT, CraftPaleOakChestBoat.class);
        minecraftClassMap.put(EntityTypes.PANDA, Panda.class);
        minecraftClassMap.put(EntityTypes.PARCHED, Parched.class);
        minecraftClassMap.put(EntityTypes.PARROT, Parrot.class);
        minecraftClassMap.put(EntityTypes.PHANTOM, Phantom.class);
        minecraftClassMap.put(EntityTypes.PIG, Pig.class);
        minecraftClassMap.put(EntityTypes.PIGLIN, Piglin.class);
        minecraftClassMap.put(EntityTypes.PIGLIN_BRUTE, PiglinBrute.class);
        minecraftClassMap.put(EntityTypes.PILLAGER, Pillager.class);
        minecraftClassMap.put(EntityTypes.POLAR_BEAR, PolarBear.class);
        minecraftClassMap.put(EntityTypes.SPLASH_POTION, ThrownSplashPotion.class);
        minecraftClassMap.put(EntityTypes.LINGERING_POTION, ThrownLingeringPotion.class);
        minecraftClassMap.put(EntityTypes.PUFFERFISH, Pufferfish.class);
        minecraftClassMap.put(EntityTypes.RABBIT, Rabbit.class);
        minecraftClassMap.put(EntityTypes.RAVAGER, Ravager.class);
        minecraftClassMap.put(EntityTypes.SALMON, Salmon.class);
        minecraftClassMap.put(EntityTypes.SHEEP, Sheep.class);
        minecraftClassMap.put(EntityTypes.SHULKER, Shulker.class);
        minecraftClassMap.put(EntityTypes.SHULKER_BULLET, ShulkerBullet.class);
        minecraftClassMap.put(EntityTypes.SILVERFISH, Silverfish.class);
        minecraftClassMap.put(EntityTypes.SKELETON, Skeleton.class);
        minecraftClassMap.put(EntityTypes.SKELETON_HORSE, SkeletonHorse.class);
        minecraftClassMap.put(EntityTypes.SLIME, Slime.class);
        minecraftClassMap.put(EntityTypes.SMALL_FIREBALL, SmallFireball.class);
        minecraftClassMap.put(EntityTypes.SNIFFER, Sniffer.class);
        minecraftClassMap.put(EntityTypes.SNOWBALL, Snowball.class);
        minecraftClassMap.put(EntityTypes.SNOW_GOLEM, SnowGolem.class);
        minecraftClassMap.put(EntityTypes.SPAWNER_MINECART, MinecartSpawner.class);
        minecraftClassMap.put(EntityTypes.SPECTRAL_ARROW, SpectralArrow.class);
        minecraftClassMap.put(EntityTypes.SPIDER, Spider.class);
        minecraftClassMap.put(EntityTypes.SPRUCE_BOAT, CraftSpruceBoat.class);
        minecraftClassMap.put(EntityTypes.SPRUCE_CHEST_BOAT, CraftSpruceChestBoat.class);
        minecraftClassMap.put(EntityTypes.SQUID, Squid.class);
        minecraftClassMap.put(EntityTypes.STRAY, Stray.class);
        minecraftClassMap.put(EntityTypes.STRIDER, Strider.class);
        minecraftClassMap.put(EntityTypes.SULFUR_CUBE, SulfurCube.class);
        minecraftClassMap.put(EntityTypes.TADPOLE, Tadpole.class);
        minecraftClassMap.put(EntityTypes.TEXT_DISPLAY, Display.TextDisplay.class);
        minecraftClassMap.put(EntityTypes.TNT, PrimedTnt.class);
        minecraftClassMap.put(EntityTypes.TNT_MINECART, MinecartTNT.class);
        minecraftClassMap.put(EntityTypes.TRADER_LLAMA, TraderLlama.class);
        minecraftClassMap.put(EntityTypes.TRIDENT, ThrownTrident.class);
        minecraftClassMap.put(EntityTypes.TROPICAL_FISH, TropicalFish.class);
        minecraftClassMap.put(EntityTypes.TURTLE, Turtle.class);
        minecraftClassMap.put(EntityTypes.VEX, Vex.class);
        minecraftClassMap.put(EntityTypes.VILLAGER, Villager.class);
        minecraftClassMap.put(EntityTypes.VINDICATOR, Vindicator.class);
        minecraftClassMap.put(EntityTypes.WANDERING_TRADER, WanderingTrader.class);
        minecraftClassMap.put(EntityTypes.WARDEN, Warden.class);
        minecraftClassMap.put(EntityTypes.WIND_CHARGE, WindCharge.class);
        minecraftClassMap.put(EntityTypes.WITCH, Witch.class);
        minecraftClassMap.put(EntityTypes.WITHER, WitherBoss.class);
        minecraftClassMap.put(EntityTypes.WITHER_SKELETON, WitherSkeleton.class);
        minecraftClassMap.put(EntityTypes.WITHER_SKULL, WitherSkull.class);
        minecraftClassMap.put(EntityTypes.WOLF, Wolf.class);
        minecraftClassMap.put(EntityTypes.ZOGLIN, Zoglin.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE, Zombie.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE_HORSE, ZombieHorse.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE_NAUTILUS, ZombieNautilus.class);
        minecraftClassMap.put(EntityTypes.ZOMBIE_VILLAGER, ZombieVillager.class);
        minecraftClassMap.put(EntityTypes.ZOMBIFIED_PIGLIN, ZombifiedPiglin.class);
        minecraftClassMap.put(EntityTypes.PLAYER, Player.class);
        minecraftClassMap.put(EntityTypes.FISHING_BOBBER, FishingHook.class);
    }
}