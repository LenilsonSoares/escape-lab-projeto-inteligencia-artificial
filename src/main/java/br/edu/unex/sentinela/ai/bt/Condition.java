package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Robot;
import java.util.function.Predicate;

/** Verifica um requisito sem executar nenhuma ação. */
public final class Condition implements Node {

    private final String name;
    private final Predicate<Robot> test;

    public Condition(String name, Predicate<Robot> test) {
        this.name = name;
        this.test = test;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Status tick(Robot robot, double deltaTime, Trace trace) {
        return test.test(robot) ? Status.SUCCESS : Status.FAILURE;
    }
}
