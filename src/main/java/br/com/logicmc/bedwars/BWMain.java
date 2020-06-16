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

    private ShopCategory blocks, fight, utilities, tools;
    private UpgradeItem sharpness, armor, forgery;

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
        EntityManager.getInstance().registerEntities();

        super.onEnable();

        getServer().getPluginManager().registerEvents(new ShopInventoryListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new InventoryListeners(), this);
        getServer().getPluginManager().registerEvents(new PhaseListener(), this);


        BWManager.getInstance().addGame("staff", new StaffArena());

        loadTranslations();
        loadItens();
    }

    public static BWMain getInstance() {
        return instance;
    }


    public ShopCategory getTools() {
        return tools;
    }

    public UpgradeItem getArmor() {
        return armor;
    }

    public UpgradeItem getSharpness() {
        return sharpness;
    }

    public UpgradeItem getForgery() {
        return forgery;
    }

    public ShopCategory getBlocks() {
        return blocks;
    }

    public ShopCategory getFight() {
        return fight;
    }

    public ShopCategory getUtilities() {
        return utilities;
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
            PacketManager.getInstance().sendChannelPacket(this, "lobby", new ArenaInfoPacket(Bukkit.getServerName(),
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
        boolean reload = false;

        // deleting unacessary worlds
        for (World world : Bukkit.getWorlds()) {
            boolean exist = false;
			
			System.out.println(world.getName());
			
            if (world.getName().equalsIgnoreCase("world"))
                continue;
            for (String arena : schematics) {
                if (world.getName().equalsIgnoreCase(arena.replace(".schematic", ""))) {
                    exist = true;
                    reload = true;
                }
            }
            if (!exist) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    chunk.unload();
                }
                File folder = world.getWorldFolder();
                Bukkit.unloadWorld(world, false);
                deleteFolder(folder);
            }
        }
        if (reload) {
            Bukkit.shutdown();
            return new ArrayList<>();
        }

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
                world.setThunderDuration(0);
                world.setWeatherDuration(0);
                world.setDifficulty(Difficulty.PEACEFUL);
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
                    generator.setHologram(new Global(generator.getLocation().add(0.0D, 0.3D, 0.0D)).setLine("10:00"));
                    generator.getHologram().build();
                }
                for (NormalGenerator generator : emerald) {
                    generator.getLocation().setWorld(world);
                    generator.setHologram(new Global(generator.getLocation().add(0.0D, 0.3D, 0.0D)).setLine("10:00"));
                    generator.getHologram().build();
                }
                BWManager.getInstance().addGame(arena,
                        new Arena(arena, 8, Arena.SOLO, spawnlobby, islands, diamond, emerald));
                BWManager.getInstance().getArena(arena).startTimer(this);
                
            }
        }
		ArrayList<String> arrayList = new ArrayList<>();
        BWManager.getInstance().getArenas().forEach(arena->arrayList.add(arena.getName()));
        return arrayList;
    }

    @Override
    public BWPlayer read(MySQL mysql, UUID uuid) {
        return null;
    }

    @Override
    public void write(MySQL mysql, UUID uuid) { }



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
        return new BWPlayer();
    }

    private void loadItens(){

        blocks = new ShopCategory(FixedItems.SHOP_BLOCKS);
        fight = new ShopCategory(FixedItems.SHOP_FIGHT);
        utilities = new ShopCategory(FixedItems.SHOP_UTILITIES);
        tools = new ShopCategory(FixedItems.SHOP_TOOLS);
        
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.WOOL, 16), new ItemStack(Material.IRON_INGOT, 4)));
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.HARD_CLAY, 16), new ItemStack(Material.IRON_INGOT, 12)));
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.WOOD, 16), new ItemStack(Material.GOLD_INGOT, 4)));
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.GLASS, 4), new ItemStack(Material.IRON_INGOT, 12)));
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.ENDER_STONE, 12), new ItemStack(Material.IRON_INGOT, 24)));
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.LADDER, 16), new ItemStack(Material.IRON_INGOT, 4)));
        blocks.getListitems().add(new ShopItem(new ItemStack(Material.OBSIDIAN, 4), new ItemStack(Material.EMERALD, 4)));

        fight.getListitems().add(new ShopItem(new ItemStack(Material.STONE_SWORD, 1), new ItemStack(Material.IRON_INGOT, 10)));
        fight.getListitems().add(new ShopItem(new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.GOLD_INGOT, 7)));
        fight.getListitems().add(new ShopItem(new ItemStack(Material.DIAMOND_SWORD, 1), new ItemStack(Material.EMERALD, 4)));
        fight.getListitems().add(new ShopItem(addEnchantment(Material.STICK, Enchantment.KNOCKBACK, 1), new ItemStack(Material.EMERALD, 4)));

        fight.getListitems().add(new ShopItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), new ItemStack(Material.IRON_INGOT, 40)));
        fight.getListitems().add(new ShopItem(new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.GOLD_INGOT, 12)));
        fight.getListitems().add(new ShopItem(new ItemStack(Material.DIAMOND_CHESTPLATE, 1), new ItemStack(Material.EMERALD, 7)));
        fight.getListitems().add(new ShopItem(new ItemStack(Material.SHEARS, 1), new ItemStack(Material.IRON_INGOT, 20)));


        tools.getListitems().add(new ShopItem(new ItemStack(Material.WOOD_AXE, 1), new ItemStack(Material.IRON_INGOT, 10)));
        tools.getListitems().add(new ShopItem(new ItemStack(Material.STONE_AXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        tools.getListitems().add(new ShopItem(new ItemStack(Material.IRON_AXE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        tools.getListitems().add(new ShopItem(new ItemStack(Material.DIAMOND_AXE, 1), new ItemStack(Material.GOLD_INGOT, 6)));

        tools.getListitems().add(new ShopItem(new ItemStack(Material.WOOD_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 10)));
        tools.getListitems().add(new ShopItem(new ItemStack(Material.IRON_PICKAXE, 1), new ItemStack(Material.IRON_INGOT, 15)));
        tools.getListitems().add(new ShopItem(new ItemStack(Material.GOLD_PICKAXE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        tools.getListitems().add(new ShopItem(new ItemStack(Material.DIAMOND_PICKAXE, 1), new ItemStack(Material.GOLD_INGOT, 6)));


        utilities.getListitems().add(new ShopItem(new ItemStack(Material.BOW, 1), new ItemStack(Material.GOLD_INGOT, 12)));
        utilities.getListitems().add(new ShopItem(addEnchantment(Material.BOW, Enchantment.ARROW_DAMAGE, 1), new ItemStack(Material.GOLD_INGOT, 24)));
        utilities.getListitems().add(new ShopItem(addEnchantment(Material.BOW, Enchantment.ARROW_DAMAGE, 2), new ItemStack(Material.EMERALD, 6)));
        utilities.getListitems().add(new ShopItem(addPotion(PotionEffectType.SPEED, 45, 2), new ItemStack(Material.EMERALD, 1)));
        utilities.getListitems().add(new ShopItem(addPotion(PotionEffectType.JUMP, 45, 5), new ItemStack(Material.EMERALD, 1)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.GOLDEN_APPLE, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.SNOW_BALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.FIREBALL, 1), new ItemStack(Material.IRON_INGOT, 40)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.TNT, 1), new ItemStack(Material.GOLD_INGOT, 4)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.ENDER_PEARL, 1), new ItemStack(Material.EMERALD, 4)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.WATER_BUCKET, 1), new ItemStack(Material.GOLD_INGOT, 3)));
        utilities.getListitems().add(new ShopItem(new ItemStack(Material.MILK_BUCKET, 1), new ItemStack(Material.GOLD_INGOT, 4)));

        sharpness = new UpgradeItem(FixedItems.UPGRADE_SHARPNESS,  (island) -> {
            int teamcomp = BWManager.getInstance().getArena(island.getArena()).getTeamcomposition();
            if(teamcomp == Arena.SOLO || teamcomp == Arena.DUO)
                return new ItemStack(Material.DIAMOND, 4);
            else
                return new ItemStack(Material.DIAMOND, 8);
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
        potionMeta.addCustomEffect(new PotionEffect(type, duration, power), true);
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
