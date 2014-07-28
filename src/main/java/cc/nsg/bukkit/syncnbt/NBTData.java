package cc.nsg.bukkit.syncnbt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.dpohvar.powernbt.nbt.NBTBase;
import me.dpohvar.powernbt.nbt.NBTContainerItem;
import me.dpohvar.powernbt.nbt.NBTTagByte;
import me.dpohvar.powernbt.nbt.NBTTagByteArray;
import me.dpohvar.powernbt.nbt.NBTTagCompound;
import me.dpohvar.powernbt.nbt.NBTTagDouble;
import me.dpohvar.powernbt.nbt.NBTTagFloat;
import me.dpohvar.powernbt.nbt.NBTTagInt;
import me.dpohvar.powernbt.nbt.NBTTagIntArray;
import me.dpohvar.powernbt.nbt.NBTTagList;
import me.dpohvar.powernbt.nbt.NBTTagLong;
import me.dpohvar.powernbt.nbt.NBTTagShort;
import me.dpohvar.powernbt.nbt.NBTTagString;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

/**
 * This is a helper class to save/restore NBT data with PowerNBT.
 * @author Stefan Berggren
 *
 */

public class NBTData {
  
  Connection connection = null;
  Database db = null;
  
  public NBTData(Database db) {
    this.connection = db.getConnection();
    this.db = db;
  }
  
