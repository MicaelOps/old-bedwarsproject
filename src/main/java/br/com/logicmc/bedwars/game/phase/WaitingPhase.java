package br.com.logicmc.bedwars.game.phase;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.PhaseControl;

import java.util.Random;

import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class WaitingPhase implements PhaseControl {

    private Scoreboard scoreboard;

    @Override
    public int onTimerCall(Arena arena) {

        int time = arena.getTime();
        time--;

        if(time != 0 ) {
            if(time > 59 && time%60 == 0)
                arena.broadcastMessage(ChatColor.YELLOW + "O jogo comeca em "+time/60+" minuto(s)");
            else if(time < 6)
                arena.broadcastMessage(ChatColor.YELLOW + "O jogo comeca em "+time+" segundo(s)");
        } else {
            int size = arena.getPlayers().size();
            if(size == 0)
                time = 60;

            if(BWMain.getInstance().isMaintenance() ) {
                if (size > 0) {
                    arena.changePhase();
                    return 0;
                } else
                    time = 120;
            } else if(size < 4)
                time = 60;

        }
        
        int i = time/60;
        arena.updateScoreboardForAll("time", "§a" + (i < 10 ? "0"+i+":" : i+":") + (time%60 < 10 ? "0"+time%60 :time%60));
        arena.setTime(time);
        return time;
    }

    @Override
    public void init(Arena arena) {

        arena.setTime(500);
     }

    @Override
    public void stop(Arena arena) {

    }

    @Override
    public boolean end(Arena arena) {
        return false;
    }

    @Override
    public PhaseControl next() {
        return new IngamePhase();
    }

 
    @Override
    public void preinit(Arena arena) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("waiting"+new Random().nextInt(10000),"dummy");

        objective.setDisplayName("§b§lBEDWARS");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore("§5").setScore(5);
		objective.getScore("§4").setScore(4);
		objective.getScore("§3").setScore(3);
		objective.getScore("§2").setScore(2);
		objective.getScore("§1").setScore(1);
		objective.getScore("§0").setScore(0);

		createTeam(scoreboard, "time", "§fTempo: ","§a00:00","§4");
		createTeam(scoreboard, "players", "§fOnline: ","§a-1","§2");
		createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§0");

		for(NormalGenerator normalGenerator : arena.getDiamond()) {
		    createArmostand(normalGenerator.getLocation().add(0.0d, 0.7D, 0.0D), Material.DIAMOND_BLOCK);
        }
        for(NormalGenerator normalGenerator : arena.getEmerald()) {
            createArmostand(normalGenerator.getLocation().add(0.0d, 0.7D, 0.0D), Material.EMERALD_BLOCK);
        }
        for(Island island : arena.getIslands()){
            Villager villager = (Villager) island.getNpc().getWorld().spawnEntity(island.getNpc(), EntityType.VILLAGER);
            villager.setCustomName("vc e ruim no pvp");
            villager.setCustomNameVisible(true);
        }

    }

    private void createArmostand(Location location, Material material){
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setHelmet(new ItemStack(material));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setSmall(false);
        armorStand.setCustomNameVisible(false);
    }

    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(entry);
    }


    @Override
    public Scoreboard getScoreboard() {
         return scoreboard;
    }
}
