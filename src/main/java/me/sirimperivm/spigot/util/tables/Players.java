package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.Logger;
import org.bukkit.entity.Player;

import java.sql.*;

@SuppressWarnings("all")
public class Players {

    private DBUtil db;
    private Logger log;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    public Players(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "players";
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
                            "`player_id` INT AUTO_INCREMENT NOT NULL," +
                            "`player_name` VARCHAR(40) NOT NULL," +
                            "`player_uuid` VARCHAR(255) NOT NULL," +
                            "`kingdom_id` INT NOT NULL," +
                            "`kingdom_role` VARCHAR(50) NOT NULL," +
                            "PRIMARY KEY (player_id)," +
                            "CONSTRAINT `pk_kid` FOREIGN KEY (kingdom_id) REFERENCES kingdoms(kingdom_id) ON DELETE CASCADE ON UPDATE CASCADE" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`player_id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "`player_name` VARCHAR(40) NOT NULL," +
                            "`player_uuid` VARCHAR(255) NOT NULL," +
                            "`kingdom_id` INTEGER NOT NULL," +
                            "`kingdom_role` VARCHAR(50) NOT NULL," +
                            "FOREIGN KEY (kingdom_id) REFERENCES kingdoms(kingdom_id) ON DELETE CASCADE ON UPDATE CASCADE" +
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

    public void insertPlayer(Player player, int kingdomId, String kingdomRole) {
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString().replace("-", "");

        String query = "INSERT INTO " + table + "(`player_name`, `player_uuid`, kingdom_id, `kingdom_role`) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, playerName);
            state.setString(2, playerUuid);
            state.setInt(3, kingdomId);
            state.setString(4, kingdomRole);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile impostare " + playerName + " come leader del regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
    }

    public boolean existsPlayerData(Player player) {
        boolean value = false;
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString().replace("-", "");

        String query = "SELECT * FROM " + table + " WHERE player_name='" + playerName + "' OR player_uuid='" + playerUuid + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                value = true;
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile capire se l'utente " + playerName + " sia presente nella tabella " + tableName + "!");
            e.printStackTrace();
        }
        return value;
    }
}
