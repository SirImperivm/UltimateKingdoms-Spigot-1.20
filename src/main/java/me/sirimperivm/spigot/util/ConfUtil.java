package me.sirimperivm.spigot.util;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.colors.Colors;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class ConfUtil {

    private Main plugin;

    private Logger log;
    private File folder, settingsFile, permissionsFile;
    private FileConfiguration settings, permissions;

    public ConfUtil(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        folder = plugin.getDataFolder();
        settingsFile = new File(folder, "settings.yml");
        settings = new YamlConfiguration();
        permissionsFile = new File(folder, "permissions.yml");
        permissions = new YamlConfiguration();

        if (!folder.exists()) folder.mkdir();

        if (!settingsFile.exists()) create(settings, settingsFile);
        if (!permissionsFile.exists()) create(permissions, permissionsFile);

        loadAll();
    }

    private void create(FileConfiguration c, File f) {
        try {
            Files.copy(plugin.getResource(f.getName()), f.toPath(), new CopyOption[0]);
            load(c, f);
        } catch (IOException e) {
            log.fail("[UltimateKingdoms] Impossibile creare il file " + f.getName() + "!");
            e.printStackTrace();
        }
    }

    public void save(FileConfiguration c, File f) {
        try {
            c.save(f);
        } catch (IOException e) {
            log.fail("[UltimateKingdoms] Impossibile salvare il file " + f.getName() + "!");
            e.printStackTrace();
        }
    }

    public void load(FileConfiguration c, File f) {
        try {
            c.load(f);
        } catch (IOException | InvalidConfigurationException e) {
            log.fail("[UltimateKingdoms] Impossibile caricare il file " + f.getName() + "!");
            e.printStackTrace();
        }
    }

    public void saveAll() {
        save(settings, settingsFile);
        save(permissions, permissionsFile);
    }

    public void loadAll() {
        load(settings, settingsFile);
        load(permissions, permissionsFile);
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public File getPermissionsFile() {
        return permissionsFile;
    }

    public FileConfiguration getSettings() {
        return settings;
    }

    public FileConfiguration getPermissions() {
        return permissions;
    }

    public String getTranslatedString(String target) {
        return Colors.translateString(this.getSettings().getString(target));
    }

    public List<String> translateList(String target) {
        List<String> notColored = this.getSettings().getStringList(target);
        List<String> colored = new ArrayList<>();

        for (String s : notColored) {
            colored.add(Colors.translateString(s));
        }

        return colored;
    }
}
