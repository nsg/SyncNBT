package cc.nsg.bukkit.syncnbt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.dpohvar.powernbt.nbt.NBTBase;
import me.dpohvar.powernbt.nbt.NBTTagByte;
import me.dpohvar.powernbt.nbt.NBTTagCompound;
import me.dpohvar.powernbt.nbt.NBTTagDouble;
import me.dpohvar.powernbt.nbt.NBTTagInt;
import me.dpohvar.powernbt.nbt.NBTTagList;
import me.dpohvar.powernbt.nbt.NBTTagLong;
import me.dpohvar.powernbt.nbt.NBTTagShort;
import me.dpohvar.powernbt.nbt.NBTTagString;

import org.bukkit.entity.Player;

public class NBTData {
  
  Connection connection = null;
  
  public NBTData(Connection connection) {
    this.connection = connection;
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
        
      case "STRING":
        statement.setString(5, new String(nbtBase.toString()));
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
  public NBTTagCompound restoreExtraNBTTags(int item_pos, int parent_id) throws SQLException {
    
    NBTTagCompound compound = new NBTTagCompound();
    
    String sql_nbt = "SELECT * FROM syncnbt_nbtdata WHERE inventory_pos = ? AND parent_id = ?";
    PreparedStatement statement = connection.prepareStatement(sql_nbt, Statement.RETURN_GENERATED_KEYS);
    statement.setInt(1, item_pos);
    statement.setInt(2, parent_id);
    ResultSet res = statement.executeQuery();
    while (res.next()) {
      int id = res.getInt("id");
      String name = res.getString("name");
      String type = res.getString("type");
      
      switch (type) {
      case "COMPOUND": // java7
        NBTTagCompound rc = restoreExtraNBTTags(item_pos, id);
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
        
      case "STRING":
        String data_str = res.getString("data");
        NBTTagString s = new NBTTagString();
        s.set(data_str);
        compound.set(name, s);
        break;          
        
      default:
        System.out.println("Error: " + type + " not defined!");
        break;
      }
      
    }
    
    return compound;
  }
}
