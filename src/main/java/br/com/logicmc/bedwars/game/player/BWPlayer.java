package br.com.logicmc.bedwars.game.player;

import org.bukkit.Color;


import java.util.UUID;

public class BWPlayer {

    private final UUID uuid;
    private final String mapname;

    private Color teamcolor;

    private int kills,beds;

    public BWPlayer(UUID uuid, String mapname) {
        this.uuid = uuid;
        this.mapname = mapname;
    }

    public String getMapname() {
        return mapname;
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
