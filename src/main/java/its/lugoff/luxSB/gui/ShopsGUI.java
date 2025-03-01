package its.lugoff.luxSB.gui;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

public class ShopsGUI {
    private final LuxSB plugin;

    public ShopsGUI(LuxSB plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("shops.size", 27);
        String title = ChatColor.translateAlternateColorCodes('&', "&a✦ Island Shops ✦");
        Inventory gui = Bukkit.createInventory(null, size, title);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, ChatColor.translateAlternateColorCodes('&', "&7 "));
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 23, 24, 25, 26}) {
            gui.setItem(i, border);
        }

        gui.setItem(10, createItem(Material.STONE, ChatColor.GREEN + "Blocks", ChatColor.GRAY + "Browse block items"));
        gui.setItem(12, createItem(Material.WHEAT, ChatColor.GREEN + "Farm", ChatColor.GRAY + "Browse farming items"));
        gui.setItem(14, createItem(Material.IRON_PICKAXE, ChatColor.GREEN + "Tools", ChatColor.GRAY + "Browse tools"));
        gui.setItem(16, createItem(Material.DIAMOND_ORE, ChatColor.GREEN + "Ores", ChatColor.GRAY + "Browse ore items"));

        gui.setItem(22, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Back", ChatColor.GRAY + "Return to main menu"));
        player.openInventory(gui);
    }

    public void openCategory(Player player, String category) {
        FileConfiguration shopsConfig = plugin.getConfig("shops.yml");
        String title = ChatColor.translateAlternateColorCodes('&', "&a✦ Island Shops - " + category);
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, title);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, ChatColor.translateAlternateColorCodes('&', "&7 "));
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            gui.setItem(i, border);
        }

        ConfigurationSection items = shopsConfig.getConfigurationSection("shops." + category.toLowerCase().replace(" ", "_") + ".items");
        if (items != null) {
            int slot = 9;
            for (String key : items.getKeys(false)) {
                Material material = Material.getMaterial(items.getString(key + ".material", "STONE"));
                int amount = items.getInt(key + ".amount", 1);
                double buyPrice = items.getDouble(key + ".buy-price", 0.0);
                double sellPrice = items.getDouble(key + ".sell-price", 0.0);

                gui.setItem(slot++, createItem(material, ChatColor.GREEN + key.replace("_", " "),
                        ChatColor.GRAY + "Amount: " + amount,
                        buyPrice > 0 ? ChatColor.YELLOW + "Buy: $" + String.format("%.2f", buyPrice) : ChatColor.RED + "Not for sale",
                        sellPrice > 0 ? ChatColor.YELLOW + "Sell: $" + String.format("%.2f", sellPrice) : ChatColor.RED + "Cannot sell",
                        ChatColor.YELLOW + "Left: Buy | Right: Sell"));
            }
        } else {
            plugin.getLogger().warning("No items found for shop category: " + category.toLowerCase().replace(" ", "_"));
        }

        gui.setItem(49, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Back", ChatColor.GRAY + "Return to shop menu"));
        player.openInventory(gui);
        plugin.getLogger().info("Opened shop category GUI for " + player.getName() + ": " + category);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}