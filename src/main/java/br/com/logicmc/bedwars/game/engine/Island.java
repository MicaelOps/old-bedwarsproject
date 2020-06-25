package br.com.logicmc.bedwars.game.engine;

import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.generator.IslandGenerator;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.bukkit.Color;
import org.bukkit.Location;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public class Island {

    private final String name,arena;
    private final BWTeam team;
    private final Location spawn;

    private boolean bedbroken;
    private int forgery,sharpness,armor;
    private Location npc,bed,upgrade;
    private IslandGenerator generator;


    public Island(String name, String arena, BWTeam color , Location spawn, Location npc, Location upgrade, Location bed, Location generator) {
        this.name = name;
        this.arena = arena;
        this.npc = npc;
        this.bed = bed;
        this.generator = new IslandGenerator(generator);
        this.spawn = spawn;
        this.team = color;
        this.upgrade = upgrade;

        bedbroken = false;
        forgery = 0;
        sharpness = 0;
        armor = 0;

        this.npc.setY(Double.sum(this.npc.getBlockY(), 0.9));
        this.upgrade.setY(Double.sum(this.upgrade.getBlockY(), 0.9));
    }

    public String getArena() {
        return arena;
    }

    public int getForgery() {
        return forgery;
    }
    public void setForgery(int forgery) {
        this.forgery = forgery;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
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

    public Location getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(Location upgrade) {
        this.upgrade = upgrade;
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
        if(upgrade == null)
            System.out.println("[Arena] upgrade location of "+name+" from arena "+arenaname+" is null");
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
        if(upgrade !=null)
            file.setLocation(arenaname+".islands."+name+".upgrade" , upgrade);
    }

    public void forEachPlayers(Consumer<? super UUID> members){
        BWManager.getInstance().getArena(getArena()).getPlayers().stream().filter(uuid -> BWManager.getInstance().getBWPlayer(uuid).getTeamcolor().equalsIgnoreCase(getTeam().name())).forEach(members);
    }
    public void setGenerator(IslandGenerator islandGenerator) {
        this.generator= islandGenerator;
    }
}
