package cc.nsg.bukkit.syncnbt;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.*;

/**
 * This is the main class for the plugin.
 * @author nsg
 *
 */

public class SyncNBT extends JavaPlugin {

  Logger log = null;
  Database db = null;
  NBTData nbt = null;
  
  @SuppressWarnings("unused")
  private ProtocolManager protocolManager;

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
    protocolManager = ProtocolLibrary.getProtocolManager();
    
    getServer().getPluginManager().registerEvents(new Listeners(this), this);
    
  }
  
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

  @Override
  public void onDisable() {
    super.onDisable();
    
    log.info("Plugin " + getName() + " is now disabled.");
  }
  
}
