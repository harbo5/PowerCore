package com.harbo.factionscore;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EventManager {
    private final Main pl;
    private final World world;

    public EventManager(Main main) {
        pl = main;
        world = Bukkit.getWorld(pl.getConfig().getString("worldName"));
    }

    /**
     * Start PowerCore event regardless of charge levl.
     */
    public void startEvent() {
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        int giantChance = pl.getConfig().getInt("giantEventChance");
        int supplyDropChance = pl.getConfig().getInt("supplyDropEventChance");
        int mcmmoChance = pl.getConfig().getInt("mcmmoEventChance");
        int itemChance = pl.getConfig().getInt("itemEventChance");
        int commandChance = pl.getConfig().getInt("commandEventChance");
        if(random > 100-giantChance && random <= 100) { //Giant Problem
            Location locT = randomLoc();
            Location loc = new Location(world, locT.getX(), 256, locT.getZ());
            loc.getChunk().load();
            new BukkitRunnable() {
                @Override
                public void run() {//spawns giant
                    while (!loc.getChunk().isLoaded()) {
                        loc.getChunk().load();
                    }
                    Giant g = GiantProblem.spawn(loc);
                    final UUID uuid = g.getUniqueId();
                    pl.giantLocs.put(uuid.toString(), g.getLocation());
                    pl.giantUUIDs.add(g.getUniqueId().toString());
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "A Giant has spawned at X: "
                            + ChatColor.GOLD + (int) loc.getX()
                            + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) loc.getY()
                            + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) loc.getZ()
                            + ChatColor.RED + " Kill the Giant for a big reward!");
                    new BukkitRunnable() { //Gives spawn location
                        @Override
                        public void run() {
                            Giant g = null;
                            for (Entity e : world.getEntities())
                                if (e instanceof Giant && uuid.toString().equals(e.getUniqueId().toString()))
                                    g = (Giant) e;

                            if (g != null) {
                                if (g.isDead() || g.getHealth() == 0) {
                                    this.cancel();

                                } else {
                                    pl.giantLocs.replace(g.getUniqueId().toString(), g.getLocation());
                                    Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "A Giant is alive at X: "
                                            + ChatColor.GOLD + (int) g.getLocation().getX()
                                            + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) g.getLocation().getY()
                                            + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) g.getLocation().getZ()
                                            + ChatColor.RED + " Kill the Giant for a big reward!");
                                }
                            } else {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(Bukkit.getServer().getPluginManager().getPlugin("PowerCore"), 600, 2400);
                }
            }.runTaskLater(pl, 60);
        } else if(random > 100-giantChance-supplyDropChance && random <= 100-giantChance) { //Supply drop - Chests filled with items will drop in a random area.
            Location loc2 = randomLoc().subtract(0, 2, 0);
            pl.chestLocs.add(loc2);
            loc2.getChunk().load();
            new BukkitRunnable() {
                @Override
                public void run() {
                    loc2.getBlock().setType(Material.CHEST);
                    Chest chest = (Chest) loc2.getBlock().getState();
                    chest.getInventory().setContents(pl.invenItems);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "A supply drop has appeared at X: "
                            + ChatColor.GOLD + (int) loc2.getX()
                            + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) loc2.getY()
                            + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) loc2.getZ()
                            + ChatColor.RED + " Find it for valuable loot!");
                }
            }.runTaskLater(pl, 60);
        } else if(random > 100-giantChance-supplyDropChance-mcmmoChance && random <= 100-giantChance-supplyDropChance) { //XP bonuses for mcmmo or exp are given to all players for a certain period of time.
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "xprate reset");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "xprate 2 true");
            Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "A server wide 2x MCMMO Multiplier has been activated for 15 minutes!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "xprate reset");
                }
            }.runTaskLater(pl, 18000);
        } else if(random > 100-giantChance-supplyDropChance-mcmmoChance-itemChance && random <= 100-giantChance-supplyDropChance-mcmmoChance) { //Free items/keys for all.
            HashMap<String, Integer> temp = new HashMap<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                String addr = p.getAddress().toString().replace("/", "").split(":")[0];
                temp.putIfAbsent(addr, 0);
                if(!p.hasPermission("powercore.admin"))
                    temp.replace(addr, temp.get(addr), temp.get(addr)+1);
                if(temp.getOrDefault(addr, 0) <= 3) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.pl.getConfig().getString("itemEventCommand").replaceAll("<player>", p.getName()));
                } else {
                    int r = ThreadLocalRandom.current().nextInt(1, 101);
                    if(r<=10)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.pl.getConfig().getString("itemEventCommand").replaceAll("<player>", p.getName()));
                    p.sendMessage(ChatColor.DARK_RED+"You have more than 5 accounts online from your IP! You will not receive a reward.");
                }
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "Every player has received an item!");
        } else if(random > 100-giantChance-supplyDropChance-mcmmoChance-itemChance-commandChance && random <= 100-giantChance-supplyDropChance-mcmmoChance-itemChance) { //Running a command on every player.
            HashMap<String, Integer> temp = new HashMap<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                String addr = p.getAddress().toString().replace("/", "").split(":")[0];
                temp.putIfAbsent(addr, 0);
                if(!p.hasPermission("powercore.admin"))
                    temp.replace(addr, temp.get(addr), temp.get(addr)+1);
                if(temp.getOrDefault(addr, 0) <= 3) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.pl.getConfig().getString("commandEventCommand").replaceAll("<player>", p.getName()));
                } else {
                    int r = ThreadLocalRandom.current().nextInt(1, 101);
                    if(r<=10)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.pl.getConfig().getString("commandEventCommand").replaceAll("<player>", p.getName()));
                    p.sendMessage(ChatColor.DARK_RED+"You have more than 5 accounts online from your IP! You will not receive a reward.");
                }
            }
            Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "Every player has received a bonus!");
        }
    }
    private Location randomLoc() {
        while (true) {
            int x2 = ThreadLocalRandom.current().nextInt(pl.getConfig().getInt("xMin"), pl.getConfig().getInt("xMax"));
            int z2 = ThreadLocalRandom.current().nextInt(pl.getConfig().getInt("zMin"), pl.getConfig().getInt("zMax"));
            int y = world.getHighestBlockAt(z2, x2).getLocation().getBlockY();
            Block material = world.getBlockAt(x2, y - 1, z2);
            if (!material.isLiquid() || Board.getInstance().getFactionAt(new FLocation(pl.getConfig().getString("worldName"), x2, z2)) == null) {
                return new Location(world,
                        x2,
                        y + 3,
                        z2);
            }
        }
    }

    /**
     * Spawns a diamond horse regardless of charge level.
     */
    public void startHorse() {
        final Location loc = randomLoc();
        final Location loc2 = new Location(world, loc.getX(), 256.0, loc.getZ());
        loc2.getChunk().load();

        new BukkitRunnable() {
            @Override
            public void run() {
                Horse horse = (Horse) world.spawnEntity(loc2, EntityType.HORSE);
                final UUID uuid = horse.getUniqueId();
                pl.horseLocs.put(uuid.toString(), horse.getLocation());
                pl.horseUUID.add(horse.getUniqueId().toString());
                horse.setAdult();
                horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 10));
                horse.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 10));
                horse.setRemoveWhenFarAway(false);
                horse.setVariant(Horse.Variant.HORSE);
                horse.setMaxDomestication(Integer.MAX_VALUE);
                horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING));
                horse.setCustomName("Diamond Horse");
                Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "A diamond horse has spawned at X: " + ChatColor.GOLD + horse.getLocation().getX() + ChatColor.RED + " Y: " + ChatColor.GOLD + horse.getLocation().getY() + ChatColor.RED + " Z: " + ChatColor.GOLD + horse.getLocation().getZ() + ChatColor.RED + " Kill the horse for a generous reward!");
                new BukkitRunnable() {//fall damage
                    @Override
                    public void run() {
                        horse.setHealth(horse.getMaxHealth());
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("PowerCore"), 40);
                new BukkitRunnable() {//gives cords
                    @Override
                    public void run() {
                        Horse horse = null;
                        for (Entity e : world.getEntities())
                            if (e instanceof Horse && e.getUniqueId().toString().equals(uuid.toString()))
                                horse = (Horse) e;

                        if (horse != null) {
                            if (horse.isDead() || horse.getHealth() == 0) {
                                this.cancel();

                            } else {
                                pl.horseLocs.replace(uuid.toString(), horse.getLocation());
                                Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "A diamond horse is alive at X: "
                                        + ChatColor.GOLD + (int) horse.getLocation().getX()
                                        + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) horse.getLocation().getY()
                                        + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) horse.getLocation().getZ()
                                        + ChatColor.RED + " Kill the horse for a generous reward!");
                            }
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Bukkit.getServer().getPluginManager().getPlugin("PowerCore"), 600, 2400);
            }
        }.runTaskLater(pl, 60);
    }
}
