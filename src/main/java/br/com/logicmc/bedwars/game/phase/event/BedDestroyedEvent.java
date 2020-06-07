package br.com.logicmc.bedwars.game.phase.event;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.account.PlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;


public class BedDestroyedEvent extends PhaseEvent{

    public BedDestroyedEvent(int inittime) {
        super(inittime, "Camas destruidas");
    }

    @Override
    public void execute(Arena arena) {
        for(Island island : arena.getIslands()){
            island.getBed().getBlock().setType(Material.AIR);
            island.setBedbroken(true);
            arena.updateScoreboardForAll(island.getTeam().name(), ChatColor.RED+" X");
        }

        for(UUID uuid : arena.getPlayers()){
            Player everyone = Bukkit.getPlayer(uuid);
            PlayerBase<BWPlayer> playerbase = BWMain.getInstance().playermanager.getPlayerBase(uuid);
            BWTeam bwTeam = BWTeam.valueOf(playerbase.getData().getTeamcolor());
            if(everyone.getDisplayName().contains(bwTeam.getChatColor()+""))
                arena.updateScoreboardTeam(everyone, bwTeam.name(), ChatColor.RED+" X (You)");
            everyone.sendTitle("", ChatColor.RED+BWMain.getInstance().messagehandler.getMessage(BWMessages.BEDS_DESTROYED, playerbase.getPreferences().getLang()));
        }

    }
}
