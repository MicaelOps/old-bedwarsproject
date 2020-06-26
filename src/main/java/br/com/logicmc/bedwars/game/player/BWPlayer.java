package br.com.logicmc.bedwars.game.player;

import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.core.account.addons.DataStats;
import com.google.gson.Gson;
import org.bukkit.ChatColor;

import br.com.logicmc.core.account.addons.Parceable;


import com.google.gson.JsonObject;
import org.bukkit.Material;

import java.util.UUID;

public class BWPlayer extends Parceable<BWPlayer> {


    private UUID uuid;
    private String name;
    private DataStats solo, squad;


    private String arena;
    private String teamcolor;

    private int teamcomp,beds,level;

    public BWPlayer(UUID uuid, String name, int level, DataStats solo, DataStats squad, int teamcomp ) {
        this.uuid = uuid;
        this.name = name;
        this.level = level;
        this.solo = solo;
        this.squad = squad;
        this.teamcomp = teamcomp;

        teamcolor="";
    }

    public String getMapname() {
        return arena;
    }

    public void setMap(String arena) {
        this.arena = arena;
    }
	
    public String getTeamcolor() {
        return teamcolor;
    }

    public void setTeamcolor(String teamcolor) {
        this.teamcolor = teamcolor;
    }


    public DataStats getStats() {
        return teamcomp == Arena.SOLO ? getSoloStats() : getSquadStats();
    }
    public int getKills() {
        return getStats().getKills();
    }
	
    public int getBeds() {
        return beds;
    }

    public void increaseKills() {
        getStats().addKills();
    }

    public void increaseBeds() {
        beds++;
    }
    public void increaseWins() {
        getStats().addWins();
    }

    public void increaseDeaths() {
        getStats().addDeaths();
    }
    public void increaseLoses(){
        getStats().addLoses();
    }

    @Override
    public void parse(JsonObject data) {
        if(data.has("data")){
            BWPlayer databw = new Gson().fromJson(data.get("data").getAsString(), BWPlayer.class);
            unify(databw);
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject data = new JsonObject();
        data.addProperty("data", new Gson().toJson(this));
        return data;
    }

    public void unify(BWPlayer data) {
        setUuid(data.getUuid());
        setLevel(data.getLevel());
        setName(data.getName());
        setSquad(data.getSquadStats());
        setSolo(data.getSoloStats());
    }

    public DataStats getSquadStats() {
        return squad;
    }

    public DataStats getSoloStats() {
        return solo;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSolo(DataStats solo) {
        this.solo = solo;
    }

    public void setSquad(DataStats squad) {
        this.squad = squad;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public void setTeamComp(int teamcomposition) {
        this.teamcomp=teamcomposition;
    }
}
