package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.Arrays; // Added import
import java.util.Random;

public class PlayerHeadListener implements Listener {
    private final LuxSB plugin;
    private final Random random;

    public PlayerHeadListener(LuxSB plugin) {
        this.plugin = plugin;
        this.random = new Random();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        // Update stats
        plugin.addPlayerKills(killer.getUniqueId(), 1);
        plugin.addPlayerDeaths(victim.getUniqueId(), 1);

        // 10% chance for head drop
        if (random.nextDouble() < 0.1) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(victim);
            int kills = plugin.getPlayerKills(victim.getUniqueId());
            int deaths = plugin.getPlayerDeaths(victim.getUniqueId());
            double value = (deaths == 0) ? kills : (double) kills / deaths; // K/D ratio
            meta.setDisplayName(ChatColor.GOLD + victim.getName() + "'s Head");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Kills: " + kills,
                    ChatColor.GRAY + "Deaths: " + deaths,
                    ChatColor.YELLOW + "Value: " + String.format("%.2f", value)
            ));
            head.setItemMeta(meta);
            event.getDrops().add(head);
            killer.sendMessage(ChatColor.GREEN + "You obtained " + victim.getName() + "'s head!");
        }
    }
}