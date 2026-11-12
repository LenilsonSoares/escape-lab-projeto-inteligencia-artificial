package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/** Mantém o mundo e os três comportamentos de navegação dos robôs. */
public final class GameWorld {
    private static final double PLAYER_SIZE = 34.0;
    private static final double PLAYER_SPEED = 230.0;
    private final Pathfinder chaserPathfinder;
    private Player player;
    private TileMap tileMap;
    private List<Enemy> enemies;
    private LaboratoryMap currentMap;
    private boolean escapeCompleted;
    private int score = 1_000;
    private int penalties;
    private final EnumSet<EnemyType> touchingEnemies = EnumSet.noneOf(EnemyType.class);

    public GameWorld() {
        this(new AStarPathfinder());
    }

    GameWorld(Pathfinder chaserPathfinder) {
        this.chaserPathfinder = Objects.requireNonNull(chaserPathfinder);
        loadMap(LaboratoryMap.ESCAPE_ROUTE);
    }

    public void selectMap(int mapNumber) {
        LaboratoryMap selected = LaboratoryMap.fromNumber(mapNumber);
        if (selected != currentMap) {
            loadMap(selected);
        }
    }

    private void loadMap(LaboratoryMap map) {
        currentMap = Objects.requireNonNull(map);
        escapeCompleted = false;
        tileMap = map.tileMap();
        touchingEnemies.clear();
        player = new Player(tileMap.playerStartX(PLAYER_SIZE), tileMap.playerStartY(PLAYER_SIZE),
                PLAYER_SIZE, PLAYER_SPEED);
        GridPosition playerTile = gridPositionAt(player.centerX(), player.centerY());
        GridPosition patrolStart = nearestFloor(new GridPosition(13, 18), List.of(playerTile, map.agentStart()));
        GridPosition interceptorStart = nearestFloor(new GridPosition(7, 10),
                List.of(playerTile, map.agentStart(), patrolStart));
        List<GridPosition> patrolPoints = List.of(
                nearestFloor(new GridPosition(1, 18), List.of()),
                nearestFloor(new GridPosition(1, 1), List.of()),
                nearestFloor(new GridPosition(13, 1), List.of()), patrolStart);
        // Cada robô mantém uma busca e uma rota independentes.
        enemies = List.of(
                new Enemy(EnemyType.CHASER, tileMap, map.agentStart(), playerTile,
                        List.of(), chaserPathfinder),
                new Enemy(EnemyType.PATROLLER, tileMap, patrolStart, playerTile,
                        patrolPoints, new AStarPathfinder()),
                new Enemy(EnemyType.INTERCEPTOR, tileMap, interceptorStart, playerTile,
                        List.of(), new AStarPathfinder()));
    }

    private GridPosition nearestFloor(GridPosition preferred, List<GridPosition> excluded) {
        List<GridPosition> candidates = new ArrayList<>();
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                GridPosition point = new GridPosition(row, column);
                if (tileMap.tileAt(row, column) == TileType.LAB_FLOOR && !excluded.contains(point)) {
                    candidates.add(point);
                }
            }
        }
        return candidates.stream()
                .min(Comparator.comparingInt(preferred::manhattanDistanceTo)).orElseThrow();
    }

    public void update(double deltaTime, MovementInput movementInput) {
        if (escapeCompleted) {
            return;
        }
        double previousX = player.x();
        double previousY = player.y();
        player.update(movementInput, deltaTime, tileMap);
        GridPosition playerTile = gridPositionAt(player.centerX(), player.centerY());
        if (playerTile.equals(currentMap.exitPosition())) {
            if (currentMap.number() == LaboratoryMap.values().length) {
                escapeCompleted = true;
            } else {
                loadMap(LaboratoryMap.fromNumber(currentMap.number() + 1));
            }
            return;
        }
        MovementInput actualMovement = new MovementInput(player.x() - previousX, player.y() - previousY);
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime, playerTile, actualMovement);
            boolean touching = player.x() < enemy.agent().x() + enemy.agent().size()
                    && player.x() + player.size() > enemy.agent().x()
                    && player.y() < enemy.agent().y() + enemy.agent().size()
                    && player.y() + player.size() > enemy.agent().y();
            if (touching && touchingEnemies.add(enemy.type())) {
                score = Math.max(0, score - 100);
                penalties++;
            } else if (!touching) {
                touchingEnemies.remove(enemy.type());
            }
        }
    }

    public Player player() { return player; }
    public TileMap tileMap() { return tileMap; }
    public List<Enemy> enemies() { return enemies; }
    // O painel principal acompanha o perseguidor amarelo.
    public List<GridPosition> navigationPath() { return autonomousAgent().path(); }
    public GridPosition navigationDestination() { return enemies.getFirst().destination(); }
    public NavigationStatus navigationStatus() { return enemies.getFirst().navigationStatus(); }
    public AutonomousAgent autonomousAgent() { return enemies.getFirst().agent(); }
    public LaboratoryMap currentMap() { return currentMap; }
    public boolean escapeCompleted() { return escapeCompleted; }
    public int score() { return score; }
    public int penalties() { return penalties; }

    private GridPosition gridPositionAt(double x, double y) {
        return new GridPosition((int) Math.floor(y / tileMap.tileSize()),
                (int) Math.floor(x / tileMap.tileSize()));
    }
}
