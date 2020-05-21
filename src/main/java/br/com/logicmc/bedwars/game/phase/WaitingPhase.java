package br.com.logicmc.bedwars.game.phase;

import br.com.logicmc.bedwars.game.engine.GameEngine;
import br.com.logicmc.bedwars.game.engine.PhaseControl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class WaitingPhase implements PhaseControl {

    @Override
    public int onTimerCall(GameEngine engine) {

        int time = engine.getTime();
        time--;

        if(time != 0 ) {
            if(time > 59 && time%60 == 0)
                Bukkit.broadcastMessage(ChatColor.YELLOW + "O jogo comeca em "+time/60+" minuto(s)");
            else if(time < 6)
                Bukkit.broadcastMessage(ChatColor.YELLOW + "O jogo comeca em "+time+" segundo(s)");
        }
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
        return new IngamePhase();
    }
}
