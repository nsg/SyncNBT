package cc.nsg.bukkit.syncnbt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Database {
  
  String hostname = null;
  int port = 3306;
  String database = null;
  String user = null;
  String password = null;
  Connection connection = null;
  Logger log = null;
  

  public Database(SyncNBT plugin) {
    
    if (plugin == null || !plugin.isEnabled()) {
      System.out.println("Error: plugin not passed to constructor, or the plugin is disabled.");
      return;
    }
    
    log = plugin.getLogger();

    // TODO: Check these values for sane data
    hostname = plugin.getConfig().getString("Database.MySQL.HostName");
    port = plugin.getConfig().getInt("Database.MySQL.Port");
    database = plugin.getConfig().getString("Database.MySQL.DatabaseName");
    user = plugin.getConfig().getString("Database.MySQL.Username");
    password = plugin.getConfig().getString("Database.MySQL.Password");

    if (!openConnection()) {
      return;
    }
    
    if (!createTables()) {
      log.severe("Error: failed to create/check tables");
      return;
    }
  }
   
  public Connection getConnection() {
    openConnection();
    return connection;
    
  }
  
  public void saveItem(int slot, String player, int amount, short durability, int type, byte data) {
    try {
      String sql = "INSERT INTO syncnbt_items (amount,durability,type,data,player_name,slot) VALUES(?,?,?,?,?,?)";
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setInt(1, amount);
      statement.setShort(2, durability);
      statement.setInt(3, type);
      statement.setByte(4, data);
      statement.setString(5, player);
      statement.setInt(6, slot);
      statement.execute();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  }
  
  private void playerState(String player, int state) {
    
    if (connection == null) {
      log.severe("Error, no connection found!");
      return;
    }
    
    String sql = "INSERT INTO syncnbt_locks (player_name, state) values(?, ?) on duplicate key update state = ?";
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setString(1, player);
      statement.setInt(2, state);
      statement.setInt(3, state);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean isPlayerLocked(String player) {
    String sql = "SELECT * FROM syncnbt_locks WHERE player_name = ?";
    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setString(1, player);
      ResultSet res = statement.executeQuery();
      if (res.next()) {
        return res.getInt("state") == 1;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return false;
  }
  
  public void lockPlayer(String player) {
    playerState(player, 1);
  }

  public void unlockPlayer(String player) {
    playerState(player, 0);
  }

  private boolean openConnection() {
    
    try {
      
      if (connection != null && connection.isValid(500)) {
        return true;
      }
      
      log.info("No valid connection found, reconnect to MySQL server.");
      
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?autoReconnect=true", user, password);
      return true;
    } catch (SQLException e) {
      log.info(e.getMessage());
    } catch (ClassNotFoundException e) {
      log.info("Error, unable to find JDBC driver");
    }
    
    log.severe("Error: failed to open a connection to the MySQL server");
    return false;
  }
  
  private boolean createTables() {
    try {
      
      String sql = "CREATE TABLE IF NOT EXISTS syncnbt_nbtdata (" +
      		"id INT(10) PRIMARY KEY AUTO_INCREMENT, inventory_pos INT(10), parent_id INT(10), name VARCHAR(255), " +
      		"type VARCHAR(16), data BLOB, player_name TEXT" +
      		");";
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.execute();

      sql = "CREATE TABLE IF NOT EXISTS syncnbt_items (" +
          "id BIGINT PRIMARY KEY AUTO_INCREMENT, amount SMALLINT, durability INT, type SMALLINT, data SMALLINT, " +
          "player_name VARCHAR(255), slot SMALLINT" +
          ");";
      statement = connection.prepareStatement(sql);
      statement.executeUpdate();

      sql = "CREATE TABLE IF NOT EXISTS syncnbt_locks (" +
          "player_name VARCHAR(255) PRIMARY KEY, state SMALLINT" +
          ");";
      statement = connection.prepareStatement(sql);
      statement.executeUpdate();
      
      return true;
    } catch (SQLException e) {
      log.info(e.getMessage());
    }
    
    return false;
  }
  
}
