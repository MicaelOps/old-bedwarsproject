package br.com.logicmc.bedwars.game.phase;

import br.com.logicmc.bedwars.game.engine.GameEngine;
import br.com.logicmc.bedwars.game.engine.PhaseControl;

public class IngamePhase implements PhaseControl {
    @Override
    public int onTimerCall(GameEngine engine) {
        int time = engine.getTime();
        time++;
        return time;
    }

    @Override
    public void init(GameEngine engine) {

    }

    @Override
    public void stop(GameEngine engine) {

    }

    @Override
    public boolean end(GameEngine engine) {
        return false;
    }

    @Override
    public PhaseControl next() {
        return null;
    }
}
