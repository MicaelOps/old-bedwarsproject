package br.com.logicmc.bedwars.game.engine.generator;

import br.com.logicmc.core.addons.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;


public class NormalGenerator implements IGenerator{

    private final Material material;
    private final Location location;
    private final int reset;

    private Hologram holograms;
    private int time,generatorlevel;

    public NormalGenerator(Location location, Material material, Hologram holograms, int reset) {
        this.material = material;
        this.holograms = holograms;
        this.location =location;
        this.reset = reset;

        time = reset;
        generatorlevel = 0;

    }

    public int getGeneratorlevel() {
        return generatorlevel;
    }
    public void increaseGeneratorLevel(){
        this.generatorlevel++;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public int getReset() {
        return reset;
    }

    @Override
    public void setNewReset() {
        time+= reset;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public Hologram getHologram() {
        return holograms;
    }

	public void setHologram(Hologram hologram) {
        this.holograms=hologram;
	}
}
