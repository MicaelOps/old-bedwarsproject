package br.com.logicmc.bedwars.game.player;

import com.google.gson.Gson;
import org.bukkit.ChatColor;

import br.com.logicmc.core.account.addons.Parceable;


import com.google.gson.JsonObject;
import org.bukkit.Material;

public class BWPlayer extends Parceable<BWPlayer> {

  
    private String arena;

    private String teamcolor;

    private int kills, deaths, wins, level, beds;

    public BWPlayer() {
        kills=0;
        level=0;
        wins=0;
		beds = 0;
		deaths=0;
        teamcolor=null;
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


    public int getKills() {
        return kills;
    }
	
    public int getBeds() {
        return beds;
    }

    public void increaseKills() {
        kills++;
    }

    public void increaseBeds() {
        beds++;
    }

    @Override
    public void parse(JsonObject data) {
        if(data.has("solo_stats")) {
			JsonObject jsonObject = new Gson().fromJson(data.get("solo_stats").getAsString(), JsonObject.class);
            this.kills = jsonObject.get("kills").getAsInt();
            this.level = jsonObject.get("level").getAsInt();
            this.wins = jsonObject.get("wins").getAsInt();
            this.deaths = jsonObject.get("deaths").getAsInt();
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonObject datastats = new JsonObject();
        datastats.addProperty("kills", this.kills);
        datastats.addProperty("deaths", this.deaths);
        datastats.addProperty("level", this.level);
        datastats.addProperty("wins", this.wins);
		jsonObject.add("solo_stats", datastats);
        return jsonObject;
    }

}
