package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;

import java.sql.*;
import java.util.HashMap;

@SuppressWarnings("all")
public class Roles {

    private DBUtil db;
    private Logger log;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    public Roles(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "roles";
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
                            "`kingdom_role` INT NOT NULL," +
                            "`role_name` VARCHAR(40) NOT NULL," +
                            "`role_weight` INT NOT NULL," +
                            "PRIMARY KEY (kingdom_role)" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`kingdom_role` INTEGER PRIMARY KEY," +
                            "`role_name` VARCHAR(40) NOT NULL," +
                            "`role_weight` INTEGER NOT NULL" +
                            ")";
            try {
                PreparedStatement state = conn.prepareStatement(query);
                state.executeUpdate();
            } catch (SQLException e) {
                log.fail("[UltimateKingdoms] Impossibile creare la tabella " + table + "!");
                e.printStackTrace();
            }
        }
    }

    public void insertRole(int roleId, String roleName, int roleWeight) {
        String query = "INSERT INTO " + table + "(`kingdom_role`, `role_name`, `role_weight`) VALUES (?, ?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, roleId);
            state.setString(2, roleName);
            state.setInt(3, roleWeight);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKings] Impossibile aggiungere il ruolo " + roleName + "!");
            e.printStackTrace();
        }
    }

    public String getRoleName(int roleId) {
        String roleName = null;
        String query = "SELECT role_name FROM roles WHERE kingdom_role=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, roleId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                roleName = rs.getString("role_name");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il nome del ruolo " + roleId + "!");
            e.printStackTrace();
        }
        return roleName;
    }

    public int getRoleId(String roleName) {
        int roleId = 0;
        String query = "SELECT kingdom_role FROM " + table + " WHERE role_name=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, roleName);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                roleId = rs.getInt("kingdom_role");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il peso per il gruppo " + roleName + "!");
            e.printStackTrace();
        }
        return roleId;
    }

    public int getRoleWeight(int roleId) {
        int weight = 0;
        String query = "SELECT role_weight FROM " + table + " WHERE kingdom_role=?";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, roleId);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                weight = rs.getInt("role_weight");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il peso per il gruppo " + roleId + "!");
            e.printStackTrace();
        }
        return weight;
    }

    public boolean existsRoleData(String roleName) {
        boolean value = false;
        String query = "SELECT kingdom_role FROM " + table + " WHERE role_name='" + roleName + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                value = true;
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile capire se il ruolo " + roleName + " sia presente nella tabella " + tableName + "!");
            e.printStackTrace();
        }
        return value;
    }

    public HashMap<Integer, Integer> roleIdsToWeight() {
        HashMap<Integer, Integer> hash = new HashMap<>();
        String query = "SELECT kingdom_role,role_weight FROM " + table;

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                hash.put(rs.getInt("kingdom_role"), rs.getInt("role_weight"));
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere la rete di ruoli e pesi.");
            e.printStackTrace();
        }
        return hash;
    }
}
