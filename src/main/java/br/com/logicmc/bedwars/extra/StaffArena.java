package br.com.logicmc.bedwars.extra;

import br.com.logicmc.bedwars.game.engine.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class StaffArena extends Arena {
    public StaffArena() {
        super("staff", 1000, Arena.SOLO, new Location(Bukkit.getWorlds().get(0), 0, 100, 0), new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    @Override
    public int getGamestate() {
        return Arena.INGAME;
    }

    @Override
    public int getTime() {
        return -1;
    }

    @Override
    public void startTimer(JavaPlugin plugin) {

    }
}
