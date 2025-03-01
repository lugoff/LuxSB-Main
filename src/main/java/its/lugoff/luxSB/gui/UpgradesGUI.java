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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradesGUI {
    private final LuxSB plugin;

    public UpgradesGUI(LuxSB plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("upgrades.size", 27);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("upgrades.title", "&b✦ Island Upgrades ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.getMaterial(guiConfig.getString("general.border-material", "CYAN_STAINED_GLASS_PANE")),
                ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.border-name", "&7 ")));
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 23, 24, 25, 26}) {
            gui.setItem(i, border);
        }

        if (island != null) {
            double sizeCost = island.getUpgrades().getSizeLevel() * plugin.getConfig().getDouble("islands.upgrades.size-cost-multiplier", 1000.0);
            gui.setItem(guiConfig.getInt("upgrades.slots.size", 11), createItem(Material.ANVIL, ChatColor.AQUA + "Island Size",
                    ChatColor.GRAY + "Increase your island size",
                    ChatColor.YELLOW + "Level: " + island.getUpgrades().getSizeLevel(),
                    island.getUpgrades().isMaxSize() ? ChatColor.RED + "Max Level" : ChatColor.YELLOW + "Cost: $" + sizeCost));

            double generatorCost = island.getUpgrades().getGeneratorLevel() * plugin.getConfig().getDouble("islands.upgrades.generator-cost-multiplier", 1500.0);
            List<String> generatorLore = new ArrayList<>(Arrays.asList(
                    ChatColor.GRAY + "Increase or toggle generator",
                    ChatColor.AQUA + "Level: " + island.getUpgrades().getGeneratorLevel(),
                    ChatColor.AQUA + "Ore Spawning: " + (island.getUpgrades().isOreSpawningEnabled() ? "Enabled" : "Disabled")
            ));

            // Add current level ore percentages
            generatorLore.add(ChatColor.WHITE + "Current Ores:");
            addOrePercentages(generatorLore, island.getUpgrades().getGeneratorLevel(), ChatColor.GREEN);

            // Add next level ore percentages or max level message
            if (island.getUpgrades().isMaxGenerator()) {
                generatorLore.add(ChatColor.RED + "Max Level"); // Changed from CYAN to RED
            } else {
                generatorLore.add(ChatColor.RED + "Next Level Ores:"); // Changed from LIGHT_PURPLE to RED
                addOrePercentages(generatorLore, island.getUpgrades().getGeneratorLevel() + 1, ChatColor.AQUA);
                generatorLore.add(ChatColor.GOLD + "Cost: $" + generatorCost);
            }

            generatorLore.add(ChatColor.YELLOW + "Left: Upgrade | Right: Toggle Ore");
            gui.setItem(guiConfig.getInt("upgrades.slots.generator", 15), createItem(Material.FURNACE, ChatColor.AQUA + "Generator Rate",
                    generatorLore.toArray(new String[0])));
        }

        gui.setItem(guiConfig.getInt("upgrades.slots.back", 22),
                createItem(Material.getMaterial(guiConfig.getString("general.back-button-material", "RED_STAINED_GLASS_PANE")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-name", "&cBack")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-lore", "&7Return to main menu"))));
        player.openInventory(gui);
    }

    private void addOrePercentages(List<String> lore, int tier, ChatColor oreColor) {
        FileConfiguration config = plugin.getConfig("generator.yml");
        String tierKey = "generator.ores.tier" + tier;
        List<Map<?, ?>> materialChances = config.getMapList(tierKey);

        // Add cobblestone as a default if not present (matches GeneratorListener logic)
        boolean hasCobblestone = materialChances.stream().anyMatch(m -> "COBBLESTONE".equals(m.get("ore")));
        if (!hasCobblestone) {
            Map<String, Object> cobbleEntry = new HashMap<>();
            cobbleEntry.put("ore", "COBBLESTONE");
            cobbleEntry.put("weight", 10.0);
            materialChances.add(cobbleEntry);
        }

        double totalWeight = materialChances.stream().mapToDouble(m -> ((Number) m.get("weight")).doubleValue()).sum();
        for (Map<?, ?> material : materialChances) {
            String materialName = ((String) material.get("ore")).toLowerCase().replace("_", " ");
            double weight = ((Number) material.get("weight")).doubleValue();
            double percentage = (weight / totalWeight) * 100;
            lore.add(oreColor + "- " + materialName + ": " + String.format("%.1f%%", percentage));
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            // Fallback in case getItemMeta returns null
            meta = Bukkit.getItemFactory().getItemMeta(material);
            if (meta == null) {
                plugin.getLogger().warning("Failed to create ItemMeta for material: " + material.name());
                return item; // Return item without meta if all else fails
            }
        }
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}