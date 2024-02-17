package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.ConfUtil;
import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Permissions {

    private DBUtil db;
    private Logger log;

    private ConfUtil config;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    private List<String> permissionsList;

    public Permissions(DBUtil db) {
        this.db = db;
        log = db.getLog();

        config = db.getConfig();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "permissions";
        data = dbName + "." + tableName;
        table = isMysql ? data : tableName;

        createTable();
        permissionsList = new ArrayList<>();

        for (String key : config.getSettings().getConfigurationSection("kingdoms.roles.leader.default-permissions").getKeys(false)) {
            if (!permissionsList.contains(key)) {
                permissionsList.add(key);
            }
        }
        setupPermissionsList();
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
                            "`perm_id` INT NOT NULL," +
                            "`perm_name` VARCHAR(40) NOT NULL," +
                            "PRIMARY KEY (perm_id)" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`perm_id` INTEGER PRIMARY KEY," +
                            "`perm_name` VARCHAR(40) NOT NULL" +
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

    public void insertPermission(String permName) {
        String query = "INSERT INTO " + table + "(`perm_name`) VALUES (?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, permName);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile inserire il permesso " + permName + "!");
            e.printStackTrace();
        }
    }

    public int getPermId(String permname) {
        int id = 0;
        String query = "SELECT perm_id FROM " + table + " WHERE perm_name='" + permname + "'";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                id = rs.getInt("perm_id");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere l'id del permesso " + permname + "!");
            e.printStackTrace();
        }
        return id;
    }

    public void setupPermissionsList() {
        for (String permName : permissionsList) {
            insertPermission(permName);
        }
    }

    public List<String> getPermissionsList() {
        return permissionsList;
    }
}
