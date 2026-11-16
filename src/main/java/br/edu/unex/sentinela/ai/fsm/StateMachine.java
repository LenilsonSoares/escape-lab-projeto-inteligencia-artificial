package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Brain;
import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import java.util.Objects;

/** Máquina de estados finitos: o robô está sempre em exatamente um estado. */
public final class StateMachine implements Brain {

    private final Robot robot;
    private final DecisionLog log;
    private EnemyState current;
    private boolean changedThisFrame;

    public StateMachine(Robot robot, EnemyState initial, DecisionLog log) {
        this.robot = Objects.requireNonNull(robot);
        this.log = Objects.requireNonNull(log);
        current = Objects.requireNonNull(initial);
        current.enter(robot);
    }

    public void changeState(EnemyState next, String reason) {
        // No máximo uma troca por quadro: evita trocas em cadeia no mesmo update.
        if (changedThisFrame) {
            return;
        }
        log.add(robot.now(), robot.name(), current.name(), next.name(), reason);
        current.exit(robot);
        current = next;
        current.enter(robot);
        changedThisFrame = true;
    }

    @Override
    public void update(double deltaTime) {
        changedThisFrame = false;
        current.update(robot, this, deltaTime);
    }

    public EnemyState current() {
        return current;
    }

    @Override
    public String currentBehavior() {
        return current.name();
    }

    @Override
    public String technique() {
        return "FSM";
    }
}
