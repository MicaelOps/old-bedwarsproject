package br.com.logicmc.bedwars.game.player;

import org.bukkit.ChatColor;

import br.com.logicmc.core.account.addons.Parceable;


import com.google.gson.JsonObject;
import org.bukkit.Material;

public class BWPlayer extends Parceable<BWPlayer> {

  
    private String arena;

    private String teamcolor;

    private Material armor;
    private int kills, beds, deaths, wins, defeats;

    public BWPlayer() {
        kills=0;
        beds=0;
        wins=0;
        defeats=0;
        teamcolor=null;
        armor = Material.AIR;
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

    public void setArmor(Material armor) {
        this.armor = armor;
    }

    public Material getArmor() {
        return armor;
    }

    public int getBeds() {
        return beds;
    }

    public int getKills() {
        return kills;
    }

    public void increaseKills() {
        kills++;
    }

    public void increaseBeds() {
        beds++;
    }

    @Override
    public void parse(JsonObject data) {
        if(data.has("beds")) {
            this.kills = data.get("kills").getAsInt();
            this.defeats = data.get("defeats").getAsInt();
            this.beds = data.get("beds").getAsInt();
            this.wins = data.get("wins").getAsInt();
            this.deaths = data.get("deaths").getAsInt();
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("kills", this.kills);
        jsonObject.addProperty("deaths", this.deaths);
        jsonObject.addProperty("beds", this.beds);
        jsonObject.addProperty("wins", this.wins);
        jsonObject.addProperty("defeats", this.defeats);
        return jsonObject;
    }

}
