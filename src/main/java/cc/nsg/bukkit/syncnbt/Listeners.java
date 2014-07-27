package cc.nsg.bukkit.syncnbt;

import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * This class listens for events from Bukkit.
 * @author Stefan Berggren
 *
 */

public class Listeners implements Listener {
  
  SyncNBT plugin = null;
  
  public Listeners(SyncNBT plugin) {
    this.plugin = plugin;
  }

  /**
   * A player leaves the server, note that this event is only triggered with
   * a clean normal quit. 
   */
  
  @EventHandler
  public void playerLogout(PlayerQuitEvent event) {
    plugin.db.openConnection();

    Player player = event.getPlayer();
    plugin.db.lockPlayer(player.getName());

    // Player inventory (not armor)
    PlayerInventory inventory = player.getInventory();
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      ItemStack is = inventory.getItem(slot);
      if (is == null) continue;
      plugin.nbt.walkInventory(inventory, player, is, slot);
    }

    // Player armor
    ItemStack is = player.getInventory().getHelmet();
    if (is != null) {;
      plugin.nbt.walkInventory(inventory, player, is, -100);
      is.setType(Material.AIR); // clear helmet
    }
    is = player.getInventory().getChestplate();
    if (is != null) {;
      plugin.nbt.walkInventory(inventory, player, is, -101);
      is.setType(Material.AIR); // clear chest
    }
    is = player.getInventory().getLeggings();
    if (is != null) {;
      plugin.nbt.walkInventory(inventory, player, is, -102);
      is.setType(Material.AIR); // clear legs
    }
    is = player.getInventory().getBoots();
    if (is != null) {;
      plugin.nbt.walkInventory(inventory, player, is, -103);
      is.setType(Material.AIR); // clear boots
    }
    
    inventory.clear();
    plugin.db.unlockPlayer(player.getName());
  }
  
  /**
   * A player logs in to the server, race conditions!
   */
  
  @EventHandler
  public void playerLogin(PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    new BukkitRunnable() {
      
      @Override
      public void run() {
          while(plugin.db.isPlayerLocked(player.getName())) {
            player.sendMessage("Items still locked by another server...");
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          
          plugin.nbt.restoreInventory(player);
          player.sendMessage("Your items are restored!");
      }

    }.runTaskLater(plugin, 40);
  }
    
}
