package me.sirimperivm.spigot.commands;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.Colors;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.Errors;
import me.sirimperivm.spigot.util.Logger;
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

    public Kingdoms(Main plugin) {
        this.plugin = plugin;

        log = plugin.getLog();
        config = plugin.getCM();
        errors = plugin.getErrors();
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
                    getUsage(s);
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
                                    Kingdom kingdom = new Kingdom(plugin, kingdomName);
                                    kingdom.createKingdom(p);
                                } else {
                                    p.sendMessage(config.getTranslatedString("messages.kingdoms.creation.error.name-required"));
                                }
                            }
                        }
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
                toReturn.add("create");
                return toReturn;
            } else if (a.length == 2) {
                List<String> toReturn = new ArrayList<>();
                toReturn.add("<nome>");
                return toReturn;
            }
        }
        return new ArrayList<>();
    }
}
