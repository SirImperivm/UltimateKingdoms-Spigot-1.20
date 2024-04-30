package me.sirimperivm.spigot.commands;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Errors;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class KingdomsAdmin implements CommandExecutor, TabCompleter {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private Errors errors;
    private DBUtil db;
    private ModUtil mod;

    public KingdomsAdmin(Main plugin) {
        this.plugin = plugin;

        log = plugin.getLog();
        config = plugin.getCM();
        errors = plugin.getErrors();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    private void getUsage(CommandSender s, int page) {
        mod.createHelp(s, "kingdoms-admin-command", page);
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (c.getName().equalsIgnoreCase("kga")) {
            if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.main"))) {
                return true;
            } else {
                if (a.length == 0) {
                    getUsage(s, 1);
                }
                else if (a.length == 1) {
                    if (a[0].equalsIgnoreCase("reload")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.reload"))) {
                            return true;
                        } else {
                            config.loadAll();
                            s.sendMessage(config.getTranslatedString("messages.admin.plugin.reloaded"));
                        }
                    } else if (a[0].equalsIgnoreCase("spy-chat")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.spy-chat"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                if (!mod.getKingdomsChatSpiesList().contains(p)) {
                                    mod.getKingdomsChatSpiesList().add(p);
                                    p.sendMessage(config.getTranslatedString("messages.admin.spy-chat.enabled"));
                                } else {
                                    mod.getKingdomsChatSpiesList().remove(p);
                                    p.sendMessage(config.getTranslatedString("messages.admin.spy-chat.disabled"));
                                }
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("bypass")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.bypass"))) {
                            return true;
                        } else {
                            if (errors.noConsole(s)) {
                                return true;
                            } else {
                                Player p = (Player) s;
                                if (!mod.getBypassPlayerList().contains(p)) {
                                    mod.getBypassPlayerList().add(p);
                                    p.sendMessage(config.getTranslatedString("messages.admin.bypass.enabled"));
                                } else {
                                    mod.getBypassPlayerList().remove(p);
                                    p.sendMessage(config.getTranslatedString("messages.admin.bypass.disabled"));
                                }
                            }
                        }
                    } else if (a[0].equalsIgnoreCase("delete-confirm")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.delete"))) {
                            return true;
                        } else {
                            mod.adminDisbandKingdom(s);
                        }
                    } else {
                        getUsage(s, 1);
                    }
                } else if (a.length == 2) {
                    if (a[0].equalsIgnoreCase("help")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.main"))) {
                            return true;
                        } else {
                            int page = Integer.parseInt(a[1]);
                            getUsage(s, page);
                        }
                    } else if (a[0].equalsIgnoreCase("delete")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.delete"))) {
                            return true;
                        } else {
                            String kingdomName = a[1];
                            mod.adminPreDisbandKingdom(s, kingdomName);
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
        if (a.length == 1) {
            List<String> toReturn = new ArrayList<>();
            if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.main"))) {
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.reload"))) {
                    toReturn.add("reload");
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.spy-chat"))) {
                    toReturn.add("spy-chat");
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.bypass"))) {
                    toReturn.add("bypass");
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.delete"))) {
                    if (!mod.getAdminDisbandKingdomCooldown().containsKey(s)) {
                        toReturn.add("delete");
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.delete"))) {
                    if (mod.getAdminDisbandKingdomCooldown().containsKey(s)) {
                        toReturn.add("delete-confirm");
                    }
                }
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.main"))) {
                    toReturn.add("help");
                }
            }
            return toReturn;
        } else if (a.length == 2) {
            List<String> toReturn = new ArrayList<>();
            if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.main"))) {
                if (a[0].equalsIgnoreCase("help")) {
                    int commandsPerPage = config.getSettings().getInt("help-creator.default.max-lines-per-command");
                    int totalCommand = mod.getTotalLines("kingdoms-admin-command");
                    int totalPages = (int) Math.floor((double) totalCommand/commandsPerPage);
                    for (int i = 1; i <= totalPages; i++) {
                        toReturn.add(String.valueOf(i));
                    }
                }
            }
            if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.delete"))) {
                if (a[0].equalsIgnoreCase("delete")) {
                    for (String kingdomName : db.getKingdoms().kingdomList()) {
                        toReturn.add(kingdomName);
                    }
                }
            }
            return toReturn;
        }
        return new ArrayList<>();
    }
}
