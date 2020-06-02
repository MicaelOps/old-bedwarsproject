package br.com.logicmc.bedwars.game.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.PhaseControl;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.addons.hologram.Hologram;
import br.com.logicmc.core.system.party.Party;
import br.com.logicmc.core.system.party.PartyManager;

public class IngamePhase implements PhaseControl {

    private final HashSet<NormalGenerator> generators;
    private final List<String> available;
    private final String[] teams;
    
    private Scoreboard scoreboard;
    private int islandgenerators;

    public IngamePhase() {
        generators = new HashSet<>();
        available = new ArrayList<>();
        teams = new String[] {"AQUA", "BLACK", "GREEN", "RED", "BLUE", "GRAY", "WHITE", "YELLOW"};
        islandgenerators = -1;
    }

    @Override
    public int onTimerCall(Arena arena) {

        arena.setTime(arena.getTime() + 1);
        int time = arena.getTime();

        if (islandgenerators == time) {
            for (Island island : arena.getIslands()) {
                island.getGenerator().getWorld().dropItem(island.getGenerator(), new ItemStack(Material.IRON_INGOT));
            }
            islandgenerators = arena.getTime() + 5;
        }

        for (NormalGenerator generator : generators) {
            if (generator.reset(time)) {
                generator.spawn();
                generator.setNewReset();
            }
            Hologram hologram = generator.getHologram();

            int remainingtime = generator.getTime() - time;
            int i = remainingtime / 60;
            if (hologram != null)
                hologram.editText(
                        "§c" + (i < 10 ? "0" + i + ":" : i + ":") + (remainingtime % 60 < 10 ? "0" + remainingtime % 60 : remainingtime % 60));
        }
        return time;
    }

    @Override
    public void init(Arena arena) {
        arena.setTime(1);
        arena.setGamestate(Arena.INGAME);
        islandgenerators = arena.getTime() + 5;
        
        int index = 0;

        for(UUID vips : arena.getPreteam().keySet()){
            String team = arena.getPreteam().get(vips);
            for(int i = 0; i < available.size(); i++) {
                if(available.get(i).equalsIgnoreCase(team)){
                    BWMain.getInstance().playermanager.getPlayerBase(vips).getData().setTeamcolor(team);
                    available.remove(i);
                    break;
                }
            }
        }

        //add players to team
        for(UUID uuid : arena.getPlayers()) {

            PlayerBase<BWPlayer> baseplayer = BWMain.getInstance().playermanager.getPlayerBase(uuid);
            BWPlayer bwPlayer = baseplayer.getData();
            Party party = PartyManager.getInstance().getParty(baseplayer.getPartyid());

            
            if(bwPlayer.getTeamcolor() == null || bwPlayer.getTeamcolor().isEmpty()) {

        
                if(party != null) {

                    // adds uuid to the same team as other members
                    for(UUID members : party.getMembers()) { // contains owner and everyone
                        if(arena.getPlayers().contains(members)){
                            BWPlayer memberbw = BWMain.getInstance().playermanager.getPlayerBase(uuid).getData();
                            
                            if(memberbw.getTeamcolor() != null && !bwPlayer.getTeamcolor().isEmpty()) {
                                
                                // member already has team but uuid dosen't therefore we have to make sure we at least try to add uuid to the me team as member.
                                for(int i = 0; i < available.size(); i++) {
                                    if(available.get(i).equalsIgnoreCase(memberbw.getTeamcolor())){
                                        bwPlayer.setTeamcolor(available.get(i));
                                        available.remove(i);
                                        break;
                                    }
                                }

                                // member team is already full
                                if(bwPlayer.getTeamcolor() == null || bwPlayer.getTeamcolor().isEmpty()) {
                                    bwPlayer.setTeamcolor(available.get(index));
                                    index+=1;
                                }                         
                            }
                        }
                    }
                    

                    // if the members yet dont have teams we have to add uuid to a team and also the members
                    if(bwPlayer.getTeamcolor() == null || bwPlayer.getTeamcolor().isEmpty()) {

                        String userteam = available.get(index);

                        bwPlayer.setTeamcolor(userteam);
                        index+=1; // follows order

                        for(UUID members : party.getMembers()) {
                            BWMain.getInstance().playermanager.getPlayerBase(members).getData().setTeamcolor(available.get(index));
                            index+=1; 
                        }
                    }

                } else {
                    bwPlayer.setTeamcolor(available.get(index));
                    index+=1;
                }
            }
            Team scteam = scoreboard.getTeam(bwPlayer.getTeamcolor());
            if(scteam.getEntries().isEmpty()) {
                scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§"+index).setScore(index+5);
                scteam.addEntry("§"+index);
                index++;
            }

            //prepare player
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage(bwPlayer.getTeamcolor());
            arena.updateScoreboardTeam(player, bwPlayer.getTeamcolor(), " §7(You)");

        }

  

        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§l").setScore((index+5+1));
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§k").setScore((index+5+2));
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§o").setScore((index+5+3));
        scoreboard.getTeam("upgrade").addEntry("§k");

        arena.getPreteam().clear();
    
        
 
        available.clear();
    }

    @Override
    public void stop(Arena engine) {

    }

    @Override
    public boolean end(Arena engine) {
        return false;
    }

    @Override
    public PhaseControl next() {
        return new EndPhase();
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
    
    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        if(!entry.isEmpty())
            team.addEntry(entry);
    }

    private void createArmostand(Location location, Material material){
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setHelmet(new ItemStack(material));
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setSmall(false);
        armorStand.setCustomNameVisible(false);
    }

    @Override
    public void preinit(Arena arena) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ingame"+new Random().nextInt(10000),"dummy");

        objective.setDisplayName("§b§lBEDWARS");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		objective.getScore("§e").setScore(4);
		objective.getScore("§d").setScore(3);
		objective.getScore("§c").setScore(2);
		objective.getScore("§b").setScore(1);
		objective.getScore("§a").setScore(0);

        createTeam(scoreboard, "upgrade", "§fDiamond II em","§a 00:00","");
        createTeam(scoreboard, "kills", "§fMatou: ","§a0","§d");
        createTeam(scoreboard, "beds", "§fCapturas: ","§a0","§c");
        createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§a");
        
        World world = Bukkit.getWorld(arena.getName());

        for (Location diamond : arena.getDiamond()) {

            diamond.setWorld(world);
        
            createArmostand(diamond, Material.DIAMOND_BLOCK);
            generators.add(new NormalGenerator(diamond, Material.DIAMOND,
                    new Hologram(diamond.subtract(0.0D, 0.6D, 0.0D), "10:00"), 80));
        }
        for (Location emerald : arena.getEmerald()) {
            emerald.setWorld(world);
            createArmostand(emerald, Material.EMERALD_BLOCK);
            generators.add(new NormalGenerator(emerald, Material.EMERALD,
                    new Hologram(emerald.subtract(0.0D, 0.6D, 0.0D), "10:00"), 90));
        }
        int index = 0;
        for(String team : teams){
            createTeam(scoreboard, team,"§aV §f"+WordUtils.capitalize(team.toLowerCase()) ,"", "");
            for(int i = 0; i < arena.getTeamcomposition(); i++) {
                available.add(index, team);
                index++;
            }
        }
    }
}
