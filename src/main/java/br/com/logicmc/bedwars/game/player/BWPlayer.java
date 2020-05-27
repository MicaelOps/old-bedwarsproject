package br.com.logicmc.bedwars.game.player;

import org.bukkit.ChatColor;

import br.com.logicmc.core.account.addons.Parceable;

import java.util.UUID;

import com.google.gson.JsonObject;

public class BWPlayer extends Parceable<BWPlayer> {

    private UUID uuid;
    private String arena;

    private ChatColor teamcolor;

    private int kills, beds, deaths, wins, defeats;

    public BWPlayer() {}

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

    public void increaseKills() {
        kills++;
    }

    public void increaseBeds() {
        beds++;
    }

    @Override
    public void parse(JsonObject data) {
        if(data.has("beds")) {
            this.uuid = UUID.fromString(data.get("uuid").getAsString());
            this.arena = data.get("arena").getAsString();
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
        jsonObject.addProperty("uuid", this.uuid.toString());
        jsonObject.addProperty("kills", this.kills);
        jsonObject.addProperty("deaths", this.deaths);
        jsonObject.addProperty("beds", this.beds);
        jsonObject.addProperty("wins", this.wins);
        jsonObject.addProperty("defeats", this.defeats);
        jsonObject.addProperty("arena", this.arena);
        return jsonObject;
    }

}
