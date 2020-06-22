package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;
import java.util.UUID;

public class InventoryListeners implements Listener {

    private final BWMain plugin;

    public InventoryListeners() {
        plugin = BWMain.getInstance();
    }

    @EventHandler
    public void onclick(InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem();

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
            return;


        if(event.getClickedInventory() instanceof PlayerInventory){
            event.setCancelled(stack.getType() == Material.WOOL  && event.getSlot() == 39);
        }
        else {
            if (event.getInventory().getName().equalsIgnoreCase("Teams")) {

                Player player = (Player) event.getWhoClicked();
                BWTeam bwTeam = BWTeam.getTeam(stack.getItemMeta().getDisplayName().substring(2));
                Arena arena = BWManager.getInstance().getArena(player.getWorld().getName());

                Optional<UUID> uuid = arena.getPreteam().keySet().stream().filter(uuid1 -> arena.getPreteam().get(uuid1).equalsIgnoreCase(bwTeam.name())).findFirst();

                if(uuid.isPresent()){
                    player.sendMessage(BWMain.getInstance().messagehandler.getMessage(BWMessages.TEAM_ALREADY_CHOSEN, BWMain.getInstance().getLang(player)).replace("{player}", Bukkit.getPlayer(uuid.get()).getDisplayName()));
                } else {
                    player.closeInventory();

                    player.getInventory().remove(Material.WOOL);
                    plugin.playermanager.getPlayerBase(player.getUniqueId()).getData().setTeamcolor(bwTeam.name());
                    player.sendMessage(bwTeam.getChatColor() + bwTeam.name() + " selected");
                    BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getPreteam().put(player.getUniqueId(), bwTeam.name());

                    ItemStack vv = new ItemStack(Material.WOOL, 1, bwTeam.getData());
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName(bwTeam.getChatColor() + bwTeam.name());
                    vv.setItemMeta(meta);
                    player.getInventory().addItem(vv);
                    player.getInventory().setHelmet(vv);
                }

                event.setCancelled(true);
            }

            else if (event.getInventory().getName().equalsIgnoreCase("Players")) {

                String name = stack.getItemMeta().getDisplayName().substring(2);
                Player target = Bukkit.getPlayer(name);
                if(target!=null)
                    event.getWhoClicked().teleport(target);
                event.getWhoClicked().closeInventory();
            }
        }
    }
}
