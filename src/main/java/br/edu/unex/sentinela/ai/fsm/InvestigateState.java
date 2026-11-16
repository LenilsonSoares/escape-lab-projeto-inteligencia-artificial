package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.ai.memory.Evidence;
import java.util.Optional;

/** Vai até a pista (ruído ou relato de rádio) e olha em volta. */
public final class InvestigateState implements EnemyState {

    static final double LOOK_TIME = 2.0;
    private static final double RETARGET_INTERVAL = 1.0;

    private Evidence clue;
    private double lookTimer;
    private double retargetTimer;

    @Override
    public String name() {
        return "INVESTIGAR";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(false);
        clue = robot.memory().bestEvidence(robot.now()).orElse(null);
        if (clue != null) {
            robot.goTo(clue.position());
        }
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (Reactions.reactToThreat(robot, machine)) {
            return;
        }
        if (clue == null) {
            machine.changeState(new ReturnState(), "SEM PISTA");
            return;
        }
        retargetTimer += deltaTime;
        if (retargetTimer >= RETARGET_INTERVAL) {
            retargetTimer = 0.0;
            followNewerClue(robot);
        }
        if (robot.routeFailed()) {
            robot.memory().invalidate(clue.eventId());
            machine.changeState(new ReturnState(), "PISTA INALCANÇÁVEL");
            return;
        }
        if (!robot.arrived()) {
            return;
        }
        lookTimer += deltaTime;
        robot.lookAround(deltaTime);
        if (lookTimer >= LOOK_TIME) {
            robot.memory().invalidateNear(clue.position(), 1);
            if (robot.memory().bestEvidence(robot.now()).isPresent()) {
                machine.changeState(new InvestigateState(), "OUTRA PISTA");
            } else {
                machine.changeState(new ReturnState(), "NADA ENCONTRADO");
            }
        }
    }

    // Um ruído mais novo substitui o destino sem recomeçar o estado.
    private void followNewerClue(Robot robot) {
        Optional<Evidence> best = robot.memory().bestEvidence(robot.now());
        if (best.isPresent() && best.get().time() > clue.time()
                && !best.get().position().equals(clue.position())) {
            clue = best.get();
            lookTimer = 0.0;
            robot.goTo(clue.position());
        }
    }
}
