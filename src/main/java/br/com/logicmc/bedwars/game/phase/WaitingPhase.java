package br.com.logicmc.bedwars.game.phase;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.PhaseControl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class WaitingPhase implements PhaseControl {

    @Override
    public int onTimerCall(Arena arena) {

        int time = arena.getTime();
        time--;

        if(time != 0 ) {
            if(time > 59 && time%60 == 0)
                Bukkit.broadcastMessage(ChatColor.YELLOW + "O jogo comeca em "+time/60+" minuto(s)");
            else if(time < 6)
                Bukkit.broadcastMessage(ChatColor.YELLOW + "O jogo comeca em "+time+" segundo(s)");
        } else {
            int size = arena.getPlayers().size();
            if(size == 0)
                time = 60;

            if(BWMain.getInstance().isMaintenance() ) {
                if (size > 0)
                    arena.changePhase();
                else
                    time = 120;
            } else if(size < 4)
                time = 60;

        }
        return time;
    }

    @Override
    public void init(Arena arena) { }

    @Override
    public void stop(Arena arena) {

    }

    @Override
    public boolean end(Arena arena) {
        return false;
    }

    @Override
    public PhaseControl next() {
        return new IngamePhase();
    }

}
