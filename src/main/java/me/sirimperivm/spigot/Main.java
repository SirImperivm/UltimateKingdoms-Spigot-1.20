package me.sirimperivm.spigot;

import com.sk89q.worldguard.protection.flags.StateFlag;
import me.sirimperivm.spigot.commands.Kingdoms;
import me.sirimperivm.spigot.commands.KingdomsAdmin;
import me.sirimperivm.spigot.events.Event;
import me.sirimperivm.spigot.extras.PapiExpansion;
import me.sirimperivm.spigot.extras.WorldGuardExpansion;
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

    private PapiExpansion papi;
    private WorldGuardExpansion wge;

    private boolean foudWg = false;

    private StateFlag kingdom_access;

    void setupDependencies() {
        if (getPluginManager().getPlugin("PlaceholderAPI") != null) {
            log.success("&d!! SoftDependency trovata: PlaceholderAPI !!");
            papi = new PapiExpansion(plugin);
            papi.register();
            log.success("&bPlaceholderAPI collegato correttamente!");
        }
    }

    @Override
    public void onLoad() {
        plugin = this;
        log = new Logger(plugin);
        if (getPluginManager().getPlugin("WorldGuard") != null) {
            wge = new WorldGuardExpansion(plugin);
            foudWg = true;
            kingdom_access = wge.getKingdom_access();
        }
    }

    @Override
    public void onEnable() {
        serverVersion = getBukkitVersion();
        config = new ConfUtil(plugin);
        errors = new Errors(plugin);
        db = new DBUtil(plugin);
        db.setup();
        mod = new ModUtil(plugin);
        mod.setupSettings();
        mod.setupRoles();
        setupDependencies();

        getCommand("kg").setExecutor(new Kingdoms(plugin));
        getCommand("kg").setTabCompleter(new Kingdoms(plugin));
        getCommand("kga").setExecutor(new KingdomsAdmin(plugin));
        getCommand("kga").setTabCompleter(new KingdomsAdmin(plugin));

        getPluginManager().registerEvents(new Event(plugin), plugin);

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

    public WorldGuardExpansion getWge() {
        return wge;
    }

    public StateFlag getKingdom_access() {
        return kingdom_access;
    }

    public boolean isFoudWg() {
        return foudWg;
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
