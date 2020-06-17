package br.com.logicmc.bedwars.extra.customentity;

import net.minecraft.server.v1_8_R3.*;

public class SuperDragon extends EntityEnderDragon {
    public SuperDragon(World world) {
        super(world);
    }

    @Override
    public boolean isInvulnerable(DamageSource damagesource) {
        return true;
    }

    @Override
    public boolean ad() {
        return  true;
    }
}
