package br.com.logicmc.bedwars.commands;


import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.account.addons.Groups;
import br.com.logicmc.core.system.command.CommandAdapter;
import br.com.logicmc.core.system.command.SimpleComamnd;
import org.bukkit.entity.Player;

public class SetLocations extends CommandAdapter {


    public SetLocations(BWMain baseplugin) {
        super(baseplugin);
    }

    @SimpleComamnd(name = "setlobby", permission = Groups.ADMIN)
    public void setspawn(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        plugin.mainconfig.setLocation("spawn", player.getLocation());
        player.sendMessage("Lobby setado com sucesso");
    }


    @SimpleComamnd(name = "setarena", permission = Groups.ADMIN)
    public void arenaconfig(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        if(strings.length == 0) {
            
        }
    }
}
