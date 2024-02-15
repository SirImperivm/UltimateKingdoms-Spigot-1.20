package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.Logger;
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
    private Player kingdomLeader;

    private String kingdomLeaderName;
    private String kingdomLeaderUuid;

    private List<String> kingdoms;

    public Kingdom(Main plugin, String kingdomName, Player kingdomLeader) {
        this.plugin = plugin;
        this.kingdomName = kingdomName;
        this.kingdomLeader = kingdomLeader;

        String kingdomLeaderName = kingdomLeader.getName();
        String kingdomLeaderUUID = kingdomLeader.getUniqueId().toString().replace("-", "");

        this.kingdomLeaderName = kingdomLeaderName;
        this.kingdomLeaderUuid = kingdomLeaderUUID;

        log = plugin.getLog();
        config = plugin.getCM();
        db = plugin.getDB();

        kingdoms = db.getKingdoms().kingdomList();

        int maxMembers = config.getSettings().getInt("kingdoms.levels.level-0.max-members");
        this.maxMembers = maxMembers;

        if (!kingdoms.contains(kingdomName))
        db.getKingdoms().createKingdom(kingdomName, maxMembers);
    }

    public String getKingdomName() {
        return kingdomName;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public Player getKingdomLeader() {
        return kingdomLeader;
    }

    public String getKingdomLeaderName() {
        return kingdomLeaderName;
    }

    public String getKingdomLeaderUuid() {
        return kingdomLeaderUuid;
    }
}
