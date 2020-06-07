package br.com.logicmc.bedwars.game.phase.event;

import br.com.logicmc.bedwars.game.engine.Arena;
import br.com.logicmc.bedwars.game.engine.generator.NormalGenerator;
import org.bukkit.Material;

public class GeneratorEvent extends PhaseEvent{

    private final int type;
    public GeneratorEvent(int inittime, String eventname, int type) {
        super(inittime, eventname);
        this.type = type;
    }

    @Override
    public void execute(Arena arena) {
        if(type == 0) {
            for(NormalGenerator generator : arena.getDiamond()){
                generator.increaseGeneratorLevel();
            }
        } else {
            for(NormalGenerator generator : arena.getEmerald()){
                generator.increaseGeneratorLevel();
            }
        }
    }
}
