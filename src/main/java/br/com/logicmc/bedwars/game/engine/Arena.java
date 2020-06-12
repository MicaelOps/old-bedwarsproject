package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.BWMain;

import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.addons.TimeScheduler;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.phase.EndPhase;
import br.com.logicmc.bedwars.game.phase.IngamePhase;
import br.com.logicmc.bedwars.game.phase.WaitingPhase;
import br.com.logicmc.core.system.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;


public class Arena {


    public static final int WAITING = 0,INGAME=1,END=2;
    public static final int DUO = 2, SOLO  = 1, QUADRA = 4;

    private final String name;
    final int maxplayers;

    private final HashMap<UUID, String> preteam = new HashMap<>();

    private final int teamcomposition;
    private final HashSet<UUID> players;
    private final HashSet<Island> islands;
    private final HashSet<Location> blocks;
    private final HashSet<NormalGenerator> diamond,emerald;
    private final PhaseControl[] controls = {new WaitingPhase(), new IngamePhase(), new EndPhase()};

    private Location lobby;
    private BukkitTask task;
    private int phaseControl;
    private int gamestate , time,allotedplayers;



    public Arena(String name, int maxplayers, int teamcomposition, Location lobby, HashSet<Island> islands, HashSet<NormalGenerator> diamond, HashSet<NormalGenerator> emerald) {
        this.name = name;
        this.maxplayers = maxplayers;
        this.teamcomposition = teamcomposition;
        this.diamond = diamond;
        this.emerald = emerald;
        this.islands = islands;
        this.lobby = lobby;
        blocks = new HashSet<>();
        players = new HashSet<>();
        time = 500;
        allotedplayers = 0;
        gamestate = WAITING;
        phaseControl = 0;

        initScoreboards();

    }

    private void initScoreboards(){
        for(PhaseControl control : controls) {
            control.preinit(this);
        }
    }
    public HashSet<Location> getBlocks() {
        return blocks;
    }

    public HashMap<UUID, String> getPreteam() {
        return preteam;
    }

    public HashSet<Island> getIslands() {
        return islands;
    }

    public HashSet<NormalGenerator> getDiamond() {
        return diamond;
    }

    public HashSet<NormalGenerator> getEmerald() {
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
    public Scoreboard getScoreboard() {
        return controls[phaseControl].getScoreboard();
    }

    public int getGamestate() {
        return gamestate;
    }

    public void setGamestate(int gamestate) {
        this.gamestate = gamestate;
    }


    public BukkitTask getTask() {
        return task;
    }

    public void changePhase() {
        controls[phaseControl].stop(this);

        if(controls[phaseControl] instanceof EndPhase) {
            initScoreboards();
            BWMain.getInstance().updateArena(getName());
            phaseControl = 0;
        }else
            phaseControl+=1;

        for(UUID ingameplayers : getPlayers()) {
            Player ingamePlayer = Bukkit.getPlayer(ingameplayers);
            ingamePlayer.setScoreboard(controls[phaseControl].getScoreboard());
        }
        controls[phaseControl].init(this);
    }
    public void changeTime() {
        time = controls[phaseControl].onTimerCall(this);
    }

    public int getTime() {
        return time;
    }


    public boolean checkend(){
        return getPlayers().stream().filter(uuid -> Bukkit.getPlayer(uuid).getGameMode()==GameMode.SURVIVAL).count() <= getTeamcomposition();
    }
    public boolean hasSpaceforPlayer() {
        return allotedplayers < maxplayers;
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

    /***
     * Decrease space for another player
     */
    public void decrementAllotedPlayers(){ allotedplayers--; }
    /***
     * Starts arena timer
     */
    public void startTimer(JavaPlugin plugin) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, new TimeScheduler(this), 0L, 20L);
    }

    public String getName() {
        return name;
    }

    public void brocastTimeMessage(BWMessages messages, int time) {
        for(UUID ingameplayers : getPlayers()) {
            Bukkit.getPlayer(ingameplayers).sendMessage(BWMain.getInstance().messagehandler.getMessage(messages, BWMain.getInstance().getLang(Bukkit.getPlayer(ingameplayers))).replace("{time}",""+time));
        }
    }
    public void updateScoreboardForAll(String team, String suffix) {
        for(UUID ingameplayers : getPlayers()) {
            Player ingamePlayer = Bukkit.getPlayer(ingameplayers);
            updateScoreboardTeam(ingamePlayer, team, suffix);
        }
    }
    public void updateScoreboardTeam(Player player, String team, String suffix) {
        BWMain.getInstance().updateSuffix(player, player.getScoreboard(), team, suffix);
    }

    /***
     * Saving arena to config
     */
    public void save() {
        YamlFile file = BWMain.getInstance().mainconfig;
        cleanSection(file, getName()+".diamond");
        cleanSection(file, getName()+".emerald");
        diamond.forEach(diamondd -> {
            file.setLocation(getName()+".diamond.d"+new Random().nextInt(10000), diamondd.getLocation());
        });
        emerald.forEach(emeraldd -> {
            file.setLocation(getName()+".emerald.e"+new Random().nextInt(10000), emeraldd.getLocation());
        });
        islands.forEach(island -> island.save(getName(), file));
    }
    private void cleanSection(YamlFile config, String path) {
        ConfigurationSection section = config.getConfig().getConfigurationSection(path);
        if(section != null) {
            section.getKeys(false).forEach((string)->config.getConfig().set(path,null)); // deleting old
        }
    }
}
