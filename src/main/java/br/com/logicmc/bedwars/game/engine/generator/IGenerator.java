package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IGenerator {

    Material getMaterial();

    boolean reset(int time);

    void setNewReset();

    Location getLocation();

    default void spawn() {
        getLocation().getWorld().dropItem(getLocation(), new ItemStack(getMaterial()));
    }
}
