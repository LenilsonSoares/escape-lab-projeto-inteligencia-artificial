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
    private final Pathfinder pathfinder;

    private Player player;
    private TileMap tileMap;
    private AutonomousAgent autonomousAgent;
    private LaboratoryMap currentMap;
    private GridPosition navigationDestination;
    private double timeSinceLastRouteCalculation;
    private boolean escapeCompleted;

    public GameWorld() {
        this(new AStarPathfinder());
    }

    GameWorld(Pathfinder pathfinder) {
        this.pathfinder = Objects.requireNonNull(
                pathfinder,
                "O calculador de rotas não pode ser nulo"
        );
        loadMap(LaboratoryMap.ESCAPE_ROUTE);
    }

    public void selectMap(int mapNumber) {
        LaboratoryMap selectedMap = LaboratoryMap.fromNumber(mapNumber);
        if (selectedMap != currentMap) {
            loadMap(selectedMap);
        }
    }

    private void loadMap(LaboratoryMap map) {
        this.currentMap = Objects.requireNonNull(map, "O mapa não pode ser nulo");
        this.escapeCompleted = false;
        this.tileMap = map.tileMap();
        this.player = new Player(
                tileMap.playerStartX(PLAYER_SIZE),
                tileMap.playerStartY(PLAYER_SIZE),
                PLAYER_SIZE,
                PLAYER_SPEED
        );
        this.navigationDestination = gridPositionAt(player.centerX(), player.centerY());

        List<GridPosition> initialPath = pathfinder.findPath(
                tileMap,
                map.agentStart(),
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
        this.timeSinceLastRouteCalculation = 0.0;
    }

    public void update(double deltaTime, MovementInput movementInput) {
        if (escapeCompleted) {
            return;
        }

        player.update(movementInput, deltaTime, tileMap);
        GridPosition currentPlayerTile = gridPositionAt(player.centerX(), player.centerY());
        if (currentPlayerTile.equals(currentMap.exitPosition())) {
            advanceMap();
            return;
        }

        // O intervalo evita executar uma nova busca a cada quadro da animação.
        timeSinceLastRouteCalculation = Math.min(
                ROUTE_RECALCULATION_INTERVAL_SECONDS,
                timeSinceLastRouteCalculation + deltaTime
        );

        if (!currentPlayerTile.equals(navigationDestination)
                && timeSinceLastRouteCalculation >= ROUTE_RECALCULATION_INTERVAL_SECONDS) {
            recalculateRoute(currentPlayerTile);
        }

        autonomousAgent.update(deltaTime);
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

    public LaboratoryMap currentMap() {
        return currentMap;
    }

    public boolean escapeCompleted() {
        return escapeCompleted;
    }

    private void recalculateRoute(GridPosition newDestination) {
        // A nova busca parte do tile atual; o agente nunca retorna ao início do mapa.
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

    private void advanceMap() {
        if (currentMap.number() == LaboratoryMap.values().length) {
            escapeCompleted = true;
            return;
        }
        loadMap(LaboratoryMap.fromNumber(currentMap.number() + 1));
    }

    private GridPosition gridPositionAt(double centerX, double centerY) {
        int row = (int) Math.floor(centerY / tileMap.tileSize());
        int column = (int) Math.floor(centerX / tileMap.tileSize());
        return new GridPosition(row, column);
    }
}
