package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.ai.coop.TeamBlackboard;
import br.edu.unex.sentinela.ai.director.AiDirector;
import br.edu.unex.sentinela.ai.perception.NoiseEvent;
import br.edu.unex.sentinela.ai.perception.NoiseSource;
import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.entity.Surroundings;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Estado do jogo e ordem de atualização dos sistemas: jogador, cartão e saída,
 * ruídos, robôs (perceber, decidir, mover), contatos e diretor de IA.
 */
public final class GameWorld {
    private static final double PLAYER_SIZE = 34.0;
    private static final double PLAYER_SPEED = 230.0;
    private static final double SNEAK_FACTOR = 0.5;
    private static final double STEP_INTERVAL = 0.45;
    public static final double STEP_NOISE_TILES = 3.5;
    private static final double SNEAK_NOISE_TILES = 1.5;
    private static final double CARD_NOISE_TILES = 7.0;
    private static final double DOOR_NOISE_TILES = 6.0;
    private static final double NOISE_DISPLAY_TIME = 1.0;
    private static final double NEAR_THREAT_TILES = 5.0;
    private static final int START_SCORE = 1_000;
    private static final int CONTACT_PENALTY = 100;

    private final Pathfinder chaserPathfinder;
    private final int startScore;
    private final TeamBlackboard team = new TeamBlackboard();
    private final AiDirector director = new AiDirector();
    private final DecisionLog decisions = new DecisionLog();
    private final List<NoiseEvent> recentNoises = new ArrayList<>();
    private final List<NoiseEvent> newNoises = new ArrayList<>();
    private final EnumSet<EnemyType> touchingEnemies = EnumSet.noneOf(EnemyType.class);
    private AiMode mode;
    private Difficulty difficulty = Difficulty.NORMAL;
    private Player player;
    private TileMap tileMap;
    private List<Enemy> enemies;
    private LaboratoryMap currentMap;
    private boolean escapeCompleted;
    private boolean captured;
    private int score;
    private int penalties;
    private double time;
    private double stepTimer;
    private double lastHitTime = Double.NEGATIVE_INFINITY;
    private long nextEventId = 1;

    public GameWorld() {
        this(new AStarPathfinder(), AiMode.FULL_AI);
    }

    GameWorld(Pathfinder chaserPathfinder) {
        this(chaserPathfinder, AiMode.NAVIGATION_DEMO);
    }

    GameWorld(Pathfinder chaserPathfinder, AiMode mode) {
        this(chaserPathfinder, mode, START_SCORE);
    }

    GameWorld(Pathfinder chaserPathfinder, AiMode mode, int startScore) {
        this.chaserPathfinder = Objects.requireNonNull(chaserPathfinder);
        this.mode = Objects.requireNonNull(mode);
        this.startScore = startScore;
        score = startScore;
        loadMap(LaboratoryMap.ESCAPE_ROUTE);
    }

    public void selectMap(int mapNumber) {
        LaboratoryMap selected = LaboratoryMap.fromNumber(mapNumber);
        if (selected != currentMap) {
            loadMap(selected);
        }
    }

    /** Alterna entre a IA completa e a demonstração do A* das entregas anteriores. */
    public void toggleMode() {
        mode = mode == AiMode.FULL_AI ? AiMode.NAVIGATION_DEMO : AiMode.FULL_AI;
        loadMap(currentMap);
    }

    /** Troca o nível (só no briefing ou depois da captura) e recomeça o setor. */
    public void changeDifficulty(int direction) {
        Difficulty next = difficulty.step(direction);
        if (next == difficulty) {
            return;
        }
        difficulty = next;
        director.setBudgetRange(difficulty.minAttackers(), difficulty.startAttackers(),
                difficulty.maxAttackers());
        loadMap(currentMap);
    }

    /** Nova partida depois da captura. */
    public void restart() {
        score = startScore;
        penalties = 0;
        captured = false;
        director.reset();
        loadMap(LaboratoryMap.ESCAPE_ROUTE);
    }

