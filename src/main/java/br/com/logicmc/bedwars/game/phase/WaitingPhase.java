package br.com.logicmc.bedwars.game.phase;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.customentity.EntityManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.PhaseControl;

import java.util.Random;

import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import net.minecraft.server.v1_8_R3.Packet;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class WaitingPhase implements PhaseControl {

    @Override
    public int onTimerCall(Arena arena) {

        int time = arena.getTime();
        time--;

        if(time != 0 ) {
            if(time > 59 && time%60 == 0)
                arena.brocastTimeMessage(BWMessages.GAME_STARTS_IN_MINITUES,time/60);
            else if(time < 6)
                arena.brocastTimeMessage(BWMessages.GAME_STARTS_IN_SECONDS,time);
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
        

        return time;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public void init(Arena arena) {
        arena.setGamestate(Arena.WAITING);
        arena.setTime(500);
        arena.forEachScoreboard(scoreboard -> scoreboard.getTeam("mode").setSuffix("§a"+(arena.getTeamcomposition() == Arena.SOLO ? "Solo" : "Squad")));
        arena.forEachScoreboard(scoreboard -> scoreboard.getTeam("map").setSuffix("§a"+WordUtils.capitalize(arena.getName())));
     }

    @Override
    public void stop(Arena arena) {
        arena.forEachScoreboard(scoreboard ->scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister());
        arena.forEachScoreboard(scoreboard ->scoreboard.getTeams().forEach(Team::unregister));
    }



    @Override
    public void preinit(Arena arena) {

		for(NormalGenerator normalGenerator : arena.getDiamond()) {
		    createArmostand(normalGenerator.getLocation().clone().subtract(0.0D,4.0D, 0.0D), Material.DIAMOND_BLOCK);
        }
        for(NormalGenerator normalGenerator : arena.getEmerald()) {
            createArmostand(normalGenerator.getLocation().clone().subtract(0.0D,4.0D, 0.0D), Material.EMERALD_BLOCK);
        }
        for(Island island : arena.getIslands()){
            EntityManager.getInstance().spawnImmobileVillager(island.getNpc(), "Blocks & Tools");
            EntityManager.getInstance().spawnImmobileVillager(island.getUpgrade(), "Upgrades");
        }

    }

    @Override
    public Scoreboard createScoreboard(String lang, Scoreboard scoreboard) {
        if(scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

        if(objective == null) {
            objective = scoreboard.registerNewObjective("waiting" + new Random().nextInt(10000), "dummy");
            objective.setDisplayName("§e§lBED WARS");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        }

        objective.getScore("§8").setScore(8);
        objective.getScore("§7").setScore(7);
        objective.getScore("§6").setScore(6);
        objective.getScore("§5").setScore(5);
        objective.getScore("§4").setScore(4);
        objective.getScore("§3").setScore(3);
        objective.getScore("§2").setScore(2);
        objective.getScore("§1").setScore(1);
        objective.getScore("§0").setScore(0);

        createTeam(scoreboard, "mode", "§f"+getTranslatedMessage(BWMessages.WORD_MODE, lang)+": ","§a","§7");
        createTeam(scoreboard, "map", "§f"+getTranslatedMessage(BWMessages.WORD_MAP, lang)+": ","§a","§6");
        createTeam(scoreboard, "time", "§f"+getTranslatedMessage(BWMessages.WORD_TIME, lang),"","§4");
        createTeam(scoreboard, "players", "§f"+getTranslatedMessage(BWMessages.WORD_ONLINE, lang)+": ","§a-1","§2");
        createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§0");

        return scoreboard;
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


    private String getTranslatedMessage(BWMessages msg, String lang) {
        return BWMain.getInstance().messagehandler.getMessage(msg, lang);
    }

}
