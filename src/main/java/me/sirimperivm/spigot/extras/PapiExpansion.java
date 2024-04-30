package me.sirimperivm.spigot.extras;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.entities.Kingdom;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.colors.Colors;
import me.sirimperivm.spigot.util.other.Logger;
import me.sirimperivm.spigot.util.other.Strings;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("all")
public class PapiExpansion extends PlaceholderExpansion {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private Strings strings;
    private DBUtil db;
    private ModUtil mod;

    public PapiExpansion(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        config = plugin.getCM();
        strings = plugin.getStrings();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    @Override
    public String getIdentifier() {
        return "ukg";
    }

    @Override
    public String getAuthor() {
        return "SirImperivm_";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String param) {
        String path = "other.placeholders.";
        String toReturn = "";

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-level"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String level = db.getKingdoms().getKingdomLevel(playerKingdom.getKingdomName());

                toReturn = level;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-next-level"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String level = db.getKingdoms().getKingdomLevel(playerKingdom.getKingdomName());
                String nextLevel = config.getSettings().getString("kingdoms.levels." + level + ".rankup.next");

                toReturn = nextLevel;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-gold-amount"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String goldAmount = String.valueOf(playerKingdom.getGoldAmount());

                toReturn = goldAmount;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-gold-amount-formatted"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String goldAmount = strings.formatNumber(playerKingdom.getGoldAmount());

                toReturn = goldAmount;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-next-level-cost"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String level = db.getKingdoms().getKingdomLevel(playerKingdom.getKingdomName());
                String nextLevel = config.getSettings().getString("kingdoms.levels." + level + ".rankup.next");
                String nextLevelCost = String.valueOf(config.getSettings().getDouble("kingdoms.levels" + level + ".rankup.cost"));

                toReturn = nextLevelCost;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-next-level-cost-formatted"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String level = db.getKingdoms().getKingdomLevel(playerKingdom.getKingdomName());
                String nextLevel = config.getSettings().getString("kingdoms.levels." + level + ".rankup.next");
                String nextLevelCost = strings.formatNumber(config.getSettings().getDouble("kingdoms.levels" + level + ".rankup.cost"));

                toReturn = nextLevelCost;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-name"))) {
            toReturn = Colors.translateString("");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);

                toReturn = playerKingdom.getKingdomName();
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-role"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                int kingdomRoleId = db.getPlayers().getKingdomRole(online);
                String kingdomRole = db.getRoles().getRoleName(kingdomRoleId);

                toReturn = strings.capitalize(kingdomRole);
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-role-tag"))) {
            toReturn = Colors.translateString("&cN/A");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                int kingdomRoleId = db.getPlayers().getKingdomRole(online);
                String kingdomRole = db.getRoles().getRoleName(kingdomRoleId);
                String kingdomRoleTag = config.getTranslatedString("kingdoms.roles." + kingdomRole + ".chat-tag");

                toReturn = kingdomRoleTag;
            }
        }

        if (param.equalsIgnoreCase(config.getSettings().getString(path + "kingdom-tag"))) {
            toReturn = Colors.translateString("");
            Player online = (Player) player;
            if (db.getPlayers().existsPlayerData(online)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(online);
                String kingdomName = playerKingdom.getKingdomName();
                int kingdomRoleId = db.getPlayers().getKingdomRole(online);
                String kingdomRole = db.getRoles().getRoleName(kingdomRoleId);
                String kingdomRoleTag = config.getSettings().getString("kingdoms.roles." + kingdomRole + ".chat-tag");

                toReturn = Colors.translateString(kingdomRoleTag + kingdomName);
            }
        }

        return toReturn;
    }
}
