package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class IslandGenerator extends NormalGenerator{


    private int gold,emerald;

    public IslandGenerator(Location location) {
        super(location, Material.IRON_INGOT, null, 3);
        gold = getTime() + 10;
        emerald=0;
    }

    @Override
    public int getReset() {
        return 1;
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
    private void multiplespawn(boolean can){
        if(can){
            getLocation().getWorld().dropItem(getLocation(), new ItemStack(Material.GOLD_INGOT));
            gold+=10;
        }

        super.spawn();
    } 
}
