package br.com.logicmc.bedwars.game.player;

import org.bukkit.Color;


import java.util.UUID;

public class BWPlayer {

    private final UUID uuid;
    private final String arena;

    private Color teamcolor;

    private int kills,beds;

    public BWPlayer(UUID uuid, String arena) {
        this.uuid = uuid;
        this.arena = arena;
    }

    public String getMapname() {
        return arena;
    }

    public Color getTeamcolor() {
        return teamcolor;
    }

    public void setTeamcolor(Color teamcolor) {
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
