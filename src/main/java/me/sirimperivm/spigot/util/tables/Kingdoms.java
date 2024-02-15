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

    private static boolean isMysql;

    public Kingdoms(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "kingdoms";
        data = dbName + "." + tableName;

        createTable();
    }

    public boolean tableExists() {
        boolean value = false;

        try {
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet rs = isMysql ? dmd.getTables(null, null, data, new String[]{"TABLE"}) : dmd.getTables(null, null, tableName, new String[]{"TABLE"});
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
                    "CREATE TABLE " + data + "(" +
                            "`kingdom_id` INT AUTO_INCREMENT NOT NULL, " +
                            "`kingdomName` VARCHAR(70) NOT NULL, " +
                            "`maxMembers` INT NOT NULL, " +
                            "`kingdomLevel` INT NULL DEFAULT 0, " +
                            "`kingdomPoints` INT NULL DEFAULT 0," +
                            " PRIMARY KEY (kingdom_id)" +
                            ")" :
                    "CREATE TABLE " + tableName + "(" +
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

    public void createKingdom(String kingdomName, int maxMembers) {
        String query = isMysql ?
                "INSERT INTO " + data + "(`kingdomName`, `maxMembers`) VALUES ('{name}', {members})".replace("{name}", kingdomName).replace("{members}", String.valueOf(maxMembers)) :
                "INSERT INTO " + tableName + "(`kingdomName`, `maxMembers`) VALUES ('{name}', {members})".replace("{name}", kingdomName).replace("{members}", String.valueOf(maxMembers));

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.executeUpdate();
            log.success("[UltimateKingdoms] Il regno {kname} è stato creato con successo!".replace("{kname}", kingdomName));
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile creare il regno " + kingdomName + "!");
            e.printStackTrace();
        }
    }

    public List<String> kingdomList() {
        List<String> list = new ArrayList<>();
        String query = "SELECT kingdomName FROM " + tableName;

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
