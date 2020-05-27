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

        int time = arena.getTime(), i = time/60;
        arena.updateScoreboardForAll("time","§a" + (i < 10 ? "0"+i+":" : i+":") + (time%60 < 10 ? "0"+time%60 :time%60));
    }
}
