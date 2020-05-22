package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
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
        BWManager.getInstance().addNewPlayer(new BWPlayer(player.getUniqueId(), "doces")); // for test purposes

        if(event.getArenaname().equalsIgnoreCase("staff")) {

        }

        for(Player other : Bukkit.getOnlinePlayers()) {
            if(!mapname.equalsIgnoreCase(BWManager.getInstance().getBWPlayer(other.getUniqueId()).getMapname())) {
                player.hidePlayer(other);
            }
        }
    }
    @EventHandler
    public void onquitplayer(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Arena arena = BWManager.getInstance().getArenabyUUID(uuid);
        event.setQuitMessage(null);

        arena.getPlayers().remove(uuid);

        if(arena.getGamestate() == Arena.WAITING) {
            arena.decrementAllotedPlayers();
        } else {
            arena.
        }
        BWMain.getInstance().updateArena(arena.getName());
    }

    public void preparePlayer() {

    }
}
