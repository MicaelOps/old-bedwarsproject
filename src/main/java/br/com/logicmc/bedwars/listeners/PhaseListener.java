package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.GameEngine;
import org.bukkit.entity.Creature;
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
        event.setCancelled(check(event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbuild(BlockBreakEvent event) {
        event.setCancelled(check(event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(BlockPlaceEvent event) {
        event.setCancelled(check(event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(PlayerPickupItemEvent event) {
        event.setCancelled(check(event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(PlayerDropItemEvent event) {
        event.setCancelled(check(event.getPlayer()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(EntitySpawnEvent event) {
        event.setCancelled(event.getEntity() instanceof Creature);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void foodlevelchange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof  Player)
            event.setCancelled(check((Player) event.getEntity()));
    }
    @EventHandler(priority= EventPriority.HIGHEST)
    public void entitydamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof  Player)
            event.setCancelled(check((Player) event.getEntity()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void entitydamage(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof  Player)
            event.setCancelled(check((Player) event.getEntity()));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(CreatureSpawnEvent event) {
        event.setCancelled(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
    private boolean check(Player player) {
        return BWManager.getInstance().getArenabyUUID(player.getUniqueId()).getGamestate() == GameEngine.WAITING;
    }
}
