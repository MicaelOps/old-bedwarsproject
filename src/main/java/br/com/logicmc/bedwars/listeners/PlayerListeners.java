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

        String mapname = "doces"; // test purposes
        BWManager.getInstance().addNewPlayer(new BWPlayer(player.getUniqueId(), "doces")); // for test purposes

        plugin.utils.cleanPlayer(player);
        plugin.utils.clearChat(player);

        if(event.getArenaname().equalsIgnoreCase("staff")) {
            plugin.giveItem(player, 0, FixedItems.STAFF_ARENA_SPECTATE);
        } else {
            for(Player other : Bukkit.getOnlinePlayers()) {
                if(!mapname.equalsIgnoreCase(BWManager.getInstance().getBWPlayer(other.getUniqueId()).getMapname())) {
                    player.hidePlayer(other);
                }
            }
            Arena arena = BWManager.getInstance().getArena(mapname);
            arena.getPlayers().forEach((inuuid) ->plugin.messagehandler.sendMessage(Bukkit.getPlayer(inuuid), BWMessages.NEWPLAYER));
            BWManager.getInstance().getArena(mapname).updateScoreboardForAll("players" , ChatColor.GRAY+""+arena.getPlayers().size());

            player.teleport(arena.getLobby());
            plugin.giveItem(player, 0, FixedItems.ONLY_VIP_CHOOSETEAM);
        }

    }
    @EventHandler
    public void onquitplayer(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Arena arena = BWManager.getInstance().getArenabyUUID(uuid);
        event.setQuitMessage(null);

        arena.getPlayers().remove(uuid);

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
