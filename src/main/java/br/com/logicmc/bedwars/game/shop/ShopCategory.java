package br.com.logicmc.bedwars.game.shop;

import br.com.logicmc.bedwars.extra.FixedItems;

import java.util.ArrayList;

import java.util.List;

public class ShopCategory {

    private FixedItems menu;
    private List<ShopItem> listitems;

    public ShopCategory(FixedItems fixedItems){
        this.menu = fixedItems;
        this.listitems = new ArrayList<>();
    }

    public FixedItems getMenu() {
        return menu;
    }

    public List<ShopItem> getListitems() {
        return listitems;
    }

}
