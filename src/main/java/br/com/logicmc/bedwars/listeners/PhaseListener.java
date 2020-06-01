package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PhaseListener implements Listener {


    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockdamage(BlockDamageEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbuild(BlockBreakEvent event){event.setCancelled(check(event.getPlayer().getLocation())); }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(BlockPlaceEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(PlayerPickupItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(PlayerDropItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(EntitySpawnEvent event) {
        event.setCancelled(check(event.getLocation()) || !(event.getEntityType()== EntityType.DROPPED_ITEM || event.getEntityType() == EntityType.ARMOR_STAND));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void foodlevelchange(FoodLevelChangeEvent event) {
        event.setCancelled(check(event.getEntity().getLocation()));
    }
    @EventHandler(priority= EventPriority.HIGHEST)
    public void entitydamage(EntityDamageEvent event) {
        event.setCancelled(check(event.getEntity().getLocation()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void entitydamage(EntityDamageByEntityEvent event) {
        event.setCancelled(check(event.getEntity().getLocation()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(CreatureSpawnEvent event) {
        event.setCancelled(check(event.getLocation()) || !(event.getEntityType()== EntityType.DROPPED_ITEM || event.getEntityType() == EntityType.ARMOR_STAND));
    }
    private boolean check(Location location) {
        return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate() == Arena.WAITING;
    }
}
