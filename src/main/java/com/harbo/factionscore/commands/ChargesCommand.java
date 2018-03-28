package com.harbo.factionscore.commands;

import com.harbo.factionscore.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChargesCommand implements CommandExecutor {
    Main pl;
    public ChargesCommand(Main main){
        pl=main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(sender instanceof Player){
            Player p = (Player)sender;
            if (p.hasPermission("powercore.admin") || p.isOp()) {
                if (args.length == 0) {
                    p.sendMessage(ChatColor.RED + "---PowerCore Charge---\n" +
                            ChatColor.GOLD + "/charges add <amount>" + ChatColor.RED + " - Add charges to the core\n" +
                            ChatColor.GOLD + "/charges set <amount>" + ChatColor.RED + " - Set the amount of charges.");
                    return true;
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        int set;
                        try {
                            set = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            p.sendMessage(ChatColor.DARK_RED + "You didn't enter a number!");
                            return true;
                        }
                        if (set > pl.getChargeTotal() || set < 0) {
                            p.sendMessage(ChatColor.DARK_RED + "You must choose a number between 0 and " + pl.getChargeTotal());
                            return true;
                        }
                        pl.setCharge(set);
                        p.sendMessage(ChatColor.GREEN + "Charge level set to " + pl.getCharge());
                        return true;
                    } else if (args[0].equalsIgnoreCase("add")) {
                        int add;
                        try {
                            add = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            p.sendMessage(ChatColor.DARK_RED + "You didn't enter a number!");
                            return true;
                        }
                        if (pl.getCharge() + add <= pl.getChargeTotal()) {
                            if (add <= 0) {
                                p.sendMessage(ChatColor.DARK_RED + "You must add at least 1 charge!");
                                return true;
                            }
                            pl.addCharge(add);
                            p.sendMessage(ChatColor.GREEN + "Charge level set to " + pl.getCharge());
                            return true;
                        } else {
                            pl.setCharge(pl.getChargeTotal());
                            p.sendMessage(ChatColor.YELLOW + "The amount you added exceed the max, the charge level has been set to its max.");
                            return true;
                        }
                    } else {
                        p.sendMessage(ChatColor.DARK_RED + "Unknown parameters! Use /charges");
                        return true;
                    }
                } else {
                    p.sendMessage(ChatColor.DARK_RED + "Unknown parameters! Use /charges");
                    return true;
                }
            } else {
                p.sendMessage(ChatColor.DARK_RED + "Insufficient Permissions!");
                return true;
            }
        } else {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "---PowerCore Charge---\n" +
                        ChatColor.GOLD + "/charges add <amount>" + ChatColor.RED + " - Add charges to the core\n" +
                        ChatColor.GOLD + "/charges set <amount>" + ChatColor.RED + " - Set the amount of charges.");
                return true;
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("set")) {
                    int set;
                    try {
                        set = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.DARK_RED + "You didn't enter a number!");
                        return true;
                    }
                    if (set > pl.getChargeTotal() || set < 0) {
                        sender.sendMessage(ChatColor.DARK_RED + "You must choose a number between 0 and " + pl.getChargeTotal());
                        return true;
                    }
                    pl.setCharge(set);
                    sender.sendMessage(ChatColor.GREEN + "Charge level set to " + pl.getCharge());
                    return true;
                } else if (args[0].equalsIgnoreCase("add")) {
                    int add;
                    try {
                        add = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.DARK_RED + "You must enter a number!");
                        return true;
                    }
                    if (pl.getCharge() + add <= pl.getChargeTotal()) {
                        if (add <= 0) {
                            sender.sendMessage(ChatColor.DARK_RED + "You must add at least 1 charge!");
                            return true;
                        }
                        pl.addCharge(add);
                        sender.sendMessage(ChatColor.GREEN + "Charge level set to " + pl.getCharge());
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "The amount you added exceed the max, the charge level has been set to its max.");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "---PowerCore Charge---\n" +
                            ChatColor.GOLD + "/charges add <amount>" + ChatColor.RED + " - Add charges to the core\n" +
                            ChatColor.GOLD + "/charges set <amount>" + ChatColor.RED + " - Set the amount of charges.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "---PowerCore Charge---\n" +
                        ChatColor.GOLD + "/charges add <amount>" + ChatColor.RED + " - Add charges to the core\n" +
                        ChatColor.GOLD + "/charges set <amount>" + ChatColor.RED + " - Set the amount of charges.");
                return true;
            }
        }
    }
}
