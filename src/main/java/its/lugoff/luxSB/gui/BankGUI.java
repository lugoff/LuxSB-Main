package its.lugoff.luxSB.gui;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankGUI {
    private final LuxSB plugin;
    private final Map<UUID, Integer> selectedAmounts;

    public BankGUI(LuxSB plugin) {
        this.plugin = plugin;
        this.selectedAmounts = new HashMap<>();
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.GOLD + "✦ Island Bank ✦");
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.GRAY + " ");

        // Border: top, bottom, and sides
        for (int i = 0; i < 9; i++) gui.setItem(i, border);
        for (int i = 27; i < 36; i++) gui.setItem(i, border);
        gui.setItem(9, border);
        gui.setItem(18, border);
        gui.setItem(17, border);
        gui.setItem(26, border);

        // Bank info
        int amount = selectedAmounts.getOrDefault(player.getUniqueId(), 0);
        gui.setItem(13, createItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Selected Amount: $" + amount,
                ChatColor.GRAY + "Adjust below to deposit or withdraw"));

        // Controls
        gui.setItem(20, createItem(Material.EMERALD, ChatColor.GREEN + "Increase",
                ChatColor.GRAY + "Left: +10  Right: +100",
                ChatColor.GRAY + "Shift+Left: +1000  Shift+Right: +10000"));
        gui.setItem(21, createItem(Material.REDSTONE, ChatColor.RED + "Decrease",
                ChatColor.GRAY + "Left: -10  Right: -100",
                ChatColor.GRAY + "Shift+Left: -1000  Shift+Right: -10000"));
        gui.setItem(23, createItem(Material.DIAMOND, ChatColor.AQUA + "Deposit",
                ChatColor.GRAY + "Deposit the selected amount"));
        gui.setItem(24, createItem(Material.GOLD_NUGGET, ChatColor.YELLOW + "Withdraw",
                ChatColor.GRAY + "Withdraw the selected amount"));

        // Back button
        gui.setItem(31, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Back",
                ChatColor.GRAY + "Return to Island Menu"));

        player.openInventory(gui);
    }

    public void adjustAmount(Player player, int change) {
        int current = selectedAmounts.getOrDefault(player.getUniqueId(), 0);
        int newAmount = Math.max(0, current + change);
        selectedAmounts.put(player.getUniqueId(), newAmount);
    }

    public int getSelectedAmount(Player player) {
        return selectedAmounts.getOrDefault(player.getUniqueId(), 0);
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