package cc.nsg.bukkit.syncnbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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
    map.put("saved_date", new Date());
    
    return JSONObject.toJSONString(map);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> JSON2Map(String json) {
    return (Map<String, Object>) JSONValue.parse(json);
  }
  
  private List<String> itemStack2SerializedList(ItemStack[] itemstack) {
    List<String> list = new ArrayList<>();
    StreamSerializer ss = StreamSerializer.getDefault();
    
    for (ItemStack is : itemstack) {
      if (is == null) {
        list.add(null);
      } else {
        try {
          list.add(ss.serializeItemStack(is));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
    return list;
  }

}
