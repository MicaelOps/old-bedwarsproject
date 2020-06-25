package br.com.logicmc.bedwars.game.phase;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.Schematic;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.PhaseControl;

public class EndPhase implements PhaseControl {

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
            arena.getBlocks().forEach(simpleBlock -> simpleBlock.toLocation(arena.getName()).getBlock().setType(Material.AIR));
            arena.getBlocks().clear();
            arena.getDestroyedBlocks().forEach(destroyedBlock -> {
                Block block = destroyedBlock.getSimpleBlock().toLocation(arena.getName()).getBlock();
                block.setType(destroyedBlock.getType());
                block.setData(destroyedBlock.getData());
            });
            Bukkit.getWorld(arena.getName()).getEntitiesByClass(EnderDragon.class).forEach(Entity::remove);
            arena.getPreteam().clear();
            arena.getPlayers().clear();
            arena.getIslands().clear();
            time = 2;
            World world = Bukkit.getWorld(arena.getName());
            world.getEntities().forEach(Entity::remove);
            world.getLivingEntities().forEach(LivingEntity::remove);
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

            arena.getDestroyedBlocks().clear();
            arena.setGamestate(Arena.WAITING);
            arena.getPhase(0).preinit(arena);
            arena.changePhase();

        }
        return time;
    }

    @Override
    public int getIndex() {
        return 6;
    }

    @Override
    public void init(Arena arena) {
        arena.setGamestate(Arena.END);
        kickall = arena.getTime()+10;
        if(!arena.getPlayers().isEmpty()){
            BWTeam winner = null;
            for(UUID uuid : arena.getPlayers()){
                Player player = Bukkit.getPlayer(uuid);
                BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(uuid);
                if(player.getGameMode() == GameMode.SURVIVAL){
                    winner = BWTeam.valueOf(bwPlayer.getTeamcolor());
                    arena.updateScoreboardForAll("winner",winner.getChatColor()+bwPlayer.getTeamcolor());
                }
                if(winner == BWTeam.valueOf(bwPlayer.getTeamcolor())){
                    bwPlayer.increaseWins();
                } else {
                    bwPlayer.increaseLoses();
                }
            }
            if(winner != null) {
                BWTeam finalWinner = winner;
                for(UUID uuid : arena.getPlayers()){
                    Player player = Bukkit.getPlayer(uuid);
                    String lang = BWMain.getInstance().getLang(player);
                    if(BWManager.getInstance().getBWPlayer(uuid).getTeamcolor().equalsIgnoreCase(winner.name())){
                        player.sendTitle(BWMain.getInstance().messagehandler.getMessage(BWMessages.HEADTITLE_VICTORY, lang), BWMain.getInstance().messagehandler.getMessage(BWMessages.LOWERTITLE_VICTORY, lang));
                    } else {
                        player.sendTitle(BWMain.getInstance().messagehandler.getMessage(BWMessages.HEADTITLE_GAMELOST, lang), BWMain.getInstance().messagehandler.getMessage(BWMessages.LOWERTITLE_GAMELOST, lang));
                    }
                }
            }
        }
    }


    @Override
    public void stop(Arena arena) {
        arena.forEachScoreboard(scoreboard ->scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister());
        arena.forEachScoreboard(scoreboard ->scoreboard.getTeams().forEach(Team::unregister));
    }



    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(entry);
    }

    @Override
    public void preinit(Arena arena) {

    }

    @Override
    public Scoreboard createScoreboard(Arena arena, String lang, Scoreboard scoreboard) {
        if(scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

        if(objective == null) {
            objective = scoreboard.registerNewObjective("end" + new Random().nextInt(10000), "dummy");
            objective.setDisplayName("§e§lBED WARS");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        }
        objective.getScore("§4").setScore(4);
        objective.getScore("§3").setScore(3);
        objective.getScore("§2").setScore(2);
        objective.getScore("§1").setScore(1);
        objective.getScore("§0").setScore(0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        createTeam(scoreboard, "date","§7"+dtf.format(now),"","§4");

        createTeam(scoreboard, "winner", "§f"+BWMain.getInstance().messagehandler.getMessage(BWMessages.WORD_TEAM, lang)+" ","","§2");
        createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§0");
        return scoreboard;
    }
}
