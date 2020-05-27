package br.com.logicmc.bedwars.game.phase;

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
}
