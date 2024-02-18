package me.sirimperivm.spigot.util.tables;

import me.sirimperivm.spigot.util.DBUtil;
import me.sirimperivm.spigot.util.other.Logger;

import java.sql.*;

@SuppressWarnings("all")
public class PermissionsRoles {

    private DBUtil db;
    private Logger log;

    private static Connection conn;

    private static String dbName;
    private static String tableName;
    private static String data;
    private static String table;

    private static boolean isMysql;

    public PermissionsRoles(DBUtil db) {
        this.db = db;
        log = db.getLog();

        conn = db.conn;
        isMysql = db.isMysql();

        dbName = isMysql ? db.dbName : null;
        tableName = "permissions_roles";
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
                            "`assoc_id` INT NOT NULL AUTO_INCREMENT," +
                            "`kingdom_id` INT NOT NULL," +
                            "`kingdom_role` INT NOT NULL," +
                            "`kingdom_role` INT NOT NULL," +
                            "`perm_id` INT NOT NULL," +
                            "PRIMARY KEY (assoc_id)," +
                            "CONSTRAINT `pe_ro_kiid` FOREIGN KEY (kingdom_id) REFERENCES kingdoms(kingdom_id) ON DELETE CASCADE ON UPDATE CASCADE," +
                            "CONSTRAINT `pe_ro_roleid` FOREIGN KEY (kingdom_role) REFERENCES roles(kingdom_role)," +
                            "CONSTRAINT `pe_ro_permid` FOREIGN KEY (perm_id) REFERENCES permissions(perm_id)" +
                            ")" :
                    "CREATE TABLE " + table + "(" +
                            "`assoc_id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "`kingdom_id` INTEGER NOT NULL," +
                            "`kingdom_role` INTEGER NOT NULL," +
                            "`perm_id` INTEGER NOT NULL" +
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

    public void insertPerm(int kingdomId, int roleId, int permId) {
        String query = "INSERT INTO " + table + " (`kingdom_id`, `kingdom_role`, `perm_id`) VALUES (?, ?, ?)";

        try {
            PreparedStatement state = conn.prepareStatement(query);
            state.setInt(1, kingdomId);
            state.setInt(2, roleId);
            state.setInt(3, permId);
            state.executeUpdate();
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile aggiungere il permesso " + permId + " al ruolo " + roleId + " per il regno " + kingdomId + "!");
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

}
