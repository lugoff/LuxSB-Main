package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class HeadTokenListener implements Listener {
    private final LuxSB plugin;

    public HeadTokenListener(LuxSB plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // Simple token reward: 1 token per mob kill
        plugin.addHeadTokens(killer.getUniqueId(), 1);
        killer.sendMessage(ChatColor.GREEN + "You earned 1 Head Token! Total: " + plugin.getHeadTokens(killer.getUniqueId()));
    }
}