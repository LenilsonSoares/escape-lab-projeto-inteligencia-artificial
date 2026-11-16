package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Robot;

/**
 * Folha que realiza uma tarefa ao longo de vários ticks. Quando outro ramo
 * assume o controle, a árvore chama abort para a ação liberar o que reservou.
 */
public abstract class Action implements Node {

    private final String name;

    protected Action(String name) {
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final Status tick(Robot robot, double deltaTime, Trace trace) {
        trace.ran(this);
        return run(robot, deltaTime);
    }

    protected abstract Status run(Robot robot, double deltaTime);

    protected void abort(Robot robot) {
    }
}
