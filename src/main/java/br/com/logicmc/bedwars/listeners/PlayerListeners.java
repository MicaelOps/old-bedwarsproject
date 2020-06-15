package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.events.PlayerJoinArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

        BWManager.getInstance().getArenabyUUID(player.getUniqueId()).getPlayers().remove(player.getUniqueId());

        if(!bwPlayer.getMapname().equalsIgnoreCase("staff")) {
            plugin.giveItem(player, 0, FixedItems.STAFF_ARENA_SPECTATE);
            bwPlayer.setMap(player.getWorld().getName());
            bwPlayer.setTeamcolor("");
        }

        Bukkit.getPluginManager().callEvent(new PlayerJoinArenaEvent(player, player.getWorld().getName()));
    }
    @EventHandler
    public void onplayerjoinarena(PlayerJoinArenaEvent event) {
        Player player = event.getPlayer();
        plugin.playermanager.getPlayerBase(player).getData().setMap(event.getArenaname());
        plugin.playermanager.getPlayerBase(player).getData().setTeamcolor("");

        plugin.utils.cleanPlayer(player);
        plugin.utils.clearChat(player);

        player.setOp(true);//test purposes

        Arena arena = BWManager.getInstance().getArena(event.getArenaname());


        arena.getPlayers().add(player.getUniqueId());

        for(Player other : Bukkit.getOnlinePlayers()) {
            if(!event.getPlayer().getWorld().getName().equalsIgnoreCase(other.getLocation().getWorld().getName())) {
                event.getPlayer().hidePlayer(other);
            } else {
                if(arena.getGamestate() == Arena.WAITING) {
                    player.showPlayer(other);
                    player.setDisplayName(player.getName());
                    other.showPlayer(player);
                } else {
                    player.setDisplayName("[SPECTATOR] "+player.getName());
                    player.showPlayer(other);
                }
            }
        }

        player.setScoreboard(arena.getScoreboard());
        player.teleport(BWManager.getInstance().getArena(event.getArenaname()).getLobby());

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
            player.setGameMode(GameMode.SURVIVAL);
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
            for(UUID ingameplayers : arena.getPlayers()) {
                arena.updateScoreboardTeam(Bukkit.getPlayer(ingameplayers), "players" , ChatColor.GRAY+""+arena.getPlayers().size());
            }
        } else if(arena.getGamestate() == Arena.INGAME){
            if(arena.checkend())
                arena.changePhase();
        }
    }
    @EventHandler
    public void chat(AsyncPlayerChatEvent event){
        event.setCancelled(true);
        Arena arena = BWManager.getInstance().getArena(event.getPlayer().getLocation().getWorld().getName());

        if(event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY))
            return;

        if(arena.getGamestate() == Arena.WAITING){
            for(UUID uuid : arena.getPlayers()){
                Bukkit.getPlayer(uuid).sendMessage(ChatColor.WHITE+event.getPlayer().getDisplayName()+ChatColor.YELLOW+": "+ChatColor.GRAY+event.getMessage());
            }
        } else {
            for(UUID uuid : arena.getPlayers()){
                if(Bukkit.getPlayer(uuid).getDisplayName().charAt(1) == event.getPlayer().getDisplayName().charAt(1)){
                    Bukkit.getPlayer(uuid).sendMessage(ChatColor.GREEN+"[TEAM] "+event.getPlayer().getDisplayName()+ChatColor.YELLOW+": "+ChatColor.GRAY+event.getMessage());
                }
            }
        }
    }


    @EventHandler
    public void interactpl(PlayerInteractEvent event) {
        ItemStack item = event.getItem();


        if (item == null || item.getType().equals(Material.AIR))
            return;

        if(item.getType() == Material.COMPASS)
            event.setCancelled(true);

        if(!item.hasItemMeta() || !event.getAction().name().contains("RIGHT"))
            return;

        if(event.getAction().name().contains("RIGHT")) {
            if(item.getType() == Material.WOOL && event.getPlayer().getGameMode() == GameMode.ADVENTURE){
                PlayerBase<BWPlayer> base = BWMain.getInstance().playermanager.getPlayerBase(event.getPlayer().getUniqueId());
                if(base.isVip() || base.isStaff()){
                    Inventory inventory = Bukkit.createInventory(null, 9, "Teams");
                    for(BWTeam team : BWTeam.values()){
                        ItemStack stack = new ItemStack(Material.WOOL, 1 , team.getData());
                        ItemMeta meta = stack.getItemMeta();
                        meta.setDisplayName(team.getChatColor()+team.name());
                        stack.setItemMeta(meta);
                        inventory.addItem(stack);
                    }
                    event.getPlayer().openInventory(inventory);
                } else
                    event.getPlayer().sendMessage(BWMain.getInstance().messagehandler.getMessage(BWMessages.ERROR_ONLY_VIP, base.getPreferences().getLang()));
            } else if(event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)){
                if(item.getType() == Material.ENDER_PEARL){
                    Arena arena = BWManager.getInstance().getArena(event.getPlayer().getWorld().getName());
                    Inventory inventory = Bukkit.createInventory(null, 18, "Players");
                    for(UUID uuid : arena.getPlayers()){
                        Player target = Bukkit.getPlayer(uuid);
                        if(target.getGameMode() == GameMode.SURVIVAL){
                            ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1 , (short)3);
                            SkullMeta meta = (SkullMeta) stack.getItemMeta();
                            meta.setOwner(target.getName());
                            meta.setDisplayName(target.getDisplayName());
                            stack.setItemMeta(meta);
                            inventory.addItem(stack);
                        }
                    }
                    event.getPlayer().openInventory(inventory);
                } else if(item.getType() == Material.REDSTONE){
                    BWMain.getInstance().sendRedirect(event.getPlayer(),"lobbybedwars-1");
                }
            }
        }
    }

}
