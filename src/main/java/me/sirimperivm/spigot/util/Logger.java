package me.sirimperivm.spigot.util;

import me.sirimperivm.spigot.Main;

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
