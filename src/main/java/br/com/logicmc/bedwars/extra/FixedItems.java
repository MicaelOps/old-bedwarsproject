package br.com.logicmc.bedwars.extra;

import br.com.logicmc.core.message.MessageHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public enum FixedItems {

	UPGRADE_SHARPNESS(Material.DIAMOND_SWORD, BWMessages.UPGRADE_SHARPNESS),
	UPGRADE_FORGERY(Material.FURNACE, BWMessages.UPGRADE_FORGERY, BWMessages.UPGRADE_FORGERY_SOLO),
	UPGRADE_ARMOR(Material.IRON_CHESTPLATE, BWMessages.UPGRADE_ARMOR, BWMessages.UPGRADE_ARMOR_SOLO),

	UPGRADE_SHARPNESS_SQUAD(Material.DIAMOND_SWORD, BWMessages.UPGRADE_SHARPNESS),
	UPGRADE_FORGERY_SQUAD(Material.FURNACE, BWMessages.UPGRADE_FORGERY, BWMessages.UPGRADE_FORGERY_SQUAD),
	UPGRADE_ARMOR_SQUAD(Material.IRON_CHESTPLATE, BWMessages.UPGRADE_ARMOR, BWMessages.UPGRADE_ARMOR_SQUAD),


	SHOP_TOOLS(Material.STONE_PICKAXE, BWMessages.SHOP_TOOLS, BWMessages.CLICK_VIEW),
	SHOP_UTILITIES(Material.GOLDEN_APPLE, BWMessages.SHOP_UTILITIES, BWMessages.CLICK_VIEW),
	SHOP_BLOCKS(Material.STAINED_CLAY, BWMessages.SHOP_BLOCKS, BWMessages.CLICK_VIEW),
	SHOP_FIGHT(Material.WOOD_SWORD, BWMessages.SHOP_FIGHT, BWMessages.CLICK_VIEW),
	SHOP_POTIONS(Material.BREWING_STAND_ITEM, BWMessages.SHOP_POTION, BWMessages.CLICK_VIEW),
	SHOP_BOW(Material.BOW, BWMessages.SHOP_BOW, BWMessages.CLICK_VIEW),
	SHOP_ARMOR(Material.CHAINMAIL_BOOTS, BWMessages.SHOP_ARMOR, BWMessages.CLICK_VIEW),
	SHOP_QUICKSHOP(Material.NETHER_STAR, BWMessages.QUICKSHOP, BWMessages.CLICK_VIEW),

	STAFF_ARENA_SPECTATE(Material.BOOK, BWMessages.STAFF_ARENA_SPECTATE),
	SPECTATE_PLAYERS(Material.ENDER_PEARL, BWMessages.SPECTATE_PLAYERS),
	SPECTATE_JOINLOBBY(Material.REDSTONE, BWMessages.SPECTATE_JOINLOBBY),
	SPECTATE_JOINNEXT(Material.GLOWSTONE_DUST, BWMessages.SPECTATE_JOINNEXT),
	ONLY_VIP_CHOOSETEAM(Material.WOOL, BWMessages.CHOOSE_TEAM_ITEM, BWMessages.CHOOSE_TEAM_ITEM_LORE);
	
	private final Material material;
	private final BWMessages name;
	private final ItemStack layout;

	private BWMessages lore;

	FixedItems(Material material, BWMessages name) {
		this.material= material;
		this.name = name;
		this.layout = buildStack();
	}
	FixedItems(Material material, BWMessages name, BWMessages lore) {
		this.material= material;
		this.name = name;
		this.layout = buildStack();
		this.lore = lore;
	}

	public Material getMaterial() {
		return material;
	}

	private ItemStack buildStack() {
		ItemStack stack = new ItemStack(material);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name.name());
		stack.setItemMeta(meta);
		return stack;
	}
	public ItemStack getBuild(MessageHandler handler, String lang) {
		ItemStack stack = this.layout.clone();
		ItemMeta meta = stack.getItemMeta();
		if(lore != null)
			meta.setLore(Arrays.asList(handler.getMessage(lore, lang).split("line")));
		meta.setDisplayName(handler.getMessage(name, lang));
		stack.setItemMeta(meta);
		return stack;
	}

}
