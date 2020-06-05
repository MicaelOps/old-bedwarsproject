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
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class PhaseListener implements Listener {



    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockdamage(EntityExplodeEvent event) {
        if(event.getEntityType() == EntityType.PRIMED_TNT){
            event.blockList().removeIf(block->BWManager.getInstance().getArena(block.getLocation().getWorld().getName()).getBlocks().contains(block.getLocation()));
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbuild(BlockBreakEvent event){
        boolean cancelled = check(event.getPlayer().getLocation(), event.getPlayer());
        if(!cancelled){
            if(event.getBlock().getType() == Material.BED_BLOCK) {
                Player player = event.getPlayer();
                Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                for(Island island : arena.getIslands()){
                    if(!island.isBedbroken() &&island.getBed().distance(event.getBlock().getLocation()) < 5.0D){
                        PlayerBase<BWPlayer> bwPlayer = BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId());
                        if(bwPlayer.getData().getTeamcolor().equalsIgnoreCase(island.getTeam().name()))
                            cancelled = true;
                        else {
                            BWTeam bwTeam = island.getTeam();
                            island.setBedbroken(true);
                            for(UUID uuid : arena.getPlayers()){
                                Player everyone = Bukkit.getPlayer(uuid);
                                String string = BWMain.getInstance().messagehandler.getMessage(BWMessages.BED_DESTROYED, BWMain.getInstance().playermanager.getPlayerBase(uuid).getPreferences().getLang());
                                everyone.sendTitle(""+ChatColor.BOLD+bwTeam.getChatColor()+bwTeam.name(),  string);
                                if(everyone.getDisplayName().contains(bwTeam.getChatColor()+""))
                                    arena.updateScoreboardTeam(everyone, bwTeam.name(), ChatColor.RED+" X (You)");
                                else
                                    arena.updateScoreboardTeam(everyone, bwTeam.name(), ChatColor.RED+" X");
                            }
                        }
                        break;
                    }
                }
            } else{
                HashSet<Location> blocks = BWManager.getInstance().getArena(event.getBlock().getLocation().getWorld().getName()).getBlocks();
                if(blocks.contains(event.getBlock().getLocation())){
                    blocks.remove(event.getBlock().getLocation());
                } else
                    cancelled=true;
            }
        }
        event.setCancelled(cancelled);
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(BlockPlaceEvent event) {
        boolean cancelled = check(event.getBlock().getLocation());
        event.setCancelled(cancelled);

        if(!cancelled)
            BWManager.getInstance().getArena(event.getBlock().getLocation().getWorld().getName()).getBlocks().add(event.getBlock().getLocation());

    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(PlayerPickupItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(PlayerDropItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(EntitySpawnEvent event) {
        event.setCancelled(event.getEntityType()== EntityType.DROPPED_ITEM || event.getEntityType() == EntityType.ARMOR_STAND|| event.getEntityType() == EntityType.VILLAGER);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void foodlevelchange(FoodLevelChangeEvent event) {
        event.setCancelled(check(event.getEntity().getLocation()));
    }
    @EventHandler(priority= EventPriority.HIGHEST)
    public void entitydamage(EntityDamageEvent event) {
        boolean damage = event.getEntityType()==EntityType.VILLAGER || check(event.getEntity().getLocation(), event.getEntity());
        if(!damage) {
            if(event.getDamage() >= ((Player) event.getEntity()).getHealth()) {
                damage = true;
                Player player = (Player) event.getEntity();
                Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                PlayerBase<BWPlayer> bwPlayer = BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId());
                BWPlayer bedwars = bwPlayer.getData();
                for (Island island : arena.getIslands()) {
                    if (island.getTeam().name().equalsIgnoreCase(bedwars.getTeamcolor())) {

                        player.setGameMode(GameMode.SURVIVAL);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 ,5));
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        player.getInventory().setArmorContents(null);

                        if (island.isBedbroken()) {
                            player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + BWMain.getInstance().messagehandler.getMessage(BWMessages.ELIMINATED, bwPlayer.getPreferences().getLang()), BWMain.getInstance().messagehandler.getMessage(BWMessages.ELIMINATED_MESSAGE, bwPlayer.getPreferences().getLang()));
                            arena.getPlayers().remove(player.getUniqueId());
                            player.setDisplayName("[SPECTATOR] "+player.getName());
                            for (UUID uuid : arena.getPlayers()) {
                                Bukkit.getPlayer(uuid).hidePlayer(player);
                            }
                            player.getInventory().clear();
                            BWMain.getInstance().giveItem(player, 8, FixedItems.SPECTATE_JOINLOBBY);
                            BWMain.getInstance().giveItem(player, 7, FixedItems.SPECTATE_JOINNEXT);
                            BWMain.getInstance().giveItem(player, 0, FixedItems.SPECTATE_PLAYERS);
                        } else {
                            player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + BWMain.getInstance().messagehandler.getMessage(BWMessages.DEAD, bwPlayer.getPreferences().getLang()), BWMain.getInstance().messagehandler.getMessage(BWMessages.RESPAWN_MESSAGE, bwPlayer.getPreferences().getLang()));
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                                    player.teleport(arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor())).findFirst().get().getSpawn());
                                    player.setGameMode(GameMode.SURVIVAL);
                                    String name = bedwars.getArmor().name().replace("_CHESTPLATE","");

                                    player.getInventory().setHelmet(addEnchantment(Material.valueOf(name+"_HELMET"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                    player.getInventory().setChestplate(addEnchantment(Material.valueOf(name+"_CHESTPLATE"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                    player.getInventory().setLeggings(addEnchantment(Material.valueOf(name+"_LEGGINGS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                    player.getInventory().setBoots(addEnchantment(Material.valueOf(name+"_BOOTS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                }
                            }.runTaskLater(BWMain.getInstance(), 60L);
                        }
                        break;
                    }
                }
            }
        }
        event.setCancelled(damage);
    }

    private ItemStack addEnchantment(Material material, Enchantment enchantment, int level){
        ItemStack itemStack = new ItemStack(material);
        if(level != 0)
            itemStack.addEnchantment(enchantment,level);
        return itemStack;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void entitydamage(EntityDamageByEntityEvent event) {
        boolean damage = check(event.getEntity().getLocation());
        if(!damage){
            if(event.getEntity() instanceof Player && event.getDamager() instanceof Player){
                damage = BWMain.getInstance().playermanager.getPlayerBase(event.getEntity().getUniqueId()).getData().getTeamcolor().equalsIgnoreCase(BWMain.getInstance().playermanager.getPlayerBase(event.getDamager().getUniqueId()).getData().getTeamcolor());
                System.out.println(damage);
                if(!damage) {
                    if(event.getDamage() >= ((Player) event.getEntity()).getHealth()) {
                        damage = true;
                        Player player = (Player) event.getEntity();
                        Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                        PlayerBase<BWPlayer> bwPlayer = BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId());
                        BWPlayer bedwars = bwPlayer.getData();
                        for (Island island : arena.getIslands()) {
                            if (island.getTeam().name().equalsIgnoreCase(bedwars.getTeamcolor())) {

                                player.setGameMode(GameMode.SURVIVAL);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 ,5));
                                player.setAllowFlight(true);
                                player.setFlying(true);
                                player.getInventory().setArmorContents(null);


                                if (island.isBedbroken()) {
                                    player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + BWMain.getInstance().messagehandler.getMessage(BWMessages.ELIMINATED, bwPlayer.getPreferences().getLang()), BWMain.getInstance().messagehandler.getMessage(BWMessages.ELIMINATED_MESSAGE, bwPlayer.getPreferences().getLang()));
                                    arena.getPlayers().remove(player.getUniqueId());
                                    player.setDisplayName("[SPECTATOR] "+player.getName());
                                    for (UUID uuid : arena.getPlayers()) {
                                        Bukkit.getPlayer(uuid).hidePlayer(player);
                                    }
                                    player.getInventory().clear();
                                    BWMain.getInstance().giveItem(player, 8, FixedItems.SPECTATE_JOINLOBBY);
                                    BWMain.getInstance().giveItem(player, 7, FixedItems.SPECTATE_JOINNEXT);
                                    BWMain.getInstance().giveItem(player, 0, FixedItems.SPECTATE_PLAYERS);
                                } else {
                                    player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + BWMain.getInstance().messagehandler.getMessage(BWMessages.DEAD, bwPlayer.getPreferences().getLang()), BWMain.getInstance().messagehandler.getMessage(BWMessages.RESPAWN_MESSAGE, bwPlayer.getPreferences().getLang()));
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                                            player.teleport(arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor())).findFirst().get().getSpawn());
                                            player.setGameMode(GameMode.SURVIVAL);
                                            String name = bedwars.getArmor().name().replace("_CHESTPLATE","");

                                            player.getInventory().setHelmet(addEnchantment(Material.valueOf(name+"_HELMET"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                            player.getInventory().setChestplate(addEnchantment(Material.valueOf(name+"_CHESTPLATE"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                            player.getInventory().setLeggings(addEnchantment(Material.valueOf(name+"_LEGGINGS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                            player.getInventory().setBoots(addEnchantment(Material.valueOf(name+"_BOOTS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                                        }
                                    }.runTaskLater(BWMain.getInstance(), 60L);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        event.setCancelled(damage);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(CreatureSpawnEvent event) {
        event.setCancelled(event.getEntityType()== EntityType.DROPPED_ITEM || event.getEntityType() == EntityType.ARMOR_STAND|| event.getEntityType() == EntityType.VILLAGER);
    }
    private boolean check(Location location) {
        return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate() == Arena.WAITING;
    }
    private boolean check(Location location, Entity player) {
        if(player instanceof Player)
            return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate() == Arena.WAITING || ((Player)player).hasPotionEffect(PotionEffectType.INVISIBILITY);
        else
            return check(location);
    }
}
