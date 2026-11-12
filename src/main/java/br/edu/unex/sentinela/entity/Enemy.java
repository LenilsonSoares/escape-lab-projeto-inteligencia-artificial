package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.game.NavigationStatus;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import java.util.List;
import java.util.Objects;

/** Escolhe o destino pelo comportamento e usa A* para calcular a rota. */
public final class Enemy {
    private static final double RECALCULATION_INTERVAL = 0.20;
    private final EnemyType type;
    private final TileMap tileMap;
    private final Pathfinder pathfinder;
    private final AutonomousAgent agent;
    private final List<GridPosition> patrolPoints;
    private GridPosition destination;
    private int patrolIndex;
    private double elapsed;

    public Enemy(EnemyType type, TileMap tileMap, GridPosition start,
            GridPosition playerPosition, List<GridPosition> patrolPoints, Pathfinder pathfinder) {
        this.type = Objects.requireNonNull(type);
        this.tileMap = Objects.requireNonNull(tileMap);
        this.pathfinder = Objects.requireNonNull(pathfinder);
        this.patrolPoints = List.copyOf(patrolPoints);
        if (type == EnemyType.PATROLLER && this.patrolPoints.size() < 2) {
            throw new IllegalArgumentException("A patrulha precisa de pelo menos dois pontos");
        }
        for (GridPosition point : this.patrolPoints) {
            if (!tileMap.isWalkable(point.row(), point.column())) {
                throw new IllegalArgumentException("Ponto de patrulha bloqueado");
            }
        }
        destination = type == EnemyType.PATROLLER ? this.patrolPoints.getFirst() : playerPosition;
        List<GridPosition> path = pathfinder.findPath(tileMap, start, destination);
        agent = new AutonomousAgent(tileMap, path.isEmpty() ? List.of(start) : path,
                28.0, type.speed());
        if (path.isEmpty()) { agent.stop(); }
    }

    public void update(double deltaTime, GridPosition playerPosition, MovementInput movement) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("Delta time deve ser finito e não negativo");
        }
        elapsed = Math.min(RECALCULATION_INTERVAL, elapsed + deltaTime);
        boolean patrolArrived = type == EnemyType.PATROLLER && agent.hasReachedDestination();
        if (patrolArrived && elapsed >= RECALCULATION_INTERVAL) {
            patrolIndex = (patrolIndex + 1) % patrolPoints.size();
        }
        GridPosition target = switch (type) {
            case CHASER -> playerPosition;
            case PATROLLER -> patrolPoints.get(patrolIndex);
            case INTERCEPTOR -> interceptDestination(tileMap, playerPosition, movement);
        };
        if (elapsed >= RECALCULATION_INTERVAL
                && (!target.equals(destination) || patrolArrived)) {
            List<GridPosition> path = pathfinder.findPath(tileMap, agent.currentGridPosition(), target);
            destination = target;
            elapsed = 0.0;
            if (path.isEmpty()) { agent.stop(); } else { agent.replacePath(path); }
        }
        agent.update(deltaTime);
    }

    static GridPosition interceptDestination(TileMap map, GridPosition player, MovementInput movement) {
        int columnStep = 0;
        int rowStep = 0;
        if (Math.abs(movement.horizontal()) >= Math.abs(movement.vertical())) {
            columnStep = (int) Math.signum(movement.horizontal());
        } else {
            rowStep = (int) Math.signum(movement.vertical());
        }
        GridPosition target = player;
        for (int step = 0; step < 2; step++) {
            GridPosition next = new GridPosition(target.row() + rowStep, target.column() + columnStep);
            if (!map.isWalkable(next.row(), next.column())) { break; }
            target = next;
        }
        return target;
    }

    public EnemyType type() { return type; }
    public AutonomousAgent agent() { return agent; }
    public GridPosition destination() { return destination; }
    public NavigationStatus navigationStatus() {
        if (agent.path().isEmpty()) { return NavigationStatus.NO_PATH; }
        return agent.hasReachedDestination() ? NavigationStatus.DESTINATION_REACHED : NavigationStatus.MOVING;
    }
}
