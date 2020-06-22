package br.com.logicmc.bedwars.game.player.team;

import br.com.logicmc.core.system.redis.packet.PacketManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum BWTeam {

    NAVY(ChatColor.BLUE, Color.NAVY, (short)3 , null,"BLUE","BLUE"),
    PINK(ChatColor.LIGHT_PURPLE, Color.FUCHSIA, (short)6),
    GREEN(ChatColor.GREEN, Color.GREEN, (short)5),
    PURPLE(ChatColor.DARK_PURPLE, Color.PURPLE,(short)10, null, null, "MAGENTA"),
    YELLOW(ChatColor.YELLOW, Color.YELLOW, (short)4),
    GRAY(ChatColor.GRAY, Color.GRAY, (short)7),
    RED(ChatColor.RED, Color.RED, (short)14),
    WHITE(ChatColor.WHITE, Color.WHITE, (short)0);

    private final ChatColor chatColor;
    private final Color color;
    private final short data;
    private final String english,spanish,portuguese;

    BWTeam(ChatColor chatcolor, Color color, short data, String english, String spanish, String portuguese) {
        this.chatColor=chatcolor;
        this.color = color;
        this.data = data;
        this.english = english == null ? name() : english;
        this.spanish = spanish == null ? name() : spanish;
        this.portuguese = portuguese == null ? name() : portuguese;
    }

    BWTeam(ChatColor chatcolor, Color color, short data) {
        this.chatColor=chatcolor;
        this.color = color;
        this.data = data;
        this.english = name();
        this.spanish = name();
        this.portuguese = name();
    }

    public static  BWTeam getTeam(String team){
        BWTeam finalteam = null;
        for(BWTeam bwTeam : values()){
            if(bwTeam.name().equalsIgnoreCase(team)){
                finalteam = bwTeam;
            } else if(bwTeam.getName("en").equalsIgnoreCase(team)) {
                finalteam = bwTeam;
            } else if(bwTeam.getName("es").equalsIgnoreCase(team)) {
                finalteam = bwTeam;
            } else if(bwTeam.getName("pt").equalsIgnoreCase(team)) {
                finalteam = bwTeam;
            }
        }
        return finalteam;
    }

    public String getName(String lang){
        switch (lang) {
            default:
                return spanish;
            case "pt":
                return portuguese;
            case "en":
                return english;
        }
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
