package br.com.logicmc.bedwars.game.engine;

import org.bukkit.Location;


public class WorldArena {

    private final String mapname;
    private final Location arenalocation;


    public WorldArena(String mapname, Location arenalocation) {
        this.mapname = mapname;
        this.arenalocation = arenalocation;
    }

    public String getMapname() {
        return mapname;
    }

    public Location getArenalocation() {
        return arenalocation;
    }
}
