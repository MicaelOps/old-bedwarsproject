package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.bedwars.game.shop.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import java.util.function.Consumer;

public class ShopInventoryListeners implements Listener {

    private final BWMain plugin;
    
    public ShopInventoryListeners() {
        plugin = BWMain.getInstance();
    }

    @EventHandler
    public void onclick(InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem();

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta())
            return;

        if (event.getInventory().getName().equalsIgnoreCase("Upgrades")) {

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            BWPlayer bwplayer =BWManager.getInstance().getBWPlayer(player.getUniqueId());

            for(Island island : BWManager.getInstance().getArena(bwplayer.getMapname()).getIslands()){
                if(island.getTeam().name().equalsIgnoreCase(bwplayer.getTeamcolor())) {

                    ItemStack cost = new ItemStack(Material.AIR);
                    Consumer<Island> upgrade = null;

                    if(stack.getType() == Material.IRON_CHESTPLATE) {
                        cost = plugin.getArmor().getCost(island);
                        upgrade = plugin.getArmor().getUpgrademethod();
                    } else if(stack.getType() == Material.DIAMOND_SWORD) {
                        cost = plugin.getSharpness().getCost(island);
                        upgrade = plugin.getSharpness().getUpgrademethod();
                    } else if(stack.getType() == Material.FURNACE) {
                        cost = plugin.getForgery().getCost(island);
                        upgrade = plugin.getForgery().getUpgrademethod();
                    }

                    if(cost.getType() != Material.AIR && upgrade != null){
                        if(player.getInventory().contains(cost.getType(), cost.getAmount())) {
                            upgrade.accept(island);
                            player.closeInventory();
                        } else
                            plugin.messagehandler.sendMessage(player, BWMessages.MISSING_AMOUNT);
                    }
                    break;
                }
            }

        } else if (event.getInventory().getName().equalsIgnoreCase("Categories")) {
            event.setCancelled(true);
            Inventory inventory = null;
            int i = 10;

            if (stack.getType() == Material.DIAMOND_SWORD) {
                inventory = Bukkit.createInventory(null, 36, "Shop");
                for (ShopItem shopItem : plugin.getFight().getListitems()) {
                    if (i == 17)
                        i = 19;
                    else if(i== 26)
                        i = 28;
                    inventory.setItem(i, shopItem.getMenu());
                    i++;
                }
            } else if (stack.getType() == Material.GOLDEN_APPLE) {
                inventory = Bukkit.createInventory(null, 45, "Shop");
                for (ShopItem shopItem : plugin.getUtilities().getListitems()) {
                    if (i == 17)
                        i = 19;
                    else if(i== 26)
                        i = 28;
                    else if(i== 35)
                        i = 37;
                    inventory.setItem(i, shopItem.getMenu());
                    i++;
                }
            } else if (stack.getType() == Material.STONE) {
                inventory = Bukkit.createInventory(null, 27, "Shop");
                for (ShopItem shopItem : plugin.getBlocks().getListitems()) {
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

                    removeItem(new ItemStack(costmaterial, amount), player.getInventory());
                    ItemMeta meta = stack.getItemMeta();
                    meta.setLore(new ArrayList<>());
                    stack.setItemMeta(meta);

                    String name = stack.getType().name();
                    if(name.contains("SWORD")){
                        for(int i = 0; i< player.getInventory().getSize(); i++){
                            ItemStack sword = player.getInventory().getItem(i);
                            if(sword != null && sword.getType().name().contains("SWORD")) {
                                player.getInventory().setItem(i, stack);
                                break;
                            }
                        }

                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();
                        if(island.getSharpness() != 0)
                            stack.addEnchantment(Enchantment.DAMAGE_ALL, island.getSharpness());
                    } else if(name.equalsIgnoreCase("WOOL")){
                        stack = new ItemStack(Material.WOOL, stack.getAmount(), BWTeam.valueOf(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor()).getData());
                        player.getInventory().addItem(stack);
                    } else if(name.contains("AXE") || name.contains("PICKAXE")){
                        player.getInventory().addItem(stack);
                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();
                        if(island.getSharpness() != 0)
                            stack.addEnchantment(Enchantment.DIG_SPEED, island.getSharpness());
                    } else if(name.contains("CHESTPLATE")){
                        name = name.replace("_CHESTPLATE","");
                        BWManager.getInstance().getBWPlayer(player.getUniqueId()).setArmor(stack.getType());
                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();

                        player.getInventory().setLeggings(addEnchantment(Material.valueOf(name+"_LEGGINGS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor()));
                        player.getInventory().setBoots(addEnchantment(Material.valueOf(name+"_BOOTS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor()));
                    } else {
                        player.getInventory().addItem(stack);
                    }
                } else {
                    plugin.messagehandler.sendMessage(player, BWMessages.MISSING_AMOUNT);
                    player.closeInventory();
                }
            } else
                plugin.messagehandler.sendMessage(player, BWMessages.FULL_INVENTORY);
        }

    }

    @EventHandler
    public void interactnpc(PlayerInteractEntityEvent event){

        Player player =event.getPlayer();
        Entity entity = event.getRightClicked();

        if(player.getGameMode() != GameMode.SURVIVAL)
            return;


        if(entity.getType() == EntityType.VILLAGER){

            event.setCancelled(true);

            if(entity.getCustomName().equalsIgnoreCase("Upgrades")) {
                Inventory inventory = Bukkit.createInventory(null, 27, "Upgrades");
                String lang = plugin.getLang(player);
                for(Island island : BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands()){
                    if(island.getTeam().name().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor())){
                        inventory.setItem(11, plugin.getArmor().getMenu(lang,island));
                        inventory.setItem(12, plugin.getForgery().getMenu(lang, island));
                        inventory.setItem(13, plugin.getSharpness().getMenu(lang, island));
                        player.openInventory(inventory);
                        break;
                    }
                }
            } else {
                Inventory inventory = Bukkit.createInventory(null, 27, "Categories");
                inventory.setItem(11, plugin.getBlocks().getMenu().getBuild(plugin.messagehandler, plugin.playermanager.getPlayerBase(player.getUniqueId()).getPreferences().getLang()));
                inventory.setItem(13, plugin.getFight().getMenu().getBuild(plugin.messagehandler, plugin.playermanager.getPlayerBase(player.getUniqueId()).getPreferences().getLang()));
                inventory.setItem(15, plugin.getUtilities().getMenu().getBuild(plugin.messagehandler, plugin.playermanager.getPlayerBase(player.getUniqueId()).getPreferences().getLang()));
                player.openInventory(inventory);
            }
        }
    }
    private ItemStack addEnchantment(Material material, Enchantment enchantment, int level){
        ItemStack itemStack = new ItemStack(material);
        if(level != 0)
            itemStack.addEnchantment(enchantment,level);
        return itemStack;
    }

    private void removeItem(ItemStack stack, Inventory inventory){
        ItemStack[] contents = inventory.getContents();
        int amount = stack.getAmount();
        for(int i = 0; i<contents.length; i++){
            if(contents[i] != null && contents[i].getType() == stack.getType()){
                ItemStack content = contents[i];
                if(content.getAmount() > amount){
                    content.setAmount(content.getAmount() - amount);
                    inventory.setItem(i, content);
                    return;
                } else if(content.getAmount() == amount){
                    inventory.setItem(i, new ItemStack(Material.AIR));
                    return;
                } else {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                    amount = amount-content.getAmount();
                }
            }
        }
    }
}