package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

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
        String query = isMysql ?
                "CREATE TABLE IF NOT EXISTS " + table + "(" +
                        "`player_id` INT AUTO_INCREMENT NOT NULL, " +
                        "`player_name` VARCHAR(40) NOT NULL, " +
                        "`player_uuid` VARCHAR(255) NOT NULL, " +
                        "`kingdom_id` INT NOT NULL, " +
                        "`kingdom_role` INT NOT NULL, " +
                        "PRIMARY KEY (player_id), " +
                        "FOREIGN KEY (kingdom_id) REFERENCES kingdoms(kingdom_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                        "FOREIGN KEY (kingdom_role) REFERENCES roles(kingdom_role) ON UPDATE CASCADE ON DELETE CASCADE" +
                        ")" :
                "CREATE TABLE IF NOT EXISTS " + table + "(" +
                        "`player_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "`player_name` VARCHAR(40) NOT NULL, " +
                        "`player_uuid` VARCHAR(255) NOT NULL, " +
                        "`kingdom_id` INTEGER NOT NULL, " +
                        "`kingdom_role` INTEGER NOT NULL, " +
                        "FOREIGN KEY(kingdom_id) REFERENCES kingdoms(kingdom_id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                        "FOREIGN KEY(kingdom_role) REFERENCES roles(kingdom_role) ON UPDATE CASCADE ON DELETE CASCADE" +
                        ")";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile creare la tabella " + tableName + "!");
            e.printStackTrace();
        }
    }

    public void insertPlayer(Player player, int kingdomId, int kingdomRole) {
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();

        String query = "INSERT INTO " + table + "(`player_name`, `player_uuid`, kingdom_id, kingdom_role) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, playerName);
            state.setString(2, playerUuid);
            state.setInt(3, kingdomId);
            state.setInt(4, kingdomRole);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile impostare " + playerName + " come leader del regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
    }

    public void updateRole(Player player, int kingdomRole) {
        String playerName = player.getName();

        String query = "UPDATE " + table + " SET kingdom_role=? WHERE player_name=?";
        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomRole);
            state.setString(2, playerName);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile aggiornare il ruolo dell'utente " + playerName + " su " + kingdomRole + "!");
            e.printStackTrace();
        }
    }

    public void updateRole(String playerName, int kingdomRole) {
        String query = "UPDATE " + table + " SET kingdom_role=? WHERE player_name=?";
        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomRole);
            state.setString(2, playerName);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile aggiornare il ruolo dell'utente " + playerName + " su " + kingdomRole + "!");
            e.printStackTrace();
        }
    }

    public void dropPlayer(Player player) {
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();

        String query = "DELETE FROM " + table + " WHERE player_name='" + playerName + "' OR player_uuid='" + playerUuid + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile rimuovere dati riguardanti l'utente " + playerName + "!");
            e.printStackTrace();
        }
    }

    public void dropPlayer(String playerName) {
        String playerUuid = getPlayerUUID(playerName).toString();
        String query = "DELETE FROM " + table + " WHERE player_name='" + playerName + "' OR player_uuid='" + playerUuid + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile rimuovere dati riguardanti l'utente " + playerName + "!");
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

    public int getKingdomRole(Player player) {
        int role = 0;
        String playerName = player.getName();
        String query = "SELECT kingdom_role FROM " + tableName + " WHERE player_name='" + playerName + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                role = rs.getInt("kingdom_role");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il ruolo nel regno dell'utente " + playerName + "!");
            e.printStackTrace();
        }
        return role;
    }

    public int getKingdomRole(String playerName) {
        int role = 0;
        String query = "SELECT kingdom_role FROM " + tableName + " WHERE player_name='" + playerName + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                role = rs.getInt("kingdom_role");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il ruolo nel regno dell'utente " + playerName + "!");
            e.printStackTrace();
        }
        return role;
    }

    public int getKingdomId(Player player) {
        int kid = 0;
        String playerName = player.getName();
        String query = "SELECT kingdom_id FROM " + tableName + " WHERE player_name='" + playerName + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                kid = rs.getInt("kingdom_id");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere l'id del regno dell'utente " + playerName + "!");
            e.printStackTrace();
        }
        return kid;
    }

    public int getKingdomId(String playerName) {
        int kid = 0;
        String query = "SELECT kingdom_id FROM " + tableName + " WHERE player_name='" + playerName + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                kid = rs.getInt("kingdom_id");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere l'id del regno dell'utente " + playerName + "!");
            e.printStackTrace();
        }
        return kid;
    }

    public int getPlayersCount(int kingdomId) {
        int count = 0;
        String query = "SELECT COUNT(*) AS conto FROM " + table + " WHERE kingdom_id=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                count = rs.getInt("conto");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il conto dei player del regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
        return count;
    }

    public boolean existsPlayerData(Player player) {
        boolean value = false;
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();

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

    public String getLeaderName(int kingdomId) {
        String leader = null;
        String query = isMysql ? "SELECT player_name FROM {dbname}players, {dbname}kingdoms, {dbname}roles WHERE {dbname}players.kingdom_id={dbname}kingdoms.kingdom_id AND {dbname}players.kingdom_role={dbname}roles.kingdom_role AND {dbname}players.kingdom_id=? AND {dbname}roles.role_name=?"
                .replace("{dbname}", dbName)
                :
                "SELECT player_name FROM players, kingdoms, roles WHERE players.kingdom_id=kingdoms.kingdom_id AND players.kingdom_role=roles.kingdom_role AND players.kingdom_id=? AND roles.role_name=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.setString(2, "leader");
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                leader = rs.getString("player_name");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il leader del regno " + db.getKingdoms().getKingdomName(kingdomId) + "!");
            e.printStackTrace();
        }
        return leader;
    }

    public UUID getPlayerUUID(String playerName) {
        UUID playerUUID = null;
        String query = "SELECT player_uuid FROM " + table + " WHERE player_name=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, playerName);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                playerUUID = UUID.fromString(rs.getString("player_uuid"));
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere l'uuid del player " + playerUUID + "!");
            e.printStackTrace();
        }
        return playerUUID;
    }
}
