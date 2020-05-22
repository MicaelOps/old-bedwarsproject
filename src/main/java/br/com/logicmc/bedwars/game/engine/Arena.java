package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.game.addons.TimeScheduler;
import br.com.logicmc.bedwars.game.phase.WaitingPhase;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.system.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class Arena {


    public static final int WAITING = 0,INGAME=1,END=2;

    private final String name;
    final int maxplayers;

    private final HashMap<Color, BWTeam> teams = new HashMap<>();

    private final HashSet<UUID> players;

    private BukkitTask task;
    private PhaseControl phaseControl;
    private int gamestate , time,allotedplayers;

    public Arena(String name, int maxplayers) {
        this.name = name;
        this.maxplayers = maxplayers;

        players = new HashSet<>();
        time = 0;
        allotedplayers = 0;
        gamestate = WAITING;
        phaseControl = new WaitingPhase();
    }

    public HashSet<UUID> getPlayers() {
        return players;
    }

    public int getGamestate() {
        return gamestate;
    }

    public void setGamestate(int gamestate) {
        this.gamestate = gamestate;
    }

    public boolean endOfPhase(){
        return phaseControl.end(this);
    }

    public void changePhase() {
        phaseControl.stop(this);
        phaseControl = phaseControl.next();
        phaseControl.init(this);
    }
    public void changeTime() {
        time = phaseControl.onTimerCall(this);
    }

    public int getTime() {
        return time;
    }

    public boolean hasSpaceforPlayer() {
        return getPlayers().size() == allotedplayers;
    }
    /***
     * Checks if arena state and returns it according to the ServerState class
     * @return if arena is available or not
     */
    public ServerState getServerState() {
        return  gamestate == WAITING ?  ServerState.ONLINE : ServerState.UNAVAIBLE;
    }

    /***
     * Add space for another player
     */
    public void incrementAllotedPlayers() {
        allotedplayers++;
    }

    public void decrementAllotedPlayers(){ allotedplayers--; }
    /***
     * Starts arena timer
     */
    public void startTimer(JavaPlugin plugin) {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new TimeScheduler(this), 0L, 20L);
    }

    public String getName() {
        return name;
    }
}
