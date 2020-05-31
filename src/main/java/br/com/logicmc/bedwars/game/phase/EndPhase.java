package br.com.logicmc.bedwars.game.phase;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.PhaseControl;

public class EndPhase implements PhaseControl {
    @Override
    public int onTimerCall(Arena arena) {
        return 0;
    }

    @Override
    public void init(Arena arena) {

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
        return null;
    }

    @Override
    public Scoreboard buildScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("end"+new Random().nextInt(10000),"dummy");

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
        return scoreboard;
    }
    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(entry);
    }
}
