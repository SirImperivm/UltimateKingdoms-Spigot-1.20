package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("all")
public class Kingdom {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;

    private String kingdomName;
    private int maxMembers;

    private List<String> kingdoms;

    public Kingdom(Main plugin, String kingdomName) {
        this.plugin = plugin;
        this.kingdomName = kingdomName;

        log = plugin.getLog();
        config = plugin.getCM();
        db = plugin.getDB();

        int maxMembers = config.getSettings().getInt("kingdoms.levels.level-0.max-members");
        this.maxMembers = maxMembers;
    }

    public boolean createKingdom(Player leader) {
        kingdoms = db.getKingdoms().kingdomList();

        if (db.getPlayers().existsPlayerData(leader)) {
            leader.sendMessage(config.getTranslatedString("messages.kingdoms.creation.error.has-a-kingdom"));
            return false;
        }

        if (kingdoms.contains(kingdomName)) {
            leader.sendMessage(config.getTranslatedString("messages.kingdoms.creation.error.already-exists"));
            return false;
        }

        db.getKingdoms().insertKingdom(kingdomName, maxMembers);

        int kingdomId = db.getKingdoms().getKingdomId(kingdomName);
        db.getPlayers().insertPlayer(leader, kingdomId, "leader");

        String leaderName = leader.getName();
        leader.sendMessage(config.getTranslatedString("messages.kingdoms.creation.success.created")
                .replace("{0}", kingdomName));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(config.getTranslatedString("messages.kingdoms.creation.success.created-broadcast")
                    .replace("{0}", leaderName)
                    .replace("{1}", kingdomName));
        }

        return true;
    }

    public String getKingdomName() {
        return kingdomName;
    }

    public int getMaxMembers() {
        return maxMembers;
    }
}
