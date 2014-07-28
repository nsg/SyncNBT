package cc.nsg.bukkit.syncnbt;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.*;

/**
 * This is the main class that extends JavaPlugin.
 * @author Stefan Berggren
 *
 */

public class SyncNBT extends JavaPlugin {

  private Logger log = null;
  
  protected Database db = null;
  protected NBTData nbt = null;
  
  /**
   * onEnable is triggered when the plugin is successfully loaded.
   */
  @Override
  public void onEnable() {
    super.onEnable();
    
    log = this.getLogger();
    log.info("Loading " + getName() + " version " + getDescription().getVersion());
    
    saveDefaultConfig();

    // PowerNBT is used for legacy mode 1
    if (getServer().getPluginManager().getPlugin("PowerNBT") == null) {
      log.severe("Error, unable to find the plugin PowerNBT, I will disable my self now");
      return;
    }

    // Open a connection to the database and setup tables
    db = new Database(this);
    
    // Load plugins that I depend on
    nbt = new NBTData(db); // PowerNBT
    ProtocolLibrary.getProtocolManager(); // ProtocolLib
    
    getServer().getPluginManager().registerEvents(new Listeners(this), this);
    
  }
  
  /**
   * This method listens for commands typed by the player.
   * /itemsync status
   * /itemsync mode [int]
   */
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (cmd.getName().equalsIgnoreCase("itemsync")) {
		if (args.length > 1 && args[0].equalsIgnoreCase("mode")) {
			try {
				db.setSetting(sender.getName(), new Integer(args[1]));
				return true;
			} catch (java.lang.NumberFormatException e) {
				sender.sendMessage("A number, the 2nd should be a parameter");
			}
		} else if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
			int setting = db.getSetting(sender.getName());
			sender.sendMessage("The current itemsync mode is: " + setting);
			return true;
		}
	}
    return false;
  }

  /**
   * Plugin cleanup when the plugin is unloaded, server is most likely shutting down.
   */
  @Override
  public void onDisable() {
    super.onDisable();
    
    log.info("Plugin " + getName() + " is now disabled.");
  }
  
}
