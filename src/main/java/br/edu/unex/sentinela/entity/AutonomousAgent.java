package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import java.util.Objects;

/**
 * Agente que percorre, em ordem, os pontos de uma rota calculada no tilemap.
 */
public final class AutonomousAgent {

    private static final double POSITION_EPSILON = 1.0e-9;

    private final TileMap tileMap;
    private final List<GridPosition> path;
    private final double size;
    private final double speed;

    private double x;
    private double y;
    private int nextWaypointIndex;

    public AutonomousAgent(
            TileMap tileMap,
            List<GridPosition> path,
            double size,
            double speed
    ) {
        this.tileMap = Objects.requireNonNull(tileMap, "O tilemap não pode ser nulo");
        if (!Double.isFinite(size) || size <= 0.0) {
            throw new IllegalArgumentException("O tamanho do agente deve ser maior que zero");
        }
        if (!Double.isFinite(speed) || speed <= 0.0) {
            throw new IllegalArgumentException("A velocidade do agente deve ser maior que zero");
        }

        this.path = List.copyOf(Objects.requireNonNull(path, "A rota não pode ser nula"));
        if (this.path.isEmpty()) {
            throw new IllegalArgumentException("A rota do agente não pode ser vazia");
        }

        this.size = size;
        this.speed = speed;
        validatePath();

        GridPosition start = this.path.get(0);
        this.x = centeredX(start);
        this.y = centeredY(start);
        this.nextWaypointIndex = this.path.size() > 1 ? 1 : this.path.size();
    }

    public void update(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("Delta time não pode ser negativo");
        }

        double remainingDistance = speed * deltaTime;
        if (!Double.isFinite(remainingDistance)) {
            throw new IllegalArgumentException("O deslocamento do agente deve ser finito");
        }

        while (remainingDistance > 0.0 && !hasReachedDestination()) {
            GridPosition waypoint = path.get(nextWaypointIndex);
            double targetX = centeredX(waypoint);
            double targetY = centeredY(waypoint);
            double distanceX = targetX - x;
            double distanceY = targetY - y;
            double distance = Math.hypot(distanceX, distanceY);

            if (distance <= POSITION_EPSILON) {
                moveTo(targetX, targetY);
                nextWaypointIndex++;
                continue;
            }

            if (distance <= remainingDistance + POSITION_EPSILON) {
                moveTo(targetX, targetY);
                remainingDistance = Math.max(0.0, remainingDistance - distance);
                nextWaypointIndex++;
                continue;
            }

            double movementRatio = remainingDistance / distance;
            moveTo(
                    x + distanceX * movementRatio,
                    y + distanceY * movementRatio
            );
            remainingDistance = 0.0;
        }
    }

    public List<GridPosition> path() {
        return path;
    }

    public GridPosition destination() {
        return path.get(path.size() - 1);
    }

    public boolean hasReachedDestination() {
        return nextWaypointIndex >= path.size();
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double size() {
        return size;
    }

    public double centerX() {
        return x + size / 2.0;
    }

    public double centerY() {
        return y + size / 2.0;
    }

    private void validatePath() {
        GridPosition previous = null;
        for (GridPosition position : path) {
            if (!tileMap.isWalkable(position.row(), position.column())) {
                throw new IllegalArgumentException("A rota contém um tile não transitável");
            }
            if (!tileMap.canOccupy(
                    centeredX(position),
                    centeredY(position),
                    size,
                    size
            )) {
                throw new IllegalArgumentException("O agente não cabe em um dos tiles da rota");
            }
            if (previous != null && previous.manhattanDistanceTo(position) != 1) {
                throw new IllegalArgumentException("Os pontos da rota devem ser vizinhos ortogonais");
            }
            previous = position;
        }
    }

    private void moveTo(double nextX, double nextY) {
        if (!tileMap.canOccupy(nextX, nextY, size, size)) {
            throw new IllegalStateException("O agente tentou sair da rota navegável");
        }
        x = nextX;
        y = nextY;
    }

    private double centeredX(GridPosition position) {
        return position.column() * tileMap.tileSize()
                + (tileMap.tileSize() - size) / 2.0;
    }

    private double centeredY(GridPosition position) {
        return position.row() * tileMap.tileSize()
                + (tileMap.tileSize() - size) / 2.0;
    }
}
