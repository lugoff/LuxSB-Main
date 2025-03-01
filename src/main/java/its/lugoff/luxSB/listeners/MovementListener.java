package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {
    private final LuxSB plugin;

    public MovementListener(LuxSB plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() != plugin.getIslandManager().getSkyblockWorld()) return;

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            if (!from.getWorld().equals(plugin.getServer().getWorlds().get(0))) {
                player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
                player.sendMessage(ChatColor.RED + "You don’t have an island to be here!");
            }
            return;
        }

        if (to.getY() < 0) {
            Location safeSpot = island.getCenter().clone();
            safeSpot.setY(100);
            player.teleport(safeSpot);
            player.setFallDistance(0);
            player.sendMessage(ChatColor.YELLOW + "You fell off your island and were teleported back!");
            return;
        }

        // Check X and Z boundaries from center (radius = half of diameter)
        double centerX = island.getCenter().getX();
        double centerZ = island.getCenter().getZ();
        double toX = to.getX();
        double toZ = to.getZ();
        int radius = island.getSize(); // 25, 50, 75

        // Ensure player can reach -radius and +radius from center
        if (toX < centerX - radius || toX > centerX + radius || toZ < centerZ - radius || toZ > centerZ + radius) {
            Location safeSpot = from.clone();
            safeSpot.setX(Math.max(centerX - radius, Math.min(centerX + radius, toX)));
            safeSpot.setZ(Math.max(centerZ - radius, Math.min(centerZ + radius, toZ)));
            event.setTo(safeSpot);
            player.sendMessage(ChatColor.RED + "You can’t leave your island’s boundary!");
        }

        for (Island otherIsland : plugin.getIslandManager().getIslands().values()) {
            if (otherIsland.getOwner().equals(player.getUniqueId())) continue;
            double otherCenterX = otherIsland.getCenter().getX();
            double otherCenterZ = otherIsland.getCenter().getZ();
            int otherRadius = otherIsland.getSize();
            if (toX >= otherCenterX - otherRadius && toX <= otherCenterX + otherRadius &&
                    toZ >= otherCenterZ - otherRadius && toZ <= otherCenterZ + otherRadius) {
                Location safeSpot = from.clone();
                event.setTo(safeSpot);
                player.sendMessage(ChatColor.RED + "You can’t enter another player’s island!");
                break;
            }
        }
    }
}