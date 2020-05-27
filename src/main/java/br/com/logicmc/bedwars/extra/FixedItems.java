package br.com.logicmc.bedwars.extra;

import br.com.logicmc.core.message.MessageHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public enum FixedItems {


	STAFF_ARENA_SPECTATE(Material.BOOKSHELF, BWMessages.STAFF_ARENA_SPECTATE),
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
			meta.setLore(Arrays.asList(handler.getMessage(lore, lang).split("(line)")));
		meta.setDisplayName(handler.getMessage(name, lang));
		stack.setItemMeta(meta);
		return stack;
	}

}
