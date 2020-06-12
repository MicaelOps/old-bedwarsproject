package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface IGenerator {

    int getTime();

    void setNewReset();

    Location getLocation();

    default boolean reset(int time) { return getTime() <= time;}

    void spawn();
}
