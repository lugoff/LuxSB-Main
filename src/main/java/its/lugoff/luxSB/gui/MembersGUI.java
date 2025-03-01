package its.lugoff.luxSB.gui;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.UUID;

public class MembersGUI {
    private final LuxSB plugin;

    public MembersGUI(LuxSB plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("members.size", 36); // Increased size for better spacing
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("members.title", "&e✦ Island Members ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        // Add decorative border
        ItemStack border = createItem(Material.YELLOW_STAINED_GLASS_PANE, ChatColor.GRAY + " ");
        for (int i = 0; i < 9; i++) { // Top row
            gui.setItem(i, border);
        }
        for (int i = size - 9; i < size; i++) { // Bottom row
            gui.setItem(i, border);
        }

        if (island != null) {
            // Owner info (moved to slot 10, leftmost in row 2)
            String ownerName = plugin.getServer().getOfflinePlayer(island.getOwner()).getName();
            gui.setItem(10, createItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Owner: " + ownerName,
                    ChatColor.GRAY + "Leader of the island"));

            // Members list
            int slot = 12; // Start at row 2, slot 3
            int maxSlots = 24; // Up to slot 24 (row 3, slot 6)
            for (UUID member : island.getMembers()) {
                if (slot > maxSlots) break;
                String memberName = plugin.getServer().getOfflinePlayer(member).getName();
                if (memberName != null) {
                    gui.setItem(slot++, createItem(Material.SKELETON_SKULL, ChatColor.YELLOW + memberName,
                            ChatColor.GRAY + "Click to kick (Owner only)"));
                }
            }
        } else {
            gui.setItem(13, createItem(Material.BARRIER, ChatColor.RED + "No Island",
                    ChatColor.GRAY + "You need an island to manage members!"));
        }

        // Back button at bottom center
        gui.setItem(size - 5, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Back",
                ChatColor.GRAY + "Return to Island Menu"));

        player.openInventory(gui);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(material);
            if (meta == null) {
                plugin.getLogger().warning("Failed to create ItemMeta for material: " + material.name());
                return item;
            }
        }
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}