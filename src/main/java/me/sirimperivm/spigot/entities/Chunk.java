package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Chunk {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;
    private ModUtil mod;

    private List<Chunk> chunksList;

    public Chunk(Main plugin) {
        this.plugin = plugin;
        log = plugin.getLog();
        chunksList = new ArrayList<>();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();
    }

    public void obtainChunk(Player player) {
        String playerName = player.getName();

        String world;
        int playerX, playerZ;
        int minX, maxX, minY, maxY, minZ, maxZ;
        Kingdom playerKingdom;
        int kingdomId;

        world = player.getLocation().getWorld().getName();
        playerX = player.getLocation().getBlockX();
        playerZ = player.getLocation().getBlockZ();

        minX = playerX;
        minY = player.getLocation().getWorld().getMinHeight();
        minZ = playerZ;

        while (minX%16!=0) {
            minX--;
        }
        while (minZ%16!=0) {
            minZ--;
        }

        maxX = minX+15;
        maxY = player.getLocation().getWorld().getMaxHeight();
        maxZ = minZ+15;

        playerKingdom = mod.getPlayerKingdom(player);
        kingdomId = db.getKingdoms().getKingdomId(playerKingdom.getKingdomName());

        db.getChunks().insertChunk(world, minX, maxX, minY, maxY, minZ, maxZ, kingdomId);

        player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.success.claimed"));

        List<Player> onlineKingdomPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);
        int finalMinX = minX;
        int finalMinZ = minZ;
        onlineKingdomPlayers.forEach(online -> {
            online.sendMessage(config.getTranslatedString("messages.kingdoms.claims.info.territory-expanded")
                    .replace("{0}", world)
                    .replace("{1}", String.valueOf(finalMinX))
                    .replace("{2}", String.valueOf(finalMinZ))
                    .replace("{3}", String.valueOf(maxX))
                    .replace("{4}", String.valueOf(maxZ))
            );
        });
    }

}
