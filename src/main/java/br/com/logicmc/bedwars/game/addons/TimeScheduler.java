package br.com.logicmc.bedwars.game.addons;

import br.com.logicmc.bedwars.game.engine.Arena;

public class TimeScheduler implements Runnable{

    private final Arena engine;

    public TimeScheduler(Arena engine) {
        this.engine = engine;
    }

    @Override
    public void run() {

        engine.changeTime();

        int time = engine.getTime(), i = time/60;
        //Bukkit.getScoreboardManager().getMainScoreboard().getTeam("time").setSuffix("§a" + (i < 10 ? "0"+i+":" : i+":") + (time%60 < 10 ? "0"+time%60 :time%60));
    }
}
