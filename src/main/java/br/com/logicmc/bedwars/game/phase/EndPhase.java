package br.com.logicmc.bedwars.game.phase;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.PhaseControl;

public class EndPhase implements PhaseControl {

    private Scoreboard scoreboard;
    private int kickall;
    
    @Override
    public int onTimerCall(Arena arena) {
        int time = arena.getTime();
        time+=1;


        for(UUID uuid : arena.getPlayers()){
            Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.FIREWORK_LAUNCH, 20F, 20F);
        }


        if(time == kickall) {
            for(UUID uuid : arena.getPlayers()){
                BWMain.getInstance().sendRedirect(Bukkit.getPlayer(uuid),"lobbybedwars-1");
            }
        } else if(time == (kickall+5)){
            arena.getBlocks().forEach(location -> location.getBlock().setType(Material.AIR));
            arena.getBlocks().clear();
            arena.getPreteam().clear();
            arena.getPlayers().clear();
            arena.getIslands().clear();
            arena.setTime(0);
            World world = Bukkit.getWorld(arena.getName());
            world.getEntities().forEach(Entity::remove);
            world.getLivingEntities().forEach(LivingEntity::remove);

            arena.setGamestate(Arena.WAITING);
            arena.changePhase();

            YamlFile mainconfig = BWMain.getInstance().mainconfig;
            
            mainconfig.loopThroughSectionKeys(arena.getName()+".islands", (visland) -> {
                arena.getIslands().add(new Island(visland, arena.getName(),
                        BWTeam.valueOf(mainconfig.getConfig()
                                .getString(arena.getName() + ".islands." + visland + ".color")),
                        mainconfig.getLocation(arena.getName() + ".islands." + visland + ".spawn"),
                        mainconfig.getLocation(arena.getName() + ".islands." + visland + ".npc"),
                        mainconfig.getLocation(arena.getName() + ".islands." + visland + ".upgrade"),
                        mainconfig.getLocation(arena.getName() + ".islands." + visland + ".bed"),
                        mainconfig.getLocation(arena.getName() + ".islands." + visland + ".generator")));
            });
            for (Island island : arena.getIslands()) { // debug arenas
                island.report(arena.getName());
                if (island.getGenerator().getLocation() != null)
                    island.getGenerator().getLocation().setWorld(world);
                if (island.getNpc() != null)
                    island.getNpc().setWorld(world);
                if (island.getSpawn() != null) {
                    island.getSpawn().setWorld(world);
                    island.getSpawn().add(0.0D, 1.0D, 0.0D);
                }
                if (island.getBed() != null)
                    island.getBed().setWorld(world);
                if (island.getUpgrade() != null)
                    island.getUpgrade().setWorld(world);
            }
        }
        return time;
    }

    @Override
    public void init(Arena arena) {
        System.out.println("Restarting "+arena.getName());
        arena.setGamestate(Arena.END);
        kickall = arena.getTime()+10;
        if(!arena.getPlayers().isEmpty()){
            BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(arena.getPlayers().stream().filter(uuid-> Bukkit.getPlayer(uuid).getGameMode()== GameMode.SURVIVAL).findFirst().get());
            arena.updateScoreboardForAll("winner",BWTeam.valueOf(bwPlayer.getTeamcolor()).getChatColor()+bwPlayer.getTeamcolor());
            for(UUID uuid : arena.getPlayers()){
                Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.EXPLODE, 20F, 20F);
                Bukkit.getPlayer(uuid).sendTitle(ChatColor.BOLD+""+ChatColor.GREEN+"WINNER", "§fTeam "+ BWTeam.valueOf(bwPlayer.getTeamcolor()).getChatColor()+bwPlayer.getTeamcolor());
            }

        }

    }

    @Override
    public void stop(Arena arena) {
        scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
        scoreboard.getTeams().forEach(Team::unregister);
    }


    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(entry);
    }

    @Override
    public void preinit(Arena arena) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        if(scoreboard.getObjective(DisplaySlot.SIDEBAR) == null){
            Objective objective = scoreboard.registerNewObjective("end"+new Random().nextInt(10000),"dummy");

            objective.setDisplayName("§b§lBEDWARS");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            objective.getScore("§3").setScore(3);
            objective.getScore("§2").setScore(2);
            objective.getScore("§1").setScore(1);
            objective.getScore("§0").setScore(0);

            createTeam(scoreboard, "winner", "§fTeam ","","§2");
            createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§0");
        }
    }
}
