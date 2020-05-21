package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.game.phase.WaitingPhase;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.system.server.ServerState;
import org.bukkit.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class GameEngine {


    public static final int WAITING = 0,INGAME=1,END=2;

    private final HashMap<Color, BWTeam> teams = new HashMap<>();

    private final HashSet<UUID> players;
    private PhaseControl phaseControl;
    private final int maxplayers;
    private int gamestate , time,allotedplayers;

    public GameEngine(int maxplayers) {
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

    /***
     * Checks if arena has started ou if not checkts if it has enough space to join
     * @return if arena is available or not
     */
    public ServerState getServerState() {
        return  gamestate == WAITING ? allotedplayers == maxplayers ? ServerState.UNAVAIBLE : ServerState.ONLINE : ServerState.UNAVAIBLE;
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

    /***
     * Add space for another player
     */
    public void incrementAllotedPlayers() {
        allotedplayers++;
    }
}
