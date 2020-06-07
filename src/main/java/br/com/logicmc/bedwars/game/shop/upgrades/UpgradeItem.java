package br.com.logicmc.bedwars.game.shop.upgrades;

import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.game.engine.Island;
import org.bukkit.inventory.ItemStack;

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

    public FixedItems getMenu() {
        return item;
    }
}
