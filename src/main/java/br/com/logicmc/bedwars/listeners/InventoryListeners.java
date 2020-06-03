package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListeners implements Listener {

    @EventHandler
    public void onclick(InventoryClickEvent event){
        ItemStack stack = event.getCurrentItem();

        if(event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
            return;

        if(event.getInventory().getName().equalsIgnoreCase("Teams")) {

            Player player = (Player) event.getWhoClicked();
            BWTeam bwTeam = BWTeam.valueOf(stack.getItemMeta().getDisplayName().substring(2));
            player.closeInventory();

            player.getInventory().remove(Material.WOOL);
            BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId()).getData().setTeamcolor(bwTeam.name());
            player.sendMessage(bwTeam.getChatColor()+bwTeam.name()+" selected");
            BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getPreteam().put(player.getUniqueId(), bwTeam.name());
            event.setCancelled(true);
        }
    }
}
