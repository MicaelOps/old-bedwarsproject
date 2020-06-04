package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;

public class IslandGenerator extends NormalGenerator{


    public IslandGenerator(Location location) {
        super(location, Material.IRON_INGOT, null, 3);
    }
    @Override
    public int getReset() {
        return 3;
    }
  
}
