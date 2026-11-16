package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Brain;
import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.telemetry.DecisionLog;

/** Executa a árvore a cada quadro e cancela a ação que perdeu o controle. */
public final class BehaviorTree implements Brain {

    private final Robot robot;
    private final Node root;
    private final DecisionLog log;
    private final Trace trace = new Trace();
    private Action active;

    public BehaviorTree(Robot robot, Node root, DecisionLog log) {
        this.robot = robot;
        this.root = root;
        this.log = log;
    }

    @Override
    public void update(double deltaTime) {
        trace.clear();
        root.tick(robot, deltaTime, trace);
        Action ran = trace.activeAction();
        if (ran != active) {
            if (active != null) {
                active.abort(robot);
            }
            log.add(robot.now(), robot.name(), behaviorName(active), behaviorName(ran), "RAMO DA ÁRVORE");
            active = ran;
        }
    }

    @Override
    public String currentBehavior() {
        return behaviorName(active);
    }

    @Override
    public String technique() {
        return "BT";
    }

    private static String behaviorName(Action action) {
        return action == null ? "-" : action.name();
    }
}
