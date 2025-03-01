package its.lugoff.luxSB.team;

import its.lugoff.luxSB.LuxSB;
import its.lugoff.luxSB.island.Island;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private final LuxSB plugin;
    private final Map<UUID, UUID> invites; // Inviter -> Invitee

    public TeamManager(LuxSB plugin) {
        this.plugin = plugin;
        this.invites = new HashMap<>();
    }

    public void invitePlayer(Player inviter, Player invitee) {
        Island island = plugin.getIslandManager().getIsland(inviter.getUniqueId());
        if (island == null || !island.getOwner().equals(inviter.getUniqueId())) {
            inviter.sendMessage("You must own an island to invite players!");
            return;
        }
        invites.put(invitee.getUniqueId(), inviter.getUniqueId());
        inviter.sendMessage("Invited " + invitee.getName() + " to your island!");
        invitee.sendMessage(inviter.getName() + " has invited you to their island. Use /island accept to join.");
    }

    public void acceptInvite(Player player) {
        UUID inviter = invites.remove(player.getUniqueId());
        if (inviter == null) {
            player.sendMessage("You have no pending invites!");
            return;
        }
        Island island = plugin.getIslandManager().getIsland(inviter);
        if (island != null) {
            island.addMember(player.getUniqueId());
            player.sendMessage("You’ve joined " + plugin.getServer().getPlayer(inviter).getName() + "'s island!");
        }
    }
}