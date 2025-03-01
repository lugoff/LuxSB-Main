package its.lugoff.luxSB.missions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Mission {
    private final String name;
    private final String type; // e.g., "mine", "farm", "kill", "craft", "place"
    private final Material material; // Target block/item/entity type
    private final int goal; // Amount required
    private final double moneyReward; // Cash reward
    private final ItemStack itemReward; // Optional item reward
    private final long cooldown; // Cooldown in seconds

    public Mission(String name, String type, Material material, int goal, double moneyReward, ItemStack itemReward, long cooldown) {
        this.name = name;
        this.type = type;
        this.material = material;
        this.goal = goal;
        this.moneyReward = moneyReward;
        this.itemReward = itemReward;
        this.cooldown = cooldown;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public int getGoal() {
        return goal;
    }

    public double getMoneyReward() {
        return moneyReward;
    }

    public ItemStack getItemReward() {
        return itemReward;
    }

    public long getCooldown() {
        return cooldown;
    }
}