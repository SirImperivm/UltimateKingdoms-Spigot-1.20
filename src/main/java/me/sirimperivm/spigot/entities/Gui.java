package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all")
public class Gui {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;
    private ModUtil mod;

    private String guiName;
    private String title;
    private int rows;
    private int size;

    private String configDirectory;

    public Gui(Main plugin, String guiName) {
        this.plugin = plugin;
        this.guiName = guiName;

        configDirectory = "guis." + guiName;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();

        title = config.getTranslatedString(configDirectory + ".title");
        rows = config.getSettings().getInt(configDirectory + ".rows");
        rows = rows > 6 ? 6 : rows;
        size = rows *9;

        while (size%9!=0) {
            size--;
        }
    }

    public void sendGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, title);

        boolean fillerEnabled = config.getSettings().getBoolean(configDirectory + ".filler.enabled");
        HashMap<Integer, ItemStack> itemsList = new HashMap<>();

        ItemStack filler = new ItemStack(Material.getMaterial(config.getSettings().getString(configDirectory + ".filler.material")));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(config.getTranslatedString(configDirectory + ".filler.name"));
        fillerMeta.setLore(config.translateList(configDirectory + ".filler.lore"));
        fillerMeta.setCustomModelData(config.getSettings().getInt(configDirectory + ".filler.model"));
        filler.setItemMeta(fillerMeta);

        for (String itemKey : config.getSettings().getConfigurationSection(configDirectory + ".items").getKeys(false)) {
            String path = configDirectory + ".items." + itemKey;

            ItemStack material = new ItemStack(Material.getMaterial(config.getSettings().getString(path + ".material")));
            ItemMeta meta = material.getItemMeta();
            meta.setDisplayName(config.getTranslatedString(path + ".name"));
            meta.setLore(config.translateList(path + ".lore"));
            meta.setCustomModelData(config.getSettings().getInt(path + ".model"));
            material.setItemMeta(meta);
            List<Integer> slots = config.getSettings().getIntegerList(path + ".slots");

            for (Integer slot : slots) {
                itemsList.put(slot, material);
            }
        }

        for (Integer slot : itemsList.keySet()) {
            inv.setItem(slot, itemsList.get(slot));
        }

        if (fillerEnabled) {
            for (int i=0; i<size; i++) {
                if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR)) {
                    inv.setItem(i, filler);
                }
            }
        }

        player.openInventory(inv);
    }

    public String getGuiName() {
        return guiName;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return size;
    }
}
