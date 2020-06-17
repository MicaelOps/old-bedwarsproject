package br.com.logicmc.bedwars.game.engine;


import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public interface PhaseControl {

    int onTimerCall(Arena arena);

    void preinit(Arena arena);

    void init(Arena arena);

    void stop(Arena arena);

    void translateScoreboard(Arena arena, Player player);

    Scoreboard createScoreboard(String lang);
    org.bukkit.scoreboard.Scoreboard[] getScoreboards();

}
