package br.com.logicmc.bedwars.game.engine;

import org.bukkit.scoreboard.Scoreboard;

public interface PhaseControl {

    int onTimerCall(Arena arena);

    int getIndex();

    void preinit(Arena arena);

    void init(Arena arena);

    void stop(Arena arena);

    Scoreboard createScoreboard(String lang, Scoreboard scoreboard);

}
