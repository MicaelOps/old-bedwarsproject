package br.com.logicmc.bedwars.listeners;

import br.com.logicmc.bedwars.BWMain;
import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.core.account.PlayerBase;

import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class PhaseListener implements Listener {
    
    private final BWMain plugin;
    
    public PhaseListener(){
        plugin = BWMain.getInstance();
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockdamage(final EntityExplodeEvent event) {
        if(event.getEntityType() == EntityType.PRIMED_TNT){
            event.blockList().removeIf(block->!BWManager.getInstance().getArena(block.getLocation().getWorld().getName()).getBlocks().contains(block.getLocation()));
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbuild(final BlockBreakEvent event){

        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
        if(!event.isCancelled()) {
            if (event.getBlock().getType() == Material.BED_BLOCK) {
                final Player player = event.getPlayer();
                final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                for (final Island island : arena.getIslands()) {
                    if (!island.isBedbroken() && island.getBed().distance(event.getBlock().getLocation()) < 5.0D) {
                        final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());
                        if (bwPlayer.getData().getTeamcolor().equalsIgnoreCase(island.getTeam().name()))
                            event.setCancelled(true);
                        else {

                            final BWTeam bwTeam = island.getTeam();
                            island.setBedbroken(true);
                            bwPlayer.getData().increaseBeds();
                            arena.updateScoreboardTeam(player, "beds", ChatColor.GREEN + "" + bwPlayer.getData().getBeds());
                            for (final UUID uuid : arena.getPlayers()) {
                                final Player target = Bukkit.getPlayer(uuid);
                                target.playSound(target.getLocation(), Sound.ENDERDRAGON_GROWL, 10F, 10F);
                                String lang = plugin.playermanager.getPlayerBase(uuid).getPreferences().getLang();
                                if(BWManager.getInstance().getBWPlayer(uuid).getTeamcolor().equalsIgnoreCase(bwTeam.name())){
                                    target.sendTitle(plugin.messagehandler.getMessage(BWMessages.HEADTITLE_BED_DESTROYED, lang), plugin.messagehandler.getMessage(BWMessages.LOWERTITLE_BED_DESTROYED, lang).replace("{player}", player.getDisplayName()));
                                } else {
                                    target.sendMessage(plugin.messagehandler.getMessage(BWMessages.BED_DESTROYED, lang).replace("{bed}",bwTeam.getChatColor()+WordUtils.capitalize(bwTeam.name())).replace("{player}", player.getDisplayName()));

                                }
                                arena.updateTeamArena(bwTeam);
                            }
                        }
                        break;
                    }
                }
            } else {
                final HashSet<Location> blocks = BWManager.getInstance().getArena(event.getBlock().getLocation().getWorld().getName()).getBlocks();
                if (blocks.contains(event.getBlock().getLocation())) {
                    blocks.remove(event.getBlock().getLocation());
                } else
                    event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final BlockPlaceEvent event) {
        event.setCancelled(check(event.getBlock().getLocation()));
        if(!event.isCancelled()) {
            Location location = event.getBlock().getLocation();
            Arena arena = BWManager.getInstance().getArena(location.getWorld().getName());
            for(Island island : arena.getIslands()) {
                if(island.getGenerator().getLocation().distance(location) < 3){
                    event.setCancelled(true);
                }
            }
            if(!event.isCancelled())
                BWManager.getInstance().getArena(event.getBlock().getLocation().getWorld().getName()).getBlocks().add(event.getBlock().getLocation());
        }
    }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final PlayerPickupItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
    }
    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final PlayerDropItemEvent event) {
        event.setCancelled(check(event.getPlayer().getLocation(), event.getPlayer()));
        if(!event.isCancelled())
            event.setCancelled(event.getItemDrop().getItemStack().getType().name().contains("_SWORD"));
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void blockbreal(final EntitySpawnEvent event) {
        if(event.getEntityType() == EntityType.DROPPED_ITEM) {
            if (((Item) event.getEntity()).getItemStack().getType() == Material.BED) {
                event.setCancelled(true);
            }
        } else
            event.setCancelled(event.getEntityType() != EntityType.SMALL_FIREBALL && event.getEntityType() != EntityType.IRON_GOLEM &&event.getEntityType() != EntityType.FIREBALL && event.getEntityType() != EntityType.PLAYER && event.getEntityType() != EntityType.DROPPED_ITEM && event.getEntityType() != EntityType.ARMOR_STAND&& event.getEntityType() != EntityType.ENDER_DRAGON&& event.getEntityType() != EntityType.VILLAGER);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void foodlevelchange(final FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void foodlevelchange(final PlayerMoveEvent event) {
        if(event.getPlayer().getGameMode() == GameMode.SURVIVAL){
            Player player = event.getPlayer();
            if(event.getTo().getBlockY() < 10){
                final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());
                bwPlayer.getData().increaseDeaths();
                for (final Island island : arena.getIslands()) {
                    if (island.getTeam().name().equalsIgnoreCase(bwPlayer.getData().getTeamcolor())) {

                        for (final UUID uuid : arena.getPlayers()) {
                            Player other = Bukkit.getPlayer(uuid);
                            if(other.getGameMode() == GameMode.SURVIVAL){
                                other.hidePlayer(player);
                            }
                        }

                        respawn(arena, island, bwPlayer, player);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void entitydamage(EntityDamageEvent event) {

        if(event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getEntity() instanceof Player){
            event.setCancelled(check(event.getEntity().getLocation(), event.getEntity()));
            final Player player = (Player) event.getEntity();

            if(!event.isCancelled()) {

                if(event.getFinalDamage() >= ((Player) event.getEntity()).getHealth()) {
                    event.setCancelled(true);
                    if(player.getGameMode()==GameMode.SURVIVAL){
                        final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                        final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());
                        bwPlayer.getData().increaseDeaths();
                        for (final Island island : arena.getIslands()) {
                            if (island.getTeam().name().equalsIgnoreCase(bwPlayer.getData().getTeamcolor())) {

                                for (final UUID uuid : arena.getPlayers()) {
                                    Player other = Bukkit.getPlayer(uuid);
                                    if(other.getGameMode() == GameMode.SURVIVAL){
                                        other.hidePlayer(player);
                                    }
                                }

                                respawn(arena, island, bwPlayer, player);
                                break;
                            }
                        }
                    }
                }
            }
            if(!event.isCancelled()){
                if(player.getGameMode() == GameMode.SURVIVAL){
                    player.getInventory().getHelmet().setDurability((short)0);
                    player.getInventory().getChestplate().setDurability((short)0);
                    player.getInventory().getLeggings().setDurability((short)0);
                    player.getInventory().getBoots().setDurability((short)0);
                }
            }
        }
    }

    @EventHandler
    public void pickupevent(PlayerPickupItemEvent event){
        Material type = event.getItem().getItemStack().getType();

        if(event.getItem().getItemStack().getMaxStackSize() != 64) {
            if(type == Material.IRON_INGOT || type == Material.EMERALD || type == Material.GOLD_INGOT || type == Material.DIAMOND){
                net.minecraft.server.v1_8_R3.ItemStack nmsdummy = CraftItemStack.asNMSCopy(event.getItem().getItemStack());
                nmsdummy.getItem().c(64);
                event.getItem().setItemStack(CraftItemStack.asBukkitCopy(nmsdummy));
            }
        }
    }
    @EventHandler
    public void donotsleep(PlayerBedEnterEvent event){
        event.setCancelled(true);
    }
    @EventHandler
    public void damagedddby(EntityDamageByEntityEvent event) {
        boolean damage = check(event.getEntity().getLocation(), event.getEntity());
        if(!damage){
            if(event.getEntity() instanceof Player){

                Player player = (Player) event.getEntity();

                if(player.getGameMode() == GameMode.SURVIVAL){

                    boolean death = event.getFinalDamage() >= ((Player) event.getEntity()).getHealth();
                    final Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                    final PlayerBase<BWPlayer> bwPlayer = plugin.playermanager.getPlayerBase(player.getUniqueId());

                    if(event.getDamager() instanceof  Player){
                        damage = plugin.playermanager.getPlayerBase(event.getEntity().getUniqueId()).getData().getTeamcolor().equalsIgnoreCase(plugin.playermanager.getPlayerBase(event.getDamager().getUniqueId()).getData().getTeamcolor());

                        if(!damage && death) {
                            Player playerkiller = (Player) event.getDamager();
                            playerkiller.playSound(playerkiller.getLocation(), Sound.LEVEL_UP, 10F, 10F);
                            final BWPlayer killer = BWManager.getInstance().getBWPlayer(playerkiller.getUniqueId());
                            killer.increaseKills();
                            arena.updateScoreboardTeam(playerkiller, "kills", ChatColor.GREEN+""+killer.getKills());

                            ItemStack iron = new ItemStack(Material.IRON_INGOT, 0);
                            ItemStack emerald = new ItemStack(Material.EMERALD, 0);
                            ItemStack gold = new ItemStack(Material.GOLD_INGOT, 0);
                            ItemStack diamond = new ItemStack(Material.DIAMOND, 0);


                            for(int i = 0; i < player.getInventory().getSize(); i++){
                                ItemStack stack = player.getInventory().getItem(i);
                                if(stack != null) {

                                    if(stack.getType() == Material.IRON_INGOT){
                                        iron.setAmount(iron.getAmount()+stack.getAmount());
                                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                                    } else if(stack.getType() == Material.EMERALD){
                                        emerald.setAmount(emerald.getAmount()+stack.getAmount());
                                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                                    } else if(stack.getType() == Material.GOLD_INGOT){
                                        gold.setAmount(gold.getAmount()+stack.getAmount());
                                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                                    } else if(stack.getType() == Material.DIAMOND){
                                        diamond.setAmount(diamond.getAmount()+stack.getAmount());
                                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                                    } else if(stack.getType().name().contains("_SWORD")){
                                        if(stack.containsEnchantment(Enchantment.DAMAGE_ALL)){
                                            ItemStack woodspord = new ItemStack(Material.WOOD_SWORD);
                                            woodspord.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                                            player.getInventory().setItem(i, woodspord);
                                        } else {
                                            player.getInventory().setItem(i, new ItemStack(Material.WOOD_SWORD));
                                        }
                                    }
                                }
                            }
                            if(gold.getAmount() != 0){
                                playerkiller.sendMessage("§6+"+gold.getAmount()+" Gold");
                                playerkiller.getInventory().addItem(gold);
                            }
                            if(emerald.getAmount() != 0){
                                playerkiller.sendMessage("§2+"+emerald.getAmount()+" Emerald");
                                playerkiller.getInventory().addItem(emerald);
                            }
                            if(iron.getAmount() != 0){
                                playerkiller.sendMessage("§f+"+iron.getAmount()+" Iron");
                                playerkiller.getInventory().addItem(iron);
                            }
                            if(diamond.getAmount() != 0){
                                playerkiller.sendMessage("§b+"+iron.getAmount()+" Diamond");
                                playerkiller.getInventory().addItem(diamond);
                            }
                        }
                    }

                    if(death) {
                        damage=true;
                        bwPlayer.getData().increaseDeaths();
                        for ( Island island : arena.getIslands()) {
                            if (island.getTeam().name().equalsIgnoreCase(bwPlayer.getData().getTeamcolor())) {

                                player.setGameMode(GameMode.SPECTATOR);
                                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 ,5));
                                player.setAllowFlight(true);
                                player.setFlying(true);

                                for (final UUID uuid : arena.getPlayers()) {
                                    Player other = Bukkit.getPlayer(uuid);
                                    other.sendMessage(BWMain.getInstance().messagehandler.getMessage(BWMessages.PLAYER_KILLED_BY_PLAYER, BWMain.getInstance().getLang(other)).replace("{damager}", ((Player) event.getDamager()).getDisplayName()+"§7").replace("{player}",player.getDisplayName()+"§7"));
                                    if(other.getGameMode() == GameMode.SURVIVAL){
                                        other.hidePlayer(player);
                                    }
                                }
                                respawn(arena, island, bwPlayer, player);
                                break;
                            }
                        }
                    }
                }
            }
        }

        event.setCancelled(damage);
    }
    private void respawn(Arena arena, Island island, PlayerBase<BWPlayer> bwPlayer, Player player) {

        player.getInventory().clear();

        if(island.getSharpness() ==0) {
            player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
        } else {
            ItemStack woodspord = new ItemStack(Material.WOOD_SWORD);
            woodspord.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
            player.getInventory().addItem(woodspord);
        }

		player.setGameMode(GameMode.SPECTATOR);
        if (island.isBedbroken()) {

            if(arena.checkend())
                arena.changePhase();
            else {
                arena.updateTeamArena(BWTeam.valueOf(bwPlayer.getData().getTeamcolor()));
                player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + plugin.messagehandler.getMessage(BWMessages.ELIMINATED, bwPlayer.getPreferences().getLang()), plugin.messagehandler.getMessage(BWMessages.ELIMINATED_MESSAGE, bwPlayer.getPreferences().getLang()));

                player.setDisplayName("[SPECTATOR] "+player.getName());

                player.getInventory().clear();
                plugin.giveItem(player, 8, FixedItems.SPECTATE_JOINLOBBY);
                plugin.giveItem(player, 7, FixedItems.SPECTATE_JOINNEXT);
                plugin.giveItem(player, 0, FixedItems.SPECTATE_PLAYERS);
            }
        } else {
            player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + plugin.messagehandler.getMessage(BWMessages.DEAD, bwPlayer.getPreferences().getLang()), plugin.messagehandler.getMessage(BWMessages.RESPAWN_MESSAGE, bwPlayer.getPreferences().getLang()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    Arena arena = BWManager.getInstance().getArena(player.getLocation().getWorld().getName());
                    for (final UUID uuid : arena.getPlayers()) {
                        Player other = Bukkit.getPlayer(uuid);
                        if(other.getGameMode() == GameMode.SURVIVAL){
                            other.showPlayer(player);
                        }
                    }
                    player.sendTitle("","");
                    player.teleport(arena.getIslands().stream().filter(island -> island.getTeam().name().equalsIgnoreCase(BWManager.getInstance().getBWPlayer(player.getUniqueId()).getTeamcolor())).findFirst().get().getSpawn());
                    player.setHealth(20.0D);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.getActivePotionEffects().forEach(potion->player.removePotionEffect(potion.getType()));
                }
            }.runTaskLater(plugin, 60L);
        }
    }
    private boolean check(final Location location) {
        return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate() != Arena.INGAME;
    }
    private boolean check(final Location location, final Entity player) {
        if (player instanceof Player)
            return BWManager.getInstance().getArena(location.getWorld().getName()).getGamestate()  != Arena.INGAME || ((Player) player).hasPotionEffect(PotionEffectType.INVISIBILITY) || ((Player) player).getGameMode() != GameMode.SURVIVAL;
        else
            return check(location);
    }
}
