package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.ai.utility.ScoredPoint;
import br.edu.unex.sentinela.ai.utility.SearchPointScorer;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Procura o jogador depois de perder o contato. A Utility AI escolhe o próximo
 * ponto e o blackboard reserva esse ponto, para dois robôs não revistarem o
 * mesmo lugar. A busca tem prazo, para não durar para sempre.
 */
public final class SearchState implements EnemyState {

    static final double SEARCH_TIME = 12.0;
    static final double LOOK_TIME = 1.0;
    private static final double RESERVATION_TIME = 6.0;

    private final SearchPointScorer scorer = new SearchPointScorer();
    private final Set<GridPosition> visited = new HashSet<>();
    private List<ScoredPoint> ranking = List.of();
    private GridPosition clue;
    private GridPosition target;
    private double timer;
    private double lookTimer;

    @Override
    public String name() {
        return "PROCURAR";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(false);
        clue = robot.memory().lastSeenPosition().orElse(robot.tile());
        pickNextPoint(robot);
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (Reactions.reactToThreat(robot, machine)) {
            return;
        }
        timer += deltaTime;
        if (timer >= SEARCH_TIME) {
            machine.changeState(new ReturnState(), "BUSCA ESGOTADA");
            return;
        }
        if (target == null) {
            machine.changeState(new ReturnState(), "SEM PONTOS DE BUSCA");
            return;
        }
        if (robot.routeFailed()) {
            finishPoint(robot);
            return;
        }
        if (robot.arrived()) {
            lookTimer += deltaTime;
            robot.lookAround(deltaTime);
            if (lookTimer >= LOOK_TIME) {
                robot.memory().invalidateNear(target, 1);
                finishPoint(robot);
            }
        }
    }

    @Override
    public void exit(Robot robot) {
        robot.team().releaseAll(robot.name());
    }

    public List<ScoredPoint> ranking() {
        return ranking;
    }

    public GridPosition target() {
        return target;
    }

    private void finishPoint(Robot robot) {
        visited.add(target);
        robot.team().release(target, robot.name());
        lookTimer = 0.0;
        pickNextPoint(robot);
    }

    private void pickNextPoint(Robot robot) {
        ranking = scorer.rank(robot, clue, visited);
        target = null;
        for (ScoredPoint point : ranking) {
            if (visited.contains(point.position())) {
                continue;
            }
            if (robot.team().tryReserve(point.position(), robot.name(), robot.now(), RESERVATION_TIME)) {
                target = point.position();
                robot.goTo(target);
                return;
            }
        }
    }
}
