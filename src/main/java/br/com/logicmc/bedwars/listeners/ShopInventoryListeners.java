package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.extra.customentity.ImmobileVillager;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.bedwars.game.shop.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShopInventoryListeners implements Listener {

    private final BWMain plugin;
    
    public ShopInventoryListeners() {
        plugin = BWMain.getInstance();
    }

    @EventHandler
    public void onclick(InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem();

        if (stack == null)
            return;

        if(stack.getType() == Material.STAINED_GLASS_PANE)
            event.setCancelled(true);

        if(!stack.hasItemMeta())
            return;

        if(!(event.getClickedInventory() instanceof PlayerInventory)){
            if (event.getInventory().getName().equalsIgnoreCase("Upgrades")) {

                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                BWPlayer bwplayer =BWManager.getInstance().getBWPlayer(player.getUniqueId());

                for(Island island : BWManager.getInstance().getArena(bwplayer.getMapname()).getIslands()){
                    if(island.getTeam().name().equalsIgnoreCase(bwplayer.getTeamcolor())) {

                        ItemStack cost = new ItemStack(Material.AIR);
                        Consumer<Island> upgrade = null;

                        if(stack.getType() == Material.IRON_CHESTPLATE) {
                            cost = plugin.armor.getCost(island);
                            upgrade = plugin.armor.getUpgrademethod();
                        } else if(stack.getType() == Material.DIAMOND_SWORD) {
                            cost = plugin.sharpness.getCost(island);
                            upgrade = plugin.sharpness.getUpgrademethod();
                        } else if(stack.getType() == Material.FURNACE) {
                            cost = plugin.forgery.getCost(island);
                            upgrade = plugin.forgery.getUpgrademethod();
                        }

                        if(cost.getType() != Material.AIR && upgrade != null){
                            if(player.getInventory().contains(cost.getType(), cost.getAmount())) {
                                upgrade.accept(island);
                                removeItem(cost,player.getInventory());
                                player.closeInventory();
                                player.playSound(player.getLocation(), Sound.ANVIL_USE, 20F , 20F);
                            } else {
                                player.playSound(player.getLocation(), Sound.BLAZE_HIT, 10F, 10F);
                                plugin.messagehandler.sendMessage(player, BWMessages.MISSING_AMOUNT);
                            }
                        } else {
                            player.playSound(player.getLocation(), Sound.BLAZE_HIT, 10F, 10F);
                            plugin.messagehandler.sendMessage(player, BWMessages.ERROR_MAXIMUM_UPGRADED);
                        }
                        break;
                    }
                }

            } else if (event.getInventory().getName().equalsIgnoreCase("Shop")) {
                event.setCancelled(true);
                stack = stack.clone();
                if(event.getSlot() > 18) {
                    Player player = (Player) event.getWhoClicked();
                    if(player.getInventory().firstEmpty() != -1){
                        String[] lore = stack.getItemMeta().getLore().get(0).split(" ");
                        int amount = Integer.parseInt(lore[1].substring(2));
                        String material = lore[2];
                        if(!material.startsWith("E"))
                            material=material+"_INGOT";
                        Material costmaterial = Material.valueOf(material.toUpperCase());
                        if(player.getInventory().contains(costmaterial, amount)){

                            removeItem(new ItemStack(costmaterial, amount), player.getInventory());
                            ItemMeta meta = stack.getItemMeta();
                            meta.setLore(new ArrayList<>());
                            stack.setItemMeta(meta);

                            String name = stack.getType().name();
                            player.playSound(player.getLocation(), Sound.LEVEL_UP, 10F, 10F);
                            if(name.contains("SWORD")){
                                for(int i = 0; i< player.getInventory().getSize(); i++){
                                    ItemStack sword = player.getInventory().getItem(i);
                                    if(sword != null && sword.getType().name().contains("SWORD")) {
                                        Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();
                                        if(island.getSharpness() != 0)
                                            stack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL,1);
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
                                Island island = BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands().stream().filter(islands -> player.getDisplayName().contains(""+islands.getTeam().getChatColor())).findFirst().get();

                                player.getInventory().setLeggings(addEnchantment(Material.valueOf(name+"_LEGGINGS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor()));
                                player.getInventory().setBoots(addEnchantment(Material.valueOf(name+"_BOOTS"), Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor()));
                            } else {
                                player.getInventory().addItem(stack);
                            }
                        } else {
                            player.playSound(player.getLocation(), Sound.BLAZE_HIT, 10F, 10F);
                            plugin.messagehandler.sendMessage(player, BWMessages.MISSING_AMOUNT);
                            player.closeInventory();
                        }
                    } else {
                        player.playSound(player.getLocation(), Sound.BLAZE_HIT, 10F, 10F);
                        plugin.messagehandler.sendMessage(player, BWMessages.FULL_INVENTORY);
                    }
                } else {
                    openShop(stack, (Player) event.getWhoClicked(), event.getInventory());


                    ItemStack stack1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5);
                    event.getInventory().setItem(event.getSlot()+9, stack1);
                }
            }

        }

    }

    @EventHandler
    public void interactnpc(PlayerInteractEntityEvent event){

        Player player =event.getPlayer();
        Entity entity = event.getRightClicked();

        if(player.getGameMode() != GameMode.SURVIVAL || !(entity instanceof Villager))
            return;

        net.minecraft.server.v1_8_R3.Entity d = ((CraftEntity)entity).getHandle();
        String shop = ((ImmobileVillager)d).getShop();
        if(entity.getType() == EntityType.VILLAGER){

            event.setCancelled(true);

            if(shop.equalsIgnoreCase("Upgrades")) {
                Inventory inventory = Bukkit.createInventory(null, 27, "Upgrades");
                String lang = plugin.getLang(player);
                for(Island island : BWManager.getInstance().getArena(player.getLocation().getWorld().getName()).getIslands()){
                    if(island.getTeam().name().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor())){
                        inventory.setItem(11, plugin.armor.getMenu(lang,island));
                        inventory.setItem(12, plugin.forgery.getMenu(lang, island));
                        inventory.setItem(13, plugin.sharpness.getMenu(lang, island));
                        player.openInventory(inventory);
                        break;
                    }
                }
            } else {
                openShop(null, player, null);
            }
        }
    }

    private void openShop(ItemStack stack,  Player player, Inventory inventory) {
        boolean open = inventory == null;
        String lang = BWMain.getInstance().getLang(player);

        if(open) {
            inventory = Bukkit.createInventory(null, 54, "Shop");
            inventory.setItem(0, plugin.quickshop.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(2, plugin.blocks.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(3, plugin.swords.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(4, plugin.shoparmor.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(5, plugin.tools.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(6, plugin.bows.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(7, plugin.potions.getMenu().getBuild(plugin.messagehandler, lang));
            inventory.setItem(8, plugin.utilities.getMenu().getBuild(plugin.messagehandler, lang));
        }

        ItemStack stack1 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)10);
        inventory.setItem(1, stack1);
        for(int i = 9; i < 18; i++){
            inventory.setItem(i, stack1);
        }

        List<ShopItem> itemList = new ArrayList<>();

        if(stack == null || stack.getType() == FixedItems.SHOP_QUICKSHOP.getMaterial())
            itemList = plugin.quickshop.getListitems();
        else if (stack.getType() == FixedItems.SHOP_FIGHT.getMaterial())
            itemList = plugin.swords.getListitems();
        else if (stack.getType() == FixedItems.SHOP_UTILITIES.getMaterial())
            itemList =plugin.utilities.getListitems();
        else if (stack.getType() == FixedItems.SHOP_BLOCKS.getMaterial())
            itemList =  plugin.blocks.getListitems();
        else if (stack.getType() == FixedItems.SHOP_TOOLS.getMaterial())
            itemList = plugin.tools.getListitems();
        else if (stack.getType() == FixedItems.SHOP_POTIONS.getMaterial())
            itemList = plugin.potions.getListitems();
        else if (stack.getType() == FixedItems.SHOP_BOW.getMaterial())
            itemList = plugin.bows.getListitems();
        else if (stack.getType() == FixedItems.SHOP_ARMOR.getMaterial())
            itemList = plugin.shoparmor.getListitems();




        for(int i = 0; i < 26; i++){

            int slot = i + 19;

            if(slot== 26 || slot== 27)
                slot = 28;
            else if(slot == 35 || slot == 36)
                slot = 37;
            else if(slot == 44)
                break;

            if( i >= itemList.size())
                inventory.setItem(slot, new ItemStack(Material.AIR));
            else
                inventory.setItem(slot, itemList.get(i).displayMenu(lang));
        }

        if(open)
            player.openInventory(inventory);
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
