package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private final LuxSB plugin;

    public PlayerDeathListener(LuxSB plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island != null) {
            Location respawnLoc = island.getCenter().clone();
            respawnLoc.setY(100); // Default island height
            player.setBedSpawnLocation(respawnLoc, true); // Force respawn at island
        }
    }
}