package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import java.util.List;
import java.util.Objects;

/**
 * Mantém o estado atual do mundo e aplica suas regras de atualização.
 */
public final class GameWorld {

    private static final double PLAYER_SIZE = 34.0;
    private static final double PLAYER_SPEED = 230.0;
    private static final double AGENT_SIZE = 28.0;
    private static final double AGENT_SPEED = 100.0;
    private static final double ROUTE_RECALCULATION_INTERVAL_SECONDS = 0.20;
    private static final GridPosition AGENT_START = new GridPosition(13, 1);

    private final Player player;
    private final TileMap tileMap;
    private final Pathfinder pathfinder;
    private final AutonomousAgent autonomousAgent;

    private GridPosition navigationDestination;
    private double timeSinceLastRouteCalculation;
    private double width;
    private double height;

    public GameWorld(double width, double height) {
        this(width, height, new AStarPathfinder());
    }

    GameWorld(double width, double height, Pathfinder pathfinder) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
        this.pathfinder = Objects.requireNonNull(
                pathfinder,
                "O calculador de rotas não pode ser nulo"
        );
        this.tileMap = TileMap.createLaboratory();
        this.player = new Player(
                tileMap.playerStartX(PLAYER_SIZE),
                tileMap.playerStartY(PLAYER_SIZE),
                PLAYER_SIZE,
                PLAYER_SPEED
        );
        this.navigationDestination = gridPositionAt(player.centerX(), player.centerY());

        List<GridPosition> initialPath = pathfinder.findPath(
                tileMap,
                AGENT_START,
                navigationDestination
        );
        if (initialPath.isEmpty()) {
            throw new IllegalStateException("Não foi possível calcular a rota do agente");
        }
        this.autonomousAgent = new AutonomousAgent(
                tileMap,
                initialPath,
                AGENT_SIZE,
                AGENT_SPEED
        );
    }

    public void update(double deltaTime, MovementInput movementInput) {
        player.update(movementInput, deltaTime, tileMap);
        timeSinceLastRouteCalculation = Math.min(
                ROUTE_RECALCULATION_INTERVAL_SECONDS,
                timeSinceLastRouteCalculation + deltaTime
        );

        GridPosition currentPlayerTile = gridPositionAt(player.centerX(), player.centerY());
        if (!currentPlayerTile.equals(navigationDestination)
                && timeSinceLastRouteCalculation >= ROUTE_RECALCULATION_INTERVAL_SECONDS) {
            recalculateRoute(currentPlayerTile);
        }

        autonomousAgent.update(deltaTime);
    }

    public void resize(double width, double height) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
    }

    public Player player() {
        return player;
    }

    public TileMap tileMap() {
        return tileMap;
    }

    public List<GridPosition> navigationPath() {
        return autonomousAgent.path();
    }

    public GridPosition navigationDestination() {
        return navigationDestination;
    }

    public NavigationStatus navigationStatus() {
        if (navigationPath().isEmpty()) {
            return NavigationStatus.NO_PATH;
        }
        if (autonomousAgent.hasReachedDestination()) {
            return NavigationStatus.DESTINATION_REACHED;
        }
        return NavigationStatus.MOVING;
    }

    public AutonomousAgent autonomousAgent() {
        return autonomousAgent;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    private void recalculateRoute(GridPosition newDestination) {
        GridPosition currentAgentTile = autonomousAgent.currentGridPosition();
        List<GridPosition> newPath = pathfinder.findPath(
                tileMap,
                currentAgentTile,
                newDestination
        );

        navigationDestination = newDestination;
        timeSinceLastRouteCalculation = 0.0;
        if (newPath.isEmpty()) {
            autonomousAgent.stop();
        } else {
            autonomousAgent.replacePath(newPath);
        }
    }

    private GridPosition gridPositionAt(double centerX, double centerY) {
        int row = (int) Math.floor(centerY / tileMap.tileSize());
        int column = (int) Math.floor(centerX / tileMap.tileSize());
        return new GridPosition(row, column);
    }

    private static double requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " deve ser maior que zero");
        }
        return value;
    }
}
