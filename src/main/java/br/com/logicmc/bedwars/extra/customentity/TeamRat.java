package br.com.logicmc.bedwars.extra.customentity;

import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.GameMode;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class TeamRat extends EntitySilverfish {

    private final BWTeam team;

    public TeamRat(World world, BWTeam team, String lang) {
        super(world);
        setCustomNameVisible(true);
        setCustomName(team.getChatColor() + team.getName(lang) + " SilverFish");
        this.team = team;


        //clearing entity intelligence
        List<?> goalB = (List<?>) getPrivateField("b", PathfinderGoalSelector.class, goalSelector);
        goalB.clear();
        List<?> goalC = (List<?>) getPrivateField("c", PathfinderGoalSelector.class, goalSelector);
        goalC.clear();
        List<?> targetB = (List<?>) getPrivateField("b", PathfinderGoalSelector.class, targetSelector);
        targetB.clear();
        List<?> targetC = (List<?>) getPrivateField("c", PathfinderGoalSelector.class, targetSelector);
        targetC.clear();

        //adding intelligence
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));

    }

    static class PathfinderGoalNearestPlayerTarget<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

        private final BWTeam team;
        private int targetchance = 20;

        public PathfinderGoalNearestPlayerTarget(BWTeam team, final EntityCreature entitycreature, Class<T> oclass, int i, boolean flag, boolean flag1, final Predicate<? super T> predicate) {
            super(entitycreature, oclass, 20, flag, flag1, predicate);
            this.team = team;
            this.c = new Predicate() {
                public boolean a(T t0) {
                    if (predicate != null && !predicate.apply(t0))
                        return false;
                    if (t0 instanceof EntityCreeper)
                        return false;
                    if (t0 instanceof EntityHuman) {


                        EntityHuman human = (EntityHuman) t0;
                        if(human.getBukkitEntity().getGameMode() != GameMode.SURVIVAL) {
                            return false;
                        }

                        BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(t0.getUniqueID());

                        if(bwPlayer.getTeamcolor().equalsIgnoreCase(team.name()))
                            return false;

                        double d0 = TeamRat.PathfinderGoalNearestPlayerTarget.this.f();
                        if (t0.isSneaking())
                            d0 *= 0.800000011920929D;
                        if (t0.isInvisible()) {
                            float f = ((EntityHuman)t0).bY();
                            if (f < 0.1F)
                                f = 0.1F;
                            d0 *= (0.7F * f);
                        }
                        if (t0.g(entitycreature) > d0)
                            return false;
                    }
                    return TeamRat.PathfinderGoalNearestPlayerTarget.this.a(t0, false);
                }

                public boolean apply(Object object) {
                    return a((T) object);
                }
            };
        }


        @Override
        public boolean a() {
            if (this.targetchance > 0 && this.e.bc().nextInt(this.targetchance) != 0)
                return false;
            double d0 = f();
            List<Entity> list = this.e.world.a((Class)this.a, this.e.getBoundingBox().grow(d0, 4.0D, d0), Predicates.and(this.c, IEntitySelector.d));
            list.sort(this.b);
            if (list.isEmpty())
                return false;

            for(int i =0 ; i <= list.size(); i++){
                Entity entity = list.get(i);

                if(entity instanceof EntityHuman){
                    if(((EntityHuman)entity).getBukkitEntity().getGameMode() == GameMode.SURVIVAL) {
                        BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(entity.getUniqueID());

                        if(!bwPlayer.getTeamcolor().equalsIgnoreCase(team.name())) {
                            this.d = (EntityLiving) entity;
                            return true;
                        }
                    }
                }
            }
            return false;
        }
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
