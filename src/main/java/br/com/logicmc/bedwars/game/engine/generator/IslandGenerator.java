package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class IslandGenerator extends NormalGenerator{


    private ItemStack ironstack,goldstack;
    private int gold,emerald;

    public IslandGenerator(Location location) {
        super(location, Material.IRON_INGOT, null, 3);
        gold = getTime() + 10;

        ironstack = setStack(Material.IRON_INGOT, 48);
        goldstack = setStack(Material.GOLD_INGOT, 12);
        emerald=0;
    }

    @Override
    public void spawn() {
        boolean generategold = gold <= getTime();
        if(getGeneratorlevel() == 0){
            multiplespawn(generategold);
        } else if(getGeneratorlevel() == 1){
            multiplespawn(generategold);
            multiplespawn(generategold);
        } else if(getGeneratorlevel() == 2){
            multiplespawn(generategold);
            multiplespawn(generategold);
            multiplespawn(generategold);

            if(emerald <=getTime()) {
                getLocation().getWorld().dropItem(getLocation(), new ItemStack(Material.EMERALD));
                emerald+=60;
            }
            
        } else if(getGeneratorlevel() == 3){
            multiplespawn(generategold);
            multiplespawn(generategold);
            multiplespawn(generategold);
            multiplespawn(generategold);

            if(emerald <=getTime()) {
                getLocation().getWorld().dropItem(getLocation(), new ItemStack(Material.EMERALD));
                emerald+=30;
            }
        }
    }

    public void setIronstack(ItemStack ironstack) {
        this.ironstack = ironstack;
    }

    public void setGoldstack(ItemStack goldstack) {
        this.goldstack = goldstack;
    }


    private void multiplespawn(boolean can){
        if(can){
            getLocation().getWorld().dropItem(getLocation(), goldstack);
            gold+=10;
        }

        getLocation().getWorld().dropItem(getLocation(), ironstack);
    } 
}
