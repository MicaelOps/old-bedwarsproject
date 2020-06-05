package br.com.logicmc.bedwars.game.shop;

import br.com.logicmc.bedwars.extra.FixedItems;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;

public class ShopCategory {

    private FixedItems menu;
    private HashSet<ShopItem> listitems;

    public ShopCategory(FixedItems fixedItems){
        this.menu = fixedItems;
        this.listitems = new HashSet<>();
    }

    public FixedItems getMenu() {
        return menu;
    }

    public HashSet<ShopItem> getListitems() {
        return listitems;
    }

    public void addAll(ShopItem... items){
        listitems.addAll(Arrays.asList(items));

        addAll(new ShopItem(new ItemStack(Material.WOOL, 16), new ItemStack(Material.IRON_INGOT, 4)));
        addAll(new ShopItem(new ItemStack(Material.HARD_CLAY, 16), new ItemStack(Material.IRON_INGOT, 12)));
        addAll(new ShopItem(new ItemStack(Material.WOOD, 16), new ItemStack(Material.GOLD_INGOT, 4)));
        addAll(new ShopItem(new ItemStack(Material.GLASS, 4), new ItemStack(Material.IRON_INGOT, 12)));
        addAll(new ShopItem(new ItemStack(Material.ENDER_STONE, 12), new ItemStack(Material.IRON_INGOT, 24)));
        addAll(new ShopItem(new ItemStack(Material.LADDER, 16), new ItemStack(Material.IRON_INGOT, 4)));
        addAll(new ShopItem(new ItemStack(Material.OBSIDIAN, 4), new ItemStack(Material.EMERALD, 4)));

        addAll(new ShopItem(new ItemStack(Material.STONE_SWORD, 1), new ItemStack(Material.IRON_INGOT, 10)));
        addAll(new ShopItem(new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.GOLD_INGOT, 7)));
        addAll(new ShopItem(new ItemStack(Material.DIAMOND_SWORD, 1), new ItemStack(Material.EMERALD, 4)));
        addAll(new ShopItem(addEnchantment(Material.STICK, Enchantment.KNOCKBACK, 1), new ItemStack(Material.EMERALD, 4)));

        addAll(new ShopItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), new ItemStack(Material.IRON_INGOT, 40)));
        addAll(new ShopItem(new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.GOLD_INGOT, 12)));
        addAll(new ShopItem(new ItemStack(Material.DIAMOND_CHESTPLATE, 1), new ItemStack(Material.EMERALD, 7)));
        addAll(new ShopItem(new ItemStack(Material.SHEARS, 1), new ItemStack(Material.IRON_INGOT, 20)));

        addAll(new ShopItem(new ItemStack(Material.WOOD_AXE, 1), new ItemStack(Material.IRON_INGOT, 10)));
        addAll(new ShopItem(new ItemStack(Material.STONE_AXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        addAll(new ShopItem(new ItemStack(Material.IRON_AXE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        addAll(new ShopItem(new ItemStack(Material.DIAMOND_AXE, 1), new ItemStack(Material.GOLD_INGOT, 6)));

        addAll(new ShopItem(new ItemStack(Material.WOOD_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 10)));
        addAll(new ShopItem(new ItemStack(Material.IRON_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        addAll(new ShopItem(new ItemStack(Material.GOLD_PICKAXE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        addAll(new ShopItem(new ItemStack(Material.DIAMOND_PICKAXE, 1), new ItemStack(Material.GOLD_INGOT, 6)));


        addAll(new ShopItem(new ItemStack(Material.BOW, 1), new ItemStack(Material.GOLD_INGOT, 12)));
        addAll(new ShopItem(addEnchantment(Material.BOW, Enchantment.ARROW_DAMAGE, 1), new ItemStack(Material.GOLD_INGOT, 24)));
        addAll(new ShopItem(addEnchantment(Material.BOW, Enchantment.ARROW_DAMAGE, 2), new ItemStack(Material.EMERALD, 6)));
        addAll(new ShopItem(addPotion(PotionEffectType.SPEED, 45, 2), new ItemStack(Material.EMERALD, 1)));
        addAll(new ShopItem(addPotion(PotionEffectType.JUMP, 45, 5), new ItemStack(Material.EMERALD, 1)));
        addAll(new ShopItem(new ItemStack(Material.GOLDEN_APPLE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        addAll(new ShopItem(new ItemStack(Material.SNOW_BALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        addAll(new ShopItem(new ItemStack(Material.FIREBALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        addAll(new ShopItem(new ItemStack(Material.TNT, 1), new ItemStack(Material.GOLD_INGOT, 4)));
        addAll(new ShopItem(new ItemStack(Material.ENDER_PEARL, 1), new ItemStack(Material.EMERALD, 4)));
        addAll(new ShopItem(new ItemStack(Material.WATER_BUCKET, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        addAll(new ShopItem(new ItemStack(Material.MILK_BUCKET, 1), new ItemStack(Material.GOLD_INGOT, 4)));
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
