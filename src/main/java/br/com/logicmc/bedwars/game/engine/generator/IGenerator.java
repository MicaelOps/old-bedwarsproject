package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IGenerator {

    Material getMaterial();

    int getReset();

    int getTime();

    void setNewReset();

    Location getLocation();

    default boolean reset(int time) { return getTime() == time;}

    default void spawn() {
        getLocation().getWorld().dropItem(getLocation(), new ItemStack(getMaterial()));
    }
}
