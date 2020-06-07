package br.com.logicmc.bedwars.game.phase.event;

import br.com.logicmc.bedwars.game.engine.Arena;


public abstract class PhaseEvent {

    private final int inittime;
    private final String eventname;

    public PhaseEvent(int inittime, String eventname) {
        this.inittime = inittime;
        this.eventname = eventname;
    }

    public String getEventname() {
        return eventname;
    }

    public abstract void execute(Arena arena);

    public int getInittime() {
        return inittime;
    }
}
