package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.customentity.EntityManager;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.account.PlayerBase;
import org.bukkit.*;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class ItemInteractListeners implements Listener {


    @EventHandler
    public void interactpl(PlayerInteractEvent event) {
        ItemStack item = event.getItem();


        if (item == null || item.getType().equals(Material.AIR))
            return;

        if(!event.getAction().name().contains("RIGHT"))
            return;

        Player player = event.getPlayer();
        if(item.getType() == Material.COMPASS){
            event.setCancelled(true);
            Arena arena = BWManager.getInstance().getArena(player.getWorld().getName());
            if(arena.getGamestate() == Arena.INGAME){
                double lesserdistance = 9999;
                Location location = null;
                String displayname = null;
                for(UUID uuid : arena.getPlayers()){
                    Player nearby = Bukkit.getPlayer(uuid);
                    if(nearby.getGameMode() == GameMode.SURVIVAL && player.getDisplayName().charAt(1) != nearby.getDisplayName().charAt(1)){
                        double distance = player.getLocation().distance(nearby.getLocation());
                        if(distance <= lesserdistance){
                            location = nearby.getLocation();
                            lesserdistance = distance;
                            displayname = nearby.getDisplayName();
                        }
                    }
                }
                BWMain.getInstance().send(player, BWMain.getInstance().messagehandler.getMessage(BWMessages.WORD_TARGET, BWMain.getInstance().getLang(player)) +" "+ displayname + ChatColor.RESET + "- "+ChatColor.RED +lesserdistance+"m");
                player.setCompassTarget(location);
            }
        } else if(item.getType() == Material.MONSTER_EGG && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.setCancelled(true);
            ItemStack stack = player.getInventory().getItemInHand();
            stack.setAmount(stack.getAmount() - 1);

            if(stack.getAmount() < 1)
                player.setItemInHand(new ItemStack(Material.AIR));
            else
                player.setItemInHand(stack);
            BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(player.getUniqueId());

            if(item.getData().getData() == 99){

                for(Island island : BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands()){
                    if(island.getTeam().name().equalsIgnoreCase(bwPlayer.getTeamcolor())){
                        EntityManager.getInstance().spawnTeamIronGolem(player.getLocation(), island , BWTeam.valueOf(bwPlayer.getTeamcolor()), BWMain.getInstance().getLang(player));
                    }
                }
            }

        } else if(item.getType() == Material.FIREBALL && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            event.setCancelled(true);
            Fireball fireball = player.getWorld().spawn(player.getLocation().add(0.0D, 3.0D, 0.0D), Fireball.class);
            fireball.setBounce(true);
            fireball.setIsIncendiary(false);
            fireball.setVelocity(player.getEyeLocation().getDirection().multiply(2));
            fireball.setYield(2F);
            player.getInventory().setItemInHand(new ItemStack(Material.FIREBALL, player.getInventory().getItemInHand().getAmount()-1));
        }


        if(!item.hasItemMeta())
            return;

        if(event.getAction().name().contains("RIGHT")) {
            if(item.getType() == Material.WOOL && event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
                PlayerBase<BWPlayer> base = BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId());
                if (base.isVip() || base.isStaff()) {
                    Inventory inventory = Bukkit.createInventory(null, 9, "Teams");
                    for (BWTeam team : BWTeam.values()) {
                        ItemStack stack = new ItemStack(Material.WOOL, 1, team.getData());
                        ItemMeta meta = stack.getItemMeta();
                        meta.setDisplayName(team.getChatColor() + team.getName(base.getPreferences().getLang()));
                        stack.setItemMeta(meta);
                        inventory.addItem(stack);
                    }
                    player.openInventory(inventory);
                } else
                    player.sendMessage(BWMain.getInstance().messagehandler.getMessage(BWMessages.ERROR_ONLY_VIP, base.getPreferences().getLang()));
            } else if(event.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)){
                if(item.getType() == Material.ENDER_PEARL){
                    event.setCancelled(true);
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

    @EventHandler
    public void snowballsummon(ProjectileHitEvent event) {
        if(event.getEntity().getShooter() instanceof  Player){
            Player player = (Player) event.getEntity().getShooter();
            EntityManager.getInstance().spawnTeamSilverFish(event.getEntity().getLocation() , BWTeam.valueOf(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor()), BWMain.getInstance().getLang(player));
        }
    }

}
