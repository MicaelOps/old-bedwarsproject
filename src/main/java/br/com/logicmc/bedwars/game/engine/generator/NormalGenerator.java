package br.com.logicmc.bedwars.game.engine.generator;

import br.com.logicmc.core.addons.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;


public class NormalGenerator implements IGenerator{

    private final Location location;
    private final int reset;

    private Hologram holograms;
    private int time,generatorlevel;

    private final ItemStack drop;

    public NormalGenerator(Location location, Material material, Hologram holograms, int reset) {
        this.holograms = holograms;
        this.location =location;
        this.reset = reset;

        time = reset;
        generatorlevel = 0;
        drop = setStack(material, 32);

    }
    public ItemStack setStack(Material material, int amount){
        ItemStack dummy = new ItemStack(material, 1);
        net.minecraft.server.v1_8_R3.ItemStack nmsdummy = CraftItemStack.asNMSCopy(dummy);
        nmsdummy.getItem().c(amount);
        return CraftItemStack.asBukkitCopy(nmsdummy);
    }


    public int getGeneratorlevel() {
        return generatorlevel;
    }
    public void increaseGeneratorLevel(){
        this.generatorlevel++;
    }


    @Override
    public int getTime() {
        return time;
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

    @Override
    public void spawn() {
        getLocation().getWorld().dropItem(getLocation(), drop);
    }
}
