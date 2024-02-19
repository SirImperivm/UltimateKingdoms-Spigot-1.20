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

import java.util.List;

@SuppressWarnings("all")
public class PapiExpansion extends PlaceholderExpansion {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;
    private ModUtil mod;

    public PapiExpansion(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    @Override
    public String getIdentifier() {
        return "ukingdoms";
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
        String toReturn = "&4Placeholder ERROR!";

        if (param.equalsIgnoreCase(config.getTranslatedString("placeholders.kingdom-level.placeholder"))) {
            Player onlinePlayer = (Player) player;
            if (db.getPlayers().existsPlayerData(onlinePlayer)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(onlinePlayer);
                String kingdomName = playerKingdom.getKingdomName();
                String kingdomLevel = db.getKingdoms().getKingdomLevel(kingdomName);

                toReturn = config.getTranslatedString("placeholders.kingdom-level.return-format".replace("{0}", String.valueOf(kingdomLevel)));
            } else {
                toReturn = Colors.translateString("&4N/A");
            }
        }

        if (param.equalsIgnoreCase(config.getTranslatedString("placeholders.kingdom-next-level.placeholder"))) {
            Player onlinePlayer = (Player) player;
            if (db.getPlayers().existsPlayerData(onlinePlayer)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(onlinePlayer);
                String kingdomName = playerKingdom.getKingdomName();
                String kingdomLevel = db.getKingdoms().getKingdomLevel(kingdomName);
                String nextLevel = config.getSettings().getString("kingdoms.levels." + kingdomLevel + ".rankup.next");

                toReturn = config.getTranslatedString("placeholders.kingdom-next-level-return-format".replace("{0}", nextLevel));
            } else {
                toReturn = Colors.translateString("&4N/A");
            }
        }

        if (param.equalsIgnoreCase(config.getTranslatedString("placeholders.kingdom-gold-amount.placeholder"))) {
            Player onlinePlayer = (Player) player;
            if (db.getPlayers().existsPlayerData(onlinePlayer)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(onlinePlayer);
                String kingdomName = playerKingdom.getKingdomName();
                int goldAmount = db.getKingdoms().getGoldAmount(kingdomName);

                toReturn = config.getTranslatedString("placeholders.kingdom-gold-amount.return-format").replace("{0}", String.valueOf(goldAmount));
            } else {
                toReturn = Colors.translateString("&4N/A");
            }
        }

        if (param.equalsIgnoreCase(config.getTranslatedString("placeholders.kingdom-gold-amount-formatted.placeholder"))) {
            Player onlinePlayer = (Player) player;
            if (db.getPlayers().existsPlayerData(onlinePlayer)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(onlinePlayer);
                String kingdomName = playerKingdom.getKingdomName();
                int format_size = config.getSettings().getInt("other.strings.number-formatter.format-size");
                List<String> associations = config.getSettings().getStringList("other.strings.number-formatter.associations");
                String goldAmount = Strings.formatNumber(db.getKingdoms().getGoldAmount(kingdomName), format_size, associations);

                toReturn = config.getTranslatedString("placeholders.kingdom-gold-amount-formatted.return-format").replace("{0}", goldAmount);
            } else {
                toReturn = Colors.translateString("&4N/A");
            }
        }

        if (param.equalsIgnoreCase(config.getTranslatedString("placeholders.kingdom-nextrankup-cost.placeholder"))) {
            Player onlinePlayer = (Player) player;
            if (db.getPlayers().existsPlayerData(onlinePlayer)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(onlinePlayer);
                String kingdomName = playerKingdom.getKingdomName();
                String kingdomLevel = db.getKingdoms().getKingdomLevel(kingdomName);
                int rankupCost = config.getSettings().getInt("kingdoms.levels." + kingdomLevel + ".rankup.cost");

                toReturn = config.getTranslatedString("placeholders.kingdom-nextrankup-cost.return-format").replace("{0}", String.valueOf(rankupCost));
            } else {
                toReturn = Colors.translateString("&4N/A");
            }
        }

        if (param.equalsIgnoreCase(config.getTranslatedString("placeholders.kingdom-nextrankup-cost-formatted.placeholder"))) {
            Player onlinePlayer = (Player) player;
            if (db.getPlayers().existsPlayerData(onlinePlayer)) {
                Kingdom playerKingdom = mod.getPlayerKingdom(onlinePlayer);
                String kingdomName = playerKingdom.getKingdomName();
                String kingdomLevel = db.getKingdoms().getKingdomLevel(kingdomName);
                int format_size = config.getSettings().getInt("other.strings.number-formatter.format-size");
                List<String> associations = config.getSettings().getStringList("other.strings.number-formatter.associations");
                String rankupCost = Strings.formatNumber(config.getSettings().getInt("kingdoms.levels." + kingdomLevel + ".rankup.cost"), format_size, associations);

                toReturn = config.getTranslatedString("placeholders.kingdom-nextrankup-cost-formatted.return-format").replace("{0}", String.valueOf(rankupCost));
            } else {
                toReturn = Colors.translateString("&4N/A");
            }
        }

        return toReturn;
    }
}
