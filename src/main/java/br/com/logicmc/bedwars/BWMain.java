package br.com.logicmc.bedwars;

import br.com.logicmc.bedwars.extra.BWMessages;
import br.com.logicmc.bedwars.extra.FixedItems;
import br.com.logicmc.bedwars.extra.Schematic;
import br.com.logicmc.bedwars.extra.StaffArena;
import br.com.logicmc.bedwars.extra.YamlFile;
import br.com.logicmc.bedwars.extra.customentity.EntityManager;
import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.engine.generator.IslandGenerator;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import br.com.logicmc.bedwars.game.shop.ShopCategory;
import br.com.logicmc.bedwars.game.shop.ShopItem;
import br.com.logicmc.bedwars.game.shop.upgrades.UpgradeItem;
import br.com.logicmc.bedwars.listeners.InventoryListeners;
import br.com.logicmc.bedwars.listeners.ShopInventoryListeners;
import br.com.logicmc.bedwars.listeners.PhaseListener;
import br.com.logicmc.bedwars.listeners.PlayerListeners;
import br.com.logicmc.core.account.addons.DataStats;
import br.com.logicmc.core.addons.hologram.types.Global;
import br.com.logicmc.core.system.command.CommandLoader;
import br.com.logicmc.core.system.minigame.ArenaInfoPacket;
import br.com.logicmc.core.system.minigame.MinigamePlugin;
import br.com.logicmc.core.system.mysql.MySQL;
import br.com.logicmc.core.system.redis.packet.PacketManager;
import br.com.logicmc.core.system.server.ServerState;
import br.com.logicmc.core.system.server.ServerType;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BWMain extends MinigamePlugin<BWPlayer> {

    private static BWMain instance;
    public YamlFile mainconfig;
    private boolean maintenance;
    private Location spawnlocation;

    public ShopCategory blocks, swords, shoparmor, bows, utilities, tools, potions,quickshop;
    public UpgradeItem sharpness, armor, forgery;

    @Override
    public void onEnable() {
        instance = this;

        if (!loadConfig()) {
            System.out.println("Error while loading config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        spawnlocation = mainconfig.getLocation("spawn");
        maintenance = mainconfig.getConfig().getBoolean("maintenance");


        if (spawnlocation == null)
            System.out.println("[Arena] Lobby location is null");

        CommandLoader.loadPackage(this, BWMain.class, "br.com.logicmc.bedwars.commands");
        super.onEnable();

        loadTranslations();
        loadItens();

        getServer().getPluginManager().registerEvents(new ShopInventoryListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new InventoryListeners(), this);
        getServer().getPluginManager().registerEvents(new PhaseListener(), this);


        BWManager.getInstance().addGame("staff", new StaffArena());
        BWManager.getInstance().getArenas().forEach(Arena::firstStartup); // due to translation error

    }


    @Override
    public BWPlayer read(MySQL mysql, UUID uuid, String name) {
        BWPlayer data = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = mysql.getConnection().prepareStatement("SELECT * FROM bedwars_players WHERE uuid='" + uuid + "';");
            rs = stm.executeQuery();
            if (rs.next()) {
                data = new BWPlayer(uuid, name, rs.getInt("level"),
                        getGson().fromJson(rs.getString("solo_stats"), DataStats.class),
                        getGson().fromJson(rs.getString("squad_stats"), DataStats.class),0);
            } else {
                data = new BWPlayer(uuid, name, 0, new DataStats(), new DataStats(),0);
            }
            stm.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void write(MySQL mysql, UUID uuid) {
        System.out.println("updating user mysql " + uuid);
        PreparedStatement stm = null;
        ResultSet rs = null;
        BWPlayer data = playermanager.getPlayerBase(uuid).getData();
        try {
            stm = mysql.getConnection().prepareStatement("SELECT * FROM bedwars_players WHERE uuid='" + uuid + "';");
            rs = stm.executeQuery();
            if (rs.next()) {
                mysql.update("UPDATE bedwars_players SET name='" + data.getName() + "', level=" + data.getLevel()
                        + ", solo_stats='" + getGson().toJson(data.getSoloStats()) + "', squad_stats='"
                        + getGson().toJson(data.getSquadStats()) + "' WHERE uuid='" + uuid + "';");
            } else {
                mysql.update("INSERT INTO bedwars_players (uuid, name, level, solo_stats, squad_stats) VALUES ('"
                        + data.getUuid() + "','" + data.getName() + "'," + data.getLevel() + ",'"
                        + getGson().toJson(data.getSoloStats()) + "','" + getGson().toJson(data.getSquadStats())
                        + "');");
            }
            rs.close();
            stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static BWMain getInstance() {
        return instance;
    }


    @Override
    public ServerType getServerType() {
        return ServerType.GAME;
    }

    @Override
    public boolean isAvailable(String arenaname, int size) {
        return BWManager.getInstance().getArena(arenaname).hasSpaceforPlayer();
    }

    @Override
    public void allocateSpace(String arenaname, int size) {
        BWManager.getInstance().getArena(arenaname).incrementAllotedPlayers();
    }

    @Override
    public Consumer<String> getUpdateArenaMethod() {
        return (arena) -> {
            Arena gameEngine = BWManager.getInstance().getArena(arena);
            System.out.println("updating arena " + arena);
            PacketManager.getInstance().sendChannelPacket(this, "server-listener", new ArenaInfoPacket(Bukkit.getServerName(),
                    arena, true, gameEngine.getPlayers().size(), gameEngine.getServerState()));
        };
    }

    @Override
    public ServerState getArenaState(String arenaname) {
        return BWManager.getInstance().getArena(arenaname).getServerState();
    }

    @Override
    public List<String> getArenas() {
        ArrayList<String> arrayList = new ArrayList<>();
        BWManager.getInstance().getArenas().forEach(arena -> arrayList.add(arena.getName()));
        return arrayList;
    }

    private void deleteFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory())
                deleteFolder(file);
            else
                file.delete();
        }
        folder.delete();
    }

    @Deprecated
    @Override
    public List<String> loadArenas() {
        List<String> schematics = mainconfig.getConfig().getStringList("schematics");

        if (schematics == null || schematics.isEmpty())
            return new ArrayList<>();

        Schematic lobby = Schematic.read(getResource("LobbyBW.schematic"));

        for (String arena : schematics) {

            arena = arena.replace(".schematic", "");

            WorldCreator wc = new WorldCreator(arena);
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            wc.createWorld();
            World world = Bukkit.createWorld(wc);
            System.out.println("[Arena] Pasting lobby for " + arena);
            lobby.paste(new Location(world, 0, 100, 0));

            Schematic schematic = Schematic.read(new File(getDataFolder(), arena + ".schematic"));

            if (schematic == null)
                schematics.remove(arena + ".schematic");
            else {
                System.out.println("[Arena] Pasting map for " + arena);
                schematic.paste(new Location(world, 250, 100, 250));

                HashSet<Island> islands = new HashSet<>();
                HashSet<NormalGenerator> diamond = new HashSet<>(), emerald = new HashSet<>();
                AtomicReference<Location> lobbyloc = new AtomicReference<>();
                spawnlocation.setWorld(world);
                lobbyloc.set(spawnlocation);
                String finalArena = arena;

                mainconfig.loopThroughSectionKeys(arena, (visland) -> {

                    if (visland.equalsIgnoreCase("islands"))
                        mainconfig.loopThroughSectionKeys(finalArena + ".islands", (island) -> {
                            islands.add(new Island(island, finalArena,
                                    BWTeam.valueOf(mainconfig.getConfig()
                                            .getString(finalArena + ".islands." + island + ".color")),
                                    mainconfig.getLocation(finalArena + ".islands." + island + ".spawn"),
                                    mainconfig.getLocation(finalArena + ".islands." + island + ".npc"),
                                    mainconfig.getLocation(finalArena + ".islands." + island + ".upgrade"),
                                    mainconfig.getLocation(finalArena + ".islands." + island + ".bed"),
                                    mainconfig.getLocation(finalArena + ".islands." + island + ".generator")));
                        });
                    else if (visland.equalsIgnoreCase("diamond"))
                        mainconfig
                                .loopThroughSectionKeys(finalArena + ".diamond",
                                        (string) -> diamond.add(new NormalGenerator(
                                                mainconfig.getLocation(finalArena + ".diamond." + string),
                                                Material.DIAMOND, null, 80)));
                    else if (visland.equalsIgnoreCase("emerald"))
                        mainconfig
                                .loopThroughSectionKeys(finalArena + ".emerald",
                                        (string) -> emerald.add(new NormalGenerator(
                                                mainconfig.getLocation(finalArena + ".emerald." + string),
                                                Material.EMERALD, null, 90)));

                });

                world.setStorm(false);
                world.setPVP(true);
                world.setAutoSave(false);
                world.setThundering(false);
                world.setDifficulty(Difficulty.EASY);
                world.setThunderDuration(0);
                world.setWeatherDuration(0);
                world.setGameRuleValue("mobGriefing", "true");
                world.setGameRuleValue("tntExplodes", "true");
                world.getEntities().forEach(Entity::remove);
                world.getLivingEntities().forEach(LivingEntity::remove);
                world.setGameRuleValue("doDaylightCycle", "false");
                Location spawnlobby = lobbyloc.get().clone().add(0.0D, 1.2D, 0.0D);
                spawnlobby.setWorld(world);
                for (Island island : islands) { // debug arenas
                    island.report(arena);
                    if(island.getGenerator().getLocation() !=null)
                        island.getGenerator().getLocation().setWorld(world);
                    if(island.getNpc() !=null)
                        island.getNpc().setWorld(world);
                    if(island.getSpawn()!=null) {
                        island.getSpawn().setWorld(world);
                        island.getSpawn().add(0.0D, 1.0D, 0.0D);
                    }
                    if(island.getBed()!=null)
                        island.getBed().setWorld(world);
                    if(island.getUpgrade()!=null)
                        island.getUpgrade().setWorld(world);

                }
                for (NormalGenerator generator : diamond) {
                    generator.getLocation().setWorld(world);
                    Global global = new Global(generator.getLocation().clone().subtract(0.0D,0.8D,0.0D));
                    global.setLines(ChatColor.YELLOW + "Tier "+ChatColor.RED+"I",  ChatColor.BOLD+""+ChatColor.AQUA+"Diamond", "10:10");
                    generator.setHologram(global);
                    generator.getHologram().build();
                }
                for (NormalGenerator generator : emerald) {
                    generator.getLocation().setWorld(world);
                    Global global = new Global(generator.getLocation().clone().subtract(0.0D,0.7D,0.0D));
                    global.setLines(ChatColor.YELLOW + "Tier "+ChatColor.RED+"I",  ChatColor.BOLD+""+ChatColor.GREEN+"Emerald", "10:10");
                    generator.setHologram(global);
                    generator.getHologram().build();
                }
                Arena garena = new Arena(arena, 8, Arena.SOLO, spawnlobby, islands, diamond, emerald);
                BWManager.getInstance().addGame(arena,
                        garena);
                garena.startTimer(this);
                
            }
        }
		ArrayList<String> arrayList = new ArrayList<>();
        BWManager.getInstance().getArenas().forEach(arena->arrayList.add(arena.getName()));
        return arrayList;
    }

    private boolean loadConfig() {
        
        if(!getDataFolder().exists())
            getDataFolder().mkdirs();

        mainconfig = new YamlFile("config.yml");
        return mainconfig.loadResource(this);
    }


    public void giveItem(Player player, int slot, FixedItems item) {
        player.getInventory().setItem(slot, item.getBuild(messagehandler, playermanager.getPlayerBase(player).getPreferences().getLang()));
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    @Override
    public BWPlayer createDataInstance() {
        return new BWPlayer(null, "s", 0, new DataStats(), new DataStats(), Arena.SOLO);
    }

    private void loadItens(){

        blocks = new ShopCategory(FixedItems.SHOP_BLOCKS);
        swords = new ShopCategory(FixedItems.SHOP_FIGHT);
        utilities = new ShopCategory(FixedItems.SHOP_UTILITIES);
        tools = new ShopCategory(FixedItems.SHOP_TOOLS);
        bows = new ShopCategory(FixedItems.SHOP_BOW);
        potions = new ShopCategory(FixedItems.SHOP_POTIONS);
        shoparmor = new ShopCategory(FixedItems.SHOP_ARMOR);
        quickshop = new ShopCategory(FixedItems.SHOP_QUICKSHOP);

        quickshop.addItem(new ShopItem(new ItemStack(Material.WOOL, 16), new ItemStack(Material.IRON_INGOT, 4)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.STONE_SWORD, 1), new ItemStack(Material.IRON_INGOT, 10)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.GOLD_INGOT, 7)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), new ItemStack(Material.IRON_INGOT, 40)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.STONE_AXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.IRON_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.SHEARS, 1), new ItemStack(Material.IRON_INGOT, 20)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.GOLDEN_APPLE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.SNOW_BALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        quickshop.addItem(new ShopItem(new ItemStack(Material.FIREBALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        
        blocks.addItem(new ShopItem(new ItemStack(Material.WOOL, 16), new ItemStack(Material.IRON_INGOT, 4)));
        blocks.addItem(new ShopItem(new ItemStack(Material.HARD_CLAY, 16), new ItemStack(Material.IRON_INGOT, 12)));
        blocks.addItem(new ShopItem(new ItemStack(Material.WOOD, 16), new ItemStack(Material.GOLD_INGOT, 4)));
        blocks.addItem(new ShopItem(new ItemStack(Material.GLASS, 4), new ItemStack(Material.IRON_INGOT, 12)));
        blocks.addItem(new ShopItem(new ItemStack(Material.ENDER_STONE, 12), new ItemStack(Material.IRON_INGOT, 24)));
        blocks.addItem(new ShopItem(new ItemStack(Material.LADDER, 16), new ItemStack(Material.IRON_INGOT, 4)));
        blocks.addItem(new ShopItem(new ItemStack(Material.OBSIDIAN, 4), new ItemStack(Material.EMERALD, 4)));

        swords.addItem(new ShopItem(new ItemStack(Material.STONE_SWORD, 1), new ItemStack(Material.IRON_INGOT, 10)));
        swords.addItem(new ShopItem(new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.GOLD_INGOT, 7)));
        swords.addItem(new ShopItem(new ItemStack(Material.DIAMOND_SWORD, 1), new ItemStack(Material.EMERALD, 4)));
        swords.addItem(new ShopItem(addEnchantment(Material.STICK, Enchantment.KNOCKBACK, 1), new ItemStack(Material.EMERALD, 4)));

        shoparmor.addItem(new ShopItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), new ItemStack(Material.IRON_INGOT, 40)));
        shoparmor.addItem(new ShopItem(new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.GOLD_INGOT, 12)));
        shoparmor.addItem(new ShopItem(new ItemStack(Material.DIAMOND_CHESTPLATE, 1), new ItemStack(Material.EMERALD, 7)));

        potions.addItem(new ShopItem(addPotion(PotionEffectType.SPEED, 45, 2), new ItemStack(Material.EMERALD, 1)));
        potions.addItem(new ShopItem(addPotion(PotionEffectType.JUMP, 45, 5), new ItemStack(Material.EMERALD, 1)));

        tools.addItem(new ShopItem(new ItemStack(Material.WOOD_AXE, 1), new ItemStack(Material.IRON_INGOT, 10)));
        tools.addItem(new ShopItem(new ItemStack(Material.STONE_AXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        tools.addItem(new ShopItem(new ItemStack(Material.IRON_AXE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        tools.addItem(new ShopItem(new ItemStack(Material.DIAMOND_AXE, 1), new ItemStack(Material.GOLD_INGOT, 6)));

        tools.addItem(new ShopItem(new ItemStack(Material.WOOD_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 10)));
        tools.addItem(new ShopItem(new ItemStack(Material.IRON_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        tools.addItem(new ShopItem(new ItemStack(Material.GOLD_PICKAXE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        tools.addItem(new ShopItem(new ItemStack(Material.DIAMOND_PICKAXE, 1), new ItemStack(Material.GOLD_INGOT, 6)));

        bows.addItem(new ShopItem(new ItemStack(Material.BOW, 1), new ItemStack(Material.GOLD_INGOT, 12)));
        bows.addItem(new ShopItem(addEnchantment(Material.BOW, Enchantment.ARROW_DAMAGE, 1), new ItemStack(Material.GOLD_INGOT, 24)));
        bows.addItem(new ShopItem(addEnchantment(Material.BOW, Enchantment.ARROW_DAMAGE, 2), new ItemStack(Material.EMERALD, 6)));

        utilities.addItem(new ShopItem(new ItemStack(Material.COMPASS, 1), new ItemStack(Material.IRON_INGOT, 50)));
        utilities.addItem(new ShopItem(new ItemStack(Material.SHEARS, 1), new ItemStack(Material.IRON_INGOT, 20)));
        utilities.addItem(new ShopItem(new ItemStack(Material.GOLDEN_APPLE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        utilities.addItem(new ShopItem(new ItemStack(Material.SNOW_BALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        utilities.addItem(new ShopItem(new ItemStack(Material.FIREBALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        utilities.addItem(new ShopItem(new ItemStack(Material.TNT, 1), new ItemStack(Material.GOLD_INGOT, 4)));
        utilities.addItem(new ShopItem(new ItemStack(Material.ENDER_PEARL, 1), new ItemStack(Material.EMERALD, 4)));
        utilities.addItem(new ShopItem(new ItemStack(Material.WATER_BUCKET, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        utilities.addItem(new ShopItem(new ItemStack(Material.MILK_BUCKET, 1), new ItemStack(Material.GOLD_INGOT, 4)));

        sharpness = new UpgradeItem(FixedItems.UPGRADE_SHARPNESS,  (island) -> {
            int teamcomp = BWManager.getInstance().getArena(island.getArena()).getTeamcomposition();
            if(island.getSharpness() == 0){
                if(teamcomp == Arena.SOLO || teamcomp == Arena.DUO)
                    return new ItemStack(Material.DIAMOND, 4);
                else
                    return new ItemStack(Material.DIAMOND, 8);
            }
            else
                return new ItemStack(Material.AIR);
        },(island)-> {
            island.setSharpness(1);
            island.forEachPlayers(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                BWMain.getInstance().messagehandler.sendMessage(player, BWMessages.SHARPNESS_UPGRADED);
                for(int i = 0; i <player.getInventory().getSize(); i++){
                    ItemStack stack = player.getInventory().getItem(i);
                    if(stack != null && stack.getType().name().contains("SWORD"))
                        stack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
                }
            });
        });

        forgery = new UpgradeItem(FixedItems.UPGRADE_FORGERY, (island) -> {
            int teamcomp = BWManager.getInstance().getArena(island.getArena()).getTeamcomposition();
            if(teamcomp == Arena.SOLO || teamcomp == Arena.DUO){
                if(island.getForgery() == 0)
                    return new ItemStack(Material.DIAMOND, 2);
                else if(island.getForgery() == 1)
                    return new ItemStack(Material.DIAMOND, 4);
                else if(island.getForgery() == 2)
                    return new ItemStack(Material.DIAMOND, 6);
                else if(island.getForgery() == 3)
                    return new ItemStack(Material.DIAMOND, 8);
                else
                    return new ItemStack(Material.AIR);
            } else {
                if(island.getForgery() == 0)
                    return new ItemStack(Material.DIAMOND, 4);
                else if(island.getForgery() == 1)
                    return new ItemStack(Material.DIAMOND, 8);
                else if(island.getForgery() == 2)
                    return new ItemStack(Material.DIAMOND, 12);
                else if(island.getForgery() == 3)
                    return new ItemStack(Material.DIAMOND, 16);
                else
                    return new ItemStack(Material.AIR);
            }
        }, (island)-> {
            island.setForgery(island.getForgery()+1);
            IslandGenerator generator = island.getGenerator();
            generator.increaseGeneratorLevel();
            if(island.getForgery() == 1){
                generator.setIronstack(generator.setStack(Material.IRON_INGOT, 64));
                generator.setGoldstack(generator.setStack(Material.GOLD_INGOT, 24));
            } else if(island.getForgery() == 2){
                generator.setGoldstack(generator.setStack(Material.GOLD_INGOT, 36));
            } else if(island.getForgery() == 3){
                generator.setGoldstack(generator.setStack(Material.GOLD_INGOT, 48));
            }
            island.forEachPlayers(uuid -> BWMain.getInstance().messagehandler.sendMessage(Bukkit.getPlayer(uuid), BWMessages.FORGERY_UPGRADED));
        });

        armor = new UpgradeItem(FixedItems.UPGRADE_ARMOR, (island) -> {
            int teamcomp = BWManager.getInstance().getArena(island.getArena()).getTeamcomposition();
            if(teamcomp == Arena.SOLO || teamcomp == Arena.DUO) {
                if(island.getArmor() == 0)
                    return new ItemStack(Material.DIAMOND, 2);
                else if(island.getArmor() == 1)
                    return new ItemStack(Material.DIAMOND, 4);
                else if(island.getArmor() == 2)
                    return new ItemStack(Material.DIAMOND, 8);
                else if(island.getArmor() == 3)
                    return new ItemStack(Material.DIAMOND, 16);
                else
                    return new ItemStack(Material.AIR);
            } else {
                if(island.getArmor() == 0)
                    return new ItemStack(Material.DIAMOND, 5);
                else if(island.getArmor() == 1)
                    return new ItemStack(Material.DIAMOND, 10);
                else if(island.getArmor() == 2)
                    return new ItemStack(Material.DIAMOND, 20);
                else if(island.getArmor() == 3)
                    return new ItemStack(Material.DIAMOND, 30);
                else
                    return new ItemStack(Material.AIR);
            }
        }, (island)-> {
            island.setArmor(island.getArmor()+1);
            island.forEachPlayers(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                BWMain.getInstance().messagehandler.sendMessage(player, BWMessages.ARMOR_UPGRADED);
                player.getInventory().getHelmet().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor());
                player.getInventory().getChestplate().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor());
                player.getInventory().getLeggings().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor());
                player.getInventory().getBoots().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, island.getArmor());
            });
        });
    }
    private void loadTranslations() {
		new YamlFile("pt-msg.yml").loadResource(this);
        new YamlFile("en-msg.yml").loadResource(this);
        new YamlFile("es-msg.yml").loadResource(this);
        messagehandler.loadMessage(BWMessages.PLAYER_LEAVE_INGAME, this);
    }
    private ItemStack addPotion(PotionEffectType type , int duration, int power){
        ItemStack itemStack = new ItemStack(Material.POTION, 1);
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        potionMeta.addCustomEffect(new PotionEffect(type, duration, power), false);
        itemStack.setItemMeta(potionMeta);
        return itemStack;
    }

    private ItemStack addEnchantment(Material material, Enchantment enchantment, int level){
        ItemStack itemStack = new ItemStack(material);
        itemStack.addUnsafeEnchantment(enchantment,level);
        return itemStack;
    }

    public ItemStack createColorouedArmor(Material material, Color color){
        ItemStack stack = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(color);
        stack.setItemMeta(meta);
        return stack;
    }
}
