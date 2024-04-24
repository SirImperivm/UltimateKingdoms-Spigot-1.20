package me.sirimperivm.spigot.commands;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.colors.Colors;
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

    private void getUsage(CommandSender s) {
        for (String usage : config.getSettings().getStringList("helps.commands.admin")) {
            s.sendMessage(Colors.translateString(usage));
        }
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (c.getName().equalsIgnoreCase("kga")) {
            if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.main"))) {
                return true;
            } else {
                if (a.length != 1) {
                    getUsage(s);
                } else {
                    if (a[0].equalsIgnoreCase("reload")) {
                        if (errors.noPermCommand(s, config.getSettings().getString("permissions.commands.admin.reload"))) {
                            return true;
                        } else {
                            config.loadAll();
                            s.sendMessage(config.getTranslatedString("messages.admin.plugin.reloaded"));
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
                    } else {
                        getUsage(s);
                    }
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
                if (s.hasPermission(config.getSettings().getString("permissions.commands.admin.bypass"))) {
                    toReturn.add("bypass");
                }
            }
            return toReturn;
        }
        return new ArrayList<>();
    }
}
