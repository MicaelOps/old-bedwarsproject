package br.com.logicmc.bedwars;

import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.extra.Schematic;
import br.com.logicmc.bedwars.extra.StaffArena;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.bedwars.listeners.InventoryListeners;
import br.com.logicmc.bedwars.listeners.PhaseListener;
import br.com.logicmc.bedwars.listeners.PlayerListeners;
import br.com.logicmc.core.addons.hologram.Hologram;
import br.com.logicmc.core.system.command.CommandLoader;
import br.com.logicmc.core.system.minigame.ArenaInfoPacket;
import br.com.logicmc.core.system.minigame.MinigamePlugin;
import br.com.logicmc.core.system.mysql.MySQL;
import br.com.logicmc.core.system.redis.packet.PacketManager;
import br.com.logicmc.core.system.server.ServerState;
import br.com.logicmc.core.system.server.ServerType;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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
    public YamlFile mainconfig;
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


        Bukkit.getPluginManager().registerEvents(new InventoryListeners(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
        Bukkit.getPluginManager().registerEvents(new PhaseListener(), this);

        messagehandler.loadMessage(BWMessages.PLAYER_LEAVE_INGAME, this);
        CommandLoader.loadPackage(this, BWMain.class, "br.com.logicmc.bedwars.commands");
        BWManager.getInstance().addGame("staff", new StaffArena());
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
            System.out.println("arena update " +arena);
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

    @Deprecated
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
            if(world.getName().equalsIgnoreCase("world"))
                continue;
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
                
                WorldCreator wc = new WorldCreator(arena);
                wc.type(WorldType.FLAT);
                wc.generatorSettings("2;0;1;"); 
                wc.createWorld();
                world = Bukkit.createWorld(wc);
                System.out.println("[Arena] Pasting lobby for "+arena);
                lobby.paste(new Location(world, 0, 100, 0));
            }


            Schematic schematic = Schematic.read(new File(getDataFolder(), arena+".schematic"));

            if(schematic == null)
                schematics.remove(arena+".schematic");
            else {
                System.out.println("[Arena] Pasting map for "+arena);
                schematic.paste(new Location(world, 250, 100, 250));

                HashSet<Island> islands =new HashSet<>();
                HashSet<NormalGenerator> diamond = new HashSet<>(),emerald =new HashSet<>();
                AtomicReference<Location> lobbyloc = new AtomicReference<>();
                spawnlocation.setWorld(world);
                lobbyloc.set(spawnlocation);
                String finalArena = arena;
                
                mainconfig.loopThroughSectionKeys(arena, (visland) -> {

                    if(visland.equalsIgnoreCase("islands"))
                        mainconfig.loopThroughSectionKeys(finalArena +".islands", (island)->{       
                             islands.add(new Island(island, BWTeam.valueOf(mainconfig.getConfig().getString(finalArena+".islands."+island+".color")),mainconfig.getLocation(finalArena +".islands."+island+".spawn"),mainconfig.getLocation(finalArena +".islands."+island+".npc") , mainconfig.getLocation(finalArena +".islands."+island+".bed"), mainconfig.getLocation(finalArena +".islands."+island+".generator")));
                        });
                    else if(visland.equalsIgnoreCase("diamond"))
                        mainconfig.loopThroughSectionKeys(finalArena +".diamond", (string)->diamond.add(new NormalGenerator(mainconfig.getLocation(finalArena +".diamond."+string), Material.DIAMOND,
                                null, 80)));
                    else if(visland.equalsIgnoreCase("emerald"))
                        mainconfig.loopThroughSectionKeys(finalArena +".emerald", (string)->emerald.add(new NormalGenerator(mainconfig.getLocation(finalArena +".emerald."+string), Material.EMERALD,
                                null, 90)));

                });

                world.setStorm(false);
                world.setAutoSave(false);
                world.setThundering(false);
                world.setThunderDuration(0);
                world.setWeatherDuration(0);
                world.setDifficulty(Difficulty.PEACEFUL);
                world.getEntities().forEach(Entity::remove);
                world.getLivingEntities().forEach(LivingEntity::remove);
                world.setGameRuleValue("doDaylightCycle", "false");
        
                for(Island island : islands) { // debug arenas
                    island.report(arena);
                    island.getGenerator().getLocation().setWorld(world);
                    island.getNpc().setWorld(world);
                    island.getSpawn().setWorld(world);
                    island.getBed().setWorld(world);
                    
                }
                for(NormalGenerator generator : diamond) { 
                    generator.getLocation().setWorld(world);
                    generator.setHologram(new Hologram(generator.getLocation().add(0.0D, 0.9D, 0.0D), "10:00"));
                }
                for(NormalGenerator generator : emerald) { 
                    generator.getLocation().setWorld(world);
                    generator.setHologram(new Hologram(generator.getLocation().add(0.0D, 0.9D, 0.0D), "10:00"));
                }
                BWManager.getInstance().addGame(arena, new Arena(arena, 8, Arena.DUO, lobbyloc.get(), islands, diamond,emerald));
                BWManager.getInstance().getArena(arena).startTimer(BWMain.getInstance());
                
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




    public void updateSuffix(Player player, Scoreboard sc, String team, String suffix) {
        net.minecraft.server.v1_8_R3.Scoreboard scoreboard = ((CraftScoreboard)sc).getHandle();
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

    @Override
    public BWPlayer createDataInstance() {
        return new BWPlayer();
    }
}
