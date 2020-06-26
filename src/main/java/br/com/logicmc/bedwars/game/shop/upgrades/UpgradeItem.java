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

    private final FixedItems solo,squad;

    private final Function<Island, ItemStack> cost;
    private final Consumer<Island> upgrademethod;

    public UpgradeItem(FixedItems solo, FixedItems squad, Function<Island, ItemStack> cost, Consumer<Island> upgrademethod) {
        this.cost=cost;
        this.solo = solo;
        this.squad = squad;
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
        ItemStack itemStack;

        if(BWManager.getInstance().getArena(island.getArena()).getTeamcomposition() < 3)
            itemStack = solo.getBuild(BWMain.getInstance().messagehandler, lang);
        else
            itemStack = squad.getBuild(BWMain.getInstance().messagehandler, lang);

        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();

        if(lore != null) {
            lore.add("");
            if(stackcost.getType() == Material.AIR)
                lore.add(BWMain.getInstance().messagehandler.getMessage(BWMessages.MAXIMUM_UPGRADED, lang));
            else
                lore.add(BWMain.getInstance().messagehandler.getMessage(BWMessages.CLICK_BUY, lang));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }


}
