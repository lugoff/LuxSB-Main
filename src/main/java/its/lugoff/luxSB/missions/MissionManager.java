package its.lugoff.luxSB.missions;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MissionManager implements Listener {
    private final LuxSB plugin;
    private final Map<UUID, Map<Mission, Integer>> playerProgress;
    private final Map<String, Mission> missions;
    private final Map<UUID, Map<String, Long>> playerCooldowns;

    public MissionManager(LuxSB plugin) {
        this.plugin = plugin;
        this.playerProgress = new HashMap<>();
        this.missions = new HashMap<>();
        this.playerCooldowns = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        loadMissions();
    }

    private void loadMissions() {
        ConfigurationSection section = plugin.getConfig("missions.yml").getConfigurationSection("missions");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection missionData = section.getConfigurationSection(key);
            if (!missionData.getBoolean("enabled", true)) continue;

            String name = missionData.getString("name", key);
            String type = missionData.getString("type", "mine");
            Material material = null;
            if (!type.equals("kill")) { // Skip material for "kill" missions
                String materialStr = missionData.getString("material", "COBBLESTONE");
                material = Material.getMaterial(materialStr.toUpperCase());
                if (material == null) {
                    plugin.getLogger().warning("Invalid material for mission '" + key + "': " + materialStr);
                    continue; // Skip invalid missions
                }
            }
            int goal = missionData.getInt("goal", 1);
            double moneyReward = missionData.getDouble("money-reward", 0.0);
            ItemStack itemReward = missionData.contains("item-reward") ?
                    new ItemStack(Material.getMaterial(missionData.getString("item-reward.material", "AIR")),
                            missionData.getInt("item-reward.amount", 1)) : null;
            long cooldown = missionData.getLong("cooldown", 0);

            missions.put(key, new Mission(name, type, material, goal, moneyReward, itemReward, cooldown));
        }
    }

    public Mission getMission(String name) {
        return missions.get(name);
    }

    public Map<String, Mission> getMissions() {
        return missions;
    }

    public void startMission(Player player, String missionName) {
        Mission mission = missions.get(missionName);
        if (mission == null) {
            player.sendMessage(ChatColor.RED + "Mission '" + missionName + "' not found!");
            return;
        }

        UUID uuid = player.getUniqueId();
        Map<String, Long> cooldowns = playerCooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        long currentTime = System.currentTimeMillis() / 1000;
        if (cooldowns.containsKey(missionName)) {
            long lastCompleted = cooldowns.get(missionName);
            if (currentTime - lastCompleted < mission.getCooldown()) {
                player.sendMessage(ChatColor.RED + "You must wait " + (mission.getCooldown() - (currentTime - lastCompleted)) + " seconds to start this mission again!");
                return;
            }
        }

        playerProgress.computeIfAbsent(uuid, k -> new HashMap<>()).put(mission, 0);
        player.sendMessage(ChatColor.GREEN + "Started mission: " + mission.getName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!playerProgress.containsKey(uuid)) return;

        Map<Mission, Integer> progress = playerProgress.get(uuid);
        for (Map.Entry<Mission, Integer> entry : progress.entrySet()) {
            Mission mission = entry.getKey();
            if (mission.getType().equals("mine") && event.getBlock().getType() == mission.getMaterial()) {
                updateProgress(player, mission, entry.getValue() + 1);
            }
        }
    }

    @EventHandler
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!playerProgress.containsKey(uuid)) return;

        Map<Mission, Integer> progress = playerProgress.get(uuid);
        for (Map.Entry<Mission, Integer> entry : progress.entrySet()) {
            Mission mission = entry.getKey();
            if (mission.getType().equals("farm") && event.getHarvestedBlock().getType() == mission.getMaterial()) {
                updateProgress(player, mission, entry.getValue() + 1);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        UUID uuid = killer.getUniqueId();
        if (!playerProgress.containsKey(uuid)) return;

        Map<Mission, Integer> progress = playerProgress.get(uuid);
        for (Map.Entry<Mission, Integer> entry : progress.entrySet()) {
            Mission mission = entry.getKey();
            if (mission.getType().equals("kill")) {
                // Check entity type directly instead of material proxy
                if (mission.getName().equalsIgnoreCase("kill_zombies") && event.getEntityType() == EntityType.ZOMBIE) {
                    updateProgress(killer, mission, entry.getValue() + 1);
                }
                // Add more kill mission types here as needed (e.g., "kill_skeletons" -> EntityType.SKELETON)
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        if (!playerProgress.containsKey(uuid)) return;

        Map<Mission, Integer> progress = playerProgress.get(uuid);
        for (Map.Entry<Mission, Integer> entry : progress.entrySet()) {
            Mission mission = entry.getKey();
            if (mission.getType().equals("craft") && event.getRecipe().getResult().getType() == mission.getMaterial()) {
                int amount = event.getRecipe().getResult().getAmount();
                updateProgress(player, mission, entry.getValue() + amount);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!playerProgress.containsKey(uuid)) return;

        Map<Mission, Integer> progress = playerProgress.get(uuid);
        for (Map.Entry<Mission, Integer> entry : progress.entrySet()) {
            Mission mission = entry.getKey();
            if (mission.getType().equals("place") && event.getBlockPlaced().getType() == mission.getMaterial()) {
                updateProgress(player, mission, entry.getValue() + 1);
            }
        }
    }

    private void updateProgress(Player player, Mission mission, int newProgress) {
        UUID uuid = player.getUniqueId();
        Map<Mission, Integer> progress = playerProgress.get(uuid);
        progress.put(mission, newProgress);
        player.sendMessage(ChatColor.YELLOW + mission.getName() + ": " + newProgress + "/" + mission.getGoal());

        if (newProgress >= mission.getGoal()) {
            plugin.getEconomyManager().depositPlayer(player, mission.getMoneyReward());
            if (mission.getItemReward() != null) player.getInventory().addItem(mission.getItemReward());
            player.sendMessage(ChatColor.GREEN + "Mission completed: " + mission.getName() + "! Reward: $" + mission.getMoneyReward() +
                    (mission.getItemReward() != null ? " + " + mission.getItemReward().getAmount() + " " + mission.getItemReward().getType().name().toLowerCase() : ""));
            progress.remove(mission);
            playerCooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(mission.getName(), System.currentTimeMillis() / 1000);
        }
    }
}