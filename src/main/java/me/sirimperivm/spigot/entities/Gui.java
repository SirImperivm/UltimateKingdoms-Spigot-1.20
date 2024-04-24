package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.colors.Colors;
import me.sirimperivm.spigot.util.other.Logger;
import me.sirimperivm.spigot.util.other.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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
    private HashMap<Integer, ItemStack> itemsList;
    private String target;
    private String title;
    private int rows;
    private int size;

    private String configDirectory;

    public Gui(Main plugin, String guiName, HashMap<Integer, ItemStack> itemsList) {
        this.plugin = plugin;
        this.guiName = guiName;
        this.itemsList = itemsList;

        configDirectory = "guis." + guiName;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();

        title = config.getTranslatedString(configDirectory + ".title");
        rows = config.getSettings().getInt(configDirectory + ".rows");
        rows = rows < 1 ? 1 : rows;
        rows = rows > 6 ? 6 : rows;
        size = rows * 9;
    }

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
        rows = rows < 1 ? 1 : rows;
        rows = rows > 6 ? 6 : rows;
        size = rows * 9;
    }

    public Gui(Main plugin, String guiName, String target) {
        this.plugin = plugin;
        this.guiName = guiName;
        this.target = target;

        configDirectory = "guis." + guiName;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();

        title = config.getTranslatedString(configDirectory + ".title".replace("{0}", Strings.capitalize(target)));
        rows = config.getSettings().getInt(configDirectory + ".rows");
        rows = rows < 1 ? 1 : rows;
        rows = rows > 6 ? 6 : rows;
        size = rows * 9;
    }

    public void sendGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, title);

        boolean fillerEnabled = config.getSettings().getBoolean(configDirectory + ".filler.enabled");
        boolean glowing = config.getSettings().getBoolean(configDirectory + ".filler.glowing");

        ItemStack filler = new ItemStack(Material.getMaterial(config.getSettings().getString(configDirectory + ".filler.material")));
        ItemMeta fillerMeta = filler.getItemMeta();
        String displayName = config.getSettings().getString(configDirectory + ".filler.name");
        if (!displayName.equalsIgnoreCase("null") && !displayName.equals("") && !displayName.equals(null)) {
            fillerMeta.setDisplayName(Colors.translateString(displayName));
        }
        if (glowing) {
            fillerMeta.addEnchant(Enchantment.LURE, 0, false);
            fillerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        List<String> lore = new ArrayList<>();
        for (String line : config.getSettings().getStringList(configDirectory + ".filler.lore")) {
            lore.add(Colors.translateString(line));
        }
        fillerMeta.setLore(lore);
        fillerMeta.setCustomModelData(config.getSettings().getInt(configDirectory + ".filler.model"));
        filler.setItemMeta(fillerMeta);

        for (Integer slot : itemsList.keySet()) {
            ItemStack item = itemsList.get(slot);
            inv.setItem(slot, item);
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

    public void sendGui(Player player, HashMap<Integer, ItemStack> itemsList) {
        Inventory inv = Bukkit.createInventory(null, size, title);

        boolean fillerEnabled = config.getSettings().getBoolean(configDirectory + ".filler.enabled");
        boolean glowing = config.getSettings().getBoolean(configDirectory + ".filler.glowing");

        ItemStack filler = new ItemStack(Material.getMaterial(config.getSettings().getString(configDirectory + ".filler.material")));
        ItemMeta fillerMeta = filler.getItemMeta();
        String displayName = config.getSettings().getString(configDirectory + ".filler.name");
        if (!displayName.equalsIgnoreCase("null") && !displayName.equals("") && !displayName.equals(null)) {
            fillerMeta.setDisplayName(Colors.translateString(displayName));
        }
        if (glowing) {
            fillerMeta.addEnchant(Enchantment.LURE, 0, false);
            fillerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        List<String> lore = new ArrayList<>();
        for (String line : config.getSettings().getStringList(configDirectory + ".filler.lore")) {
            lore.add(Colors.translateString(line));
        }
        fillerMeta.setLore(lore);
        fillerMeta.setCustomModelData(config.getSettings().getInt(configDirectory + ".filler.model"));
        filler.setItemMeta(fillerMeta);

        for (Integer slot : itemsList.keySet()) {
            ItemStack item = itemsList.get(slot);
            inv.setItem(slot, item);
        }

        if (fillerEnabled) {
            for (int i=0; i<size; i++) {
                if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRows(int rows) {
        this.rows = rows;
        this.size = rows*9;
    }
}
