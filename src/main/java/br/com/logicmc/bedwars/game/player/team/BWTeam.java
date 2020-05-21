package br.com.logicmc.bedwars.game.player.team;

import org.bukkit.Color;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.UUID;

public class BWTeam {

    private final Color color;
    private final Location spawnlocation,bedlocation;
    private final HashSet<UUID> players;

    public BWTeam(Color color, Location spawnlocation, Location bedlocation) {
        this.color = color;
        this.spawnlocation = spawnlocation;
        this.bedlocation = bedlocation;
        players = new HashSet<>();
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public HashSet<UUID> getPlayers() {
        return players;
    }

    public Color getColor() {
        return color;
    }

    public Location getSpawnlocation() {
        return spawnlocation;
    }

    public Location getBedlocation() {
        return bedlocation;
    }
}
