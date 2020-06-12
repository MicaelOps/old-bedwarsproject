package br.com.logicmc.bedwars.game.phase;

import java.util.*;
import java.util.function.Consumer;

import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.phase.event.BedDestroyedEvent;
import br.com.logicmc.bedwars.game.phase.event.GeneratorEvent;
import br.com.logicmc.bedwars.game.phase.event.PhaseEvent;
import br.com.logicmc.bedwars.game.phase.event.SuddenDeathEvent;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
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
    private int stopupgrade;

    private PhaseEvent event;

    public IngamePhase() {
        available = new ArrayList<>();
        stopupgrade = 25*60;

        event = new GeneratorEvent(300, "Diamond II", 0);
    }

    @Override
    public int onTimerCall(Arena arena) {

        arena.setTime(arena.getTime() + 1);
        int time = arena.getTime();

        for (Island island : arena.getIslands()) {
            NormalGenerator generator = island.getGenerator();
            generator.spawn();
            generator.setNewReset();

        }
        for (NormalGenerator generator : arena.getDiamond()) {
            resetGenerator(generator, time);
        }

        for (NormalGenerator generator : arena.getEmerald()) {
            resetGenerator(generator, time);
        }

        // event load
        int remainingtime = event.getInittime() - time;

        if(remainingtime > 0){

            int i = remainingtime / 60;
            arena.updateScoreboardForAll("upgrade","§f em §a" + (i < 10 ? "0" + i + ":" : i + ":") + (remainingtime % 60 < 10 ? "0" + remainingtime % 60 : remainingtime % 60));

        } else {

            event.execute(arena);

            if(time < stopupgrade) {
                if (event.getEventname().startsWith("D"))
                    event = new GeneratorEvent(time + 300, event.getEventname().replace("Diamond","Emerald")+"I", 1);
                else
                    event = new GeneratorEvent(time + 300, event.getEventname().replace("Emerald","Diamond")+"I", 0);
            } else if(event.getEventname().equalsIgnoreCase("Camas destruidas"))
                event = new SuddenDeathEvent(time+(10*60));
            else
                event = new BedDestroyedEvent(time+(10*60));

            arena.getScoreboard().getTeam("upgrade").setPrefix(ChatColor.WHITE+event.getEventname());
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
            hologram.editLine(0,"§e" + (i < 10 ? "0" + i + ":" : i + ":") + (remainingtime % 60 < 10 ? "0" + remainingtime % 60 : remainingtime % 60));
        }
    }
    @Override
    public void init(Arena arena) {
        arena.setTime(1);
        arena.setGamestate(Arena.INGAME);
        
        int index = 0,score=0;

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
                scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§"+score).setScore(score+5);
                scteam.addEntry("§"+score);
                score++;
            }

            //prepare player
            Player player = Bukkit.getPlayer(uuid);
            BWTeam team = BWTeam.valueOf(bwPlayer.getTeamcolor());
            arena.updateScoreboardTeam(player, bwPlayer.getTeamcolor(), "§a ✓ §7(You)");

            player.teleport(arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(bwPlayer.getTeamcolor())).findFirst().get().getSpawn());
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setHelmet(BWMain.getInstance().createColorouedArmor(Material.LEATHER_HELMET, team.getColor()));
            player.getInventory().setChestplate(BWMain.getInstance().createColorouedArmor(Material.LEATHER_CHESTPLATE, team.getColor()));
            player.getInventory().setLeggings(BWMain.getInstance().createColorouedArmor(Material.LEATHER_LEGGINGS, team.getColor()));
            player.getInventory().setBoots(BWMain.getInstance().createColorouedArmor(Material.LEATHER_BOOTS, team.getColor()));
            player.getInventory().setItem(0,new ItemStack(Material.WOOD_SWORD));
            player.setDisplayName(team.getChatColor()+player.getName());
        
        }

        for(UUID uuid : arena.getPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            BWPlayer bwplayer = BWManager.getInstance().getBWPlayer(uuid);
            List<String> list = new ArrayList<>();
            arena.getPlayers().stream().filter(e->!BWManager.getInstance().getBWPlayer(e).getTeamcolor().equalsIgnoreCase(bwplayer.getTeamcolor())).forEach(e->list.add(BWMain.getInstance().playermanager.getPlayerBase(e).getName()));


            if(!list.isEmpty()) {
                BWMain.getInstance().updateEntry(player, arena.getScoreboard(), "enemy", list);
                list.clear();
            }

            arena.getPlayers().stream().filter(e->BWManager.getInstance().getBWPlayer(e).getTeamcolor().equalsIgnoreCase(bwplayer.getTeamcolor())).forEach(e->list.add(BWMain.getInstance().playermanager.getPlayerBase(e).getName()));
            BWMain.getInstance().updateEntry(player, arena.getScoreboard(), "friend", list);
        }

        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§l").setScore((index+5+1));
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§k").setScore((index+5+2));
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§o").setScore((index+5+3));
        scoreboard.getTeam("upgrade").addEntry("§k");

        arena.getPreteam().clear();
        arena.getIslands().removeIf(island -> arena.getPlayers().stream().noneMatch(uuid->BWManager.getInstance().getBWPlayer(uuid).getTeamcolor().equalsIgnoreCase(island.getTeam().name())));

        available.clear();
    }

    
    @Override
    public void stop(Arena engine) {
        scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
        scoreboard.getTeams().forEach(Team::unregister);
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

        createTeam(scoreboard, "upgrade", "§f"+event.getEventname(),"§f em §a 00:00","");
        createTeam(scoreboard, "kills", "§fMatou: ","§a0","§d");
        createTeam(scoreboard, "beds", "§fCapturas: ","§a0","§c");
        createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§a");
        createTeam(scoreboard, "enemy", "§c","","");
        createTeam(scoreboard, "friend", "§a","","");

        int index = 0;
        for(BWTeam team : BWTeam.values()){
            createTeam(scoreboard, team.name(),ChatColor.BOLD+""+team.getChatColor()+team.name().charAt(0)+" §f"+WordUtils.capitalize(team.name().toLowerCase()) ,ChatColor.GREEN+" ✓", "");
            for(int i = 0; i < arena.getTeamcomposition(); i++) {
                available.add(index, team.name());
                index++;
            }
        }
    }
}
