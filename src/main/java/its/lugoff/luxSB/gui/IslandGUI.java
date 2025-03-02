package its.lugoff.luxSB.gui;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import its.lugoff.luxSB.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandGUI {
    private final LuxSB plugin;
    private final IslandManager islandManager;

    public IslandGUI(LuxSB plugin) {
        this.plugin = plugin;
        this.islandManager = plugin.getIslandManager();
    }

    public void openGUI(Player player) {
        Island island = islandManager.getIsland(player.getUniqueId());
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = 54;
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("island-menu.title", "&3✦ LuxSB Island Menu ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, ChatColor.GRAY + " ");
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            gui.setItem(i, border);
        }

        if (island == null) {
            openSchematicSelectionGUI(player);
        } else {
            gui.setItem(10, createItem(Material.COMPASS, ChatColor.GREEN + "Home",
                    ChatColor.GRAY + "Teleport to your island",
                    ChatColor.YELLOW + "Location: " + formatLocation(island.getCenter())));
            gui.setItem(12, createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + "Members",
                    ChatColor.GRAY + "Manage your island team",
                    ChatColor.YELLOW + "Members: " + island.getMembers().size()));
            gui.setItem(14, createItem(Material.ANVIL, ChatColor.AQUA + "Upgrades",
                    ChatColor.GRAY + "Enhance your island",
                    ChatColor.YELLOW + "Size: " + island.getUpgrades().getSizeLevel(),
                    ChatColor.YELLOW + "Generator: " + island.getUpgrades().getGeneratorLevel()));
            gui.setItem(16, createItem(Material.END_PORTAL_FRAME, ChatColor.LIGHT_PURPLE + "Warps",
                    ChatColor.GRAY + "Manage and visit warps"));

            gui.setItem(19, createItem(Material.GOLD_INGOT, ChatColor.GOLD + "Island Bank",
                    ChatColor.GRAY + "Manage your funds",
                    ChatColor.YELLOW + "Balance: $" + String.format("%.2f", island.getBalance())));
            gui.setItem(21, createItem(Material.BOOK, ChatColor.LIGHT_PURPLE + "Missions",
                    ChatColor.GRAY + "View available missions"));
            gui.setItem(23, createItem(Material.EMERALD, ChatColor.GREEN + "Shops",
                    ChatColor.GRAY + "Buy and sell items"));
            gui.setItem(25, createItem(Material.NAME_TAG, ChatColor.YELLOW + "Shared Islands",
                    ChatColor.GRAY + "View islands you’re invited to"));

            gui.setItem(28, createItem(Material.GRASS_BLOCK, ChatColor.AQUA + "Side Islands",
                    ChatColor.GRAY + "Purchase extensions for your island"));
            gui.setItem(30, createItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Boosts",
                    ChatColor.GRAY + "Activate temporary enhancements"));
            gui.setItem(34, createItem(Material.TNT, ChatColor.RED + "Delete Island",
                    ChatColor.GRAY + "Permanently remove your island",
                    ChatColor.YELLOW + "Refunds: $" + String.format("%.2f", island.getBalance())));

            player.openInventory(gui);
        }
    }

    public void openSchematicSelectionGUI(Player player) {
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("schematic-selection.size", 36);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("schematic-selection.title", "&3✦ Choose Island Schematic ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.getMaterial(guiConfig.getString("general.border-material", "CYAN_STAINED_GLASS_PANE")),
                ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.border-name", "&7 ")));
        for (int i = 0; i < size; i++) {
            if (i < 9 || i % 9 == 0 || i % 9 == 8 || i > 26) {
                gui.setItem(i, border);
            }
        }

        List<Map<?, ?>> schematics = plugin.getConfig().getMapList("islands.schematics");
        List<Integer> slots = guiConfig.getIntegerList("schematic-selection.schematic-slots");
        int index = 0;
        for (Map<?, ?> schematic : schematics) {
            if (index >= slots.size()) break;
            String name = (String) schematic.get("name");
            String description = schematic.containsKey("description") ? (String) schematic.get("description") : "A Skyblock island";
            double cost = schematic.containsKey("cost") ? ((Number) schematic.get("cost")).doubleValue() : 0.0;
            String iconStr = schematic.containsKey("icon") ? (String) schematic.get("icon") : "GRASS_BLOCK";
            Material icon = Material.getMaterial(iconStr) != null ? Material.getMaterial(iconStr) : Material.GRASS_BLOCK;

            gui.setItem(slots.get(index++), createItem(icon, ChatColor.GREEN + name.replace("_", " "),
                    ChatColor.GRAY + description,
                    cost > 0 ? ChatColor.YELLOW + "Cost: $" + String.format("%.2f", cost) : ChatColor.GREEN + "Free",
                    ChatColor.YELLOW + "Click to create"));
        }

        gui.setItem(guiConfig.getInt("schematic-selection.back-slot", 31),
                createItem(Material.getMaterial(guiConfig.getString("general.back-button-material", "RED_STAINED_GLASS_PANE")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-name", "&cBack")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-lore", "&7Return to previous menu"))));
        player.openInventory(gui);
    }

    public void openSideIslandsGUI(Player player) {
        Island island = islandManager.getIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.RED + "You need an island to purchase side islands!");
            player.closeInventory();
            return;
        }

        int size = 27; // Smaller symmetrical GUI
        String title = ChatColor.AQUA + "✦ Side Islands ✦";
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, ChatColor.GRAY + " ");
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}) {
            gui.setItem(i, border);
        }

        List<Map<?, ?>> sideIslands = plugin.getConfig().getMapList("islands.side-islands");
        int[] slots = {10, 12, 14, 16}; // 4 centered slots
        int index = 0;
        for (Map<?, ?> sideIsland : sideIslands) {
            if (index >= slots.length) break;
            String name = (String) sideIsland.get("name");
            String displayName = name.replace("_", " ");
            double cost = sideIsland.containsKey("cost") ? ((Number) sideIsland.get("cost")).doubleValue() : 0.0;
            String iconStr = sideIsland.containsKey("icon") ? (String) sideIsland.get("icon") : "STONE";
            Material icon = Material.getMaterial(iconStr) != null ? Material.getMaterial(iconStr) : Material.STONE;

            gui.setItem(slots[index++], createItem(icon, ChatColor.AQUA + displayName,
                    ChatColor.GRAY + "Add to your island",
                    ChatColor.YELLOW + "Cost: $" + String.format("%.2f", cost),
                    ChatColor.GRAY + "Click to purchase"));
        }

        gui.setItem(22, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Back",
                ChatColor.GRAY + "Return to Island Menu"));
        player.openInventory(gui);
    }

    public void openWarpsGUI(Player player) {
        Island island = islandManager.getIsland(player.getUniqueId());
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("warps.size", 27);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("warps.title", "&d✦ Island Warps ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.getMaterial(guiConfig.getString("warps.border-material", "PURPLE_STAINED_GLASS_PANE")),
                ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.border-name", "&7 ")));
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26}) {
            gui.setItem(i, border);
        }

        if (island != null) {
            gui.setItem(guiConfig.getInt("warps.slots.set-warp", 11), createItem(Material.COMPASS, ChatColor.GREEN + "Set Warp",
                    ChatColor.GRAY + "Set your island warp location",
                    island.getWarpLocation() != null ? ChatColor.YELLOW + "Current: Set" : ChatColor.RED + "Not Set",
                    ChatColor.YELLOW + "Cost: $" + plugin.getConfig().getDouble("islands.warps.warp-cost", 50.0)));
            gui.setItem(guiConfig.getInt("warps.slots.toggle-warp", 13), createItem(Material.LEVER, ChatColor.YELLOW + "Toggle Warp",
                    ChatColor.GRAY + "Make your warp public or private",
                    island.getWarpLocation() != null ?
                            (island.isWarpPublic() ? ChatColor.GREEN + "Public" : ChatColor.RED + "Private") :
                            ChatColor.GRAY + "Set a warp first"));
            gui.setItem(guiConfig.getInt("warps.slots.visit-warps", 15), createItem(Material.ENDER_PEARL, ChatColor.AQUA + "Visit Warps",
                    ChatColor.GRAY + "Teleport to other players' warps"));
        }

        gui.setItem(guiConfig.getInt("warps.slots.back", 22),
                createItem(Material.getMaterial(guiConfig.getString("general.back-button-material", "RED_STAINED_GLASS_PANE")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-name", "&cBack")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-lore", "&7Return to main menu"))));
        player.openInventory(gui);
    }

    public void openVisitWarpsGUI(Player player) {
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("visit-warps.size", 54);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("visit-warps.title", "&b✦ Visit Warps ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.getMaterial(guiConfig.getString("general.border-material", "CYAN_STAINED_GLASS_PANE")),
                ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.border-name", "&7 ")));
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53}) {
            gui.setItem(i, border);
        }

        List<Integer> slots = guiConfig.getIntegerList("visit-warps.warp-slots");
        int index = 0;
        for (Map.Entry<UUID, Island> entry : islandManager.getIslands().entrySet()) {
            Island targetIsland = entry.getValue();
            if (targetIsland.getWarpLocation() != null && targetIsland.isWarpPublic()) {
                if (index >= slots.size()) break;
                String ownerName = plugin.getServer().getOfflinePlayer(targetIsland.getOwner()).getName();
                gui.setItem(slots.get(index++), createItem(Material.ENDER_EYE, ChatColor.GREEN + ownerName + "'s Warp",
                        ChatColor.GRAY + "Teleport to " + ownerName + "'s island warp",
                        ChatColor.YELLOW + "Click to visit"));
            }
        }

        gui.setItem(guiConfig.getInt("visit-warps.back-slot", 49),
                createItem(Material.getMaterial(guiConfig.getString("general.back-button-material", "RED_STAINED_GLASS_PANE")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-name", "&cBack")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-lore", "&7Return to warps menu"))));
        player.openInventory(gui);
    }

    public void openSharedIslandsGUI(Player player) {
        FileConfiguration guiConfig = plugin.getConfig("gui.yml");
        int size = guiConfig.getInt("shared-islands.size", 54);
        String title = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("shared-islands.title", "&e✦ Shared Islands ✦"));
        Inventory gui = Bukkit.createInventory(player, size, title);

        ItemStack border = createItem(Material.getMaterial(guiConfig.getString("shared-islands.border-material", "YELLOW_STAINED_GLASS_PANE")),
                ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.border-name", "&7 ")));
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53}) {
            gui.setItem(i, border);
        }

        List<Integer> slots = guiConfig.getIntegerList("shared-islands.shared-slots");
        int index = 0;
        for (Map.Entry<UUID, Island> entry : islandManager.getIslands().entrySet()) {
            Island sharedIsland = entry.getValue();
            if (sharedIsland.getMembers().contains(player.getUniqueId()) && !sharedIsland.getOwner().equals(player.getUniqueId())) {
                if (index >= slots.size()) break;
                String ownerName = plugin.getServer().getOfflinePlayer(sharedIsland.getOwner()).getName();
                gui.setItem(slots.get(index++), createItem(Material.PLAYER_HEAD, ChatColor.YELLOW + ownerName + "'s Island",
                        ChatColor.GRAY + "Click to manage your membership",
                        ChatColor.YELLOW + "Left: Teleport | Right: Leave"));
            }
        }

        gui.setItem(guiConfig.getInt("shared-islands.back-slot", 49),
                createItem(Material.getMaterial(guiConfig.getString("general.back-button-material", "RED_STAINED_GLASS_PANE")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-name", "&cBack")),
                        ChatColor.translateAlternateColorCodes('&', guiConfig.getString("general.back-button-lore", "&7Return to main menu"))));
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

    private String formatLocation(Location loc) {
        return "X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ();
    }
}