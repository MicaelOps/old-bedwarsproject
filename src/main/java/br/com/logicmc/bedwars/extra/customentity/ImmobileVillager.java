package br.com.logicmc.bedwars.extra.customentity;

import net.minecraft.server.v1_8_R3.*;

import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;
import java.util.List;

public class ImmobileVillager extends EntityVillager {
    public ImmobileVillager(org.bukkit.World world) {
        super(((CraftWorld)world).getHandle());

    }
    public ImmobileVillager(World world) {
        super(world);

        
        //clearing entity intelligence
        List<?> goalB = (List<?>)getPrivateField("b", PathfinderGoalSelector.class, goalSelector); goalB.clear();
        List<?> goalC = (List<?>)getPrivateField("c", PathfinderGoalSelector.class, goalSelector); goalC.clear();
        List<?> targetB = (List<?>)getPrivateField("b", PathfinderGoalSelector.class, targetSelector); targetB.clear();
        List<?> targetC = (List<?>)getPrivateField("c", PathfinderGoalSelector.class, targetSelector); targetC.clear();

        //adding intelligence
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 14.0F));
    }


    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void move(double d0, double d1, double d2) {

    }

    public static Object getPrivateField(String fieldName, Class<?> clazz, Object object)
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
