package br.com.logicmc.bedwars.game;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.player.BWPlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class BWManager {


    private static BWManager instance;
    private final HashMap<String, Arena> arenas = new HashMap<>();

    public void addGame(String name, Arena arena) {
        arenas.put(name, arena);
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Arena getArenabyUUID(UUID uuid) {
        return arenas.get(BWMain.getInstance().playermanager.getPlayerBase(uuid).getData().getMapname());
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public BWPlayer getBWPlayer(UUID uuid) {
        return BWMain.getInstance().playermanager.getPlayerBase(uuid).getData();
    }


    public static BWManager getInstance() {

        if(instance == null)
            instance = new BWManager();

        return instance;
    }

}
