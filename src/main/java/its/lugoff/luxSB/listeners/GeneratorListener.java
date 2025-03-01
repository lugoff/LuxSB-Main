package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneratorListener implements Listener {
    private final LuxSB plugin;
    private final Random random;
    private final Map<Location, Material> nextOre;
    private final FileConfiguration config;

    public GeneratorListener(LuxSB plugin) {
        this.plugin = plugin;
        this.random = new Random(); // Single instance, reseeded per break
        this.nextOre = new HashMap<>();
        this.config = plugin.getConfig("generator.yml");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();
        if (blockType != Material.COBBLESTONE && !isOre(blockType)) return;

        event.setDropItems(false);
        ItemStack drop = getDropForBlock(blockType);
        player.getInventory().addItem(drop);
        // Play pickup sound at block location
        player.getWorld().playSound(block.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 1.0f);

        if (!isCobblestoneGenerator(block)) return;

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) return;

        double oreChance = island.getUpgrades().getGeneratorRate(); // 0.1, 0.2, 0.3
        if (!island.getUpgrades().isOreSpawningEnabled() || random.nextDouble() >= oreChance) {
            // No ore spawn, cobblestone stays
            return;
        }

        int generatorLevel = island.getUpgrades().getGeneratorLevel();
        Material nextMaterial = getMaterialForTier(generatorLevel);

        Location blockLoc = block.getLocation();
        nextOre.put(blockLoc, nextMaterial);

        new BukkitRunnable() {
            int attempts = 0;
            @Override
            public void run() {
                attempts++;
                Material currentType = block.getType();
                if (currentType == Material.COBBLESTONE && nextOre.containsKey(blockLoc)) {
                    Material queuedMaterial = nextOre.remove(blockLoc);
                    block.setType(queuedMaterial);
                    cancel();
                } else if (attempts >= 10) {
                    nextOre.remove(blockLoc);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 5L, 2L);
    }

    private boolean isCobblestoneGenerator(Block block) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        boolean hasWater = false;
        boolean hasLava = false;

        for (BlockFace face : faces) {
            Block adjacent = block.getRelative(face);
            Material type = adjacent.getType();
            if (type == Material.WATER) hasWater = true;
            else if (type == Material.LAVA) hasLava = true;
        }

        return hasWater && hasLava;
    }

    private boolean isOre(Material material) {
        return material == Material.COAL_ORE || material == Material.COPPER_ORE ||
                material == Material.IRON_ORE || material == Material.GOLD_ORE ||
                material == Material.DIAMOND_ORE || material == Material.EMERALD_ORE ||
                material == Material.LAPIS_ORE || material == Material.REDSTONE_ORE ||
                material == Material.NETHER_QUARTZ_ORE;
    }

    private ItemStack getDropForBlock(Material blockType) {
        return switch (blockType) {
            case COAL_ORE -> new ItemStack(Material.COAL, random.nextInt(4) + 2);
            case COPPER_ORE -> new ItemStack(Material.RAW_COPPER, random.nextInt(4) + 2);
            case IRON_ORE -> new ItemStack(Material.RAW_IRON, 1);
            case GOLD_ORE -> new ItemStack(Material.RAW_GOLD, 1);
            case DIAMOND_ORE -> new ItemStack(Material.DIAMOND, 1);
            case EMERALD_ORE -> new ItemStack(Material.EMERALD, 1);
            case LAPIS_ORE -> new ItemStack(Material.LAPIS_LAZULI, random.nextInt(6) + 4);
            case REDSTONE_ORE -> new ItemStack(Material.REDSTONE, random.nextInt(2) + 4);
            case NETHER_QUARTZ_ORE -> new ItemStack(Material.QUARTZ, 1);
            case COBBLESTONE -> new ItemStack(Material.COBBLESTONE, 1);
            default -> new ItemStack(Material.AIR, 0);
        };
    }

    private Material getMaterialForTier(int tier) {
        String tierKey = "generator.ores.tier" + tier;
        List<Map<?, ?>> materialChances = config.getMapList(tierKey);

        // Add cobblestone as a default option if not present
        boolean hasCobblestone = materialChances.stream().anyMatch(m -> "COBBLESTONE".equals(m.get("ore")));
        if (!hasCobblestone) {
            Map<String, Object> cobbleEntry = new HashMap<>();
            cobbleEntry.put("ore", "COBBLESTONE");
            cobbleEntry.put("weight", 10.0); // High weight for cobblestone
            materialChances.add(cobbleEntry);
        }

        double totalWeight = materialChances.stream().mapToDouble(m -> ((Number) m.get("weight")).doubleValue()).sum();
        double roll = random.nextDouble() * totalWeight;

        double currentWeight = 0;
        for (Map<?, ?> material : materialChances) {
            currentWeight += ((Number) material.get("weight")).doubleValue();
            if (roll <= currentWeight) {
                String materialName = (String) material.get("ore");
                return Material.valueOf(materialName);
            }
        }
        return Material.COBBLESTONE; // Fallback
    }
}