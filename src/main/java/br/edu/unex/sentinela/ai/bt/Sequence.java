package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Robot;
import java.util.List;

/** Sequência: executa os filhos em ordem e falha assim que um deles falha. */
public final class Sequence implements Node {

    private final String name;
    private final List<Node> children;

    public Sequence(String name, Node... children) {
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
            if (status != Status.SUCCESS) {
                return status;
            }
        }
        return Status.SUCCESS;
    }
}
