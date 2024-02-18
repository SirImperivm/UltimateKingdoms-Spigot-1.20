package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all")
public class Kingdom {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;
    private ModUtil mod;

    private String kingdomName;

    private List<String> kingdoms;
    private HashMap<String, Kingdom> kingdomHash;

    public Kingdom(Main plugin, String kingdomName) {
        this.plugin = plugin;
        this.kingdomName = kingdomName;

        log = plugin.getLog();
        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    public void createKingdom(Player leader, int leaderRoleId) {
        kingdoms = db.getKingdoms().kingdomList();

        if (db.getPlayers().existsPlayerData(leader)) {
            leader.sendMessage(config.getTranslatedString("messages.kingdoms.creation.error.has-a-kingdom"));
            return;
        }

        if (kingdoms.contains(kingdomName)) {
            leader.sendMessage(config.getTranslatedString("messages.kingdoms.creation.error.already-exists"));
            return;
        }

        int maxMembers = config.getSettings().getInt("kingdoms.levels.level-0.max-members");
        db.getKingdoms().insertKingdom(kingdomName, maxMembers);

        int kingdomId = db.getKingdoms().getKingdomId(kingdomName);
        db.getPlayers().insertPlayer(leader, kingdomId, leaderRoleId);

        mod.setupPermissions(kingdomId);

        String leaderName = leader.getName();
        leader.sendMessage(config.getTranslatedString("messages.kingdoms.creation.success.created")
                .replace("{0}", kingdomName));

        for (Player all : Bukkit.getOnlinePlayers()) {
            all.sendMessage(config.getTranslatedString("messages.kingdoms.creation.success.created-broadcast")
                    .replace("{0}", leaderName)
                    .replace("{1}", kingdomName));
        }

        return;
    }

    public String getKingdomName() {
        return kingdomName;
    }
}
