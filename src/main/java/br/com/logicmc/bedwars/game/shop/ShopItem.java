package br.com.logicmc.bedwars.game.shop;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import org.bukkit.ChatColor;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class ShopItem {



    private final ItemStack itemStack,cost;


    public ShopItem(ItemStack item, ItemStack cost){
        this.itemStack=item;
        this.cost=cost;

    }


    public ItemStack displayMenu(String lang){

        ItemStack itemStack = this.itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Arrays.asList(BWMain.getInstance().messagehandler.getMessage(BWMessages.WORD_COST, lang) + " " + getColor()+ "" + cost.getAmount() + " " + cost.getType().name().replace("_INGOT", ""), "", BWMain.getInstance().messagehandler.getMessage(BWMessages.CLICK_BUY, lang) ));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ChatColor getColor(){
        switch (cost.getType()){
            default:
                return ChatColor.WHITE;
            case GOLD_INGOT:
                return ChatColor.GOLD;
            case EMERALD:
                return ChatColor.GREEN;
        }
    }
}
