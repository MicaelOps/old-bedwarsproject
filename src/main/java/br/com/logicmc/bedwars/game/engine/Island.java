package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.engine.generator.IslandGenerator;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.bukkit.Color;
import org.bukkit.Location;

public class Island {

    private final String name;
    private final BWTeam team;

    private boolean bedbroken;
    private int generatorlevel,sharpness;
    private Location npc,bed,spawn;
    private IslandGenerator generator;


    public Island(String name, BWTeam color , Location spawn, Location npc, Location bed, Location generator) {
        this.name = name;
        this.npc = npc;
        this.bed = bed;
        this.generator = new IslandGenerator(generator);
        this.spawn = spawn;
        this.team = color;

        bedbroken = false;
        generatorlevel = 1;
        sharpness = 0;
    }

    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
    }

    public int getSharpness() {
        return sharpness;
    }

    public void setBedbroken(boolean bedbroken) {
        this.bedbroken = bedbroken;
    }

    public boolean isBedbroken() {
        return bedbroken;
    }

    public String getName() {
        return name;
    }

    public Location getSpawn() {
        return spawn;
    }

    public BWTeam getTeam() {
        return team;
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

    public IslandGenerator getGenerator() {
        return generator;
    }


    public void report(String arenaname) {
        if(npc == null)
            System.out.println("[Arena] Npc location of "+name+" from arena "+arenaname+" is null");
        if(bed == null)
            System.out.println("[Arena] Bed location of "+name+" from arena "+arenaname+" is null");
        if(generator == null)
            System.out.println("[Arena] Generator location of "+name+" from arena "+arenaname+" is null");
        if(spawn == null)
            System.out.println("[Arena] Spawn location of "+name+" from arena "+arenaname+" is null");
    }
    public void save(String arenaname, YamlFile file) {
        file.getConfig().set(arenaname+".islands."+name+".color" , team.name());
        if(npc != null)
            file.setLocation(arenaname+".islands."+name+".npc" , npc);
        if(spawn!=null)
            file.setLocation(arenaname+".islands."+name+".spawn" , spawn);
        if(bed!=null)
            file.setLocation(arenaname+".islands."+name+".bed" , bed);
        if(generator.getLocation() !=null)
            file.setLocation(arenaname+".islands."+name+".generator" , generator.getLocation());
    }

    public void setGenerator(IslandGenerator islandGenerator) {
        this.generator= islandGenerator;
    }
}
