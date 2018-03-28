package com.harbo.factionscore.commands;

import com.harbo.factionscore.EventManager;
import com.harbo.factionscore.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class PowercoreCommand implements CommandExecutor{

    private final Main pl;
    private final EventManager em;
    private final Economy econ;
    private final double costPerCharge;
    private final double xpPerCharge;

    public PowercoreCommand(Main main, EventManager eventManager, Economy econ){
        this.econ = econ;
        pl=main;
        em=eventManager;
        costPerCharge = pl.getConfig().getDouble("costPerCharge");
        xpPerCharge = pl.getConfig().getDouble("xpPerCharge");
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player p = (Player)commandSender;

        if (args.length == 0) {
            sendHelpMessage(p);
            return true;
        }
        if(args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                sendHelpMessage(p);
                return true;
            } else if (args[0].equalsIgnoreCase("startevent")) {
                if (p.hasPermission("powercore.admin")) {
                    em.startEvent();
                    return true;
                } else {
                    p.sendMessage(ChatColor.RED + "Insufficient Permission!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                p.sendMessage(ChatColor.GOLD + "The PowerCore charge is: " + ChatColor.RED + pl.getCharge());
                return true;
            } else if (args[0].equalsIgnoreCase("events")) {
                int i = 1;
                p.sendMessage(ChatColor.RED +
                        "---PowerCore Active Events---");
                for (Location l : pl.giantLocs.values()) {
                    p.sendMessage(ChatColor.GOLD + "" + i++ + ". " + ChatColor.RED + "A Giant is alive at X: "
                            + ChatColor.GOLD + (int) l.getX()
                            + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) l.getY()
                            + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) l.getZ());
                }
                for (Location l : pl.horseLocs.values()) {
                    p.sendMessage(ChatColor.GOLD + "" + i++ + ". " + ChatColor.RED + "A diamond horse is alive at X: "
                            + ChatColor.GOLD + (int) l.getX()
                            + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) l.getY()
                            + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) l.getZ());
                }
                for (Location l : pl.chestLocs) {
                    p.sendMessage(ChatColor.GOLD + "" + i++ + ". " + ChatColor.RED + "A supply drop is untouched at X: "
                            + ChatColor.GOLD + (int) l.getX()
                            + ChatColor.RED + " Y: " + ChatColor.GOLD + (int) l.getY()
                            + ChatColor.RED + " Z: " + ChatColor.GOLD + (int) l.getZ());
                }
//                        if(factionFly != null && factionFly.getTime() > System.currentTimeMillis())
//                            p.sendMessage(ChatColor.GOLD + "" + i++ + ". " + ChatColor.RED +"FactionFly is active for " + Math.round((factionFly.getTime() - System.currentTimeMillis())/1000L/60L) + " minutes.");
                return true;
            } else if (args[0].equalsIgnoreCase("horse")) {
                if ((p.hasPermission("powercore.admin") || p.isOp())) {
                    em.startHorse();
                    return true;
                } else {
                    p.sendMessage(ChatColor.DARK_RED + "Unknown parameters! Use /powercore help");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setchest") && ((p.hasPermission("powercore.admin")) || p.isOp())) {
                int k = 0;
                for (ItemStack im :
                        p.getInventory()) {
                    if (im != null)
                        k++;
                }
                if (k > 27) {
                    p.sendMessage(ChatColor.RED + "You must have 27 or fewer items in your inventory to save a chest!");
                    return true;
                }
                ItemStack[] temp = new ItemStack[27];
                pl.chest.set("storedChestData", p.getInventory().getContents());
                try {
                    pl.chest.save(pl.chestf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int j = 0;
                for (ItemStack i : p.getInventory()) {
                    try {
                        if (i.getType() != null) {
                            temp[j] = i;
                            j++;
                        }
                    } catch (Exception ignored) {
                    }
                }
                pl.invenItems = temp;
                p.sendMessage(ChatColor.GREEN + "Chest saved!");
                return true;
            } else {
                p.sendMessage(ChatColor.DARK_RED + "Unknown parameters! Use /powercore help");
                return true;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("donate")) {
                if (args[1].equalsIgnoreCase("money")) {
                    try {
                        double donation = Math.floor(Double.parseDouble(args[2]));
                        if (econ.getBalance(p) >= donation) {
                            if(donation <= 0){
                                p.sendMessage(ChatColor.DARK_RED + "You must enter a value greater than 0!");
                                return  true;
                            }
                            double charges = Math.floor((donation/costPerCharge) * 1000)/1000;
                            if(charges + pl.getCharge() > pl.getChargeTotal()){
                                p.sendMessage(ChatColor.DARK_RED + "You tried to add too many charges!");
                                return true;
                            } else {
                                pl.addCharge(charges);
                                econ.withdrawPlayer(p, donation);
                                p.sendMessage(ChatColor.GOLD + "You added " + charges +" for "+ ChatColor.GREEN +"$"+donation + ChatColor.GOLD + ".");
                                return true;
                            }
                        } else {
                            p.sendMessage(ChatColor.DARK_RED + "You don't have enough money to do that!");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        p.sendMessage(ChatColor.DARK_RED+"You must enter a number!");
                        return true;
                    }
                } else if (args[1].equalsIgnoreCase("xp")) {
                    try {
                        Integer levels = Integer.parseInt(args[2]);
                        if (p.getLevel() >= levels) {
                            if (levels <= 0) {
                                p.sendMessage(ChatColor.DARK_RED + "You must enter a value greater than 0!");
                                return true;
                            }
                            double charges = Math.floor((levels / xpPerCharge) * 1000) / 1000;
                            if(charges + pl.getCharge() > pl.getChargeTotal()) {
                                p.sendMessage(ChatColor.DARK_RED + "You tried to add to many charges!");
                                return true;
                            } else {
                                pl.addCharge(charges);
                                p.sendMessage(ChatColor.GOLD + "You donated " + levels + " levels for " + charges + " charges.");
                                p.giveExpLevels(-1 * levels);
                                return true;
                            }
                        } else {
                            p.sendMessage(ChatColor.DARK_RED + "You don't have enough levels to do that!");
                            return true;
                        }
                    } catch (NumberFormatException ex) {
                        p.sendMessage(ChatColor.DARK_RED + "You must enter an integer number!");
                        return true;
                    }
                }
            } else {
                p.sendMessage(ChatColor.DARK_RED + "Unknown parameters! Use /powercore help");
                return true;
            }
        } else {
            p.sendMessage(ChatColor.DARK_RED + "Unknown parameters! Use /powercore help");
            return true;
        }
        return true;
    }
    private void sendHelpMessage(Player p){
        p.sendMessage(ChatColor.RED +
                "---PowerCore Commands---\n" +
                ChatColor.GOLD + "/powercore donate <type> <amountOfCharges> " + ChatColor.RED + "- Donate money or xp in exchange for charges. $" + costPerCharge + " or " + xpPerCharge + " levels of XP will yield one charge.\n" +
                ChatColor.GOLD + "/powercore status " + ChatColor.RED + "- Get the status of the PowerCore\n" +
                ChatColor.GOLD + "/powercore events " + ChatColor.RED + "- List all the active events, if any.\n" +
                ChatColor.GOLD + "/powercore help " + ChatColor.RED + "- Display this message.");
        if (p.hasPermission("powercore.admin")) {
            p.sendMessage(ChatColor.DARK_RED +
                    "---Admin Commands---\n" +
                    ChatColor.GOLD + "/charges " + ChatColor.RED + "- Manage the PowerCore charges\n" +
                    ChatColor.GOLD + "/powercore horse " + ChatColor.RED + "- Start a horse event.\n" +
                    ChatColor.GOLD + "/powercore startevent " + ChatColor.RED + " - Force a PowerCore event to start.\n" +
                    ChatColor.GOLD + "/powercore setchest "+ ChatColor.RED + " - Set the chest inventory for supply drop.");
        }
    }
}
