package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;

/**
 * Mantém o estado atual do mundo e aplica suas regras de atualização.
 */
public final class GameWorld {

    private static final double PLAYER_SIZE = 34.0;
    private static final double PLAYER_SPEED = 230.0;
    private static final double AGENT_SIZE = 28.0;
    private static final double AGENT_SPEED = 100.0;
    private static final GridPosition AGENT_START = new GridPosition(13, 1);
    private static final GridPosition AGENT_DESTINATION = new GridPosition(1, 13);

    private final Player player;
    private final TileMap tileMap;
    private final List<GridPosition> navigationPath;
    private final AutonomousAgent autonomousAgent;

    private double width;
    private double height;

    public GameWorld(double width, double height) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
        this.tileMap = TileMap.createLaboratory();
        this.navigationPath = new AStarPathfinder().findPath(
                tileMap,
                AGENT_START,
                AGENT_DESTINATION
        );
        if (navigationPath.isEmpty()) {
            throw new IllegalStateException("Não foi possível calcular a rota do agente");
        }
        this.autonomousAgent = new AutonomousAgent(
                tileMap,
                navigationPath,
                AGENT_SIZE,
                AGENT_SPEED
        );
        this.player = new Player(
                tileMap.playerStartX(PLAYER_SIZE),
                tileMap.playerStartY(PLAYER_SIZE),
                PLAYER_SIZE,
                PLAYER_SPEED
        );
    }

    public void update(double deltaTime, MovementInput movementInput) {
        player.update(movementInput, deltaTime, tileMap);
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
        return navigationPath;
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

    private static double requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " deve ser maior que zero");
        }
        return value;
    }
}
