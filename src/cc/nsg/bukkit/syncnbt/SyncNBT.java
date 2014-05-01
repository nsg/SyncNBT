package cc.nsg.bukkit.syncnbt;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class for the plugin.
 * @author nsg
 *
 */

public class SyncNBT extends JavaPlugin {

  Logger log = null;
  Database db = null;
  NBTData nbt = null;
  
  @Override
  public void onEnable() {
    super.onEnable();
    
    log = this.getLogger();
    log.info("Loading " + getName() + " version " + getDescription().getVersion());
    
    saveDefaultConfig();

    if (getServer().getPluginManager().getPlugin("PowerNBT") == null) {
      log.severe("Error, unable to find the plugin PowerNBT, I will disable my self now");
      return;
    }
    
    db = new Database(this);
    nbt = new NBTData(db);
    
    getServer().getPluginManager().registerEvents(new Listeners(this), this);    
    
  }
  
  @Override
  public void onDisable() {
    super.onDisable();
    
    log.info("Plugin " + getName() + " is now disabled.");
  }
  
}
