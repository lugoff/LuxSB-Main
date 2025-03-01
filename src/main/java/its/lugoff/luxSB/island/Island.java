package its.lugoff.luxSB.island;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Island {
    private UUID owner;
    private Location center;
    private IslandUpgrades upgrades;
    private double balance;
    private Location warpLocation;
    private boolean warpPublic;
    private List<UUID> members;
    private int size;

    public Island(UUID owner, Location center) {
        this.owner = owner;
        this.center = center.clone();
        this.upgrades = new IslandUpgrades();
        this.balance = 0.0;
        this.warpLocation = null;
        this.warpPublic = true;
        this.members = new ArrayList<>();
        this.size = upgrades.getMaxSize(); // Default to level 1 radius (25)
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getCenter() {
        return center.clone();
    }

    public IslandUpgrades getUpgrades() {
        return upgrades;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Location getWarpLocation() {
        return warpLocation != null ? warpLocation.clone() : null;
    }

    public void setWarpLocation(Location warpLocation) {
        this.warpLocation = warpLocation != null ? warpLocation.clone() : null;
    }

    public boolean isWarpPublic() {
        return warpPublic;
    }

    public void setWarpPublic(boolean warpPublic) {
        this.warpPublic = warpPublic;
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(UUID member) {
        if (!members.contains(member)) members.add(member);
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public int getSize() {
        return size; // Radius: 25, 50, 75
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void teleportPlayer(Player player) {
        Location safeSpot = center.clone();
        safeSpot.setY(safeSpot.getY() + 2);
        player.teleport(safeSpot);
    }
}