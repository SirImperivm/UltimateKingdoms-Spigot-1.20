package me.sirimperivm.spigot;

import me.sirimperivm.spigot.commands.Kingdoms;
import me.sirimperivm.spigot.entities.Chunk;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Errors;
import me.sirimperivm.spigot.util.other.Logger;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getPluginManager;

@SuppressWarnings("all")
public final class Main extends JavaPlugin {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private Errors errors;
    private DBUtil db;
    private ModUtil mod;

    private int serverVersion;

    @Override
    public void onEnable() {
        plugin = this;
        serverVersion = getBukkitVersion();
        log = new Logger(plugin);
        config = new ConfUtil(plugin);
        errors = new Errors(plugin);
        db = new DBUtil(plugin);
        db.setup();
        mod = new ModUtil(plugin);
        mod.setupKingdomHash();
        mod.setupRoles();
        Chunk chunks = new Chunk(plugin);

        getCommand("kg").setExecutor(new Kingdoms(plugin));
        getCommand("kg").setTabCompleter(new Kingdoms(plugin));

        log.success("[UltimateKingdoms] Plugin attivato correttamente!");
    }

    @Override
    public void onDisable() {
        db.closeConnection();

        log.success("[UltimateKingdoms] Plugin disattivato correttamente!");
    }

    public void disablePlugin() {
        getPluginManager().disablePlugin(this);
    }

    public Main getPlugin() {
        return plugin;
    }

    public Logger getLog() {
        return log;
    }

    public ConfUtil getCM() {
        return config;
    }

    public Errors getErrors() {
        return errors;
    }

    public DBUtil getDB() {
        return db;
    }

    public ModUtil getMod() {
        return mod;
    }

    private int getBukkitVersion() {
        String version = Bukkit.getVersion();
        Validate.notEmpty(version, "Impossibile ottenere la versione del server minecraft.");

        int index = version.lastIndexOf("MC:");
        if (index != -1) {
            version = version.substring(index+4, version.length() -1);
        } else if (version.endsWith("SNAPSHOT")) {
            index = version.indexOf('-');
            version = version.substring(0, index);
        }

        int lastDot = version.lastIndexOf('.');
        if (version.indexOf('.') != lastDot) version = version.substring(0, lastDot);

        return Integer.parseInt(version.substring(2));
    }

    public int getServerVersion() {
        return serverVersion;
    }
}
