package its.lugoff.luxSB.listeners;

import its.lugoff.luxSB.LuxSB;
import org.bukkit.ChatColor;
import org.bukkit.Material; // Added import
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class HeadTokenListener implements Listener {
    private final LuxSB plugin;
    private final NamespacedKey spawnerKey; // Key to tag spawner mobs

    public HeadTokenListener(LuxSB plugin) {
        this.plugin = plugin;
        this.spawnerKey = new NamespacedKey(plugin, "from_spawner");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || !(event.getEntity() instanceof Mob)) return;

        Mob mob = (Mob) event.getEntity();
        // Check if mob is from a spawner using PersistentData
        if (!mob.getPersistentDataContainer().has(spawnerKey, PersistentDataType.BYTE) ||
                mob.getPersistentDataContainer().get(spawnerKey, PersistentDataType.BYTE) != (byte) 1) return;

        // Drop head based on mob type
        ItemStack head = getMobHead(mob.getType());
        if (head != null) {
            event.getDrops().add(head);
            plugin.addHeadTokens(killer.getUniqueId(), 1); // 1 head = 1 htoken
            killer.sendMessage(ChatColor.GREEN + "You earned 1 Head Token! Total: " + plugin.getHeadTokens(killer.getUniqueId()));
        }
    }

    private ItemStack getMobHead(EntityType type) {
        switch (type) {
            case ZOMBIE: return new ItemStack(Material.ZOMBIE_HEAD);
            case SKELETON: return new ItemStack(Material.SKELETON_SKULL);
            case CREEPER: return new ItemStack(Material.CREEPER_HEAD);
            case WITHER_SKELETON: return new ItemStack(Material.WITHER_SKELETON_SKULL);
            default: return null; // Only specific mobs drop heads
        }
    }
}