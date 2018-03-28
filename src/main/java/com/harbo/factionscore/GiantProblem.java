package com.harbo.factionscore;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Giant;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class GiantProblem extends EntityGiantZombie {
    public GiantProblem(World world){
        super(world);
    }
    @Override
    protected void initAttributes() {
        super.initAttributes();

        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(300D);
        this.setCustomName(ChatColor.RED+"Giant Problem");
        this.setCustomNameVisible(true);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(20D);
    }
    @Override
    protected Item getLoot() {

        return Items.DIAMOND;
    }

    /**
     * Spawns a giant.
     *
     * @param location Location at which to spawn the giant.
     * @return The giant entity object.
     */
    static Giant spawn(Location location){
        World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        final GiantProblem customEntity = new GiantProblem(mcWorld);
        customEntity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftLivingEntity) customEntity.getBukkitEntity()).setRemoveWhenFarAway(false);
        mcWorld.addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (Giant) customEntity.getBukkitEntity();
    }
}


