package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.GameEngine;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.core.events.PlayerJoinArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onjoinplayer(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onplayerjoinarena(PlayerJoinArenaEvent event) {
        Player player = event.getPlayer();
        String mapname = "doces"; // test purposes
        for(Player other : Bukkit.getOnlinePlayers()) {
            if(!mapname.equalsIgnoreCase(BWManager.getInstance().getBWPlayer(other.getUniqueId()).getMapname())) {
                player.hidePlayer(other);
            }
        }
        BWManager.getInstance().addNewPlayer(new BWPlayer(player.getUniqueId(), "doces")); // for test purposes
    }
    @EventHandler
    public void onquitplayer(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        GameEngine gameEngine = BWManager.getInstance().getArenabyUUID(uuid);
        event.setQuitMessage(null);
        if(gameEngine.getGamestate() == GameEngine.INGAME) {

        }
        BWManager.getInstance().removePlayer(uuid);
    }

    public void preparePlayer() {

    }
}
