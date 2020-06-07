package br.com.logicmc.bedwars.commands;


import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.generator.IslandGenerator;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.account.PlayerBase;
import br.com.logicmc.core.account.addons.Groups;
import br.com.logicmc.core.system.command.CommandAdapter;
import br.com.logicmc.core.system.command.SimpleComamnd;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SetLocations extends CommandAdapter {


    public SetLocations(BWMain baseplugin) {
        super(baseplugin);
    }

    
    @SimpleComamnd(name = "tempo", permission = Groups.YOUTUBERPLUS)
    public void settempo(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){

        if(strings.length == 0) {
            player.sendMessage("Use /tempo <tempo");
        } else {
            Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());

            if( arena == null) {
                player.sendMessage("Por favor entre no mundo de uma arena para usar este comando.");
                return;
            }
            if(arena.getGamestate() == Arena.WAITING) {
                try{
                    int i = Integer.parseInt(strings[0]);
                    if(i == 0)
                        i = 3;
                    else if( i > 800)
                        i = 500;
                    
                    arena.setTime(i);
                    player.sendMessage("tempo setado com sucesso");
                } catch (Exception e) {
                    player.sendMessage("Erro ao carregar comando");
                }
                
            }
        }

    }



    @SimpleComamnd(name = "setlobby", permission = Groups.ADMIN)
    public void setspawn(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        plugin.mainconfig.setLocation("spawn", player.getLocation());
        player.sendMessage("Lobby setado com sucesso");
    }


    @SimpleComamnd(name = "setarena", permission = Groups.ADMIN)
    public void arenaconfig(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        if(strings.length == 0)
            player.sendMessage("use /setarena <diamond/emerald>");
        else {
            Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());

            if( arena == null) {
                player.sendMessage("Por favor entre no mundo de uma arena para usar este comando.");
                return;
            }

            if(strings[0].toLowerCase().equalsIgnoreCase("diamond")) {
                arena.getDiamond().add(new NormalGenerator(player.getLocation(), Material.AIR, null, 222));
                arena.save();
                player.sendMessage("Generator diamond adicionado com sucesso");
            } else if(strings[0].toLowerCase().equalsIgnoreCase("emerald")) {
                arena.getEmerald().add(new NormalGenerator(player.getLocation(), Material.AIR, null, 222));
                arena.save();
                player.sendMessage("Generator emerald adicionado com sucesso");
            }
        }
    }
    @SimpleComamnd(name = "setisland", permission = Groups.ADMIN)
    public void islandset(BWMain plugin, Player player, PlayerBase<?> playerBase, String[] strings){
        if(strings.length == 0) {
            player.sendMessage("use /setisland list");
            player.sendMessage("use /setisland create <name> <color>  (na localizacao do spawn)");
            player.sendMessage("use /setisland <name> <npc/upgrade/generator/bed>");
        } else {
            Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());

            if( arena == null) {
                player.sendMessage("Por favor entre no mundo de uma arena para usar este comando.");
                return;
            }

            if(strings[0].toLowerCase().equalsIgnoreCase("list")) {
                for(Island island : arena.getIslands()) {
                    player.sendMessage(island.getName() + " X:"+island.getBed().getBlockX()+" Y:"+island.getBed().getBlockY()+" z:"+island.getBed().getBlockZ());
                }
            } else if(strings.length >= 2){
                if(strings[1].toLowerCase().equalsIgnoreCase("npc")) {
                    for(Island island : arena.getIslands()) {
                        if(island.getName().equalsIgnoreCase(strings[0].toLowerCase())){
                            island.setNpc(player.getLocation());
                            island.save(arena.getName(), BWMain.getInstance().mainconfig);
                            player.sendMessage("Ilha salva com sucesso;");
                        }
                    }
                } else if(strings[1].toLowerCase().equalsIgnoreCase("generator")) {
                    for(Island island : arena.getIslands()) {
                        if(island.getName().equalsIgnoreCase(strings[0].toLowerCase())){
                            island.setGenerator(new IslandGenerator(player.getLocation()));
                            island.save(arena.getName(), BWMain.getInstance().mainconfig);
                            player.sendMessage("Ilha salva com sucesso;");
                        }
                    }
                } else if(strings[1].toLowerCase().equalsIgnoreCase("upgrade")) {
                    for(Island island : arena.getIslands()) {
                        if(island.getName().equalsIgnoreCase(strings[0].toLowerCase())){
                            island.setUpgrade(player.getLocation());
                            island.save(arena.getName(), BWMain.getInstance().mainconfig);
                            player.sendMessage("Ilha salva com sucesso;");
                        }
                    }
                } else if(strings[1].toLowerCase().equalsIgnoreCase("bed")) {
                    for(Island island : arena.getIslands()) {
                        if(island.getName().equalsIgnoreCase(strings[0].toLowerCase())){
                            island.setBed(player.getLocation());
                            island.save(arena.getName(), BWMain.getInstance().mainconfig);
                            player.sendMessage("Ilha salva com sucesso;");
                        }
                    }
                } else if(strings[0].toLowerCase().equalsIgnoreCase("create")) {
                    boolean exist = false;
                    for(Island island : arena.getIslands()) {
                        if(island.getName().equalsIgnoreCase(strings[1].toLowerCase())){
                            player.sendMessage("Ilha ja e existente;");
                            exist = true;
                        }
                    }
                    if(!exist){
                        Island island = new Island(strings[1].toLowerCase(), player.getLocation().getWorld().getName(), BWTeam.valueOf(strings[2].toUpperCase()), player.getLocation(), null, null,  null,null);
                        island.save(arena.getName(), BWMain.getInstance().mainconfig);
                        arena.getIslands().add(island);
                        player.sendMessage("Ilha criada com sucesso");
                    }
                }
            }
        }
    }
}
