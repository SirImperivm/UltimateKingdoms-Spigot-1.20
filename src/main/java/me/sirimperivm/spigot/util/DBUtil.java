package me.sirimperivm.spigot.util;

import me.sirimperivm.spigot.Main;
import me.sirimperivm.spigot.util.tables.Kingdoms;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("all")
public class DBUtil {

    private Main plugin;
    private ConfUtil config;

    private Logger log;

    private String dbType;
    private String host;
    private int port;
    private String username;
    private String password;
    public String dbName;
    private String options;

    private Kingdoms kingdoms;

    public DBUtil(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getCM();
        log = plugin.getLog();

        dbType = config.getSettings().getString("storage.type");
        host = dbType.equalsIgnoreCase("mysql") ? config.getSettings().getString("storage.credentials.host") : null;
        port = dbType.equalsIgnoreCase("mysql") ? config.getSettings().getInt("storage.credentials.port") : 0;
        username = dbType.equalsIgnoreCase("mysql") ? config.getSettings().getString("storage.credentials.username") : null;
        password = dbType.equalsIgnoreCase("mysql") ? config.getSettings().getString("storage.credentials.password") : null;
        dbName = dbType.equalsIgnoreCase("mysql") ? config.getSettings().getString("storage.credentials.dbname") : "storage.db";
        options = dbType.equalsIgnoreCase("mysql") ? config.getSettings().getString("storage.credentials.options") : null;
    }

    private boolean canConnect = false;

    public Connection conn;
    private boolean mysql = false;

    private void createConnection() {
        if (dbType.equalsIgnoreCase("mysql")) {
            canConnect = true;

            if (canConnect) {
                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + options;

                try {
                    conn = DriverManager.getConnection(url, username, password);
                    log.success("[UltimateKingdoms] Plugin connesso al database MySQL con successo.");
                    mysql = true;
                } catch (SQLException e) {
                    log.fail("[UltimateKingdoms] Impossibile connettersi al database tento con SQLite.");
                    e.printStackTrace();
                    canConnect = false;
                }
            }

            if (!canConnect){
                if (!createSQLite(dbName)) {
                    plugin.disablePlugin();
                }
            }
        } else {
            if (!createSQLite(dbName)) {
                plugin.disablePlugin();
            }
        }
    }

    public void closeConnection() {
        try {
            if (conn != null || !conn.isClosed()) {
                conn.close();
                log.success("[UltimateKingdoms] Disconnesso dal database con successo.");
            }
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Impossibile disconnettersi dal database.");
            e.printStackTrace();
        }
    }

    public boolean createSQLite(String dbName) {
        try {
            File dbFolder = plugin.getDataFolder();
            if (!dbFolder.exists()) dbFolder.mkdir();
            File dbFile = new File(dbFolder, dbName);
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            conn = DriverManager.getConnection(url);
            log.success("[UltimateKingdoms] Database SQLite creato con successo.");
            return true;
        } catch (SQLException e) {
            log.fail("[UltimateKingdoms] Non è stato possibile creare il database SQLite.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isMysql() {
        return mysql;
    }

    public void setup() {
        createConnection();
        kingdoms = new Kingdoms(this);

    }

    public Logger getLog() {
        return log;
    }

    public Kingdoms getKingdoms() {
        return kingdoms;
    }
}
