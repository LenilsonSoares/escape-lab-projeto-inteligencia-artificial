package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.navigation.GridPosition;

/**
 * Persegue o jogador enquanto ele continua visível. O A* roda no enter e
 * depois no máximo a cada meio segundo, nunca em todo quadro.
 */
public final class ChaseState implements EnemyState {

    static final double REPATH_INTERVAL = 0.5;
    private double timer;

    @Override
    public String name() {
        return "PERSEGUIR";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(true);
        robot.goTo(robot.chaseTarget());
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (robot.touchedPlayer()) {
            machine.changeState(new AttackState(), "TOCOU O JOGADOR");
            return;
        }
        if (!robot.memory().inCombat()) {
            machine.changeState(new SearchState(), "PERDEU CONTATO");
            return;
        }
        timer += deltaTime;
        if (timer >= REPATH_INTERVAL) {
            timer = 0.0;
            GridPosition target = robot.chaseTarget();
            if (!target.equals(robot.destination())) {
                robot.goTo(target);
            }
        }
    }

    @Override
    public void exit(Robot robot) {
        robot.setRunning(false);
        robot.team().releaseAttackPermit(robot.name());
    }
}
