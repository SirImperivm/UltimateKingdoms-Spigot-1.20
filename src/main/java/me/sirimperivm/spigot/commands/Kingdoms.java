package me.sirimperivm.spigot.commands;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.DBUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void getUsage(CommandSender s, int page) {
        mod.createHelp(s, "kingdoms-command", page);
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {

        if (c.getName().equalsIgnoreCase("kg")) {
            if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.main"))) {
                return true;
            } else {
                if (a.length == 0) {
                    getUsage(s, 1);
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
                                mod.preDisbandPlayerKingdom(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("disband-confirm")) {
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
                    } else if (a[0].equalsIgnoreCase("claim")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.claim"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.claimChunk(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("sethome")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.sethome"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.setKingdomHome(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("home")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.home"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.reachKingdomHome(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("leave")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.leave"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.leavePlayer(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("chat")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.chat"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.insertPlayerChat(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("claims")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.claims"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.sendKingdomsClaims(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("changelead-confirm")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.changelead"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.setNewLead(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("rankup")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.changelead"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.rankupKingdom(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("unclaim")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.unclaim"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.unclaimChunk(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("unclaimall")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.unclaimall"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.unclaimAllChunks(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("unclaimall-confirm")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.unclaimall"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.unclaimAllConfirm(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("deposit")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.deposit"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.sendDepositGui(p);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("list")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.list"))) {
                            return true;
                        } else {
                            mod.sendKingdomsList(s, 1);
                        }
                    } else if (a[0].equalsIgnoreCase("showchunks")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.showchunks"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                if (mod.getChunksBordersPlayerList().contains(p)) {
                                    mod.getChunksBordersPlayerList().remove(p);
                                    p.sendMessage(config.getTranslatedString("messages.kingdoms.show-chunks.info.disabled"));
                                } else {
                                    mod.getChunksBordersPlayerList().add(p);
                                    p.sendMessage(config.getTranslatedString("messages.kingdoms.show-chunks.info.actived"));
                                }
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("permissions")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.permissions"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                mod.sendMainPermissionsGui(p);
                            }
                        }
                    } else {
                        getUsage(s, 1);
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
                    } else if (a[0].equalsIgnoreCase("help")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.main"))) {
                            return true;
                        } else {
                            int page = Integer.parseInt(a[1]);
                            getUsage(s, page);
                        }
                    } else if (a[0].equalsIgnoreCase("changelead")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.changelead"))) {
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
                                    mod.preSetNewLead(p, t);
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
                    } else if (a[0].equalsIgnoreCase("setwarp")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.setwarp"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                String warpName = a[1];

                                mod.setKingdomWarp(p, warpName);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("warp")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.warp"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                String warpName = a[1];

                                mod.reachKingdomWarp(p, warpName);
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("list")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.list"))) {
                            return true;
                        } else {
                            int page = Integer.parseInt(a[1]);
                            mod.sendKingdomsList(s, page);
                        }
                    } else if (a[0].equalsIgnoreCase("info")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.info"))) {
                            return true;
                        } else {
                            String kingdomName = a[1];
                            mod.sendKingdomInfo(s, kingdomName);
                        }
                    } else {
                        getUsage(s, 0);
                    }
                } else if (a.length == 3) {
                    if (a[0].equalsIgnoreCase("changerole")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.kingdoms.change-role"))) {
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
                                    String roleName = a[2];
                                    mod.changeRole(p, t, roleName);
                                }
                            }
                        }
                    } else {
                        getUsage(s, 1);
                    }
                } else {
                    getUsage(s, 1);
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
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.accept"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (!db.getPlayers().existsPlayerData(player)) {
                            toReturn.add("accept");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.claim"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "expand-territory")) {
                            toReturn.add("claim");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.main"))) {
                    toReturn.add("help");
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.claims"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "expand-territory")) {
                            toReturn.add("claims");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.rankup"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "manage-ranks")) {
                            toReturn.add("rankup");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.chat"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "use-chat")) {
                            toReturn.add("chat");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.changelead"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (!config.getSettings().getBoolean("kingdoms.default-settings.block-change-lead")) {
                            if (mod.hasPermission(player, "change-lead")) {
                                toReturn.add("changelead");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.leave"))) {
                    if (s instanceof Player) {
                        Player p = (Player) s;
                        if (db.getPlayers().existsPlayerData(p)) {
                            int kingdomId = db.getPlayers().getKingdomId(p);

                            String kingdomLeader = null;
                            List<String> kingdomPlayers = db.getKingdoms().getKingdomPlayers(kingdomId);
                            for (String kingdomPlayer : kingdomPlayers) {
                                int kingdomRoleId = db.getPlayers().getKingdomRole(kingdomPlayer);
                                String kingdomRole = db.getRoles().getRoleName(kingdomRoleId);

                                if (kingdomRole.equalsIgnoreCase("leader")) {
                                    kingdomLeader = kingdomPlayer;
                                    break;
                                }
                            }
                            if (!kingdomLeader.equals(p.getName())) {
                                toReturn.add("leave");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.deposit"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "deposit")) {
                            toReturn.add("deposit");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.sethome"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "set-home")) {
                            toReturn.add("sethome");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.home"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "use-home")) {
                            toReturn.add("home");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.setwarp"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "set-warps")) {
                            toReturn.add("setwarp");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.warp"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "use-warps")) {
                            toReturn.add("warp");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.disband"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "disband")) {
                            if (!mod.getDisbandCooldown().contains(player)) {
                                toReturn.add("disband");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.disband"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "disband")) {
                            if (mod.getDisbandCooldown().contains(player)) {
                                toReturn.add("disband-confirm");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.unclaim"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "expand-territory")) {
                            toReturn.add("unclaim");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.unclaimall"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "release-all-territories")) {
                            if (!mod.getReleaseAllCooldown().contains(player)) {
                                toReturn.add("unclaimall");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.unclaimall"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "release-all-territories")) {
                            if (mod.getReleaseAllCooldown().contains(player)) {
                                toReturn.add("unclaimall");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.list"))) {
                    toReturn.add("list");
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.info"))) {
                    toReturn.add("info");
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.create"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (!db.getPlayers().existsPlayerData(player)) {
                            toReturn.add("create");
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
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.invite"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "invite-players")) {
                            toReturn.add("invite");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.change-role"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "manage-players")) {
                            toReturn.add("changerole");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.permissions"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "manage-roles")) {
                            toReturn.add("permissions");
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.showchunks"))) {
                    toReturn.add("showchunks");
                }
                return toReturn;
            } else if (a.length == 2) {
                List<String> toReturn = new ArrayList<>();
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.list"))) {
                    if (a[0].equalsIgnoreCase("list")) {
                        List<Kingdom> kingdomsList = db.getKingdoms().kingdomsList();
                        Map<String, Double> map = new HashMap<>();
                        for (Kingdom kingdom : kingdomsList) {
                            String kingdomName = kingdom.getKingdomName();
                            double gold = kingdom.getGoldAmount();
                            map.put(kingdomName, gold);
                        }

                        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(map.entrySet());
                        sortedList.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

                        List<Kingdom> kingdoms = new ArrayList<>();
                        for (Map.Entry<String, Double> entry : sortedList) {
                            Kingdom kingdom = new Kingdom(plugin, entry.getKey());
                            kingdoms.add(kingdom);
                        }

                        int pageSize = 5;
                        int pageCount = (int) Math.ceil((double) kingdoms.size()/pageSize);
                        for (int page=1; page<=pageCount; page++) {
                            toReturn.add(String.valueOf(page));
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.info"))) {
                    if (a[0].equalsIgnoreCase("info")) {
                        List<Kingdom> kingdomsList = db.getKingdoms().kingdomsList();
                        for (Kingdom kingdom : kingdomsList) {
                            String kingdomName = kingdom.getKingdomName();
                            toReturn.add(kingdomName);
                        }
                    }
                }
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
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.setwarp"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "set-warps")) {
                            if (a[0].equalsIgnoreCase("setwarp")) {
                                toReturn.add("<nomewarp>");
                            }
                        }
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.warp"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "use-warps")) {
                            if (a[0].equalsIgnoreCase("warp")) {
                                toReturn.add("<nomewarp>");
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
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.change-role"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "manage-players")) {
                            if (a[0].equalsIgnoreCase("changerole")) {
                                int kingdomId = db.getPlayers().getKingdomId(player);
                                for (Player online : db.getKingdoms().kingdomPlayersList(kingdomId)) {
                                    toReturn.add(online.getName());
                                }
                            }
                        }
                    }
                }
                return toReturn;
            } else if (a.length == 3) {
                List<String> toReturn = new ArrayList<>();
                if (s.hasPermission(config.getSettings().getString("permissions.commands.kingdoms.change-role"))) {
                    if (s instanceof Player) {
                        Player player = (Player) s;
                        if (mod.hasPermission(player, "manage-players")) {
                            if (a[0].equalsIgnoreCase("changerole")) {
                                for (String rolename : config.getSettings().getConfigurationSection("kingdoms.roles").getKeys(false)) {
                                    toReturn.add(rolename);
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
