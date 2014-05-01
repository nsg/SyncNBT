package cc.nsg.bukkit.syncnbt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.dpohvar.powernbt.nbt.NBTContainerItem;
import me.dpohvar.powernbt.nbt.NBTTagCompound;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

public class Listeners implements Listener {
  
  SyncNBT plugin = null;
  
  public Listeners(SyncNBT plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void playerLogout(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    plugin.db.lockPlayer(player.getName());

    // Player inventory (not armor)
    PlayerInventory inventory = player.getInventory();
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      ItemStack is = inventory.getItem(slot);
      if (is == null) continue;
      walkInventory(inventory, player, is, slot);
    }

    // Player armor
    ItemStack is = player.getInventory().getHelmet();
    if (is != null) {;
      walkInventory(inventory, player, is, -100);
    }
    is = player.getInventory().getChestplate();
    if (is != null) {;
      walkInventory(inventory, player, is, -101);
    }
    is = player.getInventory().getLeggings();
    if (is != null) {;
      walkInventory(inventory, player, is, -102);
    }
    is = player.getInventory().getBoots();
    if (is != null) {;
      walkInventory(inventory, player, is, -103);
    }
    
    inventory.clear();
    plugin.db.unlockPlayer(player.getName());
  }
  
  @SuppressWarnings("deprecation")
  private void walkInventory(PlayerInventory inventory, Player player, ItemStack is, int slot) {
    
    int amount = is.getAmount();
    short durability = is.getDurability();
    int type = is.getTypeId();
    byte data = is.getData().getData();
    
    plugin.db.saveItem(slot, player.getName(), amount, durability, type, data);
    
    NBTContainerItem con = null;
    
    switch (slot) {
    case -100:
      con = new NBTContainerItem(inventory.getHelmet());
      break;

    case -101:
      con = new NBTContainerItem(inventory.getChestplate());
      break;

    case -102:
      con = new NBTContainerItem(inventory.getLeggings());
      break;
      
    case -103:
      con = new NBTContainerItem(inventory.getBoots());
      break;

    default:
      con = new NBTContainerItem(inventory.getItem(slot));
      break;
    }
    
    NBTTagCompound compound = con.getTag();
    if (compound == null) return;

    try {
      plugin.nbt.saveExtraNBTData(compound, slot, -1, player);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
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
          
          restoreInventory(player);
          player.sendMessage("Your items are restored!");
      }

    }.runTaskLater(plugin, 40);
  }
  
  @SuppressWarnings("deprecation")
  private void restoreInventory(Player player) {  
    PlayerInventory inventory = player.getInventory();
    Connection connection = plugin.db.getConnection();

    try {
      String sql = "SELECT * FROM syncnbt_items WHERE player_name = ?";
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setString(1, player.getName());
      ResultSet res = statement.executeQuery();
      while (res.next()) {
        int amount = res.getInt("amount");
        short durability = res.getShort("durability");
        int type = res.getInt("type");
        byte data = res.getByte("data");
        int slot = res.getInt("slot");
        
        ItemStack item = new ItemStack(type, amount, durability);
        item.setData(new MaterialData(data));
        
        NBTContainerItem con = null;
        
        switch (slot) {
        case -100:
          inventory.setHelmet(item);
          con = new NBTContainerItem(player.getInventory().getHelmet());
          break;

        case -101:
          inventory.setChestplate(item);
          con = new NBTContainerItem(player.getInventory().getChestplate());
          break;

        case -102:
          inventory.setLeggings(item);
          con = new NBTContainerItem(player.getInventory().getLeggings());
          break;
          
        case -103:
          inventory.setBoots(item);
          con = new NBTContainerItem(player.getInventory().getBoots());
          break;

        default:
          inventory.setItem(slot, item);
          con = new NBTContainerItem(player.getInventory().getItem(slot));
          break;
        }
        
        NBTTagCompound rc = plugin.nbt.restoreExtraNBTTags(slot, -1);
        con.setTag(rc);
        
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    try {
      String sql = "DELETE FROM syncnbt_items WHERE player_name = ?";
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setString(1, player.getName());
      statement.execute();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    try {
      String sql = "DELETE FROM syncnbt_nbtdata WHERE player_name = ?";
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setString(1, player.getName());
      statement.execute();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    
  }
  
}
