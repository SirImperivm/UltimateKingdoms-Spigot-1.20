package me.sirimperivm.spigot.util.other;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.colors.Colors;

@SuppressWarnings("all")
public class Logger {
    
    private Main plugin;
    public Logger(Main plugin) {
        this.plugin = plugin;
    }

    public void success(String message) {
        plugin.getServer().getConsoleSender().sendMessage(Colors.translateString("&2" + message));
    }

    public void warn(String message) {
        plugin.getServer().getConsoleSender().sendMessage(Colors.translateString("&6" + message));
    }

    public void fail(String message) {
        plugin.getServer().getConsoleSender().sendMessage(Colors.translateString("&c" + message));
    }
}
