package br.com.logicmc.bedwars.game.phase;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.PhaseControl;
import br.com.logicmc.bedwars.game.engine.generator.IGenerator;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.core.addons.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;

public class IngamePhase implements PhaseControl {


    private final HashSet<NormalGenerator> generators;
    private int islandgenerators;

    public IngamePhase() {
        generators = new HashSet<>();
        islandgenerators = -1;
    }

    @Override
    public int onTimerCall(Arena arena) {

        arena.setTime(arena.getTime()+1);
        int time = arena.getTime();

        if(islandgenerators == time) {
            for(Island island : arena.getIslands()){
                island.getGenerator().getWorld().dropItem(island.getGenerator(), new ItemStack(Material.IRON_INGOT));
            }
            islandgenerators = arena.getTime()+5;
        }

        int i = time/60;
        for(NormalGenerator generator : generators) {
            if(generator.reset(time)){
                generator.spawn();
                generator.setNewReset();
                Hologram hologram = generator.getHologram();
                if(hologram != null)
                    hologram.editText("§c" + (i < 10 ? "0"+i+":" : i+":") + (time%60 < 10 ? "0"+time%60 :time%60));
            }
        }
        return time;
    }

    @Override
    public void init(Arena arena) {
        islandgenerators = arena.getTime()+5;


        for(Location diamond : arena.getDiamond()){
            generators.add(new NormalGenerator(diamond, Material.DIAMOND, new Hologram(diamond.add(0.0D, 1.6D,0.0D), "10:00"), 80));
        }
        for(Location emerald : arena.getEmerald()){
            generators.add(new NormalGenerator(emerald, Material.EMERALD, new Hologram(emerald.add(0.0D, 1.6D,0.0D), "10:00"), 90));
        }

        int comp = arena.getTeamcomposition();
        ChatColor[] teams  = {ChatColor.RED,ChatColor.BLUE, ChatColor.GREEN, ChatColor.BLACK, ChatColor.GRAY, ChatColor.WHITE};


    }

    @Override
    public void stop(Arena engine) {

    }

    @Override
    public boolean end(Arena engine) {
        return false;
    }

    @Override
    public PhaseControl next() {
        return new EndPhase();
    }



}
