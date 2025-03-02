package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerQuitListener implements Listener {
    private final LuxSB plugin;

    public PlayerQuitListener(LuxSB plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() == Material.DIAMOND_HOE && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "side_island"), PersistentDataType.STRING)) {
                    event.getPlayer().getInventory().removeItem(item);
                    plugin.getLogger().info("Removed side island placer hoe from " + event.getPlayer().getName() + " on quit.");
                    break; // Assuming only one hoe at a time
                }
            }
        }
    }
}