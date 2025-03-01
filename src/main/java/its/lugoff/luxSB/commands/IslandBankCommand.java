package its.lugoff.luxSB.commands;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandBankCommand implements CommandExecutor {
    private final LuxSB plugin;

    public IslandBankCommand(LuxSB plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only!");
            return true;
        }

        Player player = (Player) sender;
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());

        if (island == null) {
            player.sendMessage(ChatColor.RED + "You need an island to use the bank!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.GRAY + "Usage: /islandbank <deposit|withdraw> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please enter a valid positive amount!");
            return true;
        }

        switch (action) {
            case "deposit":
                if (plugin.getEconomyManager().withdrawPlayer(player, amount)) {
                    island.setBalance(island.getBalance() + amount);
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(ChatColor.GREEN + "Deposited $" + amount + " to your island bank!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don’t have enough money to deposit $" + amount + "!");
                }
                break;
            case "withdraw":
                if (island.getBalance() >= amount) {
                    island.setBalance(island.getBalance() - amount);
                    plugin.getEconomyManager().depositPlayer(player, amount);
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(ChatColor.GREEN + "Withdrew $" + amount + " from your island bank!");
                } else {
                    player.sendMessage(ChatColor.RED + "Not enough funds in your island bank! Need $" + amount);
                }
                break;
            default:
                player.sendMessage(ChatColor.GRAY + "Usage: /islandbank <deposit|withdraw> <amount>");
                break;
        }

        return true;
    }
}