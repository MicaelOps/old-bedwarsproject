package br.com.logicmc.skywars;

import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.account.addons.Preferences;
import com.google.gson.Gson;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TestTeam {

    private static HashSet<UUID> uuids;
    private static HashMap<UUID , String> preteam;

    private final ChatColor[] teams  = {ChatColor.RED,ChatColor.BLUE, ChatColor.GREEN, ChatColor.BLACK, ChatColor.GRAY, ChatColor.WHITE};

    private int comp;

    public static void main(String[] args) {
        PlayerBase<String> playerBase = new PlayerBase<String>("localhost", UUID.randomUUID(), "asd" , 3,3,3,3,3,3, new Preferences("eu", false,false));

        playerBase.setData("apsdlaps");

        System.out.println(new Gson().toJson(playerBase));
    }

    public void initCollections() {
        preteam = new HashMap<>();
        uuids = new HashSet<>();
        
        comp = 2; // DUO

        preteam.put(UUID.randomUUID(), "RED");
        preteam.put(UUID.randomUUID(), "BLUE");
    }


    public void doAlgorithm() {

        PlayerBase<String> playerBase = new PlayerBase<String>("localhost", UUID.randomUUID(), "asd" , 3,3,3,3,3,3, new Preferences("eu", false,false));

        playerBase.setData("apsdlaps");

        System.out.println(new Gson().toJson(playerBase));
    }
}
