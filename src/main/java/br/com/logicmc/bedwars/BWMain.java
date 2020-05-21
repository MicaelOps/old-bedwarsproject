package br.com.logicmc.bedwars;

import br.com.logicmc.bedwars.extra.EmptyAccount;
import br.com.logicmc.bedwars.extra.Schematic;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.addons.VoidWorld;
import br.com.logicmc.bedwars.game.engine.GameEngine;
import br.com.logicmc.bedwars.game.engine.WorldArena;
import br.com.logicmc.core.CorePlugin;
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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BWMain extends MinigamePlugin<EmptyAccount> {

    private YamlFile mainconfig;

    @Override
    public void onEnable() {
        super.onEnable();

        if(!loadConfig()) {
            System.out.println("Error while loading config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        System.out.println(""+Bukkit.getWorlds().size());
        //buildScoreboard();
    }

    @Override
    public ServerType getServerType() {
        return ServerType.GAME;
    }

    @Override
    public boolean isAvailable(String arenaname) {
        return BWManager.getInstance().getArena(arenaname).getGamestate() == GameEngine.WAITING;
    }

    @Override
    public void allocateSpace(String arenaname) {
        BWManager.getInstance().getArena(arenaname).incrementAllotedPlayers();
    }

    @Override
    public Consumer<String> getUpdateArenaMethod() {
        return (arena) -> {
            GameEngine gameEngine = BWManager.getInstance().getArena(arena);
            PacketManager.getInstance().sendChannelPacket(this, "lobby", new ArenaInfoPacket(Bukkit.getServerName(), arena, true, gameEngine.getPlayers().size(), gameEngine.getServerState()));
        };
    }

    @Override
    public ServerState getArenaState(String arenaname) {
        return BWManager.getInstance().getArena(arenaname).getServerState();
    }

    @Override
    public List<String> loadArenas() {
        List<String> schematics = mainconfig.getConfig().getStringList("schematics"); //

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
                Bukkit.unloadWorld(world, false);
                // also delete folder
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
            }

            prepareWorld(world);

            Schematic schematic = Schematic.read(new File(getDataFolder(), arena+".schematic"));

            if(schematic == null)
                schematics.remove(arena+".schematic");
            else {
                schematic.paste(new Location(world, 0, 100, 0));
                BWManager.getInstance().addGame(arena, new GameEngine(12));
                // load chests etc...
            }
        }

        return schematics;
    }

    @Override
    public EmptyAccount read(MySQL mysql, UUID uuid) {
        return null;
    }

    @Override
    public void write(MySQL mysql, UUID uuid) { }

    private boolean loadConfig() {
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
}
