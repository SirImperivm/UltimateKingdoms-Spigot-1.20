package me.sirimperivm.spigot.util;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Chunk;
import me.sirimperivm.spigot.entities.Gui;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.other.Logger;
import me.sirimperivm.spigot.util.other.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

    public ModUtil(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
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
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.page")
                    .replace("{0}", String.valueOf(page)));
            target.sendMessage(config.getTranslatedString("formats-lists.kingdoms-list.footer"));
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
                Gui depositGui = new Gui(plugin, "kingdom-deposit");
                depositGui.sendGui(player);
            } else {
                player.sendMessage(config.getTranslatedString("messages.kingdoms.deposit.error.hasnt-permission"));
            }
        } else {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.deposit.error.not-in-a-kingdom"));
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
}
