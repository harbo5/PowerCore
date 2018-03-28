package com.harbo.factionscore;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

class OrbListener implements Listener {
    private final Main pl;
    private final Location center;
    private final int radius;
    private static final HashMap<UUID, ItemStack> players = new HashMap<>();

    OrbListener(Main pl){
        this.pl=pl;
        radius = pl.getConfig().getInt("radius");
        center = new Location(Bukkit.getWorld(pl.getConfig().getString("worldName")), pl.getConfig().getDouble("x-pos"), pl.getConfig().getDouble("y-pos"), pl.getConfig().getDouble("z-pos"));
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e){
        if(e.getItemDrop().getLocation().distance(center)<=radius){
            ItemStack is = e.getItemDrop().getItemStack();
            players.put(e.getPlayer().getUniqueId(), e.getItemDrop().getItemStack());
            e.getItemDrop().remove();
            pl.donate(e.getPlayer());
            pl.log(e.getPlayer().getName() + ": " + is.getAmount() + "x" + is.getType());
        }
    }
    static ItemStack getItem(UUID u){
        return players.get(u);
    }
    static void removePlayer(UUID u){
        players.remove(u);
    }
}
