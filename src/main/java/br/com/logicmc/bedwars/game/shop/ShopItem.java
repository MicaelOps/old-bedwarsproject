package br.com.logicmc.bedwars.game.shop;

import org.bukkit.ChatColor;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ShopItem {

    private final ItemStack itemStack,cost,menu;


    public ShopItem(ItemStack item, ItemStack cost){
        this.itemStack=item;
        this.cost=cost;

        this.menu=displayMenu();
    }


    public ItemStack displayMenu(){
        ItemStack itemStack = this.itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Collections.singletonList(ChatColor.YELLOW + "" + cost.getAmount() + " " + getColor() + cost.getType().name().replace("_INGOT", "")));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack getMenu() {
        return menu;
    }

    private ChatColor getColor(){
        switch (cost.getType()){
            default:
                return ChatColor.WHITE;
            case GOLD_INGOT:
                return ChatColor.GOLD;
            case IRON_INGOT:
                return ChatColor.WHITE;
            case EMERALD:
                return ChatColor.GREEN;
        }
    }
    public ItemStack getItemStack() {
        return itemStack;
    }
}
