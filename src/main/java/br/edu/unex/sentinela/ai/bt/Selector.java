package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Robot;
import java.util.List;

/**
 * Seletor reativo: a cada tick tenta os filhos desde o primeiro e para no
 * primeiro que não falhar. Assim um ramo mais prioritário interrompe os de baixo.
 */
public final class Selector implements Node {

    private final String name;
    private final List<Node> children;

    public Selector(String name, Node... children) {
        this.name = name;
        this.children = List.of(children);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Status tick(Robot robot, double deltaTime, Trace trace) {
        for (Node child : children) {
            Status status = child.tick(robot, deltaTime, trace);
            if (status != Status.FAILURE) {
                return status;
            }
        }
        return Status.FAILURE;
    }
}
