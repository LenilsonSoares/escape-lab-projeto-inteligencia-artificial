package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;

/** Rotina do robô patrulha: percorre os pontos da rota em ciclo. */
public final class PatrolState implements EnemyState {

    private int index;
    private boolean paused;

    @Override
    public String name() {
        return "PATRULHAR";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(false);
        index = nearestPoint(robot);
        robot.goTo(robot.routePoints().get(index));
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (Reactions.reactToThreat(robot, machine) || Reactions.reactToClue(robot, machine)) {
            return;
        }
        if (Reactions.payAttention(robot)) {
            paused = true;
            return;
        }
        List<GridPosition> points = robot.routePoints();
        if (paused) {
            paused = false;
            robot.goTo(points.get(index));
        } else if (robot.arrived() || robot.routeFailed()) {
            index = (index + 1) % points.size();
            robot.goTo(points.get(index));
        }
    }

    // Ao voltar de uma busca, continua pelo ponto mais próximo da rota.
    private static int nearestPoint(Robot robot) {
        List<GridPosition> points = robot.routePoints();
        int best = 0;
        for (int i = 1; i < points.size(); i++) {
            if (robot.tile().manhattanDistanceTo(points.get(i))
                    < robot.tile().manhattanDistanceTo(points.get(best))) {
                best = i;
            }
        }
        return best;
    }
}
