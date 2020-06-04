package br.com.logicmc.bedwars.game.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
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

    private final List<String> available;

    private Scoreboard scoreboard;
    private int islandgenerators,diamondgenerators,emeraldgenerators;

    public IngamePhase() {
        available = new ArrayList<>();
        islandgenerators = -1;
    }
    /**
     * islandgenerators is not used as a cooldown but as a minimum cooldown to verify if the generators are in cooldown;
     */

    @Override
    public int onTimerCall(Arena arena) {

        arena.setTime(arena.getTime() + 1);
        int time = arena.getTime();

        if (islandgenerators == time ) {
            for (Island island : arena.getIslands()) {
                NormalGenerator generator = island.getGenerator();
                generator.spawn();
                generator.setNewReset();
                
            }
            islandgenerators+=3;
        }
        for (NormalGenerator generator : arena.getDiamond()) {
            resetGenerator(generator, time);
        }

        for (NormalGenerator generator : arena.getEmerald()) {
            resetGenerator(generator, time);
        }
        return time;
    }

    private void resetGenerator(NormalGenerator generator, int time) {
        if (generator.reset(time)) {
            generator.spawn();
            generator.setNewReset();
        }
        Hologram hologram = generator.getHologram();

        if (hologram != null) {
            int remainingtime = generator.getTime() - time;
            int i = remainingtime / 60;
            hologram.editText("§e" + (i < 10 ? "0" + i + ":" : i + ":") + (remainingtime % 60 < 10 ? "0" + remainingtime % 60 : remainingtime % 60));
        }
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
            arena.updateScoreboardTeam(player, bwPlayer.getTeamcolor(), "§a V §7(You)");
            player.teleport(arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(bwPlayer.getTeamcolor().toUpperCase())).findFirst().get().getSpawn());
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setItem(0,new ItemStack(Material.WOOD_SWORD));
            player.getInventory().setItem(1,new ItemStack(Material.COMPASS));
            player.setDisplayName(BWTeam.valueOf(bwPlayer.getTeamcolor()).getChatColor()+player.getName());
        }
        

        arena.getIslands().removeIf(island -> BWMain.getInstance().playermanager.getPlayers().stream().noneMatch(bwPlayerPlayerBase -> bwPlayerPlayerBase.getData().getTeamcolor().equalsIgnoreCase(island.getTeam().name())));

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
        

        islandgenerators = 3;
        int index = 0;
        for(BWTeam team : BWTeam.values()){
            createTeam(scoreboard, team.name(),team.getChatColor()+WordUtils.capitalize(team.name().toLowerCase()) ,ChatColor.GREEN+" V", "");
            for(int i = 0; i < arena.getTeamcomposition(); i++) {
                available.add(index, team.name());
                index++;
            }
        }
    }
}
