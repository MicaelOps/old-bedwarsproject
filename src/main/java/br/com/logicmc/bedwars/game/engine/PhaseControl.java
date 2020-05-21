package br.com.logicmc.bedwars.game.engine;

public interface PhaseControl {

    int onTimerCall(GameEngine engine);

    void init(GameEngine engine);

    void stop(GameEngine engine);

    boolean end(GameEngine engine);

    PhaseControl next();
}
