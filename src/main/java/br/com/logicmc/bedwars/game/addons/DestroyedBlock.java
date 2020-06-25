package br.com.logicmc.bedwars.game.addons;

import org.bukkit.Location;
import org.bukkit.Material;

public class DestroyedBlock {


    private final SimpleBlock simpleBlock;
    private final Material type;
    private final byte data;

    public DestroyedBlock(SimpleBlock simpleBlock, Material type, byte data) {
        this.simpleBlock = simpleBlock;
        this.type = type;
        this.data = data;
    }

    public byte getData() {
        return data;
    }

    public SimpleBlock getSimpleBlock() {
        return simpleBlock;
    }

    public Material getType() {
        return type;
    }
}
