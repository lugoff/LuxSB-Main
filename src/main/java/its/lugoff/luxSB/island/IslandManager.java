package its.lugoff.luxSB.island;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.data.DataManager;
import its.lugoff.luxSB.schematics.SchematicLoader;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class IslandManager {
    private final LuxSB plugin;
    private final Map<UUID, Island> islands;
    private final World skyblockWorld;
    private final DataManager dataManager;

    public IslandManager(LuxSB plugin) {
        this.plugin = plugin;
        this.islands = new HashMap<>();
        this.skyblockWorld = plugin.getServer().createWorld(new WorldCreator("skyblock_world")
                .environment(World.Environment.NORMAL)
                .generator(new IslandGenerator(this)));
        this.dataManager = new DataManager(plugin);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Island island = dataManager.loadIsland(player.getUniqueId());
            if (island != null) islands.put(player.getUniqueId(), island);
        }
    }

    public Island createIsland(Player player) {
        return createIsland(player, "grass");
    }

    public Island createIsland(Player player, String schematicName) {
        UUID uuid = player.getUniqueId();
        if (islands.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You already have an island!");
            return islands.get(uuid);
        }

        int x = islands.size() * 200;
        int z = 0;
        Location center = new Location(skyblockWorld, x, 100, z);
        File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schematicName + ".schem");
        if (schematicFile.exists()) {
            SchematicLoader.loadSchematic(schematicName, center);
        } else {
            player.sendMessage(ChatColor.RED + "Schematic '" + schematicName + "' not found! Using default.");
            SchematicLoader.loadSchematic("grass", center);
        }

        Island island = new Island(uuid, center);
        islands.put(uuid, island);
        dataManager.saveIsland(island);
        player.sendMessage(ChatColor.GREEN + "Your island has been created!");
        island.teleportPlayer(player);
        return island;
    }

    public void addSideIsland(Player player, String schematicName) {
        UUID uuid = player.getUniqueId();
        Island island = getIsland(uuid);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "You need an island to add a side island!");
            return;
        }

        Location center = island.getCenter();
        int offsetX = island.getSize() + 10;
        Location sideCenter = new Location(skyblockWorld, center.getX() + offsetX, center.getY(), center.getZ());

        File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schematicName + ".schem");
        if (schematicFile.exists()) {
            SchematicLoader.loadSchematic(schematicName, sideCenter);
            island.setSize(island.getSize() + 20);
            dataManager.saveIsland(island);
        } else {
            player.sendMessage(ChatColor.RED + "Side island schematic '" + schematicName + "' not found!");
        }
    }

    public Island getIsland(UUID uuid) {
        Island island = islands.get(uuid);
        if (island == null) {
            island = dataManager.loadIsland(uuid);
            if (island != null) islands.put(uuid, island);
        }
        return island;
    }

    public void saveIsland(Island island) {
        dataManager.saveIsland(island);
    }

    public void removeIsland(Island island) {
        int radius = island.getSize();
        Location center = island.getCenter();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y < 256; y++) {
                    center.clone().add(x, y - 100, z).getBlock().setType(Material.AIR);
                }
            }
        }
    }

    public World getSkyblockWorld() {
        return skyblockWorld;
    }

    public Map<UUID, Island> getIslands() {
        return islands;
    }

    public static class IslandGenerator extends ChunkGenerator {
        private final IslandManager islandManager;

        public IslandGenerator(IslandManager islandManager) {
            this.islandManager = islandManager;
        }

        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);
            for (Island island : islandManager.getIslands().values()) {
                double baseRate = island.getUpgrades().getGeneratorRate();
                double boostMultiplier = islandManager.plugin.getBoostManager().getBoostMultiplier(island.getOwner(), "generator_boost");
                double rate = baseRate * boostMultiplier;
                if (random.nextDouble() < rate) {
                    int blockX = x * 16 + random.nextInt(16);
                    int blockZ = z * 16 + random.nextInt(16);
                    int blockY = 50 + random.nextInt(50);
                    if (Math.abs(blockX - island.getCenter().getX()) <= island.getSize() &&
                            Math.abs(blockZ - island.getCenter().getZ()) <= island.getSize()) {
                        chunk.setBlock(blockX & 15, blockY, blockZ & 15, Material.STONE);
                        if (random.nextDouble() < 0.1) chunk.setBlock(blockX & 15, blockY, blockZ & 15, Material.IRON_ORE);
                    }
                }
            }
            return chunk;
        }
    }
}