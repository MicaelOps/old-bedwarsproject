package br.com.logicmc.bedwars.extra.customentity;

import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import net.minecraft.server.v1_8_R3.EntityTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;
import java.util.Map;

public class EntityManager {

    public static EntityManager instance;

    private EntityManager(){
        addToMaps(SuperDragon.class, "EnderDragon", 63);
        addToMaps(ImmobileVillager.class, "Villager", 120);
        addToMaps(TeamIronGolem.class, "VillagerGolem", 99);
        addToMaps(TeamRat.class, "Silverfish", 60);
    }
    public static EntityManager getInstance(){

        if(instance == null) {

            instance = new EntityManager();
        }

        return instance;
    }

    public void spawnImmobileVillager(Location location, String name){
        ImmobileVillager immobileVillager = new ImmobileVillager(((CraftWorld)location.getWorld()).getHandle(), name);
        immobileVillager.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld)location.getWorld()).getHandle().addEntity(immobileVillager);
    }

    public void spawnTeamIronGolem(Location location, Island island, BWTeam bwTeam, String lang){
        TeamIronGolem golem = new TeamIronGolem(((CraftWorld)location.getWorld()).getHandle(), island , bwTeam, lang);
        golem.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld)location.getWorld()).getHandle().addEntity(golem);
    }

    public void spawnTeamSilverFish(Location location, BWTeam bwTeam, String lang){
        TeamRat rat = new TeamRat(((CraftWorld)location.getWorld()).getHandle() , bwTeam, lang);
        rat.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld)location.getWorld()).getHandle().addEntity(rat);
    }

    public void spawnDragon(Location location){
        SuperDragon dragon = new SuperDragon(((CraftWorld)location.getWorld()).getHandle());
        dragon.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        ((CraftWorld)location.getWorld()).getHandle().addEntity(dragon);
    }

    private void addToMaps(Class clazz, String name, int id)
    {
        //getPrivateField is the method from above.
        //Remove the lines with // in front of them if you want to override default entities (You'd have to remove the default entity from the map first though).
        ((Map)getPrivateField("c", EntityTypes.class, null)).put(name, clazz);
        ((Map)getPrivateField("d", EntityTypes.class, null)).put(clazz, name);
        ((Map)getPrivateField("e", EntityTypes.class, null)).put(Integer.valueOf(id), clazz);
        ((Map)getPrivateField("f", EntityTypes.class, null)).put(clazz, Integer.valueOf(id));
        ((Map)getPrivateField("g", EntityTypes.class, null)).put(name, Integer.valueOf(id));
    }


    private Object getPrivateField(String fieldName, Class<?> clazz, Object object)
    {
        Field field;
        Object o = null;
        try
        {
            field = clazz.getDeclaredField(fieldName);

            field.setAccessible(true);

            o = field.get(object);
        }
        catch(NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return o;
    }
}
