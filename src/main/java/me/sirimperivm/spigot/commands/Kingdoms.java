package me.sirimperivm.spigot.commands;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.colors.Colors;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Errors;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Kingdoms implements CommandExecutor, TabCompleter {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private Errors errors;
    private DBUtil db;
    private ModUtil mod;

    public Kingdoms(Main plugin) {
        this.plugin = plugin;

        log = plugin.getLog();
        config = plugin.getCM();
        errors = plugin.getErrors();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    private void getUsage(CommandSender s) {
        for (String usage : config.getSettings().getStringList("helps.commands.kingdoms")) {
            s.sendMessage(Colors.translateString(usage));
        }
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {

        if (c.getName().equalsIgnoreCase("kg")) {
            if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.main"))) {
                return true;
            } else {
                if (a.length == 0) {
                    getUsage(s);
                } else if (a.length == 1) {
                    if (a[0].equalsIgnoreCase("accept")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.accept"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.insertPlayerKingdom(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("disband")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.disband"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.disbandPlayerKingdom(p);
                            }
                        }
                    } else {
                        getUsage(s);
                    }
                } else if (a.length == 2) {
                    if (a[0].equalsIgnoreCase("create")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.create"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                String kingdomName = a[1];

                                if (kingdomName != null) {
                                    int leaderRoleId = config.getSettings().getInt("kingdoms.roles.leader.id");
                                    Kingdom kingdom = new Kingdom(plugin, kingdomName);
                                    kingdom.createKingdom(p, leaderRoleId);
                                } else {
                                    p.sendMessage(config.getTranslatedString("messages.kingdoms.creation.error.name-required"));
                                }
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("invite")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.invite"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                Player t = Bukkit.getPlayerExact(a[1]);

                                if (t == null || !Bukkit.getOnlinePlayers().contains(t)) {
                                    p.sendMessage(config.getTranslatedString("messages.kingdoms.general.error.user-offline"));
                                } else {
                                    Kingdom kingdom = mod.getPlayerKingdom(p);
                                    mod.invitePlayerKingdom(p, t, kingdom);
                                }
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("expel")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.expel"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                Player t = Bukkit.getPlayerExact(a[1]);

                                if (t == null || !Bukkit.getOnlinePlayers().contains(t)) {
                                    p.sendMessage(config.getTranslatedString("messages.kingdoms.general.error.user-offline"));
                                } else {
                                    mod.expelPlayerKingdom(p, t);
                                }
                            }
                        }
                    } else {
                        getUsage(s);
                    }
                } else {
                    getUsage(s);
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {

        if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.main"))) {
            if (a.length == 1) {
                List<String> toReturn = new ArrayList<>();
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.create"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (!db.getPlayers().existsPlayerData(player)) {
                            toReturn.add("create");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.disband"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "disband")) {
                            toReturn.add("disband");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.accept"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (!db.getPlayers().existsPlayerData(player)) {
                            toReturn.add("accept");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.invite"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "invite-players")) {
                            toReturn.add("invite");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.expel"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "expel-players")) {
                            toReturn.add("expel");
                        }
                    }
                }
                return toReturn;
            } else if (a.length == 2) {
                List<String> toReturn = new ArrayList<>();
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.create"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (!db.getPlayers().existsPlayerData(player)) {
                            if (a[0].equalsIgnoreCase("create")) {
                                toReturn.add("<kingdomName>");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.invite"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "invite-players")) {
                            if (a[0].equalsIgnoreCase("invite")) {
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    if (!db.getPlayers().existsPlayerData(online)) {
                                        toReturn.add(online.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.expel"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "expel-players")) {
                            if (a[0].equalsIgnoreCase("expel")) {
                                int kingdomId = db.getPlayers().getKingdomId(player);
                                for (Player online : db.getKingdoms().kingdomPlayersList(kingdomId)) {
                                    toReturn.add(online.getName());
                                }
                            }
                        }
                    }
                }
                return toReturn;
            }
        }
        return new ArrayList<>();
    }
}
