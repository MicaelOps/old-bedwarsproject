package br.com.logicmc.bedwars.game;

import br.com.logicmc.bedwars.game.engine.GameEngine;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import java.util.HashMap;
import java.util.UUID;

public class BWManager {


    private static BWManager instance;
    private final HashMap<String, GameEngine> arenas = new HashMap<>();
    private final HashMap<UUID, BWPlayer> playermanager = new HashMap<>();

    public void addGame(String name, GameEngine arena) {
        arenas.put(name, arena);
    }

    public GameEngine getArena(String name) {
        return arenas.get(name);
    }

    public GameEngine getArenabyUUID(UUID uuid) {
        return arenas.get(playermanager.get(uuid).getMapname());
    }
    public BWPlayer getBWPlayer(UUID uuid) {
        return playermanager.get(uuid);
    }
    public void addNewPlayer(BWPlayer player) {
        playermanager.put(player.getUuid(), player);
    }
    public void removePlayer(UUID uuid) {
        playermanager.remove(uuid);
    }
    public static BWManager getInstance() {

        if(instance == null)
            instance = new BWManager();

        return instance;
    }

}
