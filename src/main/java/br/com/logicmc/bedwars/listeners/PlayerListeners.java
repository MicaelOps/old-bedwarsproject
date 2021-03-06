package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.addons.bossbar.Bossbar;
import br.com.logicmc.core.events.PlayerJoinArenaEvent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    public void showworldplayers(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();

        BWPlayer bwPlayer =plugin.playermanager.getPlayerBase(player).getData();

        Arena oldarena = BWManager.getInstance().getArenabyUUID(player.getUniqueId());

        oldarena.getPlayers().remove(player.getUniqueId());

        for(UUID uuid : oldarena.getPlayers()){
            Bukkit.getPlayer(uuid).hidePlayer(player);
        }

        if(!bwPlayer.getMapname().equalsIgnoreCase("staff")) {
            plugin.giveItem(player, 0, FixedItems.STAFF_ARENA_SPECTATE);
            bwPlayer.setMap(player.getWorld().getName());
            bwPlayer.setTeamcolor("");
        } else {
            if(oldarena.checkend()){
                oldarena.changePhase();
            }
        }

        Bukkit.getPluginManager().callEvent(new PlayerJoinArenaEvent(player, player.getWorld().getName()));
    }
    @EventHandler
    public void onplayerjoinarena(PlayerJoinArenaEvent event) {
        Player player = event.getPlayer();
        PlayerBase<BWPlayer> playerBase = plugin.playermanager.getPlayerBase(player);
        BWPlayer bwPlayer = playerBase.getData();

        bwPlayer.setLevel(20);
        bwPlayer.setUuid(player.getUniqueId());
        bwPlayer.setName(player.getName());
        bwPlayer.setMap(event.getArenaname());
        bwPlayer.setTeamcolor("");

        plugin.utils.cleanPlayer(player);
        plugin.utils.clearChat(player);

        player.setOp(true);//test purposes

        Arena arena = BWManager.getInstance().getArena(event.getArenaname());
        bwPlayer.setTeamComp(arena.getTeamcomposition());

        arena.getPlayers().add(player.getUniqueId());

        // newplayers in arena have to be hidden from players in other arenas
        for(Player online : Bukkit.getOnlinePlayers()){
            if(online.getWorld().getName().equalsIgnoreCase(player.getWorld().getName())) {
                if(arena.getGamestate() == Arena.WAITING) {
                    player.showPlayer(online);
                    player.setDisplayName(player.getName());
                    online.showPlayer(player);
                } else {
                    player.setDisplayName("[SPECTATOR] "+player.getName());

                    player.showPlayer(online);
                    if(online.getGameMode() == GameMode.SURVIVAL)
                        online.hidePlayer(player);
                    else
                        online.showPlayer(player);
                }
            } else {
                player.hidePlayer(online);
            }
        }

        player.setScoreboard(arena.getScoreboard(playerBase.getPreferences().getLang()));
        player.teleport(BWManager.getInstance().getArena(event.getArenaname()).getLobby());
        player.getEnderChest().clear();
        player.setHealth(20.0D);
        player.setDisplayName("§7"+player.getName());
        player.setGameMode(GameMode.ADVENTURE);

        if(arena.getGamestate() == Arena.WAITING){

            arena.incrementAllotedPlayers();
            player.getInventory().clear();
            player.setDisplayName(player.getName());
            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
            player.getInventory().setArmorContents(null);
            plugin.giveItem(player, 0, FixedItems.ONLY_VIP_CHOOSETEAM);
            player.setGameMode(GameMode.ADVENTURE);
            arena.updateScoreboardForAll("players", ChatColor.GREEN+""+arena.getPlayers().size());
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 3));
            player.setAllowFlight(true);
            player.setFlying(true);
            plugin.giveItem(player, 8, FixedItems.SPECTATE_JOINLOBBY);
            plugin.giveItem(player, 7, FixedItems.SPECTATE_JOINNEXT);
            plugin.giveItem(player, 0, FixedItems.SPECTATE_PLAYERS);
        }



    }
    @EventHandler
    public void onquitplayer(PlayerQuitEvent event) {
        Arena arena = BWManager.getInstance().getArena(event.getPlayer().getWorld().getName());
        event.setQuitMessage(null);

        if(arena.getName().equalsIgnoreCase("staff"))
            return;

        arena.getPlayers().remove(event.getPlayer().getUniqueId());

        if(arena.getGamestate() == Arena.WAITING) {
            arena.decrementAllotedPlayers();
            arena.getPreteam().remove(event.getPlayer().getUniqueId());
            for(UUID ingameplayers : arena.getPlayers()) {
                arena.updateScoreboardTeam(Bukkit.getPlayer(ingameplayers), "players" , ChatColor.GRAY+""+arena.getPlayers().size());
            }
        } else if(arena.getGamestate() == Arena.INGAME){
            BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(event.getPlayer().getUniqueId());
            bwPlayer.increaseLoses();
            if(arena.checkend())
                arena.changePhase();
            else {
                BWTeam team = BWTeam.valueOf(bwPlayer.getTeamcolor());
                for(Island island : arena.getIslands()){
                    if(island.getTeam().name().equalsIgnoreCase(bwPlayer.getTeamcolor())){
                        if(arena.getTeamcomposition() == Arena.SOLO || (!island.isBedbroken() && arena.getMembersOfTeam(team).count() == 0L)){
                            island.setBedbroken(true);
                            island.getBed().getBlock().setType(Material.AIR);
                            arena.updateTeamArena(team);
                        } else if(island.isBedbroken()) {
                            arena.updateTeamArena(team);
                        }
                    }
                }

                for (final UUID uuid : arena.getPlayers()) {
                    final Player target = Bukkit.getPlayer(uuid);
                    if(arena.getTeamcomposition() == Arena.SOLO)
                        arena.updateScoreboardTeam(target, bwPlayer.getTeamcolor() , ChatColor.GREEN+"1");
                    else
                        arena.updateScoreboardTeam(target, bwPlayer.getTeamcolor() , ChatColor.GREEN+""+arena.getTeamcomposition());
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void chat(AsyncPlayerChatEvent event){
        if(!event.isCancelled()){
            event.setCancelled(true);
            Arena arena = BWManager.getInstance().getArena(event.getPlayer().getLocation().getWorld().getName());

            if(event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY))
                return;

            if(arena.getGamestate() == Arena.WAITING){
                for(UUID uuid : arena.getPlayers()){
                    Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE+event.getPlayer().getDisplayName()+": "+ChatColor.GRAY+event.getMessage());
                }
            } else {
                for(UUID uuid : arena.getPlayers()){
                    if(event.getPlayer().getGameMode() != GameMode.SURVIVAL){
                        if(Bukkit.getPlayer(uuid).getGameMode() == GameMode.ADVENTURE){
                            Bukkit.getPlayer(uuid).sendMessage(event.getPlayer().getDisplayName()+ChatColor.YELLOW+": "+ChatColor.GRAY+event.getMessage());
                        }
                    } else {
                        if(Bukkit.getPlayer(uuid).getDisplayName().charAt(1) == event.getPlayer().getDisplayName().charAt(1)){
                            Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN+"[TEAM] "+event.getPlayer().getDisplayName()+ChatColor.YELLOW+": "+ChatColor.GRAY+event.getMessage());
                        }
                    }
                }
            }
        }
    }

}