  /**
   * Saves the content (and all sub tags) of the given NBTTagCompound to the database 
   * @param compound
   * @param item_pos
   * @param parent_id
   * @param player
   * @throws SQLException
   */
  public void saveExtraNBTData(NBTTagCompound compound, int item_pos, int parent_id, Player player) throws SQLException {
    db.openConnection();

    for (NBTBase nbtBase : compound) {
      String sql = "INSERT INTO syncnbt_nbtdata (inventory_pos, parent_id, name, type, data, player_name) VALUES(?,?,?,?,?,?)";
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setInt(1, item_pos);
      statement.setInt(2, parent_id);
      statement.setString(3, nbtBase.getName());
      statement.setString(4, nbtBase.getType().name());
      statement.setString(6, player.getName());
      
      switch (nbtBase.getType().name()) {
      case "COMPOUND":
        statement.setByte(5, (byte) 0);
        break;

      case "LIST":
        statement.setBytes(5, ((NBTTagList)nbtBase).toBytes());
        break;

      case "BYTE":
        statement.setInt(5, new Integer(nbtBase.toString()).intValue());
        break;

      case "INT":
        statement.setInt(5, new Integer(nbtBase.toString()).intValue());
        break;        

      case "SHORT":
        statement.setShort(5, new Integer(nbtBase.toString()).shortValue());
        break;        

      case "DOUBLE":
        statement.setDouble(5, new Double(nbtBase.toString()).doubleValue());
        break;        

      case "LONG":
        statement.setLong(5, new Long(nbtBase.toString()));
        break;        

      case "FLOAT":
        statement.setFloat(5, new Float(nbtBase.toString()));
        break;
        
      case "STRING":
        statement.setString(5, new String(nbtBase.toString()));
        break;        

      case "BYTEARRAY":
        statement.setBytes(5, ((NBTTagByteArray)nbtBase).toBytes());
        break;

      case "INTARRAY":
        statement.setString(5, ((NBTTagIntArray)nbtBase).toString());
        System.out.println("INTARRAY: " + ((NBTTagIntArray)nbtBase).toString() + " saved as a string!");
        break;
        
      default:
        statement.setString(5, "");
        System.out.println("Error: " + nbtBase.getType().name() + " not defined!");
        break;
      }      

      statement.executeUpdate();
      
      if (nbtBase.getType().name().equals("COMPOUND")) {
        int id = -2;
        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
          id = rs.getInt(1);
        }
        saveExtraNBTData((NBTTagCompound) nbtBase, item_pos, id, player);
      }
    }
    
  }
  
  /**
   * Restores the NBT data from the database and overwrites the players local inventory
   * @param item_pos
   * @param parent_id
   * @return
   * @throws SQLException
   */
  public NBTTagCompound restoreExtraNBTTags(int item_pos, int parent_id, Player player) throws SQLException {
    db.openConnection();
    
    NBTTagCompound compound = new NBTTagCompound();
    
    String sql_nbt = "SELECT * FROM syncnbt_nbtdata WHERE inventory_pos = ? AND parent_id = ? AND player_name = ?";
    PreparedStatement statement = connection.prepareStatement(sql_nbt, Statement.RETURN_GENERATED_KEYS);
    statement.setInt(1, item_pos);
    statement.setInt(2, parent_id);
    statement.setString(3, player.getName());
    ResultSet res = statement.executeQuery();
    while (res.next()) {
      int id = res.getInt("id");
      String name = res.getString("name");
      String type = res.getString("type");
      
      switch (type) {
      case "COMPOUND": // java7
        NBTTagCompound rc = restoreExtraNBTTags(item_pos, id, player);
        compound.set(name, rc);
        break;

      case "LIST":
        byte[] data_list = res.getBytes("data");
        NBTTagList list = new NBTTagList();
        list.fromBytes(data_list);
        compound.set(name, list);
        break;

      case "BYTE":
        byte data_byte = res.getByte("data");
        NBTTagByte b = new NBTTagByte();
        b.set(data_byte);
        compound.set(name, b);
        break;

      case "INT":
        int data_int = res.getInt("data");
        NBTTagInt i = new NBTTagInt();
        i.set(data_int);
        compound.set(name, i);
        break;

      case "SHORT":
        short data_short = res.getShort("data");
        NBTTagShort sh = new NBTTagShort();
        sh.set(data_short);
        compound.set(name, sh);
        break;        
        
      case "DOUBLE":
        int data_double = res.getInt("data");
        NBTTagDouble d = new NBTTagDouble();
        d.set(data_double);
        compound.set(name, d);
        break;        

      case "LONG":
        long data_long = res.getLong("data");
        NBTTagLong l = new NBTTagLong();
        l.set(data_long);
        compound.set(name, l);
        break;        

      case "FLOAT":
        float data_float = res.getFloat("data");
        NBTTagFloat f = new NBTTagFloat();
        f.set(data_float);
        compound.set(name, f);
        break;
        
      case "STRING":
        String data_str = res.getString("data");
        NBTTagString s = new NBTTagString();
        s.set(data_str);
        compound.set(name, s);
        break;          

/*      case "INTARRAY":
        int[] data_inta = res.getBytes("data");
        NBTTagIntArray ia = new NBTTagIntArray();
        s.set(data_str);
        compound.set(name, s);
        break;*/

      case "BYTEARRAY":
        byte[] data_bytea = res.getBytes("data");
        NBTTagByteArray ba = new NBTTagByteArray();
        ba.set(data_bytea);
        compound.set(name, ba);
        break;
        
      default:
        System.out.println("Error: " + type + " not defined!");
        break;
      }
      
    }
    
    return compound;
  }
  
  @SuppressWarnings("deprecation")
  public void walkInventory(PlayerInventory inventory, Player player, ItemStack is, int slot) {
    db.openConnection();

    int amount = is.getAmount();
    short durability = is.getDurability();
    int type = is.getTypeId();
    byte data = is.getData().getData();
    
    db.saveItem(slot, player.getName(), amount, durability, type, data);
    
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
      saveExtraNBTData(compound, slot, -1, player);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  @SuppressWarnings("deprecation")
  public void restoreInventory(Player player) {  
    db.openConnection();

    PlayerInventory inventory = player.getInventory();
    Connection connection = db.getConnection();

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
        
        NBTTagCompound rc = restoreExtraNBTTags(slot, -1, player);
        if (rc.size() > 0) {
          con.setTag(rc);
        }
        
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
