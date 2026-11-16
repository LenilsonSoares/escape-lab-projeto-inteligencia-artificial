package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.navigation.GridPosition;

/** Volta ao posto ou à rota depois de investigar ou procurar. */
public final class ReturnState implements EnemyState {

    @Override
    public String name() {
        return "RETORNAR";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(false);
        robot.goTo(home(robot));
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (Reactions.reactToThreat(robot, machine) || Reactions.reactToClue(robot, machine)) {
            return;
        }
        if (robot.arrived() || robot.routeFailed()) {
            machine.changeState(Reactions.routineOf(robot), "VOLTOU À ROTINA");
        }
    }

    private static GridPosition home(Robot robot) {
        if (robot.guardsPost()) {
            return robot.post();
        }
        GridPosition nearest = robot.routePoints().getFirst();
        for (GridPosition point : robot.routePoints()) {
            if (robot.tile().manhattanDistanceTo(point) < robot.tile().manhattanDistanceTo(nearest)) {
                nearest = point;
            }
        }
        return nearest;
    }
}
