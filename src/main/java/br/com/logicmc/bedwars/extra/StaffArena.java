package br.com.logicmc.bedwars.extra;

import br.com.logicmc.bedwars.game.engine.Arena;

public class StaffArena extends Arena {
    public StaffArena() {
        super(name, 1000);
    }

    @Override
    public int getGamestate() {
        return Arena.WAITING;
    }

    @Override
    public int getTime() {
        return -1;
    }
}
