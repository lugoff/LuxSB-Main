package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import its.lugoff.luxSB.schematics.SchematicLoader;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;

public class SideIslandPlacementListener implements Listener {
    private final LuxSB plugin;

    public SideIslandPlacementListener(LuxSB plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.DIAMOND_HOE || !item.hasItemMeta()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "side_island"), PersistentDataType.STRING)) return;

        String schematicName = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "side_island"), PersistentDataType.STRING);
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());

        if (island == null) {
            player.sendMessage(ChatColor.RED + "You need an island to place a side island!");
            return;
        }

        Location spawnLocation = event.getClickedBlock().getLocation().add(0, 1, 0); // Place above clicked block
        File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schematicName + ".schem");

        if (schematicFile.exists()) {
            SchematicLoader.loadSchematic(schematicName, spawnLocation);
            island.setSize(island.getSize() + 20); // Adjust size as needed
            plugin.getIslandManager().saveIsland(island);
            player.getInventory().removeItem(item);
            player.sendMessage(ChatColor.GREEN + "Side island '" + schematicName.replace("_", " ") + "' placed successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Side island schematic '" + schematicName + "' not found!");
        }

        event.setCancelled(true); // Prevent hoe usage
    }
}