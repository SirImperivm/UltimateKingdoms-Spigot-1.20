package me.sirimperivm.spigot.events;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Chunk;
import me.sirimperivm.spigot.entities.Gui;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.colors.Colors;
import me.sirimperivm.spigot.util.other.Errors;
import me.sirimperivm.spigot.util.other.Logger;
import me.sirimperivm.spigot.util.other.Strings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all")
public class Event implements Listener {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private Errors errors;
    private DBUtil db;
    private ModUtil mod;

    public Event(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        config = plugin.getCM();
        errors = plugin.getErrors();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        String title = e.getView().getTitle();
        Player player = (Player) e.getView().getPlayer();
        Inventory inv = e.getInventory();

        Gui kgdepo = new Gui(plugin, "kingdom-deposit");

        if (title.equals(kgdepo.getTitle())) {
            HashMap<Material, Double> depo_conv = new HashMap<>();
            for (String key : config.getSettings().getConfigurationSection("deposit-conversions").getKeys(false)) {
                String path = "deposit-conversions." + key;

                Material material = Material.getMaterial(config.getSettings().getString(path + ".type"));
                double value = config.getSettings().getDouble(path + ".value");

                depo_conv.put(material, value);
            }

            Kingdom playerKingdom = mod.getPlayerKingdom(player);
            String kingdomName = playerKingdom.getKingdomName();
            double kingdomGoldAmount = db.getKingdoms().getGoldAmount(kingdomName);
            double valueToGive = 0;
            int kingdomId = db.getKingdoms().getKingdomId(kingdomName);

            List<ItemStack> returnedItems = new ArrayList<>();
            for (ItemStack item : inv.getContents()) {
                if (item != null) {
                    Material material = item.getType();
                    if (depo_conv.containsKey(material)) {
                        int quantity = item.getAmount();
                        double itemValue = depo_conv.get(material);

                        valueToGive += ((double) quantity * itemValue);
                    } else {
                        returnedItems.add(item);
                    }
                }
            }

            for (ItemStack toReturn : returnedItems) {
                if (toReturn != null) {
                    player.getLocation().getWorld().dropItem(player.getLocation(), toReturn);
                }
            }
            double updatedValue = kingdomGoldAmount+valueToGive;
            db.getKingdoms().updateKingdomGold(kingdomId, updatedValue);

            String formattedValue = Strings.formatNumber(valueToGive, config.getSettings().getInt("other.strings.number-formatter.format-size"), config.getSettings().getStringList("other.strings.number-formatter.associations"));

            player.sendMessage(config.getTranslatedString("messages.kingdoms.deposit.success.deposited")
                    .replace("{0}", formattedValue));
            List<Player> kingdomPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);

            kingdomPlayers.forEach(online -> {
                online.sendMessage(config.getTranslatedString("messages.kingdoms.deposit.info.deposited-broadcast")
                        .replace("{0}", formattedValue)
                        .replace("{1}", player.getName()));
            });
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location playerLocation = player.getLocation();
        Location blockLocation = block.getLocation();
        Chunk actualChunk = new Chunk(plugin, player, blockLocation);

        if (mod.isInClaimedChunk(actualChunk)) {
            int actualKingdoomId = actualChunk.getKingdomId();
            if (!db.getPlayers().existsPlayerData(player) || (db.getPlayers().existsPlayerData(player) && db.getPlayers().getKingdomId(player) != actualKingdoomId)) {
                e.setCancelled(true);
                player.sendMessage(config.getTranslatedString("messages.kingdoms.building.error.isnt-your-kingdom"));
                return;
            }

            if (db.getPlayers().existsPlayerData(player)) {
                if (db.getPlayers().getKingdomId(player) == actualKingdoomId) {
                    if (!mod.hasPermission(player, "break-blocks")) {
                        e.setCancelled(true);
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.building.error.cant-break-blocks"));
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location playerLocation = player.getLocation();
        Location blockLocation = block.getLocation();
        Chunk actualChunk = new Chunk(plugin, player, blockLocation);

        if (mod.isInClaimedChunk(actualChunk)) {
            int actualKingdoomId = actualChunk.getKingdomId();
            if (!db.getPlayers().existsPlayerData(player) || (db.getPlayers().existsPlayerData(player) && db.getPlayers().getKingdomId(player) != actualKingdoomId)) {
                e.setCancelled(true);
                player.sendMessage(config.getTranslatedString("messages.kingdoms.building.error.isnt-your-kingdom"));
                return;
            }

            if (!mod.hasPermission(player, "place-blocks")) {
                e.setCancelled(true);
                player.sendMessage(config.getTranslatedString("messages.kingdoms.building.error.cant-place-blocks"));
                return;
            }
        }
    }

    @EventHandler
    public void onEndermanGrief(EntityChangeBlockEvent e) {
        Entity entity = e.getEntity();
        Block block = e.getBlock();

        Location blockLocation = block.getLocation();
        Chunk actualChunk = new Chunk(plugin, blockLocation);
        if (db.getChunks().existChunk(actualChunk)) {
            if (config.getSettings().getBoolean("kingdoms.default-settings.enderman-grief")) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        List<Block> blocksList = e.blockList();

        List<EntityType> mobList = Arrays.asList(
                EntityType.CREEPER,
                EntityType.GHAST,
                EntityType.ENDER_DRAGON,
                EntityType.WITHER
        );

        List<EntityType> tntList = Arrays.asList(
                EntityType.PRIMED_TNT,
                EntityType.MINECART_TNT
        );

        if (mobList.contains(entity.getType())) {
            for (Block block : blocksList) {
                Location blockLocation = block.getLocation();
                Chunk blockChunk = new Chunk(plugin, blockLocation);

                if (db.getChunks().existChunk(blockChunk)) {
                    if (config.getSettings().getBoolean("kingdoms.default-settings.mob-explosions")) {
                        e.blockList().remove(block);
                    }
                }
            }
        } else if (tntList.contains(entity.getType())) {
            for (Block block : blocksList) {
                Location blockLocation = block.getLocation();
                Chunk blockChunk = new Chunk(plugin, blockLocation);

                if (db.getChunks().existChunk(blockChunk)) {
                    if (config.getSettings().getBoolean("kingdoms.default-settings.tnt-explosions")) {
                        e.blockList().remove(block);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity victimEntity = e.getEntity();
        Entity killerEntity = e.getDamager();

        if (victimEntity instanceof Player && killerEntity instanceof Player) {
            Player victim = (Player) victimEntity;
            Player killer = (Player) killerEntity;

            Location victimLocation = victim.getLocation();
            Chunk victimChunk = new Chunk(plugin, victim, victimLocation);

            if (db.getChunks().existChunk(victimChunk)) {
                if (db.getPlayers().existsPlayerData(killer) && db.getPlayers().existsPlayerData(victim)) {
                    int kingdomId = db.getChunks().kingdomId(victimChunk);
                    int killerKingdomId = db.getPlayers().getKingdomId(killer);
                    int victimKingdomId = db.getPlayers().getKingdomId(victim);

                    if ((victimKingdomId == killerKingdomId) && (victimKingdomId == killerKingdomId)) {
                        if (!config.getSettings().getBoolean("kingdoms.default-settings.friendly-fire")) {
                            e.setCancelled(true);
                            killer.sendMessage(config.getTranslatedString("messages.kingdoms.pvp.error.friendly-fire-not-enabled"));
                            return;
                        }
                    }

                    if ((victimKingdomId == killerKingdomId) && (victimKingdomId != killerKingdomId)) {
                        if (!config.getSettings().getBoolean("kingdoms.default-settings.enemy-pvp")) {
                            e.setCancelled(true);
                            killer.sendMessage(config.getTranslatedString("messages.kingdoms.pvp.error.enemy-pvp-not-enabled"));
                            return;
                        }
                    }
                }

                if (db.getPlayers().existsPlayerData(victim)) {
                    int kingdomId = db.getChunks().kingdomId(victimChunk);
                    int victimKingdomId = db.getPlayers().getKingdomId(victim);

                    if (victimKingdomId == kingdomId) {
                        if (!config.getSettings().getBoolean("kingdoms.default-settings.enemy-pvp")) {
                            killer.sendMessage(config.getTranslatedString("messages.kingdoms.pvp.error.enemy-pvp-not-enabled"));
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();

            Location playerLocation = player.getLocation();
            Location blockLocation = block.getLocation();

            Material clickedMaterial = block.getType();

            List<Material> containers = Arrays.asList(
                    Material.CHEST,
                    Material.CHEST_MINECART,
                    Material.TRAPPED_CHEST,
                    Material.HOPPER,
                    Material.HOPPER_MINECART,
                    Material.FURNACE,
                    Material.FURNACE_MINECART,
                    Material.SMOKER,
                    Material.BLAST_FURNACE,
                    Material.DROPPER,
                    Material.DISPENSER,
                    Material.SHULKER_BOX
            );

            if (containers.contains(clickedMaterial)) {
                Chunk actualChunk = new Chunk(plugin, player, blockLocation);

                if (mod.isInClaimedChunk(actualChunk)) {
                    int actualKingdomId = actualChunk.getKingdomId();
                    if (!db.getPlayers().existsPlayerData(player) || (db.getPlayers().existsPlayerData(player) && actualKingdomId != db.getPlayers().getKingdomId(player))) {
                        e.setCancelled(true);
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.use-of.containers.error.isnt-your-kingdom"));
                        return;
                    }

                    if (!mod.hasPermission(player, "use-containers")) {
                        e.setCancelled(true);
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.use-of.containers.error.cant-use"));
                        return;
                    }
                }
            }

            if (clickedMaterial.toString().endsWith("_DOOR") || clickedMaterial.toString().endsWith("_GATE") || clickedMaterial.toString().endsWith("_TRAPDOOR")) {
                Chunk actualChunk = new Chunk(plugin, player, blockLocation);

                if (mod.isInClaimedChunk(actualChunk)) {
                    int actualKingdomId = actualChunk.getKingdomId();
                    if (!db.getPlayers().existsPlayerData(player) || (db.getPlayers().existsPlayerData(player) && actualKingdomId != db.getPlayers().getKingdomId(player))) {
                        e.setCancelled(true);
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.use-of.doors.error.isnt-your-kingdom"));
                        return;
                    }

                    if (!mod.hasPermission(player, "open-doors")) {
                        e.setCancelled(true);
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.use-of.doors.error.cant-use"));
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String message = e.getMessage();

        if (message.startsWith("->testString:")) {
            if (errors.noPermAction(p, config.getSettings().getString("permissions.events.test-string"))) {
                return;
            } else {
                e.setCancelled(true);
                String testString = message.split("->testString:")[1];
                p.sendMessage(Colors.translateString(testString));
            }
        }
    }
}
