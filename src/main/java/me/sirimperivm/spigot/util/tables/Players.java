package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                            "`kingdom_role` INT NOT NULL," +
                            "PRIMARY KEY (player_id)," +
                            "CONSTRAINT `pk_kid` FOREIGN KEY (kingdom_id) REFERENCES kingdoms(kingdom_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                            "CONSTRAINT `pr_rid` FOREIGN KEY (kingdom_role) REFERENCES roles(kingdom_role) ON DELETE CASCADE ON UPDATE CASCADE" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`player_id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "`player_name` VARCHAR(40) NOT NULL," +
                            "`player_uuid` VARCHAR(255) NOT NULL," +
                            "`kingdom_id` INTEGER NOT NULL," +
                            "`kingdom_role` INT NOT NULL" +
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

    public void insertPlayer(Player player, int kingdomId, int kingdomRole) {
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString().replace("-", "");

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

    public void dropPlayer(Player player) {
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString().replace("-", "");

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

    public List<String> permissionsNameList(Player player, int kingdomId) {
        List<String> permList = new ArrayList<>();
        String playerName = player.getName();

        String query =
                "SELECT perm_name " +
                "FROM players,roles,permissions,permissions_roles " +
                "WHERE players.kingdom_role=roles.kingdom_role AND " +
                "permissions_roles.kingdom_role=roles.kingdom_role AND " +
                "permissions_roles.perm_id=permissions.perm_id AND " +
                "players.player_name=? AND " +
                "players.kingdom_id=?"
        ;

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, playerName);
            state.setInt(2, kingdomId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                permList.add(rs.getString("perm_name"));
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere la lista dei permessi per l'utente " + playerName + "!");
            e.printStackTrace();
        }

        return permList;
    }

    public List<Integer> permissionsIdList(Player player, int kingdomId) {
        List<Integer> permList = new ArrayList<>();
        String playerName = player.getName();

        String query =
                "SELECT perm_id " +
                "FROM players,roles,permissions,permissions_roles " +
                "WHERE players.kingdom_role=roles.kingdom_role AND " +
                "permissions_roles.kingdom_role=roles.kingdom_role AND " +
                "permissions_roles.perm_id=permissions.perm_id AND " +
                "players.player_name? AND " +
                "players.kingdom_id=?"
        ;

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, playerName);
            state.setInt(2, kingdomId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                permList.add(rs.getInt("perm_id"));
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere la lista dei permessi per l'utente " + playerName + "!");
            e.printStackTrace();
        }

        return permList;
    }
}
