package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.ai.Brain;
import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.ai.bt.BehaviorTree;
import br.edu.unex.sentinela.ai.bt.ScoutTree;
import br.edu.unex.sentinela.ai.coop.TeamBlackboard;
import br.edu.unex.sentinela.ai.fsm.GuardState;
import br.edu.unex.sentinela.ai.fsm.PatrolState;
import br.edu.unex.sentinela.ai.fsm.SearchState;
import br.edu.unex.sentinela.ai.fsm.StateMachine;
import br.edu.unex.sentinela.ai.memory.AgentMemory;
import br.edu.unex.sentinela.ai.memory.Evidence;
import br.edu.unex.sentinela.ai.memory.EvidenceOrigin;
import br.edu.unex.sentinela.ai.memory.VisualContact;
import br.edu.unex.sentinela.ai.perception.HearingSensor;
import br.edu.unex.sentinela.ai.perception.NoiseEvent;
import br.edu.unex.sentinela.ai.perception.VisionSensor;
import br.edu.unex.sentinela.ai.utility.ScoredPoint;
import br.edu.unex.sentinela.game.NavigationStatus;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import java.util.List;
import java.util.Objects;

/**
 * Robô de segurança. No modo de IA ele percebe o jogador pela visão e pela
 * audição, guarda o que percebeu na memória e deixa a FSM ou a árvore de
 * comportamento escolher o destino; o A* calcula como chegar.
 *
 * <p>No modo de navegação (atividades de A*), o destino vem direto do tipo do
 * robô, como nas entregas anteriores.
 */
public final class Enemy implements Robot {
    private static final double RECALCULATION_INTERVAL = 0.20;
    private static final double RUN_FACTOR = 1.6;
    private static final double LOOK_SWEEP = Math.toRadians(60.0);
    private static final double LOOK_SPEED = 1.6;
    private static final double REPORT_INTERVAL = 1.0;
    private static final double HEARING_CONFIDENCE = 0.7;
    private static final double RADIO_CONFIDENCE = 0.6;
    private static final double REPORT_CONFIDENCE = 0.9;

    private final EnemyType type;
    private final TileMap tileMap;
    private final Pathfinder pathfinder;
    private final Pathfinder costFinder;
    private final AutonomousAgent agent;
    private final List<GridPosition> patrolPoints;
    private GridPosition destination;
    private int patrolIndex;
    private double elapsed;

    private GridPosition post;
    private GridPosition exitWatchPoint;
    private AgentMemory memory;
    private VisionSensor vision;
    private HearingSensor hearing;
    private TeamBlackboard team;
    private Brain brain;
    private double now;
    private double facing = Math.PI / 2.0;
    private double baseFacing = Math.PI / 2.0;
    private double lookTime;
    private double reportTimer = REPORT_INTERVAL;
    private boolean routeFailed;
    private boolean touched;
    private boolean seesPlayer;
    private MovementInput seenMovement = MovementInput.NONE;
    private GridPosition pendingTarget;
    private boolean pendingStop;
    private NoiseEvent lastHeard;
    private double speedFactor = 1.0;

