package br.com.logicmc.bedwars.game.engine.generator;

import br.com.logicmc.core.addons.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;


public class NormalGenerator implements IGenerator{

    private final Material material;
    private final Hologram holograms;
    private final Location location;

    private int time;

    public NormalGenerator(Location location, Material material, Hologram holograms, int reset) {
        this.material = material;
        this.holograms = holograms;
        this.location =location;

        time = reset;
        holograms.create();
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public boolean reset(int time) {
        return this.time==time;
    }

    public int getTime() {
        return time;
    }

    @Override
    public void setNewReset() {
        time+= time;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public Hologram getHologram() {
        return holograms;
    }
}
