package dev.bermeb.rpgchat.nms.v1_21_R6;

import dev.bermeb.rpgchat.RPGChat;
import dev.bermeb.rpgchat.server.IEntityRegister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityRegister implements IEntityRegister {

    private static CustomEntityRegistry ENTITY_REGISTRY;

    public void registerEntityClass(Class<?> clazz) {
        if (ENTITY_REGISTRY == null)
            return;
        Class<?> search = clazz;
        while ((search = search.getSuperclass()) != null && Entity.class.isAssignableFrom(search)) {
            EntityType<?> type = ENTITY_REGISTRY.findType(search);
            ResourceLocation key = ENTITY_REGISTRY.getKey(type);
            if (type == null)
                continue;
            int code = ENTITY_REGISTRY.getId(type);
            ENTITY_REGISTRY.put(code, key, type);
            return;
        }
        throw new IllegalArgumentException("unable to find valid entity superclass for class " + clazz);
    }

    @Override
    public boolean registerEntity() {
        try {
            registerEntityClass( WharStand.class );
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().info("[RPGChat] [Error] Could not register the WharStand-entity!");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(JavaPlugin.getPlugin(RPGChat.class));
        }
        return false;
    }
}