    public Enemy(EnemyType type, TileMap tileMap, GridPosition start,
            GridPosition playerPosition, List<GridPosition> patrolPoints, Pathfinder pathfinder) {
        this.type = Objects.requireNonNull(type);
        this.tileMap = Objects.requireNonNull(tileMap);
        this.pathfinder = Objects.requireNonNull(pathfinder);
        this.costFinder = type.jumpsOverEquipment() ? AStarPathfinder.withEquipmentJumps() : new AStarPathfinder();
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
                28.0, type.speed(), type.jumpsOverEquipment());
        if (path.isEmpty()) { agent.stop(); }
    }

    private Enemy(EnemyType type, TileMap tileMap, GridPosition start, List<GridPosition> routePoints,
            Pathfinder pathfinder) {
        this.type = type;
        this.tileMap = tileMap;
        this.pathfinder = pathfinder;
        this.costFinder = type.jumpsOverEquipment() ? AStarPathfinder.withEquipmentJumps() : new AStarPathfinder();
        this.patrolPoints = List.copyOf(routePoints);
        for (GridPosition point : this.patrolPoints) {
            if (!tileMap.isWalkable(point.row(), point.column())) {
                throw new IllegalArgumentException("Ponto da rotina bloqueado");
            }
        }
        destination = start;
        agent = new AutonomousAgent(tileMap, List.of(start), 28.0, type.speed(), type.jumpsOverEquipment());
    }

    /** Cria um robô com percepção, memória e decisão (FSM ou árvore de comportamento). */
    public static Enemy withAi(EnemyType type, TileMap tileMap, GridPosition start,
            List<GridPosition> routePoints, GridPosition exitWatchPoint, Pathfinder pathfinder,
            TeamBlackboard team, DecisionLog log) {
        if (routePoints.isEmpty()) {
            throw new IllegalArgumentException("A rotina precisa de pelo menos um ponto");
        }
        Enemy enemy = new Enemy(type, tileMap, start, routePoints, pathfinder);
        enemy.post = start;
        enemy.exitWatchPoint = exitWatchPoint;
        enemy.team = team;
        enemy.memory = new AgentMemory(type.displayName());
        enemy.vision = new VisionSensor(type.visionRangeTiles(), type.fieldOfViewDegrees());
        enemy.hearing = new HearingSensor(type.hearingSensitivity());
        enemy.brain = type.usesBehaviorTree()
                ? new BehaviorTree(enemy, ScoutTree.build(), log)
                : new StateMachine(enemy, type.guardsPost() ? new GuardState() : new PatrolState(), log);
        return enemy;
    }

    /** Ajuste do nível de dificuldade: velocidade e alcance da visão. */
    public void tune(double speedFactor, double visionFactor) {
        this.speedFactor = speedFactor;
        agent.setSpeedFactor(speedFactor);
        if (vision != null) {
            vision = new VisionSensor(type.visionRangeTiles() * visionFactor, type.fieldOfViewDegrees());
        }
    }

    /** Modo de navegação: destino definido pelo tipo, sem percepção. */
    public void update(double deltaTime, GridPosition playerPosition, MovementInput movement) {
        validateDelta(deltaTime);
        elapsed = Math.min(RECALCULATION_INTERVAL, elapsed + deltaTime);
        boolean patrolArrived = type == EnemyType.PATROLLER && agent.hasReachedDestination();
        if (patrolArrived && elapsed >= RECALCULATION_INTERVAL) {
            patrolIndex = (patrolIndex + 1) % patrolPoints.size();
        }
        GridPosition target = switch (type) {
            case CHASER, JUMPER -> playerPosition;
            case PATROLLER -> patrolPoints.get(patrolIndex);
            case INTERCEPTOR -> interceptDestination(tileMap, playerPosition, movement);
        };
        // Durante um salto o robô está sobre um equipamento; a nova busca
        // espera o pouso para partir de um tile transitável.
        if (elapsed >= RECALCULATION_INTERVAL
                && !agent.isJumping()
                && (!target.equals(destination) || patrolArrived)) {
            List<GridPosition> path = pathfinder.findPath(tileMap, agent.currentGridPosition(), target);
            destination = target;
            elapsed = 0.0;
            if (path.isEmpty()) { agent.stop(); } else { agent.replacePath(path); }
        }
        agent.update(deltaTime);
    }

    /** Modo de IA: perceber, decidir e depois se mover. */
    public void updateWithAi(double deltaTime, Surroundings surroundings) {
        validateDelta(deltaTime);
        if (brain == null) {
            throw new IllegalStateException("Este robô foi criado sem IA");
        }
        now = surroundings.now();
        perceive(deltaTime, surroundings);
        brain.update(deltaTime);
        double previousX = agent.x();
        double previousY = agent.y();
        agent.update(deltaTime);
        if (!agent.isJumping()) {
            applyPendingOrder();
        }
        // Em combate o robô continua olhando para o jogador enquanto corre.
        if (!(seesPlayer && memory.inCombat())) {
            turnWithMovement(agent.x() - previousX, agent.y() - previousY);
        }
    }

    private void perceive(double deltaTime, Surroundings surroundings) {
        seesPlayer = vision.canSee(tileMap, agent.centerX(), agent.centerY(), facing,
                surroundings.playerX(), surroundings.playerY());
        VisualContact contact = null;
        if (seesPlayer) {
            double distance = VisionSensor.distanceTiles(tileMap, agent.centerX(), agent.centerY(),
                    surroundings.playerX(), surroundings.playerY());
            contact = new VisualContact(surroundings.playerTile(), distance);
            seenMovement = surroundings.playerMovement();
        }
        memory.update(deltaTime, now, contact);

        if (seesPlayer && memory.inCombat()) {
            facing = Math.atan2(surroundings.playerY() - agent.centerY(), surroundings.playerX() - agent.centerX());
            baseFacing = facing;
            reportTimer += deltaTime;
            if (reportTimer >= REPORT_INTERVAL) {
                reportTimer = 0.0;
                team.report(new Evidence(surroundings.eventIds().getAsLong(), surroundings.playerTile(), now,
                        EvidenceOrigin.VISION, REPORT_CONFIDENCE, name()), now);
            }
        } else {
            reportTimer = REPORT_INTERVAL;
        }

        for (NoiseEvent noise : surroundings.noises()) {
            if (hearing.hears(noise, agent.centerX(), agent.centerY())) {
                lastHeard = noise;
                memory.hear(new Evidence(noise.id(), tileAt(noise.x(), noise.y()), noise.time(),
                        EvidenceOrigin.HEARING, HEARING_CONFIDENCE, name()));
            }
        }
        // Pelo rádio chega só a posição relatada, nunca a posição atual do jogador.
        for (Evidence report : team.deliveries(name(), now)) {
            memory.hear(report.relayedAs(EvidenceOrigin.RADIO, RADIO_CONFIDENCE));
        }
    }

    private void turnWithMovement(double movedX, double movedY) {
        if (Math.hypot(movedX, movedY) > 1.0e-6) {
            facing = Math.atan2(movedY, movedX);
            baseFacing = facing;
            lookTime = 0.0;
        }
    }

    private void applyPendingOrder() {
        if (pendingStop) {
            pendingStop = false;
            agent.stop();
        }
        if (pendingTarget != null) {
            GridPosition target = pendingTarget;
            pendingTarget = null;
            goTo(target);
        }
    }

    // Robot: pedidos da FSM e da árvore de comportamento.

    @Override
    public void goTo(GridPosition target) {
        destination = target;
        if (agent.isJumping()) {
            pendingTarget = target;
            return;
        }
        GridPosition start = agent.currentGridPosition();
        List<GridPosition> path = tileMap.isWalkable(target.row(), target.column())
                ? pathfinder.findPath(tileMap, start, target)
                : List.of();
        routeFailed = path.isEmpty();
        if (routeFailed) {
            agent.stop();
        } else {
            agent.replacePath(path);
        }
    }

    @Override
    public boolean arrived() {
        return !routeFailed && pendingTarget == null && agent.hasReachedDestination();
    }

    @Override
    public boolean routeFailed() {
        return routeFailed;
    }

    @Override
    public void stopMoving() {
        pendingTarget = null;
        if (agent.isJumping()) {
            pendingStop = true;
        } else {
            agent.stop();
        }
    }

    @Override
    public int pathCost(GridPosition target) {
        if (!tileMap.isWalkable(target.row(), target.column()) || agent.isJumping()) {
            return -1;
        }
        List<GridPosition> path = costFinder.findPath(tileMap, agent.currentGridPosition(), target);
        return path.isEmpty() ? -1 : AStarPathfinder.costOf(path);
    }

    @Override
    public void faceTowards(GridPosition target) {
        double targetX = (target.column() + 0.5) * tileMap.tileSize();
        double targetY = (target.row() + 0.5) * tileMap.tileSize();
        facing = Math.atan2(targetY - agent.centerY(), targetX - agent.centerX());
        baseFacing = facing;
        lookTime = 0.0;
    }

    @Override
    public void lookAround(double deltaTime) {
        lookTime += deltaTime;
        facing = baseFacing + Math.sin(lookTime * LOOK_SPEED) * LOOK_SWEEP;
    }

    @Override
    public void setRunning(boolean running) {
        agent.setSpeedFactor((running ? RUN_FACTOR : 1.0) * speedFactor);
    }

    @Override
    public GridPosition chaseTarget() {
        GridPosition seen = memory.lastSeenPosition().orElse(tile());
        // O batedor usa o movimento que viu para cortar o caminho do jogador.
        if (type == EnemyType.INTERCEPTOR && seesPlayer) {
            return interceptDestination(tileMap, seen, seenMovement);
        }
        return seen;
    }

    /** O mapa mudou (porta aberta): refaz a rota atual para aproveitar o novo caminho. */
    public void onMapChanged() {
        if (brain != null && destination != null && !arrived()) {
            goTo(destination);
        }
    }

    public void onPlayerContact() {
        touched = true;
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

    private GridPosition tileAt(double x, double y) {
        return new GridPosition((int) Math.floor(y / tileMap.tileSize()), (int) Math.floor(x / tileMap.tileSize()));
    }

    private static void validateDelta(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("Delta time deve ser finito e não negativo");
        }
    }

    @Override public String name() { return type.displayName(); }
    @Override public double now() { return now; }
    @Override public TileMap map() { return tileMap; }
    @Override public AgentMemory memory() { return memory; }
    @Override public TeamBlackboard team() { return team; }
    @Override public GridPosition tile() { return agent.currentGridPosition(); }
    @Override public GridPosition destination() { return destination; }
    @Override public boolean guardsPost() { return type.guardsPost(); }
    @Override public GridPosition post() { return post; }
    @Override public List<GridPosition> routePoints() { return patrolPoints; }
    @Override public GridPosition exitWatchPoint() { return exitWatchPoint; }
    @Override public boolean touchedPlayer() { return touched; }
    @Override public void clearTouch() { touched = false; }

    public EnemyType type() { return type; }
    public AutonomousAgent agent() { return agent; }
    public boolean hasAi() { return brain != null; }
    public Brain brain() { return brain; }
    public VisionSensor vision() { return vision; }
    public HearingSensor hearing() { return hearing; }
    public double facing() { return facing; }
    public boolean seesPlayer() { return seesPlayer; }
    public NoiseEvent lastHeard() { return lastHeard; }

    /** Notas da Utility AI quando o robô está procurando o jogador. */
    public List<ScoredPoint> searchRanking() {
        if (brain instanceof StateMachine machine && machine.current() instanceof SearchState search) {
            return search.ranking();
        }
        return List.of();
    }

    public AStarPathfinder.SearchStats lastSearch() {
        return pathfinder instanceof AStarPathfinder aStar ? aStar.lastSearch() : AStarPathfinder.SearchStats.NONE;
    }

    public NavigationStatus navigationStatus() {
        if (agent.path().isEmpty()) { return NavigationStatus.NO_PATH; }
        return agent.hasReachedDestination() ? NavigationStatus.DESTINATION_REACHED : NavigationStatus.MOVING;
    }
}
