package me.sirimperivm.spigot.util;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Chunk;
import me.sirimperivm.spigot.entities.Gui;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.colors.Colors;
import me.sirimperivm.spigot.util.other.Logger;
import me.sirimperivm.spigot.util.other.Strings;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

@SuppressWarnings("all")
public class ModUtil {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;

    private HashMap<String, Kingdom> kingdomHash;
    private HashMap<Player, Kingdom> kingdomInvites;
    private HashMap<Player, Integer> rolesPermissionsEditing;
    private List<Player> chunksBordersPlayerList;
    private List<Player> bypassPlayerList;
    private List<Player> releaseAllCooldown;

    public ModUtil(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();

        chunksBordersPlayerList = new ArrayList<>();
        refreshChunksBordersForPlayers();
        rolesPermissionsEditing = new HashMap<>();
        bypassPlayerList = new ArrayList<>();
        releaseAllCooldown = new ArrayList<>();
    }

    public void setupSettings() {
        kingdomHash = new HashMap<>();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskTimer(plugin, () -> {

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (db.getPlayers().existsPlayerData(online)) {
                    String onlineName = online.getName();
                    int kingdomId = db.getPlayers().getKingdomId(online);
                    String kingdomName = db.getKingdoms().getKingdomName(kingdomId);
                    Kingdom onlineKingdom = new Kingdom(plugin, kingdomName);

                    kingdomHash.put(onlineName, onlineKingdom);
                }
            }
        }, 20L, 20L);
    }

    public void setupRoles() {
        for (String key : config.getSettings().getConfigurationSection("kingdoms.roles").getKeys(false)) {
            if (!db.getRoles().existsRoleData(key)) {
                int roleId = config.getSettings().getInt("kingdoms.roles." + key + ".id");
                int weight = config.getSettings().getInt("kingdoms.roles." + key + ".weight");
                db.getRoles().insertRole(roleId, key, weight);
            }
        }
    }

    public void setupPermissions(int kingdomId) {
        for (String role : config.getSettings().getConfigurationSection("kingdoms.roles").getKeys(false)) {
            if (db.getRoles().existsRoleData(role)) {
                int roleId = config.getSettings().getInt("kingdoms.roles." + role + ".id");

                for (String permission : config.getSettings().getConfigurationSection("kingdoms.roles." + role + ".default-permissions").getKeys(false)) {
                    boolean permValue = config.getSettings().getBoolean("kingdoms.roles." + role + ".default-permissions." + permission);

                    if (permValue) {
                        int permId = db.getPermissions().getPermId(permission);
                        db.getPermissionsRoles().insertPerm(kingdomId, roleId, permId);
                    }
                }
            }
        }
    }

    public void refreshChunksBordersForPlayers() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player target : chunksBordersPlayerList) {
                if (Bukkit.getOnlinePlayers().contains(target)) {
                    visualizeChunkBorders(target);
                }
            }
        }, config.getSettings().getLong("settings.chunks-border.spawn.delay"), config.getSettings().getLong("settings.chunks-border.spawn.interval"));
    }

    public void sendKingdomsList(CommandSender target, int page) {
        List<Kingdom> kingdomsList = db.getKingdoms().kingdomsList();
        Map<String, Double> map = new HashMap<>();
        for (Kingdom kingdom : kingdomsList) {
            String kingdomName = kingdom.getKingdomName();
            double gold = kingdom.getGoldAmount();
            map.put(kingdomName, gold);
        }

        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(map.entrySet());
        sortedList.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        List<Kingdom> kingdoms = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedList) {
            Kingdom kingdom = new Kingdom(plugin, entry.getKey());
            kingdoms.add(kingdom);
        }

        int pageSize = 5;
        int pageCount = (int) Math.ceil((double) kingdoms.size()/pageSize);
        if (page < 1 || page > pageCount) {
            target.sendMessage(config.getTranslatedString("messages.kingdoms.general.error.page-not-found"));
        } else {
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.header"));
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.title"));
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.spacer"));
            int startIndex = (page-1) * pageSize;
            int endIndex = Math.min(startIndex+pageSize, kingdoms.size());
            for (int i=startIndex; i<endIndex; i++) {
                Kingdom kingdom = kingdoms.get(i);
                if (kingdom != null) {
                    String kingdomName = kingdom.getKingdomName();
                    int kingdomId = db.getKingdoms().getKingdomId(kingdomName);
                    int members = db.getPlayers().getPlayersCount(kingdomId);
                    double goldAmount = db.getKingdoms().getGoldAmount(kingdomId);
                    String formattedGoldAmount = Strings.formatNumber(goldAmount, config.getSettings().getInt("other.strings.number-formatter.format-size"), config.getSettings().getStringList("other.strings.number-formatter.associations"));

                    target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.line")
                            .replace("{0}", kingdomName)
                            .replace("{1}", String.valueOf(members))
                            .replace("{2}", formattedGoldAmount));
                }
            }
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.spacer"));
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.page")
                    .replace("{0}", String.valueOf(page)));
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.footer"));
        }
    }
    public void sendKingdomInfo(CommandSender target, String kingdomName) {
        int kingdomId = db.getKingdoms().getKingdomId(kingdomName);
        if (kingdomId != 0) {
            String messagePath = "formats-lists.kingdoms-info";

            List<String> playersList = db.getKingdoms().getKingdomPlayers(kingdomId);
            StringBuilder playersText = new StringBuilder();
            for (String playerName : playersList) {
                if (!playersList.get(playersList.size() - 1).equals(playerName)) {
                    Player player = Bukkit.getPlayerExact(playerName);
                    if (player != null) {
                        playersText.append("&a" + playerName + "->S:A054E4/,&r");
                    } else {
                        playersText.append("&7" + playerName + "->S:A054E4/,&r");
                    }
                } else {
                    Player player = Bukkit.getPlayerExact(playerName);
                    if (player != null) {
                        playersText.append("&a" + playerName + "->S:A054E4/.&r");
                    } else {
                        playersText.append("&7" + playerName + "->S:A054E4/.&r");
                    }
                }
            }

            String kingdomLevel = db.getKingdoms().getKingdomLevel(kingdomId);
            String levelTitle = config.getSettings().getString("kingdoms.levels." + kingdomLevel + ".title");
            double goldAmount = db.getKingdoms().getGoldAmount(kingdomId);
            String formattedGoldAmount = Strings.formatNumber(goldAmount, config.getSettings().getInt("other.strings.number-formatter.format-size"), config.getSettings().getStringList("other.strings.number-formatter.associations"));

            target.sendMessage(config.getTranslatedString(messagePath + ".header"));
            target.sendMessage(config.getTranslatedString(messagePath + ".title"));
            target.sendMessage(config.getTranslatedString(messagePath + ".spacer"));
            for (String line : config.getSettings().getStringList(messagePath + ".lines")) {
                target.sendMessage(Colors.translateString(line
                        .replace("{0}", kingdomName)
                        .replace("{1}", Colors.translateString(levelTitle))
                        .replace("{2}", Colors.translateString(formattedGoldAmount))
                        .replace("{3}", Colors.translateString(playersText.toString()))
                ));
            }
            target.sendMessage(config.getTranslatedString(messagePath + ".spacer"));
            target.sendMessage(config.getTranslatedString(messagePath + ".footer"));
        } else {
            target.sendMessage(config.getTranslatedString("messages.kingdoms.general.error.kingdom-not-exists"));
        }
    }

    public void sendKingdomsClaims(Player target) {
        if (db.getPlayers().existsPlayerData(target)) {
            if (hasPermission(target, "show-territories")) {
                int kingdomId = db.getPlayers().getKingdomId(target);
                HashMap<Integer, List<String>> achievedChunksData = db.getChunks().achieveChunksData(kingdomId);

                target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-claims.header"));
                target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-claims.title"));
                target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-claims.spacer"));
                for (int chunkId : achievedChunksData.keySet()) {
                    List<String> chunksData = achievedChunksData.get(chunkId);
                    String worldName = chunksData.get(0);
                    String X = chunksData.get(1);
                    String Y = chunksData.get(3);
                    String Z = chunksData.get(5);
                    target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-claims.line")
                            .replace("{0}", worldName)
                            .replace("{1}", X)
                            .replace("{2}", Y)
                            .replace("{3}", Z)
                    );
                }
                target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-claims.spacer"));
                target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-claims.footer"));
            } else {
                target.sendMessage(config.getTranslatedString("messages.kingdoms.claims-list.error.hasnt-permission"));
            }
        } else {
            target.sendMessage(config.getTranslatedString("messages.kingdoms.claims-list.error.not-in-a-kingdom"));
        }
    }

    public void invitePlayerKingdom(Player sender, Player target, Kingdom kingdom) {
        if (!db.getPlayers().existsPlayerData(target)) {
            String kingdomName = kingdom.getKingdomName();
            String senderName = sender.getName();

            kingdomInvites = new HashMap<>();
            kingdomInvites.put(target, kingdom);

            sender.sendMessage(config.getTranslatedString("messages.kingdoms.invites.success.started"));

            target.sendMessage(config.getTranslatedString("messages.kingdoms.invites.info.started")
                    .replace("{0}", kingdomName)
                    .replace("{1}", senderName));

            BukkitScheduler scheduler = Bukkit.getScheduler();

            scheduler.runTaskLater(plugin, () -> {
                if (kingdomInvites.containsKey(target)) {
                    target.sendMessage(config.getTranslatedString("messages.kingdoms.invites.info.expired"));
                    kingdomInvites.remove(target);
                }
            }, 30*20L);
        } else {
            sender.sendMessage(config.getTranslatedString("messages.kingdoms.invites.error.already-have-one"));
        }
    }

    public void unclaimAllChunks(Player player) {
        if (db.getPlayers().existsPlayerData(player)) {
            if (hasPermission(player, "release-all-territories")) {
                if (!releaseAllCooldown.contains(player)) {
                    player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.info.warning"));
                    releaseAllCooldown.add(player);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (releaseAllCooldown.contains(player)) {
                                releaseAllCooldown.remove(player);
                                player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.info.expired"));
                            }
                        }
                    }.runTaskLater(plugin, 20L * 10L);
                } else {
                    player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.error.already-set"));
                }
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.error.not-in-a-kingdom"));
        }
    }

    public void unclaimAllConfirm(Player player) {
        if (releaseAllCooldown.contains(player)) {
            int kingdomId = db.getPlayers().getKingdomId(player);
            db.getChunks().truncateTable(kingdomId);
            player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.success.removed"));
            releaseAllCooldown.remove(player);
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.unclaim-all.error.not-set"));
        }
    }

    public void insertPlayerKingdom(Player player) {
        if (kingdomInvites.containsKey(player)) {
            Kingdom kingdom = kingdomInvites.get(player);

            String kingdomName = kingdom.getKingdomName();
            int kingdomId = db.getKingdoms().getKingdomId(kingdomName);

            int kingdomRole = config.getSettings().getInt("kingdoms.roles.recruit.id");

            List<Player> onlineKGPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);

            db.getPlayers().insertPlayer(player, kingdomId, kingdomRole);

            player.sendMessage(config.getTranslatedString("messages.kingdoms.joined.success")
                    .replace("{0}", kingdomName));

            onlineKGPlayers.forEach(online -> {
                online.sendMessage(config.getTranslatedString("messages.kingdoms.joined.info")
                        .replace("{0}", player.getName()));
            });
            kingdomInvites.remove(player);
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.invites.error.never-invited"));
        }
    }

    public void expelPlayerKingdom(Player officer, Player target) {
        if (db.getPlayers().existsPlayerData(officer)) {
            if (hasPermission(officer, "expel-players")) {
                if (db.getPlayers().existsPlayerData(target)) {
                    Kingdom targetKingdom = getPlayerKingdom(target);
                    Kingdom officerKingdom = getPlayerKingdom(officer);

                    String targetKingdomName = targetKingdom.getKingdomName();
                    String officerKingdomName = officerKingdom.getKingdomName();

                    int officerRole = db.getPlayers().getKingdomRole(officer);
                    int targetRole = db.getPlayers().getKingdomRole(target);

                    int officerWeight = db.getRoles().getRoleWeight(officerRole);
                    int targetWeight = db.getRoles().getRoleWeight(targetRole);

                    if (targetKingdomName.equals(officerKingdomName)) {
                        if (officerWeight > targetWeight) {
                            int kingdomId = db.getKingdoms().getKingdomId(targetKingdomName);

                            db.getPlayers().dropPlayer(target);
                            officer.sendMessage(config.getTranslatedString("messages.kingdoms.expel.success.kicked")
                                    .replace("{0}", target.getName()));

                            List<Player> kingdomPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);
                            kingdomPlayers.forEach(online -> {
                                online.sendMessage(config.getTranslatedString("messages.kingdoms.expel.info.kicked-broadcast")
                                        .replace("{0}", target.getName()));
                            });

                            target.sendMessage(config.getTranslatedString("messages.kingdoms.expel.info.kicked-you")
                                    .replace("{0}", officerKingdomName));
                        } else {
                            officer.sendMessage(config.getTranslatedString("messages.kingdoms.expel.error.target-is-greather-than-you"));
                        }
                    } else {
                        officer.sendMessage(config.getTranslatedString("messages.kingdoms.expel.error.not-in-your-kingdom"));
                    }
                } else {
                    officer.sendMessage(config.getTranslatedString("messages.kingdoms.expel.error.not-in-kingdom"));
                }
            } else {
                officer.sendMessage(config.getTranslatedString("messages.kingdoms.expel.error.hasnt-permission"));
            }
        } else {
            officer.sendMessage(config.getTranslatedString("messages.kingdoms.expel.error.not-in-a-kingdom"));
        }
    }

    public void changeRole(Player officer, Player target, String roleName) {
        if (db.getPlayers().existsPlayerData(officer)) {
            if (hasPermission(officer, "manage-players")) {
                if (db.getPlayers().existsPlayerData(target)) {
                    if (db.getRoles().existsRoleData(roleName)) {
                        Kingdom officerKingdom = getPlayerKingdom(officer);
                        Kingdom targetKingdom = getPlayerKingdom(target);

                        String officerKingdomName = officerKingdom.getKingdomName();
                        String targetKingdomName = targetKingdom.getKingdomName();

                        if (officerKingdomName.equals(targetKingdomName)) {
                            int officerRole = db.getPlayers().getKingdomRole(officer);
                            int targetRole = db.getPlayers().getKingdomRole(target);

                            int officerWeight = db.getRoles().getRoleWeight(officerRole);
                            int targetWeight = db.getRoles().getRoleWeight(targetRole);

                            if (officerWeight > targetWeight) {
                                int targetRoleId = db.getRoles().getRoleId(roleName);
                                int targetRoleWeight = db.getRoles().getRoleWeight(targetRoleId);

                                if (officerWeight > targetRoleWeight) {
                                    int kingdomId = db.getKingdoms().getKingdomId(officerKingdomName);
                                    db.getPlayers().updateRole(target, targetRoleId);

                                    target.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.info.role-updated-you")
                                            .replace("{0}", roleName));
                                    officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.success.role-updated"));

                                    List<Player> kingdomPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);

                                    kingdomPlayers.forEach(player -> {
                                        player.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.info.role-updated-broadcast")
                                                .replace("{0}", target.getName())
                                                .replace("{1}", roleName));
                                    });
                                } else {
                                    officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.target-role-higher-than-your"));
                                }
                            } else {
                                officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.target-player-higher-than-you"));
                            }
                        } else {
                            officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.not-in-your-kingdom"));
                        }
                    } else {
                        officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.role-not-exist"));
                    }
                } else {
                    officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.target-not-in-a-kindom"));
                }
            } else {
                officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.hasnt-permission"));
            }
        } else {
            officer.sendMessage(config.getTranslatedString("messages.kingdoms.change-role.error.not-in-a-kindom"));
        }
    }

    public void disbandPlayerKingdom(Player player) {
        String playerName = player.getName();
        if (kingdomHash.containsKey(playerName)) {
            if (hasPermission(player, "disband")) {
                Kingdom playerKingdom = getPlayerKingdom(player);

                String kingdomName = playerKingdom.getKingdomName();
                int kingdomId = db.getKingdoms().getKingdomId(kingdomName);
                db.getKingdoms().deleteKingdom(kingdomName);
                if (!db.isMysql()) {
                    db.getPlayers().truncateTable(kingdomId);
                    db.getPermissionsRoles().truncateTable(kingdomId);
                    db.getChunks().truncateTable(kingdomId);
                }

                player.sendMessage(config.getTranslatedString("messages.kingdoms.disband.success.disbanded")
                        .replace("{0}", kingdomName));

                for (Player all : Bukkit.getOnlinePlayers()) {
                    all.sendMessage(config.getTranslatedString("messages.kingdoms.disband.success.disbanded-broadcast")
                            .replace("{0}", playerName)
                            .replace("{1}", kingdomName));
                }
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.disband.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.disband.error.not-have"));
        }
    }

    public void claimChunk(Player player) {
        if (db.getPlayers().existsPlayerData(player)) {
            if (hasPermission(player, "expand-territory")) {
                Kingdom playerKingdom = getPlayerKingdom(player);
                String kingdomName = playerKingdom.getKingdomName();

                int kingdomId = db.getKingdoms().getKingdomId(kingdomName);
                String kingdomLevel = db.getKingdoms().getKingdomLevel(kingdomId);
                int maxClaimableChunks = config.getSettings().getInt("kingdoms.levels." + kingdomLevel + ".max-claimable-chunks");
                int claimedChunks = db.getChunks().getClaimedChunks(kingdomId);

                Chunk chunk = new Chunk(plugin, player, player.getLocation());
                if (!db.getChunks().existChunk(chunk)) {
                    if (claimedChunks < maxClaimableChunks) {
                        chunk.obtainChunk();
                    } else {
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.max-claims-reached"));
                    }
                } else {
                    player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.claim-already-exist"));
                }
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.not-in-a-kingdom"));
        }
    }

    public void unclaimChunk(Player player) {
        if (db.getPlayers().existsPlayerData(player)) {
            if (hasPermission(player, "expand-territory")) {
                Chunk chunk = new Chunk(plugin, player, player.getLocation());
                Kingdom playerKingdom = getPlayerKingdom(player);

                if (chunk.getKingdomId() == db.getKingdoms().getKingdomId(playerKingdom.getKingdomName())) {
                    if (db.getChunks().existChunk(chunk)) {
                        chunk.unclaimChunk(chunk);
                    } else {
                        player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.claim-not-found"));
                    }
                } else {
                    player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.isnt-your.kingdom"));
                }
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.error.not-in-a-kingdom"));
        }
    }

    public void sendDepositGui(Player player) {
        if (db.getPlayers().existsPlayerData(player)) {
            if (hasPermission(player, "deposit")) {
                Gui gui = new Gui(plugin, "kingdom-deposit", createItemsList("guis.kingdom-deposit"));
                gui.sendGui(player);
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.deposit.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.deposit.error.not-in-a-kingdom"));
        }
    }

    public void sendMainPermissionsGui(Player player) {
        if (db.getPlayers().existsPlayerData(player)) {
            if (hasPermission(player, "manage-roles")) {
                int kingdomId = db.getPlayers().getKingdomId(player);
                int kingdomRoleId = db.getPlayers().getKingdomRole(player);
                int kingdomRoleWeight = db.getRoles().getRoleWeight(kingdomRoleId);
                List<Integer> rolesList = db.getRoles().getRolesList();
                HashMap<Integer, ItemStack> itemsList = new HashMap<Integer, ItemStack>();

                int rolesCount = rolesList.size()-1;
                int slot=0;
                for (Integer roleId : rolesList) {
                    int roleWeight = db.getRoles().getRoleWeight(roleId);
                    if (kingdomRoleWeight>roleWeight) {
                        String roleName = db.getRoles().getRoleName(roleId).substring(0, 1).toUpperCase()+db.getRoles().getRoleName(roleId).substring(1);

                        String path = "guis.kingdom-roles.item-creator";
                        ItemStack material = new ItemStack(Material.getMaterial(config.getSettings().getString(path + ".material")));
                        ItemMeta meta = material.getItemMeta();
                        boolean glowing = config.getSettings().getBoolean(path + ".glowing");
                        String displayName = config.getSettings().getString(path + ".name");
                        if (!displayName.equalsIgnoreCase("null") && !displayName.equalsIgnoreCase("") && !displayName.equals(null)) {
                            displayName = displayName.replace("{0}", roleName);
                            meta.setDisplayName(Colors.translateString(displayName));
                        }
                        if (glowing){
                            meta.addEnchant(Enchantment.LURE, 0, false);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                        List<String> lore = new ArrayList<>();
                        for (String line : config.getSettings().getStringList(path + ".lore")) {
                            lore.add(Colors.translateString(line
                                    .replace("{0}", roleName)
                            ));
                        }
                        meta.setLore(lore);
                        meta.setCustomModelData(roleId);
                        material.setItemMeta(meta);

                        itemsList.put(slot, material);
                        slot++;
                    }
                }

                Gui gui = new Gui(plugin, "kingdom-roles", itemsList);
                int rows = config.getSettings().getInt("guis.kingdom-roles.rows");
                if (rolesCount < 10) {
                    rows = 1;
                } else if (rolesCount > 10 && rolesCount < 19) {
                    rows = 2;
                } else if (rolesCount > 19 && rolesCount < 28) {
                    rows = 3;
                } else if (rolesCount > 28 && rolesCount < 37) {
                    rows = 4;
                } else if (rolesCount > 37 && rolesCount < 46) {
                    rows = 5;
                } else {
                    rows = 6;
                }
                gui.setRows(rows);
                gui.sendGui(player);
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.permissions-gui.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.permissions-gui.error.not-in-a-kingdom"));
        }
    }

    private HashMap<Integer, ItemStack> createItemsList(String guiPosition) {
        HashMap<Integer, ItemStack> itemsList = new HashMap<>();
        for (String key : config.getSettings().getConfigurationSection(guiPosition + ".items").getKeys(false)) {
            String path = guiPosition + ".items." + key;

            List<Integer> slots = config.getSettings().getIntegerList(path + ".slots");
            boolean glowing = config.getSettings().getBoolean(path + ".glowing");

            ItemStack is = new ItemStack(Material.getMaterial(config.getSettings().getString(path + ".material")));
            ItemMeta meta = is.getItemMeta();
            String displayName = config.getSettings().getString(path + ".name");
            if (!displayName.equalsIgnoreCase("null") && !displayName.equalsIgnoreCase("") && !displayName.equals(null)) meta.setDisplayName(Colors.translateString(displayName));
            meta.setCustomModelData(config.getSettings().getInt(path + ".model"));
            if (glowing) {
                meta.addEnchant(Enchantment.LURE, 0, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            List<String> lore = new ArrayList<>();
            for (String line : config.getSettings().getStringList(path + ".lore")) {
                lore.add(Colors.translateString(line));
            }
            meta.setLore(lore);
            is.setItemMeta(meta);

            for (Integer slot : slots) {
                itemsList.put(slot, is);
            }
        }
        return itemsList;
    }

    public HashMap<Integer, ItemStack> createRolesEditingItemsList(int kingdomId, int roleId) {
        HashMap<Integer, ItemStack> itemsList = new HashMap<>();
        String roleName = db.getRoles().getRoleName(roleId);

        int slot = 0;
        for (String permName : db.getPermissions().getActualsPermissionsList()) {
            int permId = db.getPermissions().getPermId(permName);
            boolean hasPermission = db.getPermissionsRoles().containsRole(kingdomId, roleId, permId);
            String path = hasPermission ? "guis.kingdom-roles-editing.item-creator.has-permission" : "guis.kingdom-roles-editing.item-creator.hasnt-permission";

            boolean glowing = config.getSettings().getBoolean(path + ".glowing");
            ItemStack material = new ItemStack(Material.getMaterial(config.getSettings().getString(path + ".material")));
            ItemMeta meta = material.getItemMeta();
            String displayName = config.getSettings().getString(path + ".name");
            if (!displayName.equalsIgnoreCase("null") && !displayName.equals("") && !displayName.equals(null)){
                displayName = displayName.replace("{0}", Strings.capitalize(permName));
                meta.setDisplayName(Colors.translateString(displayName));
            }
            if (glowing) {
                meta.addEnchant(Enchantment.LURE, 0, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            List<String> lore = new ArrayList<>();
            for (String line : config.getSettings().getStringList(path + ".lore")) {
                lore.add(Colors.translateString(line
                        .replace("{0}", Strings.capitalize(roleName))
                        .replace("{1}", permName)
                ));
            }
            meta.setLore(lore);
            meta.setCustomModelData(permId);
            material.setItemMeta(meta);

            itemsList.put(slot, material);
            slot++;
        }

        return itemsList;
    }

    public void visualizeChunkBorders(Player target) {
        Location loc = target.getLocation();
        Chunk chunk = new Chunk(plugin, target, loc);
        World world = loc.getWorld();

        int chunkMinX = chunk.getMinX();
        int chunkMaxX = chunk.getMaxX();

        int playerY = loc.getBlockY();
        int startY = playerY - config.getSettings().getInt("settings.chunks-border.min-y-view");
        int endY = playerY + config.getSettings().getInt("settings.chunks-border.max-y-view");

        int chunkMinZ = chunk.getMinZ();
        int chunkMaxZ = chunk.getMaxZ();

        double offsetX = config.getSettings().getDouble("settings.chunks-border.offsets.x");
        double offsetY = config.getSettings().getDouble("settings.chunks-border.offsets.y");
        double offsetZ = config.getSettings().getDouble("settings.chunks-border.offsets.z");

        List<Location> locationList = new ArrayList<>();

        for (int y=startY; y<endY; y++) {
            for (int x = chunkMinX; x < chunkMaxX; x++) {
                Location bottomLocation = new Location(world, (double) x + offsetX, (double) y + offsetY, (double) chunkMinZ + offsetZ);
                locationList.add(bottomLocation);
            }
            for (int x = chunkMinX; x < chunkMaxX + 1; x++) {
                Location topLocation = new Location(world, (double) x + offsetX, (double) y + offsetY, (double) chunkMaxZ + offsetZ);
                locationList.add(topLocation);
            }
            for (int z = chunkMinZ; z < chunkMaxZ; z++) {
                Location rightLocation = new Location(world, (double) chunkMinX + offsetX, (double) y + offsetY, (double) z + offsetZ);
                locationList.add(rightLocation);
            }
            for (int z = chunkMinZ; z < chunkMaxZ; z++) {
                Location leftLocation = new Location(world, (double) chunkMaxX + offsetX, (double) y + offsetY, (double) z + offsetZ);
                locationList.add(leftLocation);
            }
        }

        if (isInClaimedChunk(chunk)) {
            int chunkKingdomId = db.getChunks().kingdomId(chunk);
            boolean yourClaim = false;
            if (db.getPlayers().existsPlayerData(target)) {
                int playerKingdomId = db.getPlayers().getKingdomId(target);
                if (playerKingdomId == chunkKingdomId) yourClaim = true;
            }
            if (yourClaim) {
                for (Location spawnLocation : locationList) {
                    if (spawnLocation.getBlock().getType() == Material.AIR) {
                        if (hasPermission(target, "use-containers")) {
                            Particle particle = Particle.valueOf(config.getSettings().getString("settings.chunks-border.yourclaim-canbuild-particle.name"));
                            int amount = config.getSettings().getInt("settings.chunks-border.yourclaim-canbuild-particle.amount");
                            if (config.getSettings().getBoolean("settings.chunks-border.yourclaim-canbuild-particle.has-color")) {
                                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(config.getSettings().getInt("settings.chunks-border.yourclaim-canbuild-particle.color.r"), config.getSettings().getInt("settings.chunks-border.yourclaim-canbuild-particle.color.g"), config.getSettings().getInt("settings.chunks-border.yourclaim-canbuild-particle.color.b")), 1.0F);
                                target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F, dustOptions);
                            } else {
                                target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F);
                            }
                        } else {
                            Particle particle = Particle.valueOf(config.getSettings().getString("settings.chunks-border.yourclaim-cantbuild-particle.name"));
                            int amount = config.getSettings().getInt("settings.chunks-border.yourclaim-cantbuild-particle.amount");
                            if (config.getSettings().getBoolean("settings.chunks-border.yourclaim-cantbuild-particle.has-color")) {
                                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(config.getSettings().getInt("settings.chunks-border.yourclaim-cantbuild-particle.color.r"), config.getSettings().getInt("settings.chunks-border.yourclaim-cantbuild-particle.color.g"), config.getSettings().getInt("settings.chunks-border.yourclaim-cantbuild-particle.color.b")), 1.0F);
                                target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F, dustOptions);
                            } else {
                                target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F);
                            }
                        }
                    }
                }
            } else {
                for (Location spawnLocation : locationList) {
                    if (spawnLocation.getBlock().getType() == Material.AIR) {
                        Particle particle = Particle.valueOf(config.getSettings().getString("settings.chunks-border.claimed-particle.name"));
                        int amount = config.getSettings().getInt("settings.chunks-border.claimed-particle.amount");
                        if (config.getSettings().getBoolean("settings.chunks-border.claimed-particle.has-color")) {
                            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(config.getSettings().getInt("settings.chunks-border.claimed-particle.color.r"), config.getSettings().getInt("settings.chunks-border.claimed-particle.color.g"), config.getSettings().getInt("settings.chunks-border.claimed-particle.color.b")), 1.0F);
                            target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F, dustOptions);
                        } else {
                            target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F);
                        }
                    }
                }
            }
        } else {
            for (Location spawnLocation : locationList) {
                if (spawnLocation.getBlock().getType() == Material.AIR) {
                    Particle particle = Particle.valueOf(config.getSettings().getString("settings.chunks-border.unclaimed-particle.name"));
                    int amount = config.getSettings().getInt("settings.chunks-border.unclaimed-particle.amount");
                    if (config.getSettings().getBoolean("settings.chunks-border.unclaimed-particle.has-color")) {
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(config.getSettings().getInt("settings.chunks-border.unclaimed-particle.color.r"), config.getSettings().getInt("settings.chunks-border.unclaimed-particle.color.g"), config.getSettings().getInt("settings.chunks-border.unclaimed-particle.color.b")), 1.0F);
                        target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F, dustOptions);
                    } else {
                        target.spawnParticle(particle, spawnLocation, amount, 0.0F, 0.0F, 0.0F);
                    }
                }
            }
        }
    }

    public boolean isInClaimedChunk(Chunk chunk) {
        return db.getChunks().existChunk(chunk);
    }

    public boolean hasPermission(Player player, String permName) {
        if (db.getPlayers().existsPlayerData(player)) {
            int kingdomId = db.getPlayers().getKingdomId(player);

            if (db.getPlayers().permissionsNameList(player, kingdomId).contains(permName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(Player player, int permId) {
        if (db.getPlayers().existsPlayerData(player)) {
            int kingdomId = db.getPlayers().getKingdomId(player);

            if (db.getPlayers().permissionsIdList(player, kingdomId).contains(permId)) {
                return true;
            }
        }
        return false;
    }

    public Kingdom getPlayerKingdom(Player player) {
        return kingdomHash.get(player.getName());
    }

    public Logger getLog() {
        return log;
    }

    public HashMap<Player, Integer> getRolesPermissionsEditing() {
        return rolesPermissionsEditing;
    }

    public List<Player> getChunksBordersPlayerList() {
        return chunksBordersPlayerList;
    }

    public List<Player> getBypassPlayerList() {
        return bypassPlayerList;
    }

    public List<Player> getReleaseAllCooldown() {
        return releaseAllCooldown;
    }
}
