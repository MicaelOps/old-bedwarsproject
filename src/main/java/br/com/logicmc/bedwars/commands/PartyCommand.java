package br.com.logicmc.bedwars.commands;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.account.addons.Groups;
import br.com.logicmc.core.system.command.CommandAdapter;
import br.com.logicmc.core.system.command.SimpleComamnd;
import br.com.logicmc.core.system.party.Party;
import br.com.logicmc.core.system.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/***
 * temporary
 */
public class PartyCommand extends CommandAdapter {
    public PartyCommand(BWMain baseplugin) {
        super(baseplugin);
    }

    @SimpleComamnd(name = "party", permission = Groups.PLAYER)
    public void setspawn(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        if(strings.length == 0){
            player.sendMessage("use /party remove <nick>");
            player.sendMessage("use /party add <nick>");
            player.sendMessage("use /party delete");
        } else {

            Party party = PartyManager.getInstance().getParty(playerBase.getPartyid());

            if(strings.length == 1){
                if(strings[0].toLowerCase().equalsIgnoreCase("delete")){
                    if(isOwner(playerBase.getPartyid(), player)){
                        for(UUID uuid : party.getMembers()) {
                            PlayerBase<BWPlayer> member = BWMain.getInstance().playermanager.getPlayerBase(uuid);
                            if(uuid != null&&member.getPartyid() == party.getId()){
                                member.setPartyid(-1);
                            }
                        }
                        PartyManager.getInstance().removeParty(party.getId());
                        player.sendMessage("Party deletada com sucesso");
                    }
                }
            } else {
                if(strings[0].toLowerCase().equalsIgnoreCase("add")) {
                    if (isOwner(playerBase.getPartyid(), player)) {
                        Player target = Bukkit.getPlayer(strings[1]);
                        if(target == null){
                            player.sendMessage("jogador offline");
                            return;
                        }
                        PlayerBase<BWPlayer> targetplayer = BWMain.getInstance().playermanager.getPlayerBase(target.getUniqueId());
                        targetplayer.setPartyid(playerBase.getPartyid());
                        target.sendMessage("voce foi adicionado na party do "+player.getName());
                        player.sendMessage("jogador adicionado com sucesso");
                        party.getMembers().add(target.getUniqueId());
                    }
                }
                if(strings[0].toLowerCase().equalsIgnoreCase("remove")) {
                    if (isOwner(playerBase.getPartyid(), player)) {
                        Player target = Bukkit.getPlayer(strings[1]);
                        if(target == null){
                            player.sendMessage("jogador offline");
                            return;
                        }
                        PlayerBase<BWPlayer> targetplayer = BWMain.getInstance().playermanager.getPlayerBase(target.getUniqueId());
                        targetplayer.setPartyid(-1);
                        target.sendMessage("voce foi removido na party do "+player.getName());
                        player.sendMessage("jogador removido com sucesso");
                        party.getMembers().remove(target.getUniqueId());
                    }
                }
            }
        }
    }

    private boolean isOwner(int partyid, Player player ){
        if(partyid == -1 ){
            player.sendMessage("Voce nao pertence a uma party");
            return false;
        }
        Party party = PartyManager.getInstance().getParty(partyid);
        if(party == null){
            player.sendMessage("Erro ao procurar party");
            return false;
        }
        if(party.getOwner() != player.getUniqueId()){
            player.sendMessage("Voce nao e o dono da party");
            return true;
        }
        return true;
    }
}
