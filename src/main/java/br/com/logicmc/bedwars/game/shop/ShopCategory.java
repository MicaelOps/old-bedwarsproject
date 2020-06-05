package br.com.logicmc.bedwars.game.shop;

import br.com.logicmc.bedwars.extra.FixedItems;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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


    private ItemStack addPotion(PotionEffectType type , int duration, int power){
        ItemStack itemStack = new ItemStack(Material.POTION, 1);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(type, duration, power), true);
        itemStack.setItemMeta(potionMeta);
        return itemStack;
    }

    private ItemStack addEnchantment(Material material, Enchantment enchantment, int level){
        ItemStack itemStack = new ItemStack(material);
        itemStack.addEnchantment(enchantment,level);
        return itemStack;
    }
}
