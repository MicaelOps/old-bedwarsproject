package br.com.logicmc.bedwars.extra;

import br.com.logicmc.bedwars.game.engine.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;

public class StaffArena extends Arena {
    public StaffArena() {
        super("staff", 1000, Arena.SOLO, new Location(Bukkit.getWorld("world"), 0, 100, 0), new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    @Override
    public int getGamestate() {
        return Arena.WAITING;
    }

    @Override
    public int getTime() {
        return -1;
    }
}
