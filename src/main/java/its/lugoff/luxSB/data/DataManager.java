package its.lugoff.luxSB.data;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import its.lugoff.luxSB.island.IslandUpgrades;
import org.bukkit.Location;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataManager {
    private final LuxSB plugin;
    private final Gson gson;

    public DataManager(LuxSB plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveIsland(Island island) {
        File folder = new File(plugin.getDataFolder(), "playerdata");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, island.getOwner() + ".json");
        JsonObject json = new JsonObject();
        json.addProperty("owner", island.getOwner().toString());

        JsonObject center = new JsonObject();
        Location loc = island.getCenter();
        center.addProperty("world", loc.getWorld().getName());
        center.addProperty("x", loc.getX());
        center.addProperty("y", loc.getY());
        center.addProperty("z", loc.getZ());
        json.add("center", center);

        JsonObject upgrades = new JsonObject();
        IslandUpgrades islandUpgrades = island.getUpgrades();
        upgrades.addProperty("sizeLevel", islandUpgrades.getSizeLevel());
        upgrades.addProperty("generatorLevel", islandUpgrades.getGeneratorLevel());
        upgrades.addProperty("oreSpawningEnabled", islandUpgrades.isOreSpawningEnabled());
        json.add("upgrades", upgrades);

        json.addProperty("balance", island.getBalance());

        if (island.getWarpLocation() != null) {
            JsonObject warp = new JsonObject();
            Location warpLoc = island.getWarpLocation();
            warp.addProperty("world", warpLoc.getWorld().getName());
            warp.addProperty("x", warpLoc.getX());
            warp.addProperty("y", warpLoc.getY());
            warp.addProperty("z", warpLoc.getZ());
            json.add("warpLocation", warp);
            json.addProperty("warpPublic", island.isWarpPublic());
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save island data for " + island.getOwner() + ": " + e.getMessage());
        }
    }

    public Island loadIsland(UUID uuid) {
        File file = new File(plugin.getDataFolder(), "playerdata/" + uuid + ".json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            UUID owner = UUID.fromString(json.get("owner").getAsString());

            JsonObject centerJson = json.getAsJsonObject("center");
            if (centerJson == null) {
                plugin.getLogger().warning("Island data for " + uuid + " is missing 'center' object. Skipping load.");
                return null;
            }

            Location center = new Location(
                    plugin.getServer().getWorld(centerJson.get("world").getAsString()),
                    centerJson.get("x").getAsDouble(),
                    centerJson.get("y").getAsDouble(),
                    centerJson.get("z").getAsDouble()
            );

            Island island = new Island(owner, center);
            if (json.has("balance")) island.setBalance(json.get("balance").getAsDouble());

            if (json.has("upgrades")) {
                JsonObject upgradesJson = json.getAsJsonObject("upgrades");
                IslandUpgrades upgrades = island.getUpgrades();
                upgrades.setSizeLevel(upgradesJson.get("sizeLevel").getAsInt());
                island.setSize(upgrades.getMaxSize()); // Set size from sizeLevel
                upgrades.setGeneratorLevel(upgradesJson.get("generatorLevel").getAsInt());
                if (upgradesJson.has("oreSpawningEnabled")) {
                    upgrades.setOreSpawningEnabled(upgradesJson.get("oreSpawningEnabled").getAsBoolean());
                }
            }

            if (json.has("warpLocation")) {
                JsonObject warpJson = json.getAsJsonObject("warpLocation");
                Location warpLocation = new Location(
                        plugin.getServer().getWorld(warpJson.get("world").getAsString()),
                        warpJson.get("x").getAsDouble(),
                        warpJson.get("y").getAsDouble(),
                        warpJson.get("z").getAsDouble()
                );
                island.setWarpLocation(warpLocation);
                island.setWarpPublic(json.get("warpPublic").getAsBoolean());
            }

            return island;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load island data for " + uuid + ": " + e.getMessage());
            return null;
        } catch (NullPointerException e) {
            plugin.getLogger().severe("Invalid island data format for " + uuid + ": " + e.getMessage());
            return null;
        }
    }
}