    private void loadMap(LaboratoryMap map) {
        currentMap = Objects.requireNonNull(map);
        escapeCompleted = false;
        tileMap = map.createTileMap();
        touchingEnemies.clear();
        lastHitTime = Double.NEGATIVE_INFINITY;
        team.clear();
        decisions.clear();
        recentNoises.clear();
        stepTimer = 0.0;
        player = new Player(tileMap.playerStartX(PLAYER_SIZE), tileMap.playerStartY(PLAYER_SIZE),
                PLAYER_SIZE, PLAYER_SPEED);
        GridPosition playerTile = gridPositionAt(player.centerX(), player.centerY());
        // Os robôs nascem e circulam só na parte do mapa aberta desde o início.
        Set<GridPosition> open = reachableFrom(playerTile);
        GridPosition guardPost = map.agentStart();
        GridPosition patrolStart = nearestFloor(new GridPosition(13, 18), open, List.of(playerTile, guardPost));
        GridPosition interceptorStart = nearestFloor(new GridPosition(7, 10), open,
                List.of(playerTile, guardPost, patrolStart));
        GridPosition jumperStart = nearestFloor(new GridPosition(13, 10), open,
                List.of(playerTile, guardPost, patrolStart, interceptorStart));
        List<GridPosition> patrolPoints = List.of(
                nearestFloor(new GridPosition(1, 18), open, List.of()),
                nearestFloor(new GridPosition(1, 1), open, List.of()),
                nearestFloor(new GridPosition(13, 1), open, List.of()), patrolStart);
        if (mode == AiMode.NAVIGATION_DEMO) {
            enemies = navigationDemoEnemies(map, playerTile, patrolStart, interceptorStart, jumperStart,
                    patrolPoints);
            return;
        }
        GridPosition exitWatch = nearestFloor(map.exitPosition(), open, List.of());
        List<GridPosition> roamPoints = List.of(interceptorStart,
                nearestFloor(new GridPosition(7, 3), open, List.of()),
                nearestFloor(new GridPosition(10, 10), open, List.of()),
                nearestFloor(new GridPosition(7, 16), open, List.of()));
        List<GridPosition> jumperRoute = List.of(jumperStart,
                nearestFloor(new GridPosition(4, 10), open, List.of()),
                nearestFloor(new GridPosition(10, 5), open, List.of()),
                nearestFloor(new GridPosition(10, 15), open, List.of()));
        enemies = List.of(
                Enemy.withAi(EnemyType.CHASER, tileMap, guardPost, List.of(guardPost), exitWatch,
                        chaserPathfinder, team, decisions),
                Enemy.withAi(EnemyType.PATROLLER, tileMap, patrolStart, patrolPoints, exitWatch,
                        new AStarPathfinder(), team, decisions),
                Enemy.withAi(EnemyType.INTERCEPTOR, tileMap, interceptorStart, roamPoints, exitWatch,
                        new AStarPathfinder(), team, decisions),
                Enemy.withAi(EnemyType.JUMPER, tileMap, jumperStart, jumperRoute, exitWatch,
                        AStarPathfinder.withEquipmentJumps(), team, decisions));
        // O nível escolhido ajusta velocidade e alcance da visão dos robôs.
        for (Enemy enemy : enemies) {
            enemy.tune(difficulty.robotSpeed(), difficulty.visionRange());
        }
        team.setMaxAttackers(director.attackBudget());
    }

    // Robôs das atividades de A*: sabem onde o jogador está e não percebem nada.
    private List<Enemy> navigationDemoEnemies(LaboratoryMap map, GridPosition playerTile,
            GridPosition patrolStart, GridPosition interceptorStart, GridPosition jumperStart,
            List<GridPosition> patrolPoints) {
        return List.of(
                new Enemy(EnemyType.CHASER, tileMap, map.agentStart(), playerTile,
                        List.of(), chaserPathfinder),
                new Enemy(EnemyType.PATROLLER, tileMap, patrolStart, playerTile,
                        patrolPoints, new AStarPathfinder()),
                new Enemy(EnemyType.INTERCEPTOR, tileMap, interceptorStart, playerTile,
                        List.of(), new AStarPathfinder()),
                new Enemy(EnemyType.JUMPER, tileMap, jumperStart, playerTile,
                        List.of(), AStarPathfinder.withEquipmentJumps()));
    }

