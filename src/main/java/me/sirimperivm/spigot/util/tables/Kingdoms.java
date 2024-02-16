package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Kingdoms {

    private DBUtil db;
    private Logger log;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    public Kingdoms(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "kingdoms";
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
                            "`kingdom_id` INT AUTO_INCREMENT NOT NULL, " +
                            "`kingdomName` VARCHAR(70) NOT NULL, " +
                            "`maxMembers` INT NOT NULL, " +
                            "`kingdomLevel` INT NULL DEFAULT 0, " +
                            "`kingdomPoints` INT NULL DEFAULT 0," +
                            " PRIMARY KEY (kingdom_id)" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`kingdom_id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "`kingdomName` VARCHAR(70) NOT NULL," +
                            "`maxMembers` INTEGER NOT NULL," +
                            "`kingdomLevel` INTEGER NULL DEFAULT 0," +
                            "`kingdomPoints` INTEGER NULL DEFAULT 0" +
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

    public void insertKingdom(String kingdomName, int maxMembers) {
        String query = "INSERT INTO " + table + "(`kingdomName`, `maxMembers`) VALUES (?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setString(1, kingdomName);
            state.setInt(2, maxMembers);
            state.executeUpdate();
            log.success("[UltimateKingdoms] Il regno {kname} è stato creato con successo!".replace("{kname}", kingdomName));
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile creare il regno " + kingdomName + "!");
            e.printStackTrace();
        }
    }

    public String getKingdomName(int kingdomId) {
        String kingdomName = null;
        String query = "SELECT kingdomName FROM " + table + " WHERE kingdom_id=" + kingdomId;

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                kingdomName = rs.getString("kingdomName");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere il nome del regno " + kingdomId + "!");
            e.printStackTrace();
        }
        return kingdomName;
    }

    public int getKingdomId(String kingdomName) {
        int id = 0;
        String query = "SELECT kingdom_id FROM " + table + " WHERE kingdomName='" + kingdomName + "'";
        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                id = rs.getInt("kingdom_id");
                break;
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere l'id del regno " + kingdomName + "!");
            e.printStackTrace();
        }
        return id;
    }

    public List<String> kingdomList() {
        List<String> list = new ArrayList<>();
        String query = "SELECT kingdomName FROM " + table;

        try {
            PreparedStatement state = conn.prepareStatement(query);
            ResultSet rs = state.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("kingdomName"));
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile ottenere la lista dei reami creati.");
            e.printStackTrace();
        }
        return list;
    }
}