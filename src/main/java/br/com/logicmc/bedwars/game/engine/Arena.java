package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.BWMain;

import br.com.logicmc.bedwars.game.addons.TimeScheduler;
import br.com.logicmc.bedwars.game.phase.WaitingPhase;
import br.com.logicmc.core.system.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class Arena {


    public static final int WAITING = 0,INGAME=1,END=2;
    public static final int DUO = 2, SOLO  = 1, QUADRA = 4;

    private final String name;
    final int maxplayers;

    private final HashMap<UUID, ChatColor> preteam = new HashMap<>();

    private final int teamcomposition;
    private final HashSet<UUID> players;
    private final HashSet<Island> islands;
    private final HashSet<Location> diamond,emerald;

    private Location lobby;
    private BukkitTask task;
    private PhaseControl phaseControl;
    private int gamestate , time,allotedplayers;



    public Arena(String name, int maxplayers, int teamcomposition, Location lobby, HashSet<Island> islands, HashSet<Location> diamond, HashSet<Location> emerald) {
        this.name = name;
        this.maxplayers = maxplayers;
        this.teamcomposition = teamcomposition;
        this.diamond = diamond;
        this.emerald = emerald;
        this.islands = islands;
        this.lobby = lobby;

        players = new HashSet<>();
        time = 0;
        allotedplayers = 0;
        gamestate = WAITING;
        phaseControl = new WaitingPhase();
    }

    public HashMap<UUID, ChatColor> getPreteam() {
        return preteam;
    }

    public HashSet<Island> getIslands() {
        return islands;
    }

    public HashSet<Location> getDiamond() {
        return diamond;
    }

    public HashSet<Location> getEmerald() {
        return emerald;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTeamcomposition() {
        return teamcomposition;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
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

    public void updateScoreboardForAll(String team, String suffix) {
        for(UUID ingameplayers : getPlayers()) {
            Player ingamePlayer = Bukkit.getPlayer(ingameplayers);
            updateScoreboardTeam(ingamePlayer, team, suffix);
        }
    }
    public void updateScoreboardTeam(Player player, String team, String suffix) {
        BWMain.getInstance().updateSuffix(player, team, suffix);
    }
}
