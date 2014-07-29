package cc.nsg.bukkit.syncnbt;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handlers players for ProtocolLib (mode 2)
 */

public class PlayerTicker {

  private String name = null;
  private int ticker_thread_id = -1;
  private SyncNBT plugin = null;
  
  public PlayerTicker(SyncNBT plugin, String name) {
    setName(name);
    setPlugin(plugin);
  }

  // We found a new player to track
  public void startPlayerTicker() {
    getPlugin().getLogger().info("A new player called "+ name +" found, register player tracking.");
    
    String json = plugin.db.getJSONData(name);
    if (json != null) {
      getPlugin().getLogger().info("Found data in database for player " + name + ", restoring data.");
      new JSONSerializer().restorePlayer(json);
    }
    
    ticker_thread_id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
      
      @Override
      public void run() {
        Player p = Bukkit.getServer().getPlayer(name);
        if (p == null) {
          stopPlayerTicker();
        } else {
          String json = new JSONSerializer().toJSON(name);
          plugin.db.saveJSONData(name, json);
        }
      }
    }, 200L, 200L);
  }
  
  // The player is gone
  public void stopPlayerTicker(Boolean save) {
    
    if (save) {
      String json = new JSONSerializer().toJSON(name);
      plugin.db.saveJSONData(name, json);
    }
    
    getPlugin().getLogger().info("Player "+ name +" not found, unregister player tracking.");
    Bukkit.getScheduler().cancelTask(ticker_thread_id);
  }
  
  public void stopPlayerTicker() {
    stopPlayerTicker(false);
  }
    
  public String getName() {
    return name;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setPlugin(SyncNBT plugin) {
    this.plugin = plugin;
  }

  private SyncNBT getPlugin() {
    return plugin;
  }

}
