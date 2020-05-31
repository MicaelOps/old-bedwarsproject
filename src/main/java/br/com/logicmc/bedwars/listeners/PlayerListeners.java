package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.core.events.PlayerJoinArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListeners implements Listener {
    
    private final BWMain plugin;
    
    public PlayerListeners() {
        plugin = BWMain.getInstance();
    }
    
    @EventHandler
    public void onjoinplayer(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onplayerjoinarena(PlayerJoinArenaEvent event) {
        Player player = event.getPlayer();


        plugin.utils.cleanPlayer(player);
        plugin.utils.clearChat(player);

        Arena arena = BWManager.getInstance().getArena(event.getArenaname());
        arena.getPlayers().add(player.getUniqueId());
        
        player.setScoreboard(arena.getScoreboard());
        if(event.getArenaname().equalsIgnoreCase("staff")) {
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
            plugin.giveItem(player, 0, FixedItems.STAFF_ARENA_SPECTATE);
        } else {
            for(Player other : Bukkit.getOnlinePlayers()) {
                if(!event.getArenaname().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(other.getUniqueId()).getMapname())) {
                    player.hidePlayer(other);
                }
            }
            arena.getPlayers().forEach((inuuid) ->plugin.messagehandler.sendMessage(Bukkit.getPlayer(inuuid), BWMessages.NEWPLAYER));
            BWManager.getInstance().getArena(event.getArenaname()).updateScoreboardForAll("players" , ChatColor.GRAY+""+arena.getPlayers().size());

            player.teleport(arena.getLobby());
            plugin.giveItem(player, 0, FixedItems.ONLY_VIP_CHOOSETEAM);
        }
        
        
    }
    @EventHandler
    public void onquitplayer(PlayerQuitEvent event) {
        Arena arena = BWManager.getInstance().getArena(event.getPlayer().getWorld().getName());
        event.setQuitMessage(null);

        arena.getPlayers().remove(event.getPlayer().getUniqueId());

        if(arena.getName().equalsIgnoreCase("staff"))
            return;

        if(arena.getGamestate() == Arena.WAITING) {
            arena.decrementAllotedPlayers();
        } else {
            for(UUID ingameplayers : arena.getPlayers()) {

                Player ingamePlayer = Bukkit.getPlayer(ingameplayers);

                if(ingamePlayer != null)
                    plugin.messagehandler.sendMessage(ingamePlayer, BWMessages.PLAYER_LEAVE_INGAME);

                arena.updateScoreboardTeam(ingamePlayer, "players" , ChatColor.GRAY+""+arena.getPlayers().size());
            }
        }
    }
}
