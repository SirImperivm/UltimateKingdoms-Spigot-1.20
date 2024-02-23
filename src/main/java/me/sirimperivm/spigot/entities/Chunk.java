package me.sirimperivm.spigot.entities;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.ModUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("all")
public class Chunk {

    private Main plugin;
    private Logger log;

    private ConfUtil config;
    private DBUtil db;
    private ModUtil mod;

    private Player player;
    private World world;
    private String playerName, worldName;
    private int playerX, playerZ, minX, maxX, minY, maxY, minZ, maxZ, kingdomId;
    private Kingdom playerKingdom;

    private int locX, locZ;

    public Chunk(Main plugin, Location loc) {
        this.plugin = plugin;
        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();

        world = loc.getWorld();
        worldName = world.getName();
        locX = loc.getBlockX();
        locZ = loc.getBlockZ();

        minX = locX;
        minY = world.getMinHeight();
        minZ = locZ;

        while (minX%16!=0) {
            minX--;
        }
        while (minZ%16!=0) {
            minZ--;
        }

        maxX = minX+15;
        maxY = world.getMaxHeight();
        maxZ = minZ+15;
    }

    public Chunk(Main plugin, Player player, Location loc) {
        this.plugin = plugin;
        this.player = player;

        log = plugin.getLog();

        config = plugin.getCM();
        db = plugin.getDB();
        mod = plugin.getMod();

        world = loc.getWorld();
        worldName = world.getName();
        playerName = player.getName();
        playerX = loc.getBlockX();
        playerZ = loc.getBlockZ();

        minX = playerX;
        minY = world.getMinHeight();
        minZ = playerZ;

        while (minX%16!=0) {
            minX--;
        }
        while (minZ%16!=0) {
            minZ--;
        }

        maxX = minX+15;
        maxY = world.getMaxHeight();
        maxZ = minZ+15;

        playerKingdom = mod.getPlayerKingdom(player);
        kingdomId = db.getPlayers().getKingdomId(player);
    }

    public void obtainChunk() {
        db.getChunks().insertChunk(worldName, minX, maxX, minY, maxY, minZ, maxZ, kingdomId);
        player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.success.claimed"));

        List<Player> onlineKingdomPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);
        onlineKingdomPlayers.forEach(online -> {
            online.sendMessage(config.getTranslatedString("messages.kingdoms.claims.info.territory-expanded")
                    .replace("{0}", worldName)
                    .replace("{1}", String.valueOf(minX))
                    .replace("{2}", String.valueOf(minZ))
                    .replace("{3}", String.valueOf(maxX))
                    .replace("{4}", String.valueOf(maxZ))
            );
        });
    }

    public void unclaimChunk(Chunk chunk) {
        db.getChunks().dropChunk(chunk);
        player.sendMessage(config.getTranslatedString("messages.kingdoms.claims.success.unclaimed"));

        List<Player> onlineKingdomPlayers = db.getKingdoms().kingdomPlayersList(kingdomId);
        onlineKingdomPlayers.forEach(online -> {
            online.sendMessage(config.getTranslatedString("messages.kingdoms.claims.info.territory-released")
                    .replace("{0}", worldName)
                    .replace("{1}", String.valueOf(minX))
                    .replace("{2}", String.valueOf(minZ))
                    .replace("{3}", String.valueOf(maxX))
                    .replace("{4}", String.valueOf(maxZ))
            );
        });
    }

    public World getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getKingdomId() {
        return kingdomId;
    }
}
