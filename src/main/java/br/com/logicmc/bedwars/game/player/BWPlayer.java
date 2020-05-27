package br.com.logicmc.bedwars.game.player;

import org.bukkit.ChatColor;


import java.util.UUID;

public class BWPlayer {

    private final UUID uuid;
    private final String arena;

    private ChatColor teamcolor;

    private int kills,beds,deaths,wins,defeats;

    public BWPlayer(UUID uuid, String arena) {
        this.uuid = uuid;
        this.arena = arena;
        teamcolor = null;
    }

    public String getMapname() {
        return arena;
    }

    public ChatColor getTeamcolor() {
        return teamcolor;
    }

    public void setTeamcolor(ChatColor teamcolor) {
        this.teamcolor = teamcolor;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getBeds() {
        return beds;
    }

    public int getKills() {
        return kills;
    }

    public void increaseKills(){
        kills++;
    }
    public void increaseBeds(){
        beds++;
    }
}
