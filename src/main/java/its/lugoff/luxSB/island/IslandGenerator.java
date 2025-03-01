package its.lugoff.luxSB.island;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class IslandGenerator extends ChunkGenerator {
    private final IslandManager islandManager;

    public IslandGenerator(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        for (Island island : islandManager.getIslands().values()) {
            double rate = island.getUpgrades().getGeneratorRate();
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