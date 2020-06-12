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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class PhaseListener implements Listener {
    
    private final BWMain plugin;
    
    public PhaseListener(){
        plugin = BWMain.getInstance();
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockdamage(final EntityExplodeEvent event) {
        if(event.getEntityType() == EntityType.PRIMED_TNT){
            event.blockList().removeIf(block->!BWManager.getInstance().getArena(block.getLocation().getWorld().getName()).getBlocks().contains(block.getLocation()));
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbuild(final BlockBreakEvent event){

        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
        if(!event.isCancelled()) {
            if (event.getBlock().getType() == Material.BED_BLOCK) {
                final Player player = event.getPlayer();
                final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                for (final Island island : arena.getIslands()) {
                    if (!island.isBedbroken() && island.getBed().distance(event.getBlock().getLocation()) < 5.0D) {
                        final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());
                        if (bwPlayer.getData().getTeamcolor().equalsIgnoreCase(island.getTeam().name()))
                            event.setCancelled(true);
                        else {
                            final BWTeam bwTeam = island.getTeam();
                            island.setBedbroken(true);
                            bwPlayer.getData().increaseBeds();
                            arena.updateScoreboardTeam(player, "beds", ChatColor.GREEN + "" + bwPlayer.getData().getBeds());

                            for (final UUID uuid : arena.getPlayers()) {
                                final Player target = Bukkit.getPlayer(uuid);
                                target.sendTitle("" + ChatColor.BOLD + bwTeam.getChatColor() + bwTeam.name(), plugin.messagehandler.getMessage(BWMessages.BED_DESTROYED, plugin.playermanager.getPlayerBase(uuid).getPreferences().getLang()).replace("{bed}",bwTeam.name()));

                                if (target.getDisplayName().contains(bwTeam.getChatColor() + ""))
                                    arena.updateScoreboardTeam(target, bwTeam.name(), ChatColor.RED + " ✗ (You)");
                                else
                                    arena.updateScoreboardTeam(target, bwTeam.name(), ChatColor.RED + " ✗");
                            }
                        }
                        break;
                    }
                }
            } else {
                final HashSet<Location> blocks = BWManager.getInstance().getArena(event.getBlock().getLocation().getWorld().getName()).getBlocks();
                if (blocks.contains(event.getBlock().getLocation())) {
                    blocks.remove(event.getBlock().getLocation());
                } else
                    event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final BlockPlaceEvent event) {
        event.setCancelled(check(event.getBlock().getLocation()));
        if(!event.isCancelled())
            BWManager.getInstance().getArena(event.getBlock().getLocation().getWorld().getName()).getBlocks().add(event.getBlock().getLocation());
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final PlayerPickupItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final PlayerDropItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
        if(!event.isCancelled())
            event.setCancelled(event.getItemDrop().getItemStack().getType().name().contains("_SWORD"));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final EntitySpawnEvent event) {
        if(event.getEntityType() == EntityType.DROPPED_ITEM){
            if(((Item)event.getEntity()).getItemStack().getType() == Material.BED){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void foodlevelchange(final FoodLevelChangeEvent event) {
        event.setCancelled(check(event.getEntity().getLocation()));
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void entitydamage(EntityDamageEvent event) {

        if(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getEntity() instanceof Player){
            event.setCancelled(check(event.getEntity().getLocation()));
            final Player player = (Player) event.getEntity();

            if(!event.isCancelled()) {
                if(event.getFinalDamage() >= ((Player) event.getEntity()).getHealth()) {
                    event.setCancelled(true);
                    if(player.getGameMode()==GameMode.SURVIVAL){
                        final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                        final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());

                        for (final Island island : arena.getIslands()) {
                            if (island.getTeam().name().equalsIgnoreCase(bwPlayer.getData().getTeamcolor())) {

                                for (final UUID uuid : arena.getPlayers()) {
                                    Player other = Bukkit.getPlayer(uuid);
                                    if(other.getGameMode() == GameMode.SURVIVAL){
                                        other.hidePlayer(player);
                                    }
                                }

                                respawn(arena, island, bwPlayer, player);
                                break;
                            }
                        }
                    }
                }
            }
            if(!event.isCancelled()){
                if(player.getGameMode() == GameMode.SURVIVAL){
                    player.getInventory().getHelmet().setDurability((short)0);
                    player.getInventory().getChestplate().setDurability((short)0);
                    player.getInventory().getLeggings().setDurability((short)0);
                    player.getInventory().getBoots().setDurability((short)0);
                }
            }
        }
    }
    @EventHandler
    public void donotsleep(PlayerBedEnterEvent event){
        event.setCancelled(true);
    }
    @EventHandler
    public void damagedddby(EntityDamageByEntityEvent event) {
        boolean damage = check(event.getEntity().getLocation());
        if(!damage){
            if(event.getEntity() instanceof Player && event.getDamager() instanceof Player){
                damage = plugin.playermanager.getPlayerBase(event.getEntity().getUniqueId()).getData().getTeamcolor().equalsIgnoreCase(plugin.playermanager.getPlayerBase(event.getDamager().getUniqueId()).getData().getTeamcolor());
                if(!damage) {
                    if(event.getFinalDamage() >= ((Player) event.getEntity()).getHealth()) {
                        damage=true;
                        event.setCancelled(true);
                        final Player player = (Player) event.getEntity();
                        if(player.getGameMode()==GameMode.SURVIVAL) {
                            final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                            final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());
                            final BWPlayer killer = BWManager.getInstance().getBWPlayer(event.getDamager().getUniqueId());
                            killer.increaseKills();
                            arena.updateScoreboardTeam((Player) event.getDamager(), "kills", ChatColor.GREEN+""+killer.getKills());
                            for (final Island island : arena.getIslands()) {
                                if (island.getTeam().name().equalsIgnoreCase(bwPlayer.getData().getTeamcolor())) {

                                    player.setGameMode(GameMode.SPECTATOR);
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 ,5));
                                    player.setAllowFlight(true);
                                    player.setFlying(true);

                                    for (final UUID uuid : arena.getPlayers()) {
                                        Player other = Bukkit.getPlayer(uuid);
                                        other.sendMessage(BWMain.getInstance().messagehandler.getMessage(BWMessages.PLAYER_KILLED_BY_PLAYER, BWMain.getInstance().getLang(other)).replace("{damager}", ((Player) event.getDamager()).getDisplayName()).replace("{player}",player.getName()));
                                        if(other.getGameMode() == GameMode.SURVIVAL){
                                            other.hidePlayer(player);
                                        }
                                    }
                                    respawn(arena, island, bwPlayer, player);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        event.setCancelled(damage);
    }
    private void respawn(Arena arena, Island island, PlayerBase<BWPlayer> bwPlayer, Player player) {

        if (island.isBedbroken()) {
            player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + plugin.messagehandler.getMessage(BWMessages.ELIMINATED, bwPlayer.getPreferences().getLang()), plugin.messagehandler.getMessage(BWMessages.ELIMINATED_MESSAGE, bwPlayer.getPreferences().getLang()));

            player.setDisplayName("[SPECTATOR] "+player.getName());

            if(arena.checkend())
                arena.changePhase();

            player.getInventory().clear();
            plugin.giveItem(player, 8, FixedItems.SPECTATE_JOINLOBBY);
            plugin.giveItem(player, 7, FixedItems.SPECTATE_JOINNEXT);
            plugin.giveItem(player, 0, FixedItems.SPECTATE_PLAYERS);
        } else {
            player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + plugin.messagehandler.getMessage(BWMessages.DEAD, bwPlayer.getPreferences().getLang()), plugin.messagehandler.getMessage(BWMessages.RESPAWN_MESSAGE, bwPlayer.getPreferences().getLang()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                    for (final UUID uuid : arena.getPlayers()) {
                        Player other = Bukkit.getPlayer(uuid);
                        if(other.getGameMode() == GameMode.SURVIVAL){
                            other.showPlayer(player);
                        }
                    }
                    player.sendTitle("","");
                    player.teleport(arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor())).findFirst().get().getSpawn());
                    player.setHealth(20.0D);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.getActivePotionEffects().forEach(potion->player.removePotionEffect(potion.getType()));
                }
            }.runTaskLater(plugin, 60L);
        }
    }
    private boolean check(final Location location) {
        return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate() != Arena.INGAME;
    }
    private boolean check(final Location location, final Entity player) {
        if (player instanceof Player)
            return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate()  != Arena.INGAME || ((Player) player).hasPotionEffect(PotionEffectType.INVISIBILITY);
        else
            return check(location);
    }
}