    public void update(double deltaTime, MovementInput movementInput) {
        update(deltaTime, movementInput, false);
    }

    public void update(double deltaTime, MovementInput movementInput, boolean sneaking) {
        if (escapeCompleted || captured) {
            return;
        }
        time += deltaTime;
        newNoises.clear();
        recentNoises.removeIf(noise -> time - noise.time() > NOISE_DISPLAY_TIME);

        double previousX = player.x();
        double previousY = player.y();
        player.update(movementInput, deltaTime, tileMap, sneaking ? SNEAK_FACTOR : 1.0);
        GridPosition playerTile = gridPositionAt(player.centerX(), player.centerY());
        pickUpCard(playerTile);
        if (playerTile.equals(currentMap.exitPosition())) {
            if (currentMap.number() == LaboratoryMap.values().length) {
                escapeCompleted = true;
            } else {
                loadMap(LaboratoryMap.fromNumber(currentMap.number() + 1));
            }
            return;
        }
        MovementInput actualMovement = new MovementInput(player.x() - previousX, player.y() - previousY);
        makeFootsteps(deltaTime, actualMovement, sneaking);

        Surroundings surroundings = new Surroundings(time, player.centerX(), player.centerY(), playerTile,
                actualMovement, List.copyOf(newNoises), () -> nextEventId++);
        for (Enemy enemy : enemies) {
            if (mode == AiMode.FULL_AI) {
                enemy.updateWithAi(deltaTime, surroundings);
            } else {
                enemy.update(deltaTime, playerTile, actualMovement);
            }
            checkContact(enemy);
        }
        if (mode == AiMode.FULL_AI) {
            director.update(deltaTime, time, score, nearThreatFraction());
            team.setMaxAttackers(director.attackBudget());
        }
        captured = score == 0;
    }

    private void pickUpCard(GridPosition playerTile) {
        Optional<GridPosition> card = currentMap.cardPosition();
        if (card.isEmpty() || !card.get().equals(playerTile) || !tileMap.openDoors()) {
            return;
        }
        // Pegar o cartão faz barulho e as portas se abrem: os robôs refazem as rotas.
        emitNoise(tileCenter(card.get()), CARD_NOISE_TILES, NoiseSource.CARD_PICKUP);
        for (GridPosition door : doorPositions()) {
            emitNoise(tileCenter(door), DOOR_NOISE_TILES, NoiseSource.DOOR);
        }
        enemies.forEach(Enemy::onMapChanged);
    }

    private void makeFootsteps(double deltaTime, MovementInput movement, boolean sneaking) {
        if (movement.horizontal() == 0.0 && movement.vertical() == 0.0) {
            stepTimer = 0.0;
            return;
        }
        stepTimer += deltaTime;
        if (stepTimer >= STEP_INTERVAL) {
            stepTimer = 0.0;
            emitNoise(new double[] {player.centerX(), player.centerY()},
                    sneaking ? SNEAK_NOISE_TILES : STEP_NOISE_TILES, NoiseSource.FOOTSTEP);
        }
    }

    private void emitNoise(double[] point, double radiusTiles, NoiseSource source) {
        NoiseEvent noise = new NoiseEvent(nextEventId++, point[0], point[1],
                radiusTiles * tileMap.tileSize(), time, source);
        newNoises.add(noise);
        recentNoises.add(noise);
    }

    private void checkContact(Enemy enemy) {
        AutonomousAgent agent = enemy.agent();
        boolean touching = player.x() < agent.x() + agent.size()
                && player.x() + player.size() > agent.x()
                && player.y() < agent.y() + agent.size()
                && player.y() + player.size() > agent.y();
        if (touching && touchingEnemies.add(enemy.type()) && !playerProtected()) {
            lastHitTime = time;
            score = Math.max(0, score - CONTACT_PENALTY);
            penalties++;
            enemy.onPlayerContact();
            director.recordContact(time);
        } else if (!touching) {
            touchingEnemies.remove(enemy.type());
        }
    }

