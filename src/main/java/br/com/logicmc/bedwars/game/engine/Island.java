package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.extra.YamlFile;
import org.bukkit.Color;
import org.bukkit.Location;

public class Island {

    private final String name;

    private int generatorlevel;

    private Color teamid;
    private Location npc,bed,generator;


    public Island(String name, Location npc, Location bed, Location generator) {
        this.name = name;
        this.npc = npc;
        this.bed = bed;
        this.generator = generator;

        generatorlevel = 1;
    }

    public Color getTeamid() {
        return teamid;
    }

    public void setTeamid(Color teamid) {
        this.teamid = teamid;
    }

    public int getGeneratorlevel() {
        return generatorlevel;
    }

    public void setGeneratorlevel(int generatorlevel) {
        this.generatorlevel = generatorlevel;
    }

    public Location getNpc() {
        return npc;
    }

    public void setNpc(Location npc) {
        this.npc = npc;
    }

    public Location getBed() {
        return bed;
    }

    public void setBed(Location bed) {
        this.bed = bed;
    }

    public Location getGenerator() {
        return generator;
    }

    public void setGenerator(Location generator) {
        this.generator = generator;
    }

    public void report(String arenaname) {
        if(npc == null)
            System.out.println("[Arena] Npc location of "+name+" from arena "+arenaname+" is null");
        if(bed == null)
            System.out.println("[Arena] Bed location of "+name+" from arena "+arenaname+" is null");
        if(generator == null)
            System.out.println("[Arena] Generator location of "+name+" from arena "+arenaname+" is null");
    }
    public void save(String arenaname, YamlFile file) {
        file.setLocation(arenaname+".islands."+name+".npc" , npc);
        file.setLocation(arenaname+".islands."+name+".bed" , bed);
        file.setLocation(arenaname+".islands."+name+".generator" , generator);
    }
}
