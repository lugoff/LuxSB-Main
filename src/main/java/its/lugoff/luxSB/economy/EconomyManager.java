package its.lugoff.luxSB.economy;

import its.lugoff.luxSB.LuxSB;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final LuxSB plugin;
    private Economy economy;

    public EconomyManager(LuxSB plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault not found! Economy features disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("No economy provider found!");
            return;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Economy hooked into " + economy.getName());
    }

    public boolean depositPlayer(Player player, double amount) {
        if (economy == null) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double getBalance(Player player) {
        if (economy == null) return 0.0;
        return economy.getBalance(player);
    }

    public boolean withdrawPlayer(Player player, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }
}