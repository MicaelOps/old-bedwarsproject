package br.com.logicmc.bedwars.extra.customentity;

import br.com.logicmc.bedwars.game.BWManager;
import br.com.logicmc.bedwars.game.engine.Island;
import br.com.logicmc.bedwars.game.player.BWPlayer;
import br.com.logicmc.bedwars.game.player.team.BWTeam;
import com.google.common.base.Predicate;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;
import java.util.List;

public class TeamIronGolem extends EntityIronGolem {

    private final BWTeam team;

    public TeamIronGolem(World world, Island island , BWTeam team, String lang) {
        super(world);
        setCustomNameVisible(true);
        setCustomName(team.getChatColor()+team.getName(lang) +" Golem");
        this.team = team;


        //clearing entity intelligence
        List<?> goalB = (List<?>)getPrivateField("b", PathfinderGoalSelector.class, goalSelector); goalB.clear();
        List<?> goalC = (List<?>)getPrivateField("c", PathfinderGoalSelector.class, goalSelector); goalC.clear();
        List<?> targetB = (List<?>)getPrivateField("b", PathfinderGoalSelector.class, targetSelector); targetB.clear();
        List<?> targetC = (List<?>)getPrivateField("c", PathfinderGoalSelector.class, targetSelector); targetC.clear();

        //adding intelligence
        this.goalSelector.a(1, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, 1.0D, true));
        this.goalSelector.a(4, new PathfinderGoalMoveTowardsTarget(this, 0.9D, 32.0F));
        this.goalSelector.a(5, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalDefendIsland(this, island));
        this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(3, new PathfinderFindNextEnemy<>(team, this, EntityHuman.class, 10, false, true, IMonster.e));
    }


    @Override
    protected void s(net.minecraft.server.v1_8_R3.Entity entity) {
        if(entity instanceof EntityHuman) {
            if(!BWManager.getInstance().getBWPlayer(entity.getUniqueID()).getTeamcolor().equalsIgnoreCase(team.name())){
                if (bc().nextInt(20) == 0) {
                    setGoalTarget((EntityLiving) entity, EntityTargetEvent.TargetReason.COLLISION, true);
                }
                super.s(entity);
            }
        }
    }

    static class PathfinderGoalDefendIsland extends  PathfinderGoalTarget {

        private final EntityIronGolem golem;
        private final Island island;

        private EntityLiving target;


        public PathfinderGoalDefendIsland(EntityIronGolem entityirongolem, Island island) {
            super(entityirongolem, false , true);
            this.golem = entityirongolem;
            this.island = island;
            a(1);
        }


        @Override
        public boolean a() {

            Location spawnlocation = island.getSpawn();

            Player totarget = null;
            double lastdistance = 99999999;

            for(Entity nearby : spawnlocation.getWorld().getNearbyEntities(spawnlocation, 25.0D , 25.0D, 25.0D)){
                if(nearby instanceof Player){
                    Player player = (Player) nearby;

                    if(player.getGameMode() == GameMode.SURVIVAL){

                        BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(player.getUniqueId());
                        if(!bwPlayer.getTeamcolor().equalsIgnoreCase(island.getTeam().name())) {
                            double distance = spawnlocation.distance(player.getLocation());
                            if(distance <=lastdistance){
                                lastdistance = distance;
                                totarget = player;
                            }
                        }
                    }
                }
            }

            if(totarget !=null)
                target = ((CraftPlayer)totarget).getHandle();

            return totarget != null;
        }

        @Override
        public void c() {
            this.golem.setGoalTarget(this.target, EntityTargetEvent.TargetReason.DEFEND_VILLAGE, true);
            super.c();
        }
    }
    static class PathfinderFindNextEnemy<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

        public PathfinderFindNextEnemy(BWTeam team, final EntityCreature entitycreature, Class<T> oclass, int i, boolean flag, boolean flag1, final Predicate<? super T> predicate) {
            super(entitycreature, oclass, i, flag, flag1, predicate);
            this.c = new Predicate() {
                public boolean a(T t0) {
                    if (predicate != null && !predicate.apply(t0))
                        return false;
                    if (t0 instanceof EntityCreeper)
                        return false;
                    if (t0 instanceof EntityHuman) {


                        EntityHuman human = (EntityHuman) t0;
                        if (human.getBukkitEntity().getGameMode() != GameMode.SURVIVAL) {
                            return false;
                        }

                        BWPlayer bwPlayer = BWManager.getInstance().getBWPlayer(t0.getUniqueID());

                        if (bwPlayer.getTeamcolor().equalsIgnoreCase(team.name()))
                            return false;

                        double d0 = TeamIronGolem.PathfinderFindNextEnemy.this.f();
                        if (t0.isSneaking())
                            d0 *= 0.800000011920929D;
                        if (t0.isInvisible()) {
                            float f = ((EntityHuman) t0).bY();
                            if (f < 0.1F)
                                f = 0.1F;
                            d0 *= (0.7F * f);
                        }
                        if (t0.g(entitycreature) > d0)
                            return false;
                    }
                    return TeamIronGolem.PathfinderFindNextEnemy.this.a(t0, false);
                }

                public boolean apply(Object object) {
                    return a((T) object);
                }
            };
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
