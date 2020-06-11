package br.com.logicmc.bedwars.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.account.addons.Groups;
import br.com.logicmc.core.system.command.CommandAdapter;
import br.com.logicmc.core.system.command.SimpleComamnd;

public class UtilsCommands extends CommandAdapter {

    public UtilsCommands(BWMain baseplugin) {
        super(baseplugin);

    }


    @SimpleComamnd(name = "g", permission = Groups.PLAYER)
    public void globalchat(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());

        for(UUID uuid : arena.getPlayers()){
            //ChatColor.BOLD+""+team.getChatColor()+team.name().charAt(0)+" §f"+WordUtils.capitalize(team.name().toLowerCase());

            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < strings.length; i++){
                builder.append(strings[i]+" ");
            }
            Bukkit.getPlayer(uuid).sendMessage(ChatColor.GOLD+"[GLOBAL] "+player.getDisplayName()+ChatColor.YELLOW+": "+ChatColor.GRAY+builder.toString());
        }
    }

    @SimpleComamnd(name = "movearena", permission = Groups.YOUTUBERPLUS)
    public void movearena(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        if(strings.length == 0) {

            BWManager.getInstance().getArenas().forEach(arena->player.sendMessage(arena.getName()));
        } else {
            Arena arena = BWManager.getInstance().getArena(strings[0]);
            if(arena == null)
                player.sendMessage("Inexistent Arena.");
            else{
                Location location = arena.getLobby();
                location.setWorld(Bukkit.getWorld(strings[0]));
                boolean result = player.teleport(arena.getLobby());
                System.out.println("teleport to"+arena.getLobby().getWorld().getName()+" resulted in "+result);
            } 

        } 


    }
}