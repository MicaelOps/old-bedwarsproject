package br.com.logicmc.bedwars;

import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.extra.Schematic;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.addons.VoidWorld;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.account.addons.Preferences;
import br.com.logicmc.core.system.command.CommandLoader;
import br.com.logicmc.core.system.minigame.ArenaInfoPacket;
import br.com.logicmc.core.system.minigame.MinigamePlugin;
import br.com.logicmc.core.system.mysql.MySQL;
import br.com.logicmc.core.system.redis.packet.PacketManager;
import br.com.logicmc.core.system.server.ServerState;
import br.com.logicmc.core.system.server.ServerType;
import com.google.gson.Gson;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BWMain extends MinigamePlugin<BWPlayer> {

    private static BWMain instance;
    private YamlFile mainconfig;
    private boolean maintenance;
    private Location spawnlocation;


    @Override
    public void onEnable() {
        instance = this;
        
        if(!loadConfig()) {
            System.out.println("Error while loading config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }


        spawnlocation = mainconfig.getLocation("spawn");
        maintenance = mainconfig.getConfig().getBoolean("maintenance");
        if(spawnlocation == null)
            System.out.println("[Arena] Lobby location is null");


        for(Arena arena : BWManager.getInstance().getArenas()) {
            arena.startTimer(this);
        }
        
        super.onEnable();

        messagehandler.loadMessage(BWMessages.PLAYER_LEAVE_INGAME, this);
        CommandLoader.loadPackage(this, "br.com.logicmc.bedwars.commands");
    }

    public static BWMain getInstance() {
        return instance;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.GAME;
    }

    @Override
    public boolean isAvailable(String arenaname, int size) {
        return BWManager.getInstance().getArena(arenaname).hasSpaceforPlayer();
    }

    @Override
    public void allocateSpace(String arenaname, int size) {
        BWManager.getInstance().getArena(arenaname).incrementAllotedPlayers();
    }

    @Override
    public Consumer<String> getUpdateArenaMethod() {
        return (arena) -> {
            Arena gameEngine = BWManager.getInstance().getArena(arena);
            PacketManager.getInstance().sendChannelPacket(this, "lobby", new ArenaInfoPacket(Bukkit.getServerName(), arena, true, gameEngine.getPlayers().size(), gameEngine.getServerState()));
        };
    }

    @Override
    public ServerState getArenaState(String arenaname) {
        return BWManager.getInstance().getArena(arenaname).getServerState();
    }
    private void deleteFolder(File folder) {
        for(File file : folder.listFiles()) {
            if(file.isDirectory())
                deleteFolder(file);
            else
                file.delete();
        }
        folder.delete();
    }

    @Override
    public List<String> loadArenas() {
        List<String> schematics = mainconfig.getConfig().getStringList("schematics");

        if(schematics == null) 
            return new ArrayList<>();

        if(schematics.isEmpty()) 
            return new ArrayList<>();

        Schematic lobby = Schematic.read(getResource("LobbyBW.schematic"));
        boolean reload = false;
        // deleting unacessary worlds
        for(World world : Bukkit.getWorlds()) {
            boolean exist  = false;
            for(String arena : schematics) {
                if(world.getName().equalsIgnoreCase(arena.replace(".schematic",""))) {
                    exist = true;
                    reload = true;
                }
            }
            if(!exist) {
                for(Chunk chunk : world.getLoadedChunks()) {
                    chunk.unload();
                }
                File folder = world.getWorldFolder();
                Bukkit.unloadWorld(world, false);
                deleteFolder(folder);
            }
        }
        if(reload) {
            Bukkit.shutdown();
            return new ArrayList<>();
        }

        for(String arena : schematics) {

            arena = arena.replace(".schematic","");
            World world = Bukkit.getWorld(arena);

            if(world == null) {
                world = Bukkit.createWorld(new VoidWorld(arena));
                System.out.println("[Arena] Pasting lobby for "+arena);
                lobby.paste(new Location(world, 0, 100, 0));
            }


            Schematic schematic = Schematic.read(new File(getDataFolder(), arena+".schematic"));

            if(schematic == null)
                schematics.remove(arena+".schematic");
            else {
                System.out.println("[Arena] Pasting map for "+arena);
                prepareWorld(world);
                schematic.paste(new Location(world, 250, 100, 250));

                FileConfiguration config = mainconfig.getConfig();
                HashSet<Island> islands =new HashSet<>();
                HashSet<Location> diamond = new HashSet<>(),emerald =new HashSet<>();
                AtomicReference<Location> lobbyloc = new AtomicReference<>();

                String finalArena = arena;
                mainconfig.loopThroughSectionKeys(arena, (visland) -> {

                    if(visland.equalsIgnoreCase("islands"))
                        mainconfig.loopThroughSectionKeys(finalArena +".islands."+visland, (island)->{
                            lobbyloc.set(mainconfig.getLocation(finalArena + ".islands." + island + ".lobby"));
                             islands.add(new Island(island, mainconfig.getLocation(finalArena +".islands."+island+".npc") , mainconfig.getLocation(finalArena +".islands."+island+".bed"), mainconfig.getLocation(finalArena +".islands."+island+".generator")));
                        });
                    else if(visland.equalsIgnoreCase("diamond"))
                        mainconfig.loopThroughSectionKeys(finalArena +".diamond", (string)->diamond.add(mainconfig.getLocation(finalArena +".diamond")));
                    else if(visland.equalsIgnoreCase("emerald"))
                        mainconfig.loopThroughSectionKeys(finalArena +".emerald", (string)->emerald.add(mainconfig.getLocation(finalArena +".emerald")));

                });
                for(Island island : islands) { // debug arenas
                    island.report(arena);
                }
                BWManager.getInstance().addGame(arena, new Arena(arena, 12, Arena.SOLO, lobbyloc.get(), islands, diamond,emerald));
                
            }
        }

        return schematics;
    }

    @Override
    public BWPlayer read(MySQL mysql, UUID uuid) {
        return null;
    }

    @Override
    public void write(MySQL mysql, UUID uuid) { }



    private boolean loadConfig() {
        
        if(!getDataFolder().exists())
            getDataFolder().mkdirs();

        mainconfig = new YamlFile("config.yml");
        return mainconfig.loadResource(this);
    }


    private void prepareWorld(World world) {
        world.setStorm(false);
        world.setAutoSave(false);
        world.setThundering(false);
        world.setThunderDuration(0);
        world.setWeatherDuration(0);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.getEntities().forEach(Entity::remove);
        world.getLivingEntities().forEach(LivingEntity::remove);
        world.setGameRuleValue("doDaylightCycle", "false");
    }

    private void buildScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        scoreboard.getObjectives().forEach(Objective::unregister);
        scoreboard.getTeams().forEach(Team::unregister);
        Objective objective = scoreboard.registerNewObjective("skywars","dummy");

        objective.setDisplayName("§b§lBEDWARS");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // to be made
    }
    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(entry);
    }
    public void updateSuffix(Player player, String team, String suffix) {
        net.minecraft.server.v1_8_R3.Scoreboard scoreboard = ((CraftScoreboard)Bukkit.getScoreboardManager().getMainScoreboard()).getHandle();
        PacketPlayOutScoreboardTeam updatepacket = new PacketPlayOutScoreboardTeam(scoreboard.getTeam(team), 2);
        try {
            Field field = updatepacket.getClass().getDeclaredField("d");
            field.setAccessible(true);
            field.set(updatepacket, suffix);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(updatepacket);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            player.sendMessage("failed to update scoreboard please report this to the administrator.");
        }
    }

    public void giveItem(Player player, int slot, FixedItems item) {
        player.getInventory().setItem(slot, item.getBuild(messagehandler, playermanager.getPlayerBase(player).getPreferences().getLang()));
    }

    public boolean isMaintenance() {
        return maintenance;
    }
}
