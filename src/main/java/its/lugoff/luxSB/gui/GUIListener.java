package its.lugoff.luxSB.gui;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import its.lugoff.luxSB.missions.Mission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration; // Added missing import
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {
    private final LuxSB plugin;
    private final UpgradesGUI upgradesGUI;
    private final MembersGUI membersGUI;
    private final MissionsGUI missionsGUI;
    private final ShopsGUI shopsGUI;
    private final IslandGUI islandGUI;
    private final HeadShopGUI headShopGUI;

    public GUIListener(LuxSB plugin) {
        this.plugin = plugin;
        this.upgradesGUI = new UpgradesGUI(plugin);
        this.membersGUI = new MembersGUI(plugin);
        this.missionsGUI = new MissionsGUI(plugin);
        this.shopsGUI = new ShopsGUI(plugin);
        this.islandGUI = new IslandGUI(plugin);
        this.headShopGUI = new HeadShopGUI(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        String title = event.getView().getTitle();

        if (inventory == null || !isPluginGUI(title)) {
            plugin.getLogger().info("Ignoring click in non-plugin GUI: '" + title + "' for " + player.getName());
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            plugin.getLogger().info("Ignoring click: No item or metadata in '" + title + "' for " + player.getName());
            return;
        }

        String itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        String backButtonName = "Back";

        plugin.getLogger().info("Player " + player.getName() + " clicked '" + itemName + "' in '" + title + "'");

        String islandMenuTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("island-menu.title", "&3✦ LuxSB Island Menu ✦"));
        String schematicTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("schematic-selection.title", "&3✦ Choose Island Schematic ✦"));
        String warpsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("warps.title", "&d✦ Island Warps ✦"));
        String upgradesTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("upgrades.title", "&b✦ Island Upgrades ✦"));
        String missionsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("missions.title", "&d✦ Island Missions ✦"));

        if (title.equals(islandMenuTitle)) {
            if (itemName.equals("Home") && island != null) {
                island.teleportPlayer(player);
                player.sendMessage(ChatColor.GREEN + "Teleported to your island!");
                player.closeInventory();
            } else if (itemName.equals("Upgrades")) {
                try {
                    upgradesGUI.openGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Upgrades GUI!");
                    plugin.getLogger().severe("Error opening Upgrades GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Members")) {
                try {
                    membersGUI.openGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Members GUI!");
                    plugin.getLogger().severe("Error opening Members GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Missions")) {
                try {
                    missionsGUI.openGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Missions GUI!");
                    plugin.getLogger().severe("Error opening Missions GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Island Bank")) {
                try {
                    plugin.getBankGUI().openGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Bank GUI!");
                    plugin.getLogger().severe("Error opening Bank GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Shops")) {
                try {
                    shopsGUI.openGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Shops GUI!");
                    plugin.getLogger().severe("Error opening Shops GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Warps")) {
                try {
                    islandGUI.openWarpsGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Warps GUI!");
                    plugin.getLogger().severe("Error opening Warps GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Shared Islands")) {
                try {
                    islandGUI.openSharedIslandsGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Shared Islands GUI!");
                    plugin.getLogger().severe("Error opening Shared Islands GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Delete Island") && island != null) {
                double balance = island.getBalance();
                if (balance > 0) plugin.getEconomyManager().depositPlayer(player, balance);
                plugin.getIslandManager().removeIsland(island);
                plugin.getIslandManager().getIslands().remove(player.getUniqueId());
                File file = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".json");
                if (file.exists()) file.delete();
                player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
                player.sendMessage(ChatColor.RED + "Island deleted! Refunded $" + String.format("%.2f", balance));
                player.closeInventory();
                try {
                    islandGUI.openSchematicSelectionGUI(player);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to open Schematic Selection GUI!");
                    plugin.getLogger().severe("Error opening Schematic GUI for " + player.getName() + ": " + e.getMessage());
                }
            } else if (itemName.equals("Side Islands")) {
                islandGUI.openSideIslandsGUI(player);
            } else if (itemName.equals("Head Shop")) {
                headShopGUI.openGUI(player);
            }
        } else if (title.equals(schematicTitle)) {
            if (itemName.equals(backButtonName)) {
                player.closeInventory();
                plugin.getLogger().info("Closed Schematic Selection GUI for " + player.getName());
            } else {
                List<Map<?, ?>> schematics = plugin.getConfig().getMapList("islands.schematics");
                boolean schematicFound = false;
                for (Map<?, ?> schematic : schematics) {
                    String schematicName = (String) schematic.get("name");
                    String displayName = schematicName.replace("_", " ");
                    plugin.getLogger().info("Comparing '" + itemName + "' with schematic '" + displayName + "'");
                    if (itemName.equals(displayName)) {
                        schematicFound = true;
                        double cost = schematic.containsKey("cost") ? ((Number) schematic.get("cost")).doubleValue() : 0.0;
                        if (cost > 0 && !plugin.getEconomyManager().withdrawPlayer(player, cost)) {
                            player.sendMessage(ChatColor.RED + "You need $" + String.format("%.2f", cost) + " to create this island!");
                            player.closeInventory();
                            plugin.getLogger().info("Insufficient funds for " + player.getName() + " to create '" + schematicName + "'");
                            return;
                        }
                        try {
                            island = plugin.getIslandManager().createIsland(player, schematicName);
                            player.closeInventory();
                            player.sendMessage(ChatColor.GREEN + "Island created with " + itemName + "! You've been teleported.");
                            plugin.getLogger().info("Created island '" + schematicName + "' for " + player.getName());
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Failed to create island '" + itemName + "'!");
                            plugin.getLogger().severe("Error creating island '" + schematicName + "' for " + player.getName() + ": " + e.getMessage());
                        }
                        break;
                    }
                }
                if (!schematicFound) {
                    plugin.getLogger().warning("No matching schematic found for item '" + itemName + "' clicked by " + player.getName());
                }
            }
        } else if (title.equals(ChatColor.AQUA + "✦ Side Islands ✦")) {
            if (itemName.equals(backButtonName)) {
                islandGUI.openGUI(player);
            } else {
                List<Map<?, ?>> sideIslands = plugin.getConfig().getMapList("islands.side-islands");
                for (Map<?, ?> sideIsland : sideIslands) {
                    String sideName = (String) sideIsland.get("name");
                    String displayName = sideName.replace("_", " ");
                    if (itemName.equals(displayName)) {
                        double cost = sideIsland.containsKey("cost") ? ((Number) sideIsland.get("cost")).doubleValue() : 0.0;
                        if (cost > 0 && !plugin.getEconomyManager().withdrawPlayer(player, cost)) {
                            player.sendMessage(ChatColor.RED + "You need $" + String.format("%.2f", cost) + " to purchase this side island!");
                            player.closeInventory();
                            return;
                        }
                        ItemStack placementHoe = new ItemStack(Material.DIAMOND_HOE);
                        ItemMeta meta = placementHoe.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.AQUA + "Side Island Placer: " + displayName);
                            meta.setLore(Arrays.asList(ChatColor.GRAY + "Right-click a block to place " + displayName));
                            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "side_island"), PersistentDataType.STRING, sideName);
                            placementHoe.setItemMeta(meta);
                        }
                        player.getInventory().addItem(placementHoe);
                        player.sendMessage(ChatColor.GREEN + "Purchased " + itemName + "! Use the hoe to place it.");
                        player.closeInventory();
                        break;
                    }
                }
            }
        } else if (title.equals(ChatColor.DARK_PURPLE + "✦ Head Shop ✦")) {
            if (itemName.equals(backButtonName)) {
                islandGUI.openGUI(player);
            } else {
                FileConfiguration config = plugin.getConfig("shops.yml");
                List<Map<?, ?>> heads = config.getMapList("shops.head-shop.items");
                for (Map<?, ?> head : heads) {
                    String displayName = (String) head.get("display-name");
                    if (itemName.equals(displayName)) {
                        int tokenBuyCost = head.containsKey("token-buy-cost") ? ((Number) head.get("token-buy-cost")).intValue() : 0;
                        int tokenSellCost = head.containsKey("token-sell-cost") ? ((Number) head.get("token-sell-cost")).intValue() : 0;
                        Material material = Material.getMaterial((String) head.get("material"));

                        if (event.getClick() == ClickType.LEFT && tokenBuyCost > 0) {
                            if (plugin.removeHeadTokens(player.getUniqueId(), tokenBuyCost)) {
                                ItemStack headItem = new ItemStack(material);
                                player.getInventory().addItem(headItem);
                                player.sendMessage(ChatColor.GREEN + "Purchased " + itemName + " for " + tokenBuyCost + " Head Tokens!");
                                player.closeInventory();
                            } else {
                                player.sendMessage(ChatColor.RED + "You need " + tokenBuyCost + " Head Tokens to buy " + itemName + "!");
                            }
                        } else if (event.getClick() == ClickType.RIGHT && tokenSellCost > 0) {
                            ItemStack toSell = new ItemStack(material);
                            if (player.getInventory().containsAtLeast(toSell, 1)) {
                                player.getInventory().removeItem(toSell);
                                plugin.addHeadTokens(player.getUniqueId(), tokenSellCost);
                                player.sendMessage(ChatColor.GREEN + "Sold " + itemName + " for " + tokenSellCost + " Head Tokens!");
                                player.closeInventory();
                            } else {
                                player.sendMessage(ChatColor.RED + "You don’t have a " + itemName + " to sell!");
                            }
                        }
                        break;
                    }
                }
            }
        } else if (title.equals(warpsTitle)) {
            if (itemName.equals("Set Warp") && island != null) {
                double warpCost = plugin.getConfig().getDouble("islands.warps.warp-cost", 50.0);
                if (warpCost > 0 && !plugin.getEconomyManager().withdrawPlayer(player, warpCost)) {
                    player.sendMessage(ChatColor.RED + "You need $" + String.format("%.2f", warpCost) + " to set a warp!");
                } else {
                    island.setWarpLocation(player.getLocation());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(ChatColor.GREEN + "Warp set at your location!");
                }
                islandGUI.openWarpsGUI(player);
            } else if (itemName.equals("Toggle Warp") && island != null && island.getWarpLocation() != null) {
                boolean allowPrivate = plugin.getConfig().getBoolean("islands.warps.allow-private", true);
                if (allowPrivate) {
                    island.setWarpPublic(!island.isWarpPublic());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(ChatColor.GREEN + "Warp set to " + (island.isWarpPublic() ? "public" : "private") + "!");
                    islandGUI.openWarpsGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Private warps are disabled!");
                }
            } else if (itemName.equals("Visit Warps")) {
                islandGUI.openVisitWarpsGUI(player);
            } else if (itemName.equals(backButtonName)) {
                try {
                    islandGUI.openGUI(player);
                    plugin.getLogger().info("Returned " + player.getName() + " to Island GUI from Warps");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to return to Island GUI!");
                    plugin.getLogger().severe("Error returning " + player.getName() + " to Island GUI from Warps: " + e.getMessage());
                }
            }
        } else if (title.equals(ChatColor.BLUE + "✦ Visit Warps ✦")) {
            if (itemName.equals(backButtonName)) {
                islandGUI.openWarpsGUI(player);
            } else {
                for (Map.Entry<UUID, Island> entry : plugin.getIslandManager().getIslands().entrySet()) {
                    Island targetIsland = entry.getValue();
                    String ownerName = plugin.getServer().getOfflinePlayer(targetIsland.getOwner()).getName();
                    if (itemName.equals(ownerName + "'s Warp") && targetIsland.getWarpLocation() != null && targetIsland.isWarpPublic()) {
                        player.teleport(targetIsland.getWarpLocation());
                        player.sendMessage(ChatColor.GREEN + "Teleported to " + ownerName + "'s warp!");
                        player.closeInventory();
                        break;
                    }
                }
            }
        } else if (title.equals(ChatColor.YELLOW + "✦ Shared Islands ✦")) {
            if (itemName.equals(backButtonName)) {
                islandGUI.openGUI(player);
            } else {
                for (Map.Entry<UUID, Island> entry : plugin.getIslandManager().getIslands().entrySet()) {
                    Island sharedIsland = entry.getValue();
                    String ownerName = plugin.getServer().getOfflinePlayer(sharedIsland.getOwner()).getName();
                    if (itemName.equals(ownerName + "'s Island") && sharedIsland.getMembers().contains(player.getUniqueId()) && !sharedIsland.getOwner().equals(player.getUniqueId())) {
                        if (event.getClick() == ClickType.LEFT) {
                            player.teleport(sharedIsland.getCenter().clone().add(0.5, 1, 0.5));
                            player.sendMessage(ChatColor.GREEN + "Teleported to " + ownerName + "'s island!");
                            player.closeInventory();
                        } else if (event.getClick() == ClickType.RIGHT) {
                            sharedIsland.removeMember(player.getUniqueId());
                            plugin.getIslandManager().saveIsland(sharedIsland);
                            player.sendMessage(ChatColor.RED + "You have left " + ownerName + "'s island!");
                            islandGUI.openSharedIslandsGUI(player);
                        }
                        break;
                    }
                }
            }
        } else if (title.equals(upgradesTitle)) {
            if (itemName.equals("Island Size") && island != null && !island.getUpgrades().isMaxSize()) {
                double cost = island.getUpgrades().getSizeLevel() * plugin.getConfig().getDouble("islands.upgrades.size-cost-multiplier", 1000.0);
                if (plugin.getEconomyManager().withdrawPlayer(player, cost)) {
                    island.getUpgrades().setSizeLevel(island.getUpgrades().getSizeLevel() + 1);
                    island.setSize(island.getUpgrades().getMaxSize());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(ChatColor.GREEN + "Upgraded island size to level " + island.getUpgrades().getSizeLevel() + "!");
                    upgradesGUI.openGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Not enough money! Need $" + cost);
                }
            } else if (itemName.equals("Generator Rate") && island != null) {
                if (event.getClick() == ClickType.LEFT && !island.getUpgrades().isMaxGenerator()) {
                    double cost = island.getUpgrades().getGeneratorLevel() * plugin.getConfig().getDouble("islands.upgrades.generator-cost-multiplier", 1500.0);
                    if (plugin.getEconomyManager().withdrawPlayer(player, cost)) {
                        island.getUpgrades().setGeneratorLevel(island.getUpgrades().getGeneratorLevel() + 1);
                        plugin.getIslandManager().saveIsland(island);
                        player.sendMessage(ChatColor.GREEN + "Upgraded generator rate to level " + island.getUpgrades().getGeneratorLevel() + "!");
                        upgradesGUI.openGUI(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough money! Need $" + cost);
                    }
                } else if (event.getClick() == ClickType.RIGHT) {
                    boolean currentState = island.getUpgrades().isOreSpawningEnabled();
                    island.getUpgrades().setOreSpawningEnabled(!currentState);
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(ChatColor.GREEN + "Ore spawning " + (!currentState ? "enabled" : "disabled") + "!");
                    upgradesGUI.openGUI(player);
                }
            } else if (itemName.equals(backButtonName)) {
                try {
                    islandGUI.openGUI(player);
                    plugin.getLogger().info("Returned " + player.getName() + " to Island GUI from Upgrades");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to return to Island GUI!");
                    plugin.getLogger().severe("Error returning " + player.getName() + " to Island GUI from Upgrades: " + e.getMessage());
                }
            }
        } else if (title.equals(ChatColor.YELLOW + "✦ Island Members ✦")) {
            if (itemName.startsWith("Owner:") || itemName.equals("Invite Player")) {
                if (island == null || !island.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "Only the island owner can perform this action!");
                    return;
                }
                if (itemName.equals("Invite Player")) {
                    player.sendMessage(ChatColor.YELLOW + "Use /island invite <player> to invite someone!");
                }
            } else if (itemName.equals(backButtonName)) {
                islandGUI.openGUI(player);
            } else if (island != null && island.getOwner().equals(player.getUniqueId())) {
                for (UUID member : island.getMembers()) {
                    String memberName = plugin.getServer().getOfflinePlayer(member).getName();
                    if (memberName != null && itemName.equals(memberName)) {
                        island.removeMember(member);
                        plugin.getIslandManager().saveIsland(island);
                        player.sendMessage(ChatColor.GREEN + "Kicked " + memberName + " from your island!");
                        membersGUI.openGUI(player);
                        break;
                    }
                }
            }
        } else if (title.equals(missionsTitle)) {
            if (itemName.equals(backButtonName)) {
                try {
                    islandGUI.openGUI(player);
                    plugin.getLogger().info("Returned " + player.getName() + " to Island GUI from Missions");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to return to Island GUI!");
                    plugin.getLogger().severe("Error returning " + player.getName() + " to Island GUI from Missions: " + e.getMessage());
                }
            } else {
                for (Mission mission : plugin.getMissionManager().getMissions().values()) {
                    if (itemName.equals(mission.getName())) {
                        plugin.getMissionManager().startMission(player, mission.getName());
                        player.sendMessage(ChatColor.GREEN + "Started mission: " + mission.getName());
                        player.closeInventory();
                        break;
                    }
                }
            }
        } else if (title.equals(ChatColor.GREEN + "✦ Island Shops ✦")) {
            if (itemName.equals(backButtonName)) {
                islandGUI.openGUI(player);
            } else {
                shopsGUI.openCategory(player, itemName);
            }
        } else if (title.startsWith(ChatColor.GREEN + "✦ Island Shops - ")) {
            String category = title.replace(ChatColor.GREEN + "✦ Island Shops - ", "");
            if (itemName.equals(backButtonName)) {
                shopsGUI.openGUI(player);
            } else {
                ConfigurationSection items = plugin.getConfig("shops.yml").getConfigurationSection("shops." + category.toLowerCase().replace(" ", "_") + ".items");
                if (items != null) {
                    for (String key : items.getKeys(false)) {
                        if (itemName.equals(key.replace("_", " "))) {
                            Material material = Material.getMaterial(items.getString(key + ".material", "STONE"));
                            int amount = items.getInt(key + ".amount", 1);
                            double buyPrice = items.getDouble(key + ".buy-price", 0.0);
                            double sellPrice = items.getDouble(key + ".sell-price", 0.0);

                            if (event.getClick() == ClickType.LEFT && buyPrice > 0) {
                                if (island != null && island.getBalance() >= buyPrice) {
                                    island.setBalance(island.getBalance() - buyPrice);
                                    player.getInventory().addItem(new ItemStack(material, amount));
                                    plugin.getIslandManager().saveIsland(island);
                                    player.sendMessage(ChatColor.GREEN + "Bought " + amount + " " + material.name().toLowerCase() + " for $" + String.format("%.2f", buyPrice));
                                } else {
                                    player.sendMessage(ChatColor.RED + "Not enough funds in island bank! Need $" + String.format("%.2f", buyPrice));
                                }
                            } else if (event.getClick() == ClickType.RIGHT && sellPrice > 0) {
                                ItemStack toSell = new ItemStack(material, amount);
                                if (player.getInventory().containsAtLeast(toSell, amount)) {
                                    player.getInventory().removeItem(toSell);
                                    if (island != null) {
                                        island.setBalance(island.getBalance() + sellPrice);
                                        plugin.getIslandManager().saveIsland(island);
                                    }
                                    player.sendMessage(ChatColor.GREEN + "Sold " + amount + " " + material.name().toLowerCase() + " for $" + String.format("%.2f", sellPrice));
                                } else {
                                    player.sendMessage(ChatColor.RED + "You don’t have " + amount + " " + material.name().toLowerCase() + " to sell!");
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } else if (title.equals(ChatColor.GOLD + "✦ Island Bank ✦")) {
            if (island == null) {
                player.sendMessage(ChatColor.RED + "You need an island to use the bank!");
                player.closeInventory();
                return;
            }
            BankGUI bankGUI = plugin.getBankGUI();
            switch (itemName) {
                case "Increase":
                    if (event.getClick() == ClickType.LEFT) bankGUI.adjustAmount(player, 10);
                    else if (event.getClick() == ClickType.RIGHT) bankGUI.adjustAmount(player, 100);
                    else if (event.getClick() == ClickType.SHIFT_LEFT) bankGUI.adjustAmount(player, 1000);
                    else if (event.getClick() == ClickType.SHIFT_RIGHT) bankGUI.adjustAmount(player, 10000);
                    bankGUI.openGUI(player);
                    break;
                case "Decrease":
                    if (event.getClick() == ClickType.LEFT) bankGUI.adjustAmount(player, -10);
                    else if (event.getClick() == ClickType.RIGHT) bankGUI.adjustAmount(player, -100);
                    else if (event.getClick() == ClickType.SHIFT_LEFT) bankGUI.adjustAmount(player, -1000);
                    else if (event.getClick() == ClickType.SHIFT_RIGHT) bankGUI.adjustAmount(player, -10000);
                    bankGUI.openGUI(player);
                    break;
                case "Deposit":
                    int depositAmount = bankGUI.getSelectedAmount(player);
                    if (depositAmount <= 0) {
                        player.sendMessage(ChatColor.RED + "Select an amount greater than 0!");
                        return;
                    }
                    if (plugin.getEconomyManager().withdrawPlayer(player, depositAmount)) {
                        island.setBalance(island.getBalance() + depositAmount);
                        plugin.getIslandManager().saveIsland(island);
                        player.sendMessage(ChatColor.GREEN + "Deposited $" + depositAmount + " to island bank!");
                        bankGUI.adjustAmount(player, -depositAmount);
                        bankGUI.openGUI(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough money!");
                    }
                    break;
                case "Withdraw":
                    int withdrawAmount = bankGUI.getSelectedAmount(player);
                    if (withdrawAmount <= 0) {
                        player.sendMessage(ChatColor.RED + "Select an amount greater than 0!");
                        return;
                    }
                    if (island.getBalance() >= withdrawAmount) {
                        island.setBalance(island.getBalance() - withdrawAmount);
                        plugin.getEconomyManager().depositPlayer(player, withdrawAmount);
                        plugin.getIslandManager().saveIsland(island);
                        player.sendMessage(ChatColor.GREEN + "Withdrew $" + withdrawAmount + " from island bank!");
                        bankGUI.openGUI(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough funds in island bank!");
                    }
                    break;
                case "Back":
                    islandGUI.openGUI(player);
                    break;
            }
        }
    }

    private boolean isPluginGUI(String title) {
        String islandMenuTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("island-menu.title", "&3✦ LuxSB Island Menu ✦"));
        String schematicTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("schematic-selection.title", "&3✦ Choose Island Schematic ✦"));
        String warpsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("warps.title", "&d✦ Island Warps ✦"));
        String visitWarpsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("visit-warps.title", "&b✦ Visit Warps ✦"));
        String sharedIslandsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("shared-islands.title", "&e✦ Shared Islands ✦"));
        String upgradesTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("upgrades.title", "&b✦ Island Upgrades ✦"));
        String membersTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("members.title", "&e✦ Island Members ✦"));
        String missionsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("missions.title", "&d✦ Island Missions ✦"));
        String shopsTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("shops.title", "&a✦ Island Shops ✦"));
        String bankTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig("gui.yml").getString("bank.title", "&6✦ Island Bank ✦"));

        return title.equals(islandMenuTitle) ||
                title.equals(schematicTitle) ||
                title.equals(warpsTitle) ||
                title.equals(visitWarpsTitle) ||
                title.equals(sharedIslandsTitle) ||
                title.equals(upgradesTitle) ||
                title.equals(membersTitle) ||
                title.equals(missionsTitle) ||
                title.equals(shopsTitle) ||
                title.startsWith(ChatColor.GREEN + "✦ Island Shops - ") ||
                title.equals(bankTitle) ||
                title.equals(ChatColor.AQUA + "✦ Side Islands ✦") ||
                title.equals(ChatColor.DARK_PURPLE + "✦ Head Shop ✦");
    }
}