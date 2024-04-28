package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;

@SuppressWarnings("all")
public class Warps {

    private DBUtil db;
    private Logger log;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    public Warps(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "warps";
        data = dbName + "." + tableName;
        table = isMysql ? data : tableName;

        createTable();
    }

    private boolean tableExists() {
        boolean value = false;

        try {
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet rs = dmd.getTables(null, null, table, new String[]{"TABLE"});
            value = rs.next();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile capire se sia presente la tabella " + tableName + " nel database.");
            e.printStackTrace();
        }
        return value;
    }

    private void createTable() {
        if (!tableExists()) {
            String query = isMysql ?
                    "CREATE TABLE " + table + "(" +
                            "`warp_id` INT AUTO_INCREMENT NOT NULL, " +
                            "`warp_name` VARCHAR(35) NOT NULL NULL DEFAULT 'home', " +
                            "`warp_type` VARCHAR(25) NOT NULL DEFAULT 'home', " +
                            "`loc_world` VARCHAR(255) NOT NULL, " +
                            "`loc_x` DOUBLE NOT NULL, " +
                            "`loc_y` DOUBLE NOT NULL, " +
                            "`loc_z` DOUBLE NOT NULL, " +
                            "`rot_y` DOUBLE NOT NULL, " +
                            "`rot_p` DOUBLE NOT NULL, " +
                            "`kingdom_id` INT NOT NULL, " +
                            "PRIMARY KEY (warp_id), " +
                            "FOREIGN KEY (kingdom_id) REFERENCES kingdoms(kingdom_id) ON UPDATE CASCADE ON DELETE CASCADE" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`warp_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "`warp_name` VARCHAR(35) NOT NULL NULL DEFAULT 'home', " +
                            "`warp_type` VARCHAR(255) NOT NULL DEFAULT 'home', " +
                            "`loc_world` VARCHAR(255) NOT NULL, " +
                            "`loc_x` DOUBLE NOT NULL, " +
                            "`loc_y` DOUBLE NOT NULL, " +
                            "`loc_z` DOUBLE NOT NULL, " +
                            "`rot_y` DOUBLE NOT NULL, " +
                            "`rot_p` DOUBLE NOT NULL, " +
                            "`kingdom_id` INTEGER NOT NULL, " +
                            "FOREIGN KEY(kingdom_id) REFERENCES kingdoms(kingdom_id) ON UPDATE CASCADE ON DELETE CASCADE" +
                            ")";

            try {
                PreparedStatement state = conn.prepareStatement(query);
                state.executeUpdate();
            } catch (SQLException e) {
                log.fail("[UltimateKingdoms] Impossibile creare la tabella " + tableName + "!");
                e.printStackTrace();
            }
        }
    }

    public void insertWarpData(int kingdomId, Location loc, String warpName, String warpType) {
        String query = "INSERT INTO " + table + "(`warp_name`, `warp_type`, `loc_world`, `loc_x`, `loc_y`, `loc_z`, `rot_y`, `rot_p`, `kingdom_id`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, warpName);
            state.setString(2, warpType);
            state.setString(3, loc.getWorld().getName());
            state.setDouble(4, loc.getX());
            state.setDouble(5, loc.getY());
            state.setDouble(6, loc.getZ());
            state.setDouble(7, loc.getYaw());
            state.setDouble(8, loc.getPitch());
            state.setInt(9, kingdomId);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile inserire il warp: " + warpName + " per il regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
    }

    public void dropWarpData(int kingdomId, String warpType, String warpName) {
        String query = "DELETE FROM " + table + " WHERE kingdom_id=? AND warp_name=? AND warp_type=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.setString(2, warpName);
            state.setString(3, warpType);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile eliminare il warp " + warpName + " dal regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
    }

    public boolean existWarpData(int kingdomId, String warpType, String warpName) {
        boolean value = false;
        String query = "SELECT * FROM " + table + " WHERE kingdom_id=? AND warp_type=? AND warp_name=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.setString(2, warpType);
            state.setString(3, warpName);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                value = true;
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile capire se esiste un warp nel regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
        return value;
    }

    public Location getHome(int kingdomId) {
        String warpType = "home";
        Location loc = null;
        String query = "SELECT loc_world, loc_x, loc_y, loc_z, rot_y, rot_p FROM " + table + " WHERE kingdom_id=? AND warp_type=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.setString(2, warpType);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("loc_world"));
                double x = rs.getDouble("loc_x");
                double y = rs.getDouble("loc_y");
                double z = rs.getDouble("loc_z");
                float yaw = (float) rs.getDouble("loc_z");
                float pitch = (float) rs.getDouble("loc_z");
                loc = new Location(world, x, y, z, yaw, pitch);
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere la home del regno: " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
        return loc;
    }

    public Location getWarp(int kingdomId, String warpName) {
        String warpType = "warp";
        Location loc = null;
        String query = "SELECT loc_world, loc_x, loc_y, loc_z, rot_y, rot_p FROM " + table + " WHERE kingdom_id=? AND warp_type=? AND warp_name=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.setString(2, warpType);
            state.setString(3, warpName);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("loc_world"));
                double x = rs.getDouble("loc_x");
                double y = rs.getDouble("loc_y");
                double z = rs.getDouble("loc_z");
                float yaw = (float) rs.getDouble("loc_z");
                float pitch = (float) rs.getDouble("loc_z");
                loc = new Location(world, x, y, z, yaw, pitch);
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il warp " + warpName + " del regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
        return loc;
    }
}
