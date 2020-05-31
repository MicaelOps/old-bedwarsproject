package br.com.logicmc.bedwars.game.addons;

import br.com.logicmc.bedwars.game.engine.Arena;

public class TimeScheduler implements Runnable{

    private final Arena arena;

    public TimeScheduler(Arena engine) {
        this.arena = engine;
    }

    @Override
    public void run() {

        arena.changeTime();

    }
}
