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
    private final double size;
    private final double speed;

    private List<GridPosition> path;
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

        this.size = size;
        this.speed = speed;
        this.path = validateAndCopy(path);

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

        while (remainingDistance > 0.0
                && !path.isEmpty()
                && !hasReachedDestination()) {
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

    public void replacePath(List<GridPosition> newPath) {
        List<GridPosition> validatedPath = validateAndCopy(newPath);
        if (!validatedPath.get(0).equals(currentGridPosition())) {
            throw new IllegalArgumentException(
                    "A nova rota deve começar no tile atual do agente"
            );
        }

        boolean continuesCurrentSegment = nextWaypointIndex < path.size()
                && validatedPath.size() > 1
                && path.get(nextWaypointIndex).equals(validatedPath.get(1));
        path = validatedPath;
        GridPosition start = path.get(0);
        double distanceToStart = Math.hypot(
                centeredX(start) - x,
                centeredY(start) - y
        );

        // Mantém o avanço quando o primeiro passo da rota não mudou.
        nextWaypointIndex = (continuesCurrentSegment
                || distanceToStart <= POSITION_EPSILON) ? 1 : 0;
    }

    public void stop() {
        path = List.of();
        nextWaypointIndex = 0;
    }

    public boolean hasReachedDestination() {
        return !path.isEmpty() && nextWaypointIndex >= path.size();
    }

    public GridPosition currentGridPosition() {
        int row = (int) Math.floor(centerY() / tileMap.tileSize());
        int column = (int) Math.floor(centerX() / tileMap.tileSize());
        return new GridPosition(row, column);
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

    private List<GridPosition> validateAndCopy(List<GridPosition> candidatePath) {
        List<GridPosition> validatedPath = List.copyOf(
                Objects.requireNonNull(candidatePath, "A rota não pode ser nula")
        );
        if (validatedPath.isEmpty()) {
            throw new IllegalArgumentException("A rota do agente não pode ser vazia");
        }

        GridPosition previous = null;
        for (GridPosition position : validatedPath) {
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
        return validatedPath;
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
