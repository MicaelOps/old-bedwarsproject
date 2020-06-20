package br.com.logicmc.bedwars.game.player.team;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum BWTeam {

    NAVY(ChatColor.BLUE, Color.NAVY, (short)3),
    PINK(ChatColor.LIGHT_PURPLE, Color.FUCHSIA, (short)6),
    GREEN(ChatColor.GREEN, Color.GREEN, (short)5),
    PURPLE(ChatColor.DARK_PURPLE, Color.PURPLE,(short)10),
    YELLOW(ChatColor.YELLOW, Color.YELLOW, (short)4),
    GRAY(ChatColor.GRAY, Color.GRAY, (short)7),
    RED(ChatColor.RED, Color.RED, (short)14),
    WHITE(ChatColor.WHITE, Color.WHITE, (short)0);

    private final ChatColor chatColor;
    private final Color color;
    private final short data;

    BWTeam(ChatColor chatcolor, Color color, short data) {
        this.chatColor=chatcolor;
        this.color = color;
        this.data = data;
        
    }

    public Color getColor() {
        return color;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public short getData() {
        return data;
    }

}
