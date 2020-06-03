package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;

public class IslandGenerator extends NormalGenerator{

    private int generatorLevel = 1;

    public IslandGenerator(Location location) {
        super(location, Material.IRON_INGOT, null, 3);
    }

    public void setGeneratorLevel(int generatorLevel) {
        this.generatorLevel = generatorLevel;
    }

  
}
