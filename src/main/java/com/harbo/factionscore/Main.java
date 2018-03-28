package com.harbo.factionscore;

import com.harbo.factionscore.commands.ChargesCommand;
import com.harbo.factionscore.commands.PowercoreCommand;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.SphereEffect;
import de.slikey.effectlib.util.ParticleEffect;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityMountEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener {
    private double charge = 0;
    private double chargeTotal;
    private double triggerValue;
    private double junkValue;
    private double junkPay;
    private EffectManager effectManager;
    private final HashMap<Integer, HashMap<Short, Double>> values = new HashMap<>();
    private final HashMap<Integer, HashMap<Short, Double>> money = new HashMap<>();
    public ItemStack[] invenItems = new ItemStack[27];
    private World world;
    private OrbListener orbListener;
    List<String> horseUUID, giantUUIDs;
    private Economy econ;
    public File chestf;
    private File logFile;
    public FileConfiguration chest;
    private final ArrayList<UUID> arrowIds = new ArrayList<>();
    public final ArrayList<Location> chestLocs = new ArrayList<>();
    public final HashMap<String, Location> giantLocs = new HashMap<>();
    public final HashMap<String, Location> horseLocs = new HashMap<>();
    private EventManager eventManager;

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(Level.INFO, "============================================================");
        Bukkit.getLogger().log(Level.INFO, "             PowerCore v"+getDescription().getVersion()+" has been enabled.");
        Bukkit.getLogger().log(Level.INFO, "           Written by Harbo5 for Emenbee Realms.");
        Bukkit.getLogger().log(Level.INFO, "============================================================");
        saveDefaultConfig();
        createFiles();
        try {
            chest.addDefault("storedChestData", "");
            chest.options().copyDefaults(true);
            chest.save(chestf);
        } catch (Exception e){
            e.printStackTrace();
        }
        horseUUID = new ArrayList<>();
        giantUUIDs = new ArrayList<>();
        registerValues();
        triggerValue = getConfig().getInt("eventCharges");
        chargeTotal = getConfig().getInt("amountOfCharges");
        NMSUtil nmsu = new NMSUtil();
        orbListener = new OrbListener(this);
        effectManager = new EffectManager(this);
        eventManager = new EventManager(this);
        nmsu.registerEntity("Giant Problem", 53, GiantProblem.class);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(orbListener, this);
        world = Bukkit.getWorld(this.getConfig().getString("worldName"));
        junkValue = getConfig().getDouble("junkValue");
        junkPay = getConfig().getDouble("junkPay");
        charge = getConfig().getDouble("chargeValue");
        setupEconomy();
        getCommand("powercore").setExecutor(new PowercoreCommand(this, eventManager, econ));
        getCommand("charges").setExecutor(new ChargesCommand(this));
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Entity e : world.getEntities()) {
                    if(e.getType()==EntityType.GIANT && giantUUIDs.contains(e.getUniqueId().toString())) {
                        ArrayList<Player> nearBy;
                        Giant giant = (Giant)e;
                        nearBy = giant.getNearbyEntities(20, 20, 20).stream().filter(ent -> ent.getType() == EntityType.PLAYER).map(ent -> (Player) ent).collect(Collectors.toCollection(ArrayList::new));
                        if (nearBy.size() > 0) {
                            int attack = (int) (Math.random() * (nearBy.size()));
                            Arrow arrow = giant.launchProjectile(Arrow.class);
                            arrow.setVelocity(nearBy.get(attack).getEyeLocation().subtract(giant.getEyeLocation()).toVector().normalize().multiply(3));
                            arrowIds.add(arrow.getUniqueId());
                            SphereEffect sphereEffect = new SphereEffect(effectManager);
                            sphereEffect.setEntity(arrow);
                            sphereEffect.color = Color.PURPLE;
                            sphereEffect.iterations = 10;
                            sphereEffect.start();
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L);
        spawnSphere();
        try {
            ItemStack[] content = ((List<ItemStack>) chest.get("storedChestData")).toArray(new ItemStack[0]);
            Inventory inv = Bukkit.createInventory(null, 36);
            inv.setContents(content);
            int i = 0;
            for(ItemStack im : inv.getContents()){
                try {
                    if (im.getType() != null) {
                        invenItems[i]=im;
                        i++;
                    }
                } catch(Exception ignored){
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Chest initialization failed! Delete your storedChestData chest.yml line or remake chest inventory with /powercore setchest");
        }
        if(Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")){
            new HologramPlaceHolder(this);
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e){
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST){
            Location loc = e.getClickedBlock().getLocation();
            for(Location l : chestLocs){
                if(l.equals(loc)){
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The loot crate was found by " + e.getPlayer().getName());
                    chestLocs.remove(l);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e){
        if(e.getBlock().getType() == Material.CHEST){
            Location loc = e.getBlock().getLocation();
            for(Location l : chestLocs){
                if(l.equals(loc)){
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The loot crate was found by " + e.getPlayer().getName());
                    chestLocs.remove(l);
                    break;
                }
            }
        }
    }

    @Override
    public void onDisable(){
        reloadConfig();
        getConfig().set("chargeValue", charge);
        saveConfig();
    }

    /**
     * Log a message to the local log file.
     *
     * @param message Log message
     */
    public void log(String message) {
        try {
            FileWriter fw = new FileWriter(logFile, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(message);
            pw.flush();
            pw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void createFiles() {
        chestf = new File(getDataFolder(), "chest.yml");

        if (!chestf.exists()) {
            chestf.getParentFile().mkdirs();
            saveResource("chest.yml", false);
        }

        chest = new YamlConfiguration();
        try {
            chest.load(chestf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File dataFolder = getDataFolder();
            if(!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            logFile = new File(getDataFolder(), "log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().log(Level.SEVERE, "You must have Vault installed to run this plugin!");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Horse && horseUUID.contains(e.getEntity().getUniqueId().toString())) {
            horseLocs.remove(e.getEntity().getUniqueId().toString());
            if (e.getEntity().getKiller() != null) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The horse was slayed by " + e.getEntity().getKiller().getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("horseCommand").replaceAll("<player>", e.getEntity().getKiller().getName()));
            }
            else {
                Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The horse was killed.");
                ArrayList<Player> players = new ArrayList<>();
                for(Entity et : e.getEntity().getNearbyEntities(50,50,50)) {
                    if(et instanceof Player){
                        players.add((Player)et);
                    }
                }
                if(players.size()!=0){
                    int i = ThreadLocalRandom.current().nextInt(0, players.size());
                    int j = 0;
                    for(Player p : players){
                        if(j==i) {
                            p.sendMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The horse died of natural causes near you, but you received the reward!" );
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("horseCommand").replaceAll("<player>", p.getName()));
                            break;
                        }
                        j++;
                    }
                }
            }
        } else if (e.getEntity() instanceof Giant && giantUUIDs.contains(e.getEntity().getUniqueId().toString())) {
            giantLocs.remove(e.getEntity().getUniqueId().toString());
            if (e.getEntity().getKiller() != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("giantCommand").replaceAll("<player>", e.getEntity().getKiller().getName()));
                Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The giant was slayed by " + e.getEntity().getKiller().getName());
            } else
                Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The giant was killed.");
        }
    }

    @EventHandler
    private void onDespawn(ChunkUnloadEvent e) {
        for (Entity en : e.getChunk().getEntities()) {
            if (horseUUID.size()>0)
                if (horseUUID.contains(en.getUniqueId().toString())) {
                    e.setCancelled(true);
                }
            if (giantUUIDs.size() > 0)
                if (giantUUIDs.contains(en.getUniqueId().toString())) {
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMount(EntityMountEvent e){
        if(e.getMount() instanceof Horse && e.getEntity() instanceof Player && horseUUID.contains(e.getMount().getUniqueId().toString())){
            e.getEntity().sendMessage(ChatColor.RED + "You cannot mount the diamond horse!");
            e.setCancelled(true);
        }
    }

    private void spawnSphere() {
        final Location loc = new Location(Bukkit.getWorld(getConfig().getString("worldName")), getConfig().getDouble("x-pos"), getConfig().getDouble("y-pos"), getConfig().getDouble("z-pos"));
        new BukkitRunnable() {
            @Override
            public void run() {
                SphereEffect core = new SphereEffect(effectManager);
                core.visibleRange = 100;
                core.radius = getConfig().getDouble("radius") * charge / chargeTotal > 3 ? getConfig().getDouble("radius") * charge / chargeTotal : 3;
                core.setLocation(loc);
                core.iterations = 1;
                core.particle = ParticleEffect.FLAME;
                core.particles = 800;
                core.start();
            }
        }.runTaskTimer(this, 0, 10);
    }

    @SuppressWarnings("deprecation")
    double coreDonate(ItemStack im) {
        double rt;
        if (values.containsKey(im.getTypeId()) && values.get(im.getTypeId()).containsKey(im.getDurability())) {
            rt = values.get(im.getTypeId()).get(im.getDurability()) * im.getAmount();
            if(im.getItemMeta().hasLore() && im.getItemMeta().getLore().get(0).equalsIgnoreCase(" "))
                rt = rt *.2;
            return rt;
        }

        rt =  junkValue * im.getAmount();
        if(im.getItemMeta().hasLore() && im.getItemMeta().getLore().get(0).equalsIgnoreCase(" "))
            rt = rt *.2;
        return rt;
    }

    /**
     * Pays player for donation
     *
     * @param im ItemStack of thing being donated
     */
    @SuppressWarnings("deprecation")
    double payPlayer(ItemStack im) {
        if (money.containsKey(im.getTypeId()) && money.get(im.getTypeId()).containsKey(im.getDurability())) {
            return money.get(im.getTypeId()).get(im.getDurability()) * im.getAmount();
        }
        return junkPay * im.getAmount();

    }
    /**
     * Sets the charge level
     *
     * @param value The charge level you wish to set the orb to.
     */
    public void setCharge(double value) {
        if (value == chargeTotal) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The PowerCore has reached 100%! The charge will reset to zero and the special event will start!");
            eventManager.startHorse();
            charge = 0;
        } else if((value) % Math.floor(triggerValue) == 0 && (charge) != 0 && charge != chargeTotal){
            charge = Math.floor(value*1000) / 1000;
            eventManager.startEvent();
        } else {
            charge = Math.floor(value*1000) / 1000;
        }
    }

    /**
     * Add charges to the core.
     *
     * @param value Amount of charges to be added.
     */
    public void addCharge(double value) {
        if (charge + value >= chargeTotal) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[PowerCore]: " + ChatColor.RED + "The PowerCore has reached 100%! The charge will reset to zero and the special event will start!");
            eventManager.startHorse();
            charge = 0;
        } else if ((charge + value) % Math.floor(triggerValue) == 0 && (charge + value) != 0 && charge != chargeTotal) {
            eventManager.startEvent();
            if (value > triggerValue)
                for(int i = 0; i < (int)Math.floor(value/triggerValue); i++)
                    eventManager.startEvent();
            charge = Math.floor(1000*(charge + value))/1000;
        } else if(value > triggerValue){
            for(int i = 0; i < (int)Math.floor(value/triggerValue); i++)
                eventManager.startEvent();
            charge = Math.floor(1000*(charge + value))/1000;
        } else if(charge % Math.floor(triggerValue) > (charge+value) % Math.floor(triggerValue)){
            eventManager.startEvent();
            charge = Math.floor(1000*(charge + value))/1000;
        } else {
            charge = Math.floor(1000*(charge + value))/1000;
        }
    }

    private void registerValues() {
        for (String s : getConfig().getStringList("itemValues")) {
            final String[] temp = s.split(":");
            try {
                values.putIfAbsent(Integer.valueOf(temp[0]), new HashMap<>());
                values.get(Integer.valueOf(temp[0])).put(Short.valueOf(temp[1]), Double.valueOf(temp[2]));

                money.putIfAbsent(Integer.valueOf(temp[0]), new HashMap<>());
                money.get(Integer.valueOf(temp[0])).put(Short.valueOf(temp[1]), Double.valueOf(temp[3]));

            } catch (NumberFormatException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Bad config values! Make sure item values are formatted id:damageValue:charges:moneyGiven");
            }
        }
    }

    /**
     * Convert a donated item into charges.
     *
     * @param p Player who donated the item.
     */
    void donate(Player p) {

        p.sendMessage(ChatColor.GOLD + "Item donated for " + coreDonate(OrbListener.getItem(p.getUniqueId())) + " charges and received $" + payPlayer(OrbListener.getItem(p.getUniqueId())) + ".");
        addCharge(coreDonate(OrbListener.getItem(p.getUniqueId())));
        econ.depositPlayer(p, payPlayer(OrbListener.getItem(p.getUniqueId())));
        OrbListener.removePlayer(p.getUniqueId());
    }


    /**
     * Get the current charge level of the orb.
     *
     * @return current charge value.
     */
    public double getCharge() {
        return charge;
    }

    /**
     * Get the maximum charge value.
     *
     * @return maximum charge value.
     */
    public double getChargeTotal() {
        return chargeTotal;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowHit(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER && arrowIds.contains(e.getDamager().getUniqueId())) {
            arrowIds.remove(e.getEntity().getUniqueId());
            e.setDamage(20D);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void Damage (EntityDamageEvent e) {
        if(e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity() instanceof Giant && giantUUIDs.contains(e.getEntity().getUniqueId().toString())){
            e.setCancelled(true);
        } else if(e.getCause() == EntityDamageEvent.DamageCause.FALL && e.getEntity() instanceof Horse && horseUUID.contains(e.getEntity().getUniqueId().toString())) {
            e.setCancelled(true);
        }
    }

}
