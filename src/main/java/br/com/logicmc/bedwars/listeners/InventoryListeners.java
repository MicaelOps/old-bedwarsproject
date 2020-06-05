package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.bedwars.game.shop.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class InventoryListeners implements Listener {


    @EventHandler
    public void onclick(InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem();

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
            return;

        if (event.getInventory().getName().equalsIgnoreCase("Teams")) {

            Player player = (Player) event.getWhoClicked();
            BWTeam bwTeam = BWTeam.valueOf(stack.getItemMeta().getDisplayName().substring(2));
            player.closeInventory();

            player.getInventory().remove(Material.WOOL);
            BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId()).getData().setTeamcolor(bwTeam.name());
            player.sendMessage(bwTeam.getChatColor() + bwTeam.name() + " selected");
            BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getPreteam().put(player.getUniqueId(), bwTeam.name());

            ItemStack vv = new ItemStack(Material.WOOL, 1, bwTeam.getData());
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(bwTeam.getChatColor() + bwTeam.name());
            vv.setItemMeta(meta);
            player.getInventory().addItem(vv);
            event.setCancelled(true);
        } else if (event.getInventory().getName().equalsIgnoreCase("Categories")) {
            event.setCancelled(true);
            Inventory inventory = null;
            int i = 10;

            if (stack.getType() == Material.DIAMOND_SWORD) {
                inventory = Bukkit.createInventory(null, 27, "Shop");
                for (ShopItem shopItem : BWMain.getInstance().getFight().getListitems()) {
                    if (i == 18)
                        i = 19;
                    inventory.setItem(i, shopItem.getMenu());
                    i++;
                }
            } else if (stack.getType() == Material.GOLDEN_APPLE) {
                inventory = Bukkit.createInventory(null, 36, "Shop");
                for (ShopItem shopItem : BWMain.getInstance().getUtilities().getListitems()) {
                    if (i == 18)
                        i = 19;
                    else if(i== 27)
                        i = 28;
                    inventory.setItem(i, shopItem.getMenu());
                    i++;
                }
            } else if (stack.getType() == Material.STONE) {
                inventory = Bukkit.createInventory(null, 27, "Shop");
                for (ShopItem shopItem : BWMain.getInstance().getBlocks().getListitems()) {
                    if (i == 18)
                        i = 19;
                    inventory.setItem(i, shopItem.getMenu());
                    i++;
                }
            }

            event.getWhoClicked().openInventory(inventory);
        } else if (event.getInventory().getName().equalsIgnoreCase("Shop")) {
            event.setCancelled(true);
            stack = stack.clone();
            Player player = (Player) event.getWhoClicked();
            if(player.getInventory().firstEmpty() != -1){
                String[] lore = stack.getItemMeta().getLore().get(0).split(" ");
                System.out.println(lore[0].substring(2));
                int amount = Integer.parseInt(lore[0].substring(2));
                String material = lore[1].substring(2);
                if(!material.startsWith("E"))
                    material=material+"_INGOT";
                Material costmaterial = Material.valueOf(material.toUpperCase());
                if(player.getInventory().contains(costmaterial, amount)){

                    player.getInventory().removeItem(new ItemStack(costmaterial, amount));
                    ItemMeta meta = stack.getItemMeta();
                    meta.setLore(new ArrayList<>());
                    stack.setItemMeta(meta);

                    String name = stack.getType().name();
                    if(name.contains("SWORD")){
                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();
                        if(island.getSharpness() != 0)
                            stack.addEnchantment(Enchantment.DAMAGE_ALL, island.getSharpness());
                    } else if(name.contains("AXE") || name.contains("PICKAXE")){
                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();
                        if(island.getSharpness() != 0)
                            stack.addEnchantment(Enchantment.DIG_SPEED, island.getSharpness());
                    } else if(name.contains("CHESTPLATE")){
                        name = name.replace("_CHESTPLATE","");
                        BWManager.getInstance().getBWPlayer(player.getUniqueId()).setArmor(stack.getType());
                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();

                        player.getInventory().setHelmet(addEnchantment(Material.valueOf(name+"_HELMET"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                        player.getInventory().setChestplate(addEnchantment(Material.valueOf(name+"_CHESTPLATE"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                        player.getInventory().setLeggings(addEnchantment(Material.valueOf(name+"_LEGGINGS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                        player.getInventory().setBoots(addEnchantment(Material.valueOf(name+"_BOOTS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getSharpness()));
                    }
                    player.getInventory().addItem(stack);
                } else {
                    BWMain.getInstance().messagehandler.sendMessage(player, BWMessages.MISSING_AMOUNT);
                    player.closeInventory();
                }
            } else
                BWMain.getInstance().messagehandler.sendMessage(player, BWMessages.FULL_INVENTORY);
        }

    }

    @EventHandler
    public void interactnpc(PlayerInteractEntityEvent event){

        Player player =event.getPlayer();
        Entity entity = event.getRightClicked();
        if(entity.getType() == EntityType.VILLAGER){
            event.setCancelled(true);
            Inventory inventory = Bukkit.createInventory(null, 27, "Categories");
            inventory.setItem(11, BWMain.getInstance().getBlocks().getMenu().getBuild(BWMain.getInstance().messagehandler, BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId()).getPreferences().getLang()));
            inventory.setItem(13, BWMain.getInstance().getFight().getMenu().getBuild(BWMain.getInstance().messagehandler, BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId()).getPreferences().getLang()));
            inventory.setItem(15, BWMain.getInstance().getUtilities().getMenu().getBuild(BWMain.getInstance().messagehandler, BWMain.getInstance().playermanager.getPlayerBase(player.getUniqueId()).getPreferences().getLang()));
            player.openInventory(inventory);
        }
    }
    private ItemStack addEnchantment(Material material, Enchantment enchantment, int level){
        ItemStack itemStack = new ItemStack(material);
        if(level != 0)
            itemStack.addEnchantment(enchantment,level);
        return itemStack;
    }
}
