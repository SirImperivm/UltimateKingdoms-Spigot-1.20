package me.sirimperivm.spigot.util.other;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("all")
public class Errors {

    private Main plugin;
    private static ConfUtil config;

    public Errors(Main plugin) {
        this.plugin = plugin;
        config = plugin.getCM();
    }

    public static boolean noPermCommand(CommandSender s, String node) {
        if (s.hasPermission(node))
            return false;
        s.sendMessage(config.getTranslatedString("messages.errors.no-perm.command"));
        return true;
    }

    public static boolean noPermAction(Player p, String node) {
        if (p.hasPermission(node))
            return false;
        p.sendMessage(config.getTranslatedString("messages.errors.no-perm.action"));
        return true;
    }

    public static boolean noConsole(CommandSender s) {
        if (s instanceof Player)
            return false;
        s.sendMessage(config.getTranslatedString("messages.errors.no-console"));
        return true;
    }
}
