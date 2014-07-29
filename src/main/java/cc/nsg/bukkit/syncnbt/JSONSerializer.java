package cc.nsg.bukkit.syncnbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.comphenix.protocol.utility.StreamSerializer;

public class JSONSerializer {

  public String toJSON(String name) {
    Player p = Bukkit.getServer().getPlayer(name);
    Map<String, Object> map = new HashMap<>();
    
    map.put("name", name);
    map.put("exp", p.getExp());
    map.put("foodlevel", p.getFoodLevel());
    map.put("health", p.getHealth());
    map.put("air", p.getRemainingAir());   
    map.put("inventory", itemStack2SerializedList(p.getInventory().getContents()));
    map.put("armor", itemStack2SerializedList(p.getInventory().getArmorContents()));
    map.put("enderchest", itemStack2SerializedList(p.getEnderChest().getContents()));
    map.put("saved_date", new Date().toString());
    
    return JSONObject.toJSONString(map);
  }

  @SuppressWarnings("unchecked")
  public void restorePlayer(String json) {
    Map<String, Object> data = JSON2Map(json);
    Player p = Bukkit.getServer().getPlayer((String)data.get("name"));
    
    if (p == null) {
      Bukkit.getLogger().severe("SyncNBT: We tried to restore data to a player that do not exist, hu?");
      return;
    }
    
    p.setExp(((Number)data.get("exp")).floatValue());
    p.setFoodLevel(((Number)data.get("foodlevel")).intValue());
    p.setHealth(((Number)data.get("health")).doubleValue()); // was already double, but that can change.
    p.setRemainingAir(((Number)data.get("air")).intValue());
    p.getInventory().setContents(serializedList2ItemStack((List<String>)data.get("inventory")));
    p.getInventory().setArmorContents(serializedList2ItemStack((List<String>)data.get("armor")));
    p.getEnderChest().setContents(serializedList2ItemStack((List<String>)data.get("enderchest")));
    p.sendMessage("Your items are restored from " + data.get("saved_date"));
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Map<String, Object> JSON2Map(String json) {
    Object jo = JSONValue.parse(json);
    return (Map)jo;
  }
    
  private List<String> itemStack2SerializedList(ItemStack[] itemstack) {
    List<String> list = new ArrayList<>();
    
    for (ItemStack is : itemstack) {
      if (is == null) {
        list.add(null);
      } else {
        try {
          list.add(StreamSerializer.getDefault().serializeItemStack(is));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
    return list;
  }
  
  private ItemStack[] serializedList2ItemStack(List<String> lst) {
    List<ItemStack> list = new ArrayList<>();
    
    for(Object o : lst) {
      if (o == null) {
        list.add(new ItemStack(Material.AIR));
      } else {
        try {
          list.add(StreamSerializer.getDefault().deserializeItemStack(o.toString()));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
    return list.toArray(new ItemStack[list.size()]);
  }

}
