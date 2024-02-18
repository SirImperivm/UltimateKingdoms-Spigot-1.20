package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.entities.Chunk;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;

import java.sql.*;

@SuppressWarnings("all")
public class Chunks {

    private DBUtil db;
    private Logger log;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    public Chunks(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;

        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "chunks";
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
                            "`chunk_id` INT AUTO_INCREMENT NOT NULL," +
                            "`world_name` VARCHAR(125) NOT NULL,)" +
                            "`min_x` INT NOT NULL," +
                            "`max_x` INT NOT NULL," +
                            "`min_y` INT NOT NULL," +
                            "`max_y` INT NOT NULL," +
                            "`min_z` INT NOT NULL," +
                            "`max_z` INT NOT NULL," +
                            "`kingdom_id` INT NOT NULL," +
                            "PRIMARY KEY (chunk_id)," +
                            "CONSTRAINT `ch_ki_kid` FOREIGN KEY (kindom_id) REFERENCES kingdoms(kingdom_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`chunk_id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "`world_name` VARCHAR(125) NOT NULL," +
                            "`min_x` INTEGER NOT NULL," +
                            "`max_x` INTEGER NOT NULL," +
                            "`min_y` INTEGER NOT NULL," +
                            "`max_y` INTEGER NOT NULL," +
                            "`min_z` INTEGER NOT NULL," +
                            "`max_z` INTEGER NOT NULL," +
                            "`kingdom_id` INTEGER NOT NULL" +
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

    public void insertChunk(String worldName, int x, int X, int y, int Y, int z, int Z, int kingdomId) {
        String query = "INSERT INTO " + table + "(`world_name`, `min_x`,`max_x`,`min_y`,`max_y`,`min_z`,`max_z`,`kingdom_id`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, worldName);
            state.setInt(2, x);
            state.setInt(3, X);
            state.setInt(4, y);
            state.setInt(5, Y);
            state.setInt(6, z);
            state.setInt(7, Z);
            state.setInt(8, kingdomId);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile aggiungere un claim per il regno: " + kingdomId + " alle coordinate x: " + x + ", y: " + y + ", z: " + z + "!");
            e.printStackTrace();
        }
    }

    public void dropChunk(Chunk chunk) {
        String world = chunk.getWorld().getName();
        int minX = chunk.getMinX();
        int maxX = chunk.getMaxX();
        int minY = chunk.getMinY();
        int maxY = chunk.getMaxY();
        int minZ = chunk.getMinZ();
        int maxZ = chunk.getMaxZ();
        int kingdomId = chunk.getKingdomId();
        String query = "DELETE FROM " + table + " WHERE world_name=? AND min_X=? AND max_x=? AND min_y=? AND max_y=? AND min_z=? AND max_z=? AND kingdom_id=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, world);
            state.setInt(2, minX);
            state.setInt(3, maxX);
            state.setInt(4, minY);
            state.setInt(5, maxY);
            state.setInt(6, minZ);
            state.setInt(7, maxZ);
            state.setInt(8, kingdomId);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile cancellare il chunk: " + world + " - " + minX + " - " + minY + " - " + minZ + "!");
            e.printStackTrace();
        }
    }

    public void truncateTable(int kingdomId) {
        String query = "DELETE FROM " + table + " WHERE kingdom_id=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile cancellare tutti i dati del regno: " + kingdomId + "!");
            e.printStackTrace();
        }
    }

    public int getClaimedChunks(int kingdomId) {
        int chunks = 0;
        String query = "SELECT COUNT(*) AS CHUNKS FROM " + table + " WHERE kingdom_id=? GROUP BY(kingdom_id)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                chunks = rs.getInt("CHUNKS");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere la quantità di chunk claimati dal regno: " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
        return chunks;
    }

    public boolean existChunk(Chunk chunk) {
        boolean value = false;
        String world = chunk.getWorld().getName();
        int minX = chunk.getMinX();
        int maxX = chunk.getMaxX();
        int minY = chunk.getMinY();
        int maxY = chunk.getMaxY();
        int minZ = chunk.getMinZ();
        int maxZ = chunk.getMaxZ();
        int kingdomId = chunk.getKingdomId();
        String query = "SELECT chunk_id FROM " + table + " WHERE world_name=? AND min_X=? AND max_x=? AND min_y=? AND max_y=? AND min_z=? AND max_z=? AND kingdom_id=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, world);
            state.setInt(2, minX);
            state.setInt(3, maxX);
            state.setInt(4, minY);
            state.setInt(5, maxY);
            state.setInt(6, minZ);
            state.setInt(7, maxZ);
            state.setInt(8, kingdomId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                value = true;
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile capire se sia presente il chunk: " + world + " - " + minX + " - " + minY + " - " + minZ + "!");
            e.printStackTrace();
        }

        return value;
    }
}
