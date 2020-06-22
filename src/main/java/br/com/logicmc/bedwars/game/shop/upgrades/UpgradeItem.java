package br.com.logicmc.bedwars.game.shop.upgrades;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class UpgradeItem {

    private final FixedItems item;

    private final Function<Island, ItemStack> cost;
    private final Consumer<Island> upgrademethod;

    public UpgradeItem(FixedItems item, Function<Island, ItemStack> cost, Consumer<Island> upgrademethod) {
        this.cost=cost;
        this.item = item;
        this.upgrademethod = upgrademethod;

    }

    public ItemStack getCost(Island island){
        return cost.apply(island);
    }

    public Consumer<Island> getUpgrademethod() {
        return upgrademethod;
    }

    public ItemStack getMenu(String lang, Island island) {
        ItemStack stackcost = cost.apply(island);
        ItemStack itemStack = item.getBuild(BWMain.getInstance().messagehandler, lang);
        ItemMeta meta = itemStack.getItemMeta();
        if(stackcost.getType() == Material.AIR)
            meta.setLore(Collections.singletonList(BWMain.getInstance().messagehandler.getMessage(BWMessages.MAXIMUM_UPGRADED, lang)));
        else
            meta.setLore(Arrays.asList(BWMain.getInstance().messagehandler.getMessage(BWMessages.WORD_COST, lang) + " " + ChatColor.AQUA + "" + stackcost.getAmount() + " DIAMOND", "", BWMain.getInstance().messagehandler.getMessage(BWMessages.CLICK_BUY, lang) ));
        itemStack.setItemMeta(meta);
        return itemStack;
    }


}
