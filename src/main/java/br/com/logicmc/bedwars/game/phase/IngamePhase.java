package br.com.logicmc.bedwars.game.phase;

import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.PhaseControl;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.core.addons.hologram.Hologram;

public class IngamePhase implements PhaseControl {

    private final HashSet<NormalGenerator> generators;
    private int islandgenerators;

    public IngamePhase() {
        generators = new HashSet<>();
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
        islandgenerators = arena.getTime() + 5;

        for (Location diamond : arena.getDiamond()) {
            createArmostand(diamond.add(0.0D, 2.0D, 0.0D), Material.DIAMOND_BLOCK);
            generators.add(new NormalGenerator(diamond, Material.DIAMOND,
                    new Hologram(diamond.add(0.0D, 1.6D, 0.0D), "10:00"), 80));
        }
        for (Location emerald : arena.getEmerald()) {
            createArmostand(emerald.add(0.0D, 2.0D, 0.0D), Material.EMERALD_BLOCK);
            generators.add(new NormalGenerator(emerald, Material.EMERALD,
                    new Hologram(emerald.add(0.0D, 1.6D, 0.0D), "10:00"), 90));
        }


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
    public Scoreboard buildScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ingame"+new Random().nextInt(10000),"dummy");

        objective.setDisplayName("§b§lBEDWARS");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore("§8").setScore(8);
        objective.getScore("§7").setScore(7);
		objective.getScore("§6").setScore(6);
        objective.getScore("§5").setScore(5);
		objective.getScore("§4").setScore(4);
		objective.getScore("§3").setScore(3);
		objective.getScore("§2").setScore(2);
		objective.getScore("§1").setScore(1);
		objective.getScore("§0").setScore(0);

        createTeam(scoreboard, "time", "§fMelhoria: ","§a00:00","§7");
        createTeam(scoreboard, "kills", "§fMatou: ","§a3","§5");
        createTeam(scoreboard, "beds", "§fMorreu: ","§a3","§4");
		createTeam(scoreboard, "teams", "§fEquipas: ","§a3","§2");
		createTeam(scoreboard, "site", "§7www.logic","§7mc.com.br","§0");
        return scoreboard;
    }
    private void createTeam(Scoreboard scoreboard, String name, String prefix, String suffix, String entry) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(prefix);
        team.setSuffix(suffix);
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
}
