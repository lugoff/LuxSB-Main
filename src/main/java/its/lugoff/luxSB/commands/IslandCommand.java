package its.lugoff.luxSB.commands;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.gui.IslandGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IslandCommand implements CommandExecutor {
    private final LuxSB plugin;
    private final IslandGUI islandGUI;

    public IslandCommand(LuxSB plugin) {
        this.plugin = plugin;
        this.islandGUI = new IslandGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            islandGUI.openGUI(player); // Just open the GUI, no auto-creation
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /island invite <player>");
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target != null) plugin.getTeamManager().invitePlayer(player, target);
                else player.sendMessage(ChatColor.RED + "Player '" + args[1] + "' not found!");
                break;
            case "accept":
                plugin.getTeamManager().acceptInvite(player);
                break;
            case "mission":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /island mission <name>");
                    return true;
                }
                plugin.getMissionManager().startMission(player, args[1]);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand! Use /island help for a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "✦ Island Commands ✦");
        player.sendMessage(ChatColor.GRAY + "Here are all available island commands:");
        player.sendMessage(""); // Spacer

        player.sendMessage(ChatColor.GREEN + "/island" + ChatColor.GRAY + " - Opens the island menu.");
        player.sendMessage(ChatColor.GREEN + "/island help" + ChatColor.GRAY + " - Shows this help message.");
        player.sendMessage(ChatColor.GREEN + "/island invite <player>" + ChatColor.GRAY + " - Invite a player to your island.");
        player.sendMessage(ChatColor.GREEN + "/island accept" + ChatColor.GRAY + " - Accept an island invite.");
        player.sendMessage(ChatColor.GREEN + "/island mission <name>" + ChatColor.GRAY + " - Start a mission by name.");
        player.sendMessage(ChatColor.GREEN + "/islandbank <deposit|withdraw> <amount>" + ChatColor.GRAY + " - Manage your island bank balance.");

        player.sendMessage(""); // Spacer
        player.sendMessage(ChatColor.GRAY + "Use these commands to manage your Skyblock experience!");
    }
}