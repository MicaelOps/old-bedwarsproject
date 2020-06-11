package br.com.logicmc.bedwars.game.phase;

import java.util.Random;
import java.util.UUID;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.system.redis.packet.PacketManager;
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
    
    @Override
    public int onTimerCall(Arena arena) {
        int time = arena.getTime();
        time+=1;

        for(UUID uuid : arena.getPlayers()){
            Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.FIREWORK_BLAST, 20F, 20F);
        }


        if(time == 5) {

        } else if(time == 20){
            arena.getBlocks().forEach(location -> location.getBlock().setType(Material.AIR));
            arena.getBlocks().clear();
            arena.getPreteam().clear();
            arena.getPlayers().clear();
            arena.getIslands().forEach(Island::reset);
            arena.setTime(0);
            World world = Bukkit.getWorld(arena.getName());
            world.getEntities().forEach(Entity::remove);
            world.getLivingEntities().forEach(LivingEntity::remove);
            arena.initScoreboards();
            arena.setGamestate(Arena.WAITING);
            arena.changePhase();
        }
        return time;
    }

    @Override
    public void init(Arena arena) {
        BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(arena.getPlayers().stream().filter(uuid-> Bukkit.getPlayer(uuid).getGameMode()== GameMode.SURVIVAL).findFirst().get());
        arena.updateScoreboardForAll("winner", "§fTeam "+ BWTeam.valueOf(bwPlayer.getTeamcolor()).getChatColor()+bwPlayer.getTeamcolor());
        for(UUID uuid : arena.getPlayers()){
            Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.FIREWORK_LAUNCH, 20F, 20F);
            Bukkit.getPlayer(uuid).sendTitle(ChatColor.BOLD+""+ChatColor.GREEN+"WINNER", "§fTeam "+ BWTeam.valueOf(bwPlayer.getTeamcolor()).getChatColor()+bwPlayer.getTeamcolor());
        }


    }

    @Override
    public void stop(Arena arena) {
        scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
        scoreboard.getTeams().forEach(Team::unregister);
    }

    @Override
    public boolean end(Arena arena) {
        return false;
    }

    @Override
    public PhaseControl next() {
        return new WaitingPhase();
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

            createTeam(scoreboard, "winner", "§fTeam","§fganhou!","§2");
            createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§0");
        }
    }
}
