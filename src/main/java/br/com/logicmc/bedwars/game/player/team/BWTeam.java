package br.com.logicmc.bedwars.game.player.team;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum BWTeam {

    GRAY(ChatColor.GRAY, Color.GRAY, (short)1),
    RED(ChatColor.RED, Color.RED, (short)4),
    WHITE(ChatColor.WHITE, Color.WHITE, (short)1),
    AQUA(ChatColor.AQUA, Color.AQUA, (short)3);

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
