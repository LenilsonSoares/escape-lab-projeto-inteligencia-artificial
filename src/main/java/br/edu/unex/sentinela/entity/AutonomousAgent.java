package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
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
    private final boolean canJumpEquipment;

    private List<GridPosition> path;
    private double x;
    private double y;
    private int nextWaypointIndex;
    private double speedFactor = 1.0;

    public AutonomousAgent(
            TileMap tileMap,
            List<GridPosition> path,
            double size,
            double speed
    ) {
        this(tileMap, path, size, speed, false);
    }

    /**
     * @param canJumpEquipment permite rotas com saltos sobre um equipamento.
     */
    public AutonomousAgent(
            TileMap tileMap,
            List<GridPosition> path,
            double size,
            double speed,
            boolean canJumpEquipment
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
        this.canJumpEquipment = canJumpEquipment;
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

        // A distância restante permite atravessar mais de um waypoint em um
        // quadro sem perder movimento quando o delta time é maior.
        double remainingDistance = speed * speedFactor * deltaTime;
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

            boolean jumpSegment = isCurrentSegmentJump();

            if (distance <= POSITION_EPSILON) {
                moveTo(targetX, targetY, false);
                nextWaypointIndex++;
                continue;
            }

            if (distance <= remainingDistance + POSITION_EPSILON) {
                // O pouso sempre é validado, pois o tile final é transitável.
                moveTo(targetX, targetY, false);
                remainingDistance = Math.max(0.0, remainingDistance - distance);
                nextWaypointIndex++;
                continue;
            }

            double movementRatio = remainingDistance / distance;
            moveTo(
                    x + distanceX * movementRatio,
                    y + distanceY * movementRatio,
                    jumpSegment
            );
            remainingDistance = 0.0;
        }
    }

    public List<GridPosition> path() {
        return path;
    }

    public void replacePath(List<GridPosition> newPath) {
        if (isJumping()) {
            throw new IllegalStateException("A rota não pode ser trocada durante um salto");
        }
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

        // Preserva o movimento atual quando o recálculo manteve o próximo passo.
        // Isso evita pequenas travadas enquanto o destino muda de tile.
        nextWaypointIndex = (continuesCurrentSegment
                || distanceToStart <= POSITION_EPSILON) ? 1 : 0;
    }

    public void stop() {
        path = List.of();
        nextWaypointIndex = 0;
    }

    /** Verdadeiro enquanto o agente está no ar, entre a decolagem e o pouso. */
    public boolean isJumping() {
        return jumpProgress() > 0.0;
    }

    /**
     * Fração já percorrida do salto atual, de 0 a 1. Fora de um salto,
     * retorna 0. Usado pela renderização para desenhar o arco do pulo.
     */
    public double jumpProgress() {
        if (!isCurrentSegmentJump()) {
            return 0.0;
        }
        GridPosition takeOff = path.get(nextWaypointIndex - 1);
        double travelled = Math.hypot(x - centeredX(takeOff), y - centeredY(takeOff));
        if (travelled <= POSITION_EPSILON) {
            return 0.0;
        }
        double length = takeOff.manhattanDistanceTo(path.get(nextWaypointIndex)) * tileMap.tileSize();
        return Math.min(1.0, travelled / length);
    }

    /** Multiplica a velocidade base; usado quando o robô corre na perseguição. */
    public void setSpeedFactor(double factor) {
        if (!Double.isFinite(factor) || factor <= 0.0) {
            throw new IllegalArgumentException("O fator de velocidade deve ser positivo");
        }
        speedFactor = factor;
    }

    public double currentSpeed() {
        return speed * speedFactor;
    }

    public boolean canJumpEquipment() {
        return canJumpEquipment;
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
            if (previous != null
                    && previous.manhattanDistanceTo(position) != 1
                    && !isValidJump(previous, position)) {
                throw new IllegalArgumentException("Os pontos da rota devem ser vizinhos ortogonais");
            }
            previous = position;
        }
        return validatedPath;
    }

    /** Salto válido: dois tiles em linha reta com um equipamento no meio. */
    private boolean isValidJump(GridPosition from, GridPosition to) {
        if (!canJumpEquipment || !AStarPathfinder.isJump(from, to)) {
            return false;
        }
        int middleRow = (from.row() + to.row()) / 2;
        int middleColumn = (from.column() + to.column()) / 2;
        return tileMap.isJumpable(middleRow, middleColumn);
    }

    private boolean isCurrentSegmentJump() {
        return nextWaypointIndex > 0
                && nextWaypointIndex < path.size()
                && AStarPathfinder.isJump(path.get(nextWaypointIndex - 1), path.get(nextWaypointIndex));
    }

    private void moveTo(double nextX, double nextY, boolean airborne) {
        // No ar, o agente passa por cima do equipamento; a validade do salto
        // já foi conferida em validateAndCopy e o pouso é checado normalmente.
        if (!airborne && !tileMap.canOccupy(nextX, nextY, size, size)) {
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
