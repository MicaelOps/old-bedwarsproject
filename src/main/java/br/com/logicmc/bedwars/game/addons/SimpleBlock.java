package br.com.logicmc.bedwars.game.addons;

import org.bukkit.Bukkit;
import org.bukkit.Location;


public class SimpleBlock {


    private final int x,y,z;


    public SimpleBlock(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }



    public Location toLocation(String world){
        return new Location(Bukkit.getWorld(world), x , y ,z);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleBlock that = (SimpleBlock) o;
        return x == that.x &&
                y == that.y &&
                z == that.z;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + x;
        hash = 19 * hash + y;
        hash = 19 * hash + z;
        return hash;
    }
}
