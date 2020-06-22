package br.com.logicmc.bedwars.game.phase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.logicmc.bedwars.extra.BWMessages;
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
    private final int stopupgrade;

    private PhaseEvent event;

    public IngamePhase() {
        available = new ArrayList<>();
        stopupgrade = 1198;

        event = new GeneratorEvent(300, "Diamond II", 0);
    }

    @Override
    public int onTimerCall(Arena arena) {

        arena.setTime(arena.getTime() + 1);
        int time = arena.getTime();

        for (Island island : arena.getIslands()) {
            NormalGenerator generator = island.getGenerator();
            generator.spawn();

        }
        for (NormalGenerator generator : arena.getDiamond()) {
            resetGenerator(generator, time);
        }

        for (NormalGenerator generator : arena.getEmerald()) {
            resetGenerator(generator, time);
        }

        if(time % 60 == 0){
            for(UUID uuid : arena.getPlayers()){
                Player player = Bukkit.getPlayer(uuid);
                if(player.getGameMode() == GameMode.SURVIVAL){
                    BWMain.getInstance().playermanager.getPlayerBase(uuid).addCoins(10);
                    player.sendMessage("§5+10 coins");
                }
            }
        }
        int remainingtime = event.getInittime() - time;

        // event load
        if(remainingtime == 0){
            event.execute(arena);

            if(time < stopupgrade) {
                if (event.getEventname().startsWith("D"))
                    event = new GeneratorEvent(time + 300, event.getEventname().replace("Diamond","Emerald"), 1);
                else
                    event = new GeneratorEvent(time + 300, event.getEventname().replace("Emerald","Diamond")+"I", 0);
            } else if(event.getEventname().equalsIgnoreCase("Cama destruida"))
                event = new SuddenDeathEvent(time+(10*60));
            else
                event = new BedDestroyedEvent(time+(10*60));

            arena.forEachScoreboard(scoreboard-> scoreboard.getTeam("upgrade").setPrefix(ChatColor.WHITE+event.getEventname()));
            remainingtime = event.getInittime() - time;
        }

        int i = remainingtime / 60;
        int finalRemainingtime = remainingtime;
        String s = (i < 10 ? "0" + i + ":" : i + ":") + (finalRemainingtime % 60 < 10 ? "0" + finalRemainingtime % 60 : finalRemainingtime % 60);
        arena.getScoreboard("pt").getTeam("upgrade").setSuffix("§f "+getTranslatedMessage(BWMessages.PREPOSITION_IN, "pt")+" §a" + s);
        arena.getScoreboard("en").getTeam("upgrade").setSuffix("§f "+getTranslatedMessage(BWMessages.PREPOSITION_IN, "en")+" §a" + s);
        arena.getScoreboard("es").getTeam("upgrade").setSuffix("§f "+getTranslatedMessage(BWMessages.PREPOSITION_IN, "es")+" §a" + s);


        return time;
    }

    @Override
    public int getIndex() {
        return 3;
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
            hologram.editLine(2,"§e" + (i < 10 ? "0" + i + ":" : i + ":") + (remainingtime % 60 < 10 ? "0" + remainingtime % 60 : remainingtime % 60));
        }
    }
    @Override
    public void init(Arena arena) {
        arena.setTime(1);
        arena.setGamestate(Arena.INGAME);
        
        int index = 0;
        int adder = arena.getTeamcomposition() == Arena.SOLO ? 2 : 5;
        final int[] score = { 0 };

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

            AtomicBoolean increase = new AtomicBoolean(true);

            int finalscore = score[0];
            arena.forEachScoreboard(scoreboard ->  {
                Team scteam = scoreboard.getTeam(bwPlayer.getTeamcolor());
                if(scteam.getEntries().isEmpty()) {
                    scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§"+ finalscore).setScore(finalscore+adder);
                    scteam.addEntry("§"+ finalscore);
                    if(increase.get()) {
                        score[0]++;
                        increase.set(false);
                    }

                }
            });
            increase.set(true);


            //prepare player
            Player player = Bukkit.getPlayer(uuid);
            BWTeam team = BWTeam.valueOf(bwPlayer.getTeamcolor());
            arena.updateScoreboardTeam(player, bwPlayer.getTeamcolor(), "§a ✓ §7(You)");

            Optional<Island> value = arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(bwPlayer.getTeamcolor())).findFirst();

            if(value.isPresent()) {
                player.teleport(value.get().getSpawn());
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.getInventory().setHelmet(BWMain.getInstance().createColorouedArmor(Material.LEATHER_HELMET, team.getColor()));
                player.getInventory().setChestplate(BWMain.getInstance().createColorouedArmor(Material.LEATHER_CHESTPLATE, team.getColor()));
                player.getInventory().setLeggings(BWMain.getInstance().createColorouedArmor(Material.LEATHER_LEGGINGS, team.getColor()));
                player.getInventory().setBoots(BWMain.getInstance().createColorouedArmor(Material.LEATHER_BOOTS, team.getColor()));
                player.getInventory().setItem(0,new ItemStack(Material.WOOD_SWORD));
                player.setDisplayName(team.getChatColor()+player.getName());
            } else {
                player.kickPlayer("Not handled! "+bwPlayer.getTeamcolor()+" for "+arena.getName());
            }
        
        }

        for(UUID uuid : arena.getPlayers()){
            Player player = Bukkit.getPlayer(uuid);
            BWPlayer bwplayer = BWManager.getInstance().getBWPlayer(uuid);
            List<String> list = new ArrayList<>();
            arena.getPlayers().stream().filter(e->!BWManager.getInstance().getBWPlayer(e).getTeamcolor().equalsIgnoreCase(bwplayer.getTeamcolor())).forEach(e->list.add(BWMain.getInstance().playermanager.getPlayerBase(e).getName()));


            if(!list.isEmpty()) {
                arena.forEachScoreboard(scoreboard -> arena.updateEntry(player, scoreboard, "enemy", list));
                list.clear();
            }

            arena.getPlayers().stream().filter(e->BWManager.getInstance().getBWPlayer(e).getTeamcolor().equalsIgnoreCase(bwplayer.getTeamcolor())).forEach(e->list.add(BWMain.getInstance().playermanager.getPlayerBase(e).getName()));
            arena.forEachScoreboard(scoreboard -> arena.updateEntry(player, scoreboard, "friend", list));
        }

        arena.forEachScoreboard(scoreboard ->{
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§l").setScore((score[0]+adder));
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§k").setScore((score[0]+adder+1));
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§o").setScore((score[0]+adder+2));
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore("§r").setScore((score[0]+adder+3));
            scoreboard.getTeam("upgrade").addEntry("§k");
            scoreboard.getTeam("date").addEntry("§r");
        });

        arena.getPreteam().clear();
        arena.getIslands().removeIf(island -> arena.getPlayers().stream().noneMatch(uuid->BWManager.getInstance().getBWPlayer(uuid).getTeamcolor().equalsIgnoreCase(island.getTeam().name())));

        available.clear();
    }

    
    @Override
    public void stop(Arena arena) {
        arena.forEachScoreboard(scoreboard ->scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister());
        arena.forEachScoreboard(scoreboard ->scoreboard.getTeams().forEach(Team::unregister));
        event = new GeneratorEvent(300, "Diamond II", 0);
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

        int index = 0;
        for(BWTeam team : BWTeam.values()){
            for(int i = 0; i < arena.getTeamcomposition(); i++) {
                available.add(index, team.name());
                index++;
            }
        }
    }

    @Override
    public Scoreboard createScoreboard(Arena arena, String lang, Scoreboard scoreboard) {
        if(scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

        if(objective == null) {
            objective = scoreboard.registerNewObjective("ingame" + new Random().nextInt(10000), "dummy");
            objective.setDisplayName("§e§lBED WARS");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        }

        for(BWTeam team : BWTeam.values()){
            createTeam(scoreboard, team.name(),ChatColor.BOLD+""+team.getChatColor()+team.getName(lang).charAt(0)+" §f"+WordUtils.capitalize(team.getName(lang).toLowerCase()) ,ChatColor.GREEN+" ✓", "");
        }
        objective.getScore("§b").setScore(1);
        objective.getScore("§a").setScore(0);

        if(arena.getTeamcomposition() != Arena.SOLO){
            objective.getScore("§e").setScore(4);
            objective.getScore("§d").setScore(3);
            objective.getScore("§c").setScore(2);
            createTeam(scoreboard, "kills", "§f"+getTranslatedMessage(BWMessages.WORD_KILLS, lang)+": ","§a0","§d");
            createTeam(scoreboard, "beds", "§f"+getTranslatedMessage(BWMessages.WORD_CAPTURED, lang)+": ","§a0","§c");
        } else {
            createTeam(scoreboard, "kills", "§f"+getTranslatedMessage(BWMessages.WORD_KILLS, lang)+": ","§a0","");
            createTeam(scoreboard, "beds", "§f"+getTranslatedMessage(BWMessages.WORD_CAPTURED, lang)+": ","§a0","");
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        createTeam(scoreboard, "date","§7"+dtf.format(now),"","");
        createTeam(scoreboard, "upgrade", "§f"+event.getEventname(),"§f em §a 00:00","");
        createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§a");
        createTeam(scoreboard, "enemy", "§c","","");
        createTeam(scoreboard, "friend", "§a","","");
        return scoreboard;
    }

    private String getTranslatedMessage(BWMessages msg, String lang) {
        return BWMain.getInstance().messagehandler.getMessage(msg, lang);
    }
}
