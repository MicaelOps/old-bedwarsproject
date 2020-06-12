package br.com.logicmc.bedwars.game.engine.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class IslandGenerator extends NormalGenerator{


    private final ItemStack ironstack;
    private int gold,emerald;

    public IslandGenerator(Location location) {
        super(location, Material.IRON_INGOT, null, 3);
        gold = getTime() + 10;
        ItemStack dummyiron = new ItemStack(Material.IRON_INGOT, 1);
        net.minecraft.server.v1_8_R3.ItemStack iron = CraftItemStack.asNMSCopy(dummyiron);
        iron.getItem().c(48);
        ironstack = CraftItemStack.asBukkitCopy(iron);
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

    private void setMaxStackSize(){

    }
    private void multiplespawn(boolean can){
        if(can){
            getLocation().getWorld().dropItem(getLocation(), new ItemStack(Material.GOLD_INGOT));
            gold+=10;
        }

        getLocation().getWorld().dropItem(getLocation(), ironstack);
    } 
}