    // Usado só pelo diretor, que pode saber onde o jogador está; os robôs não recebem isso.
    private double nearThreatFraction() {
        int near = 0;
        for (Enemy enemy : enemies) {
            double distance = Math.hypot(enemy.agent().centerX() - player.centerX(),
                    enemy.agent().centerY() - player.centerY()) / tileMap.tileSize();
            if (distance <= NEAR_THREAT_TILES) {
                near++;
            }
        }
        return (double) near / enemies.size();
    }

    private Set<GridPosition> reachableFrom(GridPosition start) {
        Set<GridPosition> visited = new HashSet<>();
        Queue<GridPosition> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!queue.isEmpty()) {
            GridPosition current = queue.remove();
            for (int[] direction : directions) {
                GridPosition next = new GridPosition(current.row() + direction[0], current.column() + direction[1]);
                if (tileMap.isWalkable(next.row(), next.column()) && visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    private GridPosition nearestFloor(GridPosition preferred, Set<GridPosition> allowed,
            List<GridPosition> excluded) {
        return allowed.stream()
                .filter(point -> tileMap.tileAt(point.row(), point.column()) == TileType.LAB_FLOOR)
                .filter(point -> !excluded.contains(point))
                .min(Comparator.comparingInt((GridPosition point) -> preferred.manhattanDistanceTo(point))
                        .thenComparingInt(GridPosition::row)
                        .thenComparingInt(GridPosition::column))
                .orElseThrow();
    }

    private List<GridPosition> doorPositions() {
        List<GridPosition> doors = new ArrayList<>();
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.tileAt(row, column) == TileType.DOOR) {
                    doors.add(new GridPosition(row, column));
                }
            }
        }
        return doors;
    }

    private double[] tileCenter(GridPosition position) {
        double size = tileMap.tileSize();
        return new double[] {(position.column() + 0.5) * size, (position.row() + 0.5) * size};
    }

    public Player player() { return player; }
    public TileMap tileMap() { return tileMap; }
    public List<Enemy> enemies() { return enemies; }
    // O painel principal acompanha o primeiro robô (perseguidor/guarda).
    public List<GridPosition> navigationPath() { return autonomousAgent().path(); }
    public GridPosition navigationDestination() { return enemies.getFirst().destination(); }
    public NavigationStatus navigationStatus() { return enemies.getFirst().navigationStatus(); }
    public AutonomousAgent autonomousAgent() { return enemies.getFirst().agent(); }
    public LaboratoryMap currentMap() { return currentMap; }
    public boolean escapeCompleted() { return escapeCompleted; }
    public boolean captured() { return captured; }
    public int score() { return score; }
    public int penalties() { return penalties; }
    public AiMode mode() { return mode; }
    /** Verdadeiro nos segundos seguintes a um toque, quando nenhum robô desconta pontos. */
    public boolean playerProtected() { return time - lastHitTime < difficulty.hitProtection(); }
    public Difficulty difficulty() { return difficulty; }
    public double time() { return time; }
    public TeamBlackboard team() { return team; }
    public AiDirector director() { return director; }
    public DecisionLog decisions() { return decisions; }
    public List<NoiseEvent> recentNoises() { return List.copyOf(recentNoises); }
    /** Tile do cartão de acesso do mapa atual, se houver. */
    public Optional<GridPosition> cardPosition() { return currentMap.cardPosition(); }
    /** Verdadeiro depois que o jogador pegou o cartão deste mapa. */
    public boolean cardCollected() { return tileMap.doorsOpen(); }

    private GridPosition gridPositionAt(double x, double y) {
        return new GridPosition((int) Math.floor(y / tileMap.tileSize()),
                (int) Math.floor(x / tileMap.tileSize()));
    }
}
