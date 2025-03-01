package its.lugoff.luxSB.gui;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.missions.Mission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissionsGUI {
    private final LuxSB plugin;

    public MissionsGUI(LuxSB plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("missions.size", 54);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("missions.title", "&d✦ Island Missions ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + " ");
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border);
        }
        for (int i = size - 9; i < size; i++) {
            gui.setItem(i, border);
        }

        List<Mission> missions = new ArrayList<>(plugin.getMissionManager().getMissions().values());
        int startSlot = 10;
        int missionsPerRow = 7;
        int rowOffset = 9;

        for (int i = 0; i < missions.size() && i < 28; i++) {
            Mission mission = missions.get(i);
            Material icon;
            if (mission.getType().equals("kill") && mission.getMaterial() == null) {
                icon = Material.ZOMBIE_HEAD; // Explicitly use ZOMBIE_HEAD for kill missions with no material
            } else {
                icon = mission.getMaterial() != null ? mission.getMaterial() : Material.STONE;
                if (icon == null) {
                    plugin.getLogger().warning("Null material for mission '" + mission.getName() + "'. Using STONE as fallback.");
                    icon = Material.STONE;
                }
            }

            String description = mission.getType() + " " + mission.getGoal() + " " +
                    (mission.getMaterial() != null ? mission.getMaterial().name().toLowerCase().replace("_", " ") : "zombies");
            int slot = startSlot + (i % missionsPerRow) + (i / missionsPerRow) * rowOffset;
            gui.setItem(slot, createItem(icon, ChatColor.LIGHT_PURPLE + mission.getName(),
                    ChatColor.GRAY + "Task: " + ChatColor.WHITE + description,
                    ChatColor.GOLD + "Reward: $" + String.format("%.2f", mission.getMoneyReward())));
        }

        gui.setItem(size - 5, createItem(Material.RED_STAINED_GLASS_PANE,
                ChatColor.RED + "Back", ChatColor.GRAY + "Return to Island Menu"));

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