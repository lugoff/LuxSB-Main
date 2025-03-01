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

import java.util.Arrays;
import java.util.List;
import java.util.Map; // Added missing import

public class HeadShopGUI {
    private final LuxSB plugin;

    public HeadShopGUI(LuxSB plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        FileConfiguration config = plugin.getConfig("shops.yml");
        int size = 36;
        String title = ChatColor.DARK_PURPLE + "✦ Head Shop ✦";
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, ChatColor.GRAY + " ");
        for (int i = 0; i < 9; i++) gui.setItem(i, border);
        for (int i = size - 9; i < size; i++) gui.setItem(i, border);

        List<Map<?, ?>> heads = config.getMapList("shops.head-shop.items");
        int[] slots = {10, 12, 14, 16};
        int index = 0;
        for (Map<?, ?> head : heads) {
            if (index >= slots.length) break;
            String name = (String) head.get("display-name");
            String materialStr = (String) head.get("material");
            Material material = Material.getMaterial(materialStr) != null ? Material.getMaterial(materialStr) : Material.PLAYER_HEAD;
            int tokenBuyCost = head.containsKey("token-buy-cost") ? ((Number) head.get("token-buy-cost")).intValue() : 0;
            int tokenSellCost = head.containsKey("token-sell-cost") ? ((Number) head.get("token-sell-cost")).intValue() : 0;

            gui.setItem(slots[index++], createItem(material, ChatColor.AQUA + name,
                    ChatColor.GRAY + "A decorative head",
                    ChatColor.YELLOW + "Buy: " + tokenBuyCost + " Head Tokens",
                    ChatColor.YELLOW + "Sell: " + tokenSellCost + " Head Tokens",
                    ChatColor.GRAY + "Left-click to buy | Right-click to sell"));
        }

        gui.setItem(31, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Back",
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