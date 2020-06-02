package br.com.logicmc.bedwars.game.engine;


public interface PhaseControl {

    int onTimerCall(Arena arena);

    void preinit(Arena arena);

    void init(Arena arena);

    void stop(Arena arena);

    boolean end(Arena arena);

    org.bukkit.scoreboard.Scoreboard getScoreboard();


    PhaseControl next();
}
