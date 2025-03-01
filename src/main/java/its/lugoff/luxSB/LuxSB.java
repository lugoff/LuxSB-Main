package its.lugoff.luxSB;

import its.lugoff.luxSB.commands.IslandBankCommand;
import its.lugoff.luxSB.commands.IslandCommand;
import its.lugoff.luxSB.data.DataManager;
import its.lugoff.luxSB.economy.EconomyManager;
import its.lugoff.luxSB.gui.BankGUI;
import its.lugoff.luxSB.gui.GUIListener;
import its.lugoff.luxSB.island.Island;
import its.lugoff.luxSB.island.IslandManager;
import its.lugoff.luxSB.listeners.GeneratorListener;
import its.lugoff.luxSB.listeners.MovementListener;
import its.lugoff.luxSB.listeners.PlayerDeathListener;
import its.lugoff.luxSB.listeners.SideIslandPlacementListener;
import its.lugoff.luxSB.listeners.HeadTokenListener; // Add this import
import its.lugoff.luxSB.missions.MissionManager;
import its.lugoff.luxSB.team.TeamManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class LuxSB extends JavaPlugin {
    private IslandManager islandManager;
    private MissionManager missionManager;
    private EconomyManager economyManager;
    private TeamManager teamManager;
    private DataManager dataManager;
    private BankGUI bankGUI;
    private Map<UUID, Integer> headTokens; // Add head tokens map

    @Override
    public void onEnable() {
        islandManager = new IslandManager(this);
        missionManager = new MissionManager(this);
        economyManager = new EconomyManager(this);
        teamManager = new TeamManager(this);
        dataManager = new DataManager(this);
        bankGUI = new BankGUI(this);
        headTokens = new HashMap<>(); // Initialize head tokens

        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("islandbank").setExecutor(new IslandBankCommand(this));
        new GUIListener(this);
        new GeneratorListener(this);
        new MovementListener(this);
        new PlayerDeathListener(this);
        new SideIslandPlacementListener(this);
        new HeadTokenListener(this); // Register new listener

        File schematicsDir = new File(getDataFolder(), "schematics");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
            getLogger().info("Created schematics folder at " + schematicsDir.getPath());
        }

        saveDefaultConfig();
        updateConfigWithSchematics(schematicsDir);
        saveConfigIfNotExists("generator.yml");
        saveConfigIfNotExists("missions.yml");
        saveConfigIfNotExists("shops.yml");
        saveConfigIfNotExists("gui.yml");

        getLogger().info("LuxSB has been enabled!");
    }

    @Override
    public void onDisable() {
        for (Island island : islandManager.getIslands().values()) {
            dataManager.saveIsland(island);
        }
        getLogger().info("LuxSB has been disabled!");
    }

    // Existing getters...
    public IslandManager getIslandManager() { return islandManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public DataManager getDataManager() { return dataManager; }
    public BankGUI getBankGUI() { return bankGUI; }

    // Add head tokens methods
    public int getHeadTokens(UUID playerUUID) {
        return headTokens.getOrDefault(playerUUID, 0);
    }

    public void addHeadTokens(UUID playerUUID, int amount) {
        headTokens.put(playerUUID, getHeadTokens(playerUUID) + amount);
    }

    public boolean removeHeadTokens(UUID playerUUID, int amount) {
        int current = getHeadTokens(playerUUID);
        if (current >= amount) {
            headTokens.put(playerUUID, current - amount);
            return true;
        }
        return false;
    }

    public FileConfiguration getConfig(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveConfigIfNotExists(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateConfigWithSchematics(File schematicsDir) {
        FileConfiguration config = getConfig();
        List<Map<String, Object>> schematics = new ArrayList<>();
        List<?> rawSchematics = config.getMapList("islands.schematics");
        for (Object rawSchematic : rawSchematics) {
            if (rawSchematic instanceof Map) {
                schematics.add((Map<String, Object>) rawSchematic);
            }
        }

        Map<String, Map<String, Object>> existingSchematics = new HashMap<>();
        for (Map<String, Object> schematic : schematics) {
            String name = (String) schematic.get("name");
            existingSchematics.put(name, schematic);
        }

        File[] files = schematicsDir.listFiles((dir, name) -> name.endsWith(".schem"));
        if (files != null) {
            for (File file : files) {
                String schematicName = file.getName().replace(".schem", "");
                if (!existingSchematics.containsKey(schematicName)) {
                    Map<String, Object> newSchematic = new HashMap<>();
                    newSchematic.put("name", schematicName);
                    newSchematic.put("description", "A Skyblock island");
                    newSchematic.put("cost", 0.0);
                    newSchematic.put("icon", "GRASS_BLOCK");
                    schematics.add(newSchematic);
                    getLogger().info("Added new schematic '" + schematicName + "' to config with defaults.");
                }
            }
        }

        config.set("islands.schematics", schematics);
        saveConfig();
    }
}