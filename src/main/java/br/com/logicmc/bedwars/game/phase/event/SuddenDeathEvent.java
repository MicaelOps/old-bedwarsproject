package br.com.logicmc.bedwars.game.phase.event;

import br.com.logicmc.bedwars.extra.customentity.EntityManager;
import br.com.logicmc.bedwars.game.engine.Arena;


public class SuddenDeathEvent extends PhaseEvent{
    public SuddenDeathEvent(int inittime) {

        super(inittime, "Morte subita");
    }

    @Override
    public void execute(Arena arena) {
        EntityManager.getInstance().spawnDragon(arena.getDiamond().iterator().next().getLocation());
    }
}
