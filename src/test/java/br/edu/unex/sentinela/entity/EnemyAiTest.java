package br.edu.unex.sentinela.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.ai.coop.TeamBlackboard;
import br.edu.unex.sentinela.ai.memory.EvidenceOrigin;
import br.edu.unex.sentinela.ai.perception.NoiseEvent;
import br.edu.unex.sentinela.ai.perception.NoiseSource;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnemyAiTest {

    private static final double FRAME = 1.0 / 60.0;
    private static final int TILE = 40;

    private final TeamBlackboard team = new TeamBlackboard();
    private final DecisionLog log = new DecisionLog();
    private long nextId = 1;
    private double now;

    @Test
    void patrolSeesPlayerAheadAndStartsChase() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "##########");
        Enemy patrol = patrol(map, new GridPosition(1, 1), new GridPosition(1, 8));

        run(patrol, new GridPosition(1, 5), 3);

        assertEquals("PERSEGUIR", patrol.brain().currentBehavior());
        assertTrue(patrol.memory().inCombat());
        assertTrue(team.attackers().contains("PATRULHEIRO"));
    }

    @Test
    void wallHidesThePlayer() {
        TileMap map = mapOf(
                "##########",
                "#....#...#",
                "##########");
        Enemy patrol = patrol(map, new GridPosition(1, 1), new GridPosition(1, 4));

        run(patrol, new GridPosition(1, 7), 60);

        assertEquals("PATRULHAR", patrol.brain().currentBehavior());
        assertFalse(patrol.seesPlayer());
    }

    @Test
    void playerBehindTheRobotIsNotSeen() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "##########");
        Enemy patrol = patrol(map, new GridPosition(1, 3), new GridPosition(1, 8));

        run(patrol, new GridPosition(1, 1), 30);

        assertFalse(patrol.seesPlayer());
        assertEquals("PATRULHAR", patrol.brain().currentBehavior());
    }

    @Test
    void chaseCallsAStarAtMostTwiceASecond() {
        TileMap map = mapOf(
                "############",
                "#..........#",
                "############");
        CountingPathfinder pathfinder = new CountingPathfinder();
        Enemy patrol = Enemy.withAi(EnemyType.PATROLLER, map, new GridPosition(1, 1),
                List.of(new GridPosition(1, 1), new GridPosition(1, 10)), new GridPosition(1, 10),
                pathfinder, team, log);
        run(patrol, new GridPosition(1, 5), 3);
        assertEquals("PERSEGUIR", patrol.brain().currentBehavior());
        int callsAtChaseStart = pathfinder.calls;

        // O jogador muda de tile em todo quadro durante 1 segundo.
        for (int frame = 0; frame < 60; frame++) {
            int column = 4 + (frame / 6) % 3;
            run(patrol, new GridPosition(1, column), 1);
        }

        assertTrue(pathfinder.calls - callsAtChaseStart <= 2);
    }

    @Test
    void losingContactStartsSearchAndLaterReturnsToRoutine() {
        TileMap map = mapOf(
                "###########",
                "#.........#",
                "#.#######.#",
                "#.........#",
                "###########");
        Enemy patrol = patrol(map, new GridPosition(1, 1), new GridPosition(1, 9));
        GridPosition farAway = new GridPosition(30, 30);
        run(patrol, new GridPosition(1, 4), 3);
        assertEquals("PERSEGUIR", patrol.brain().currentBehavior());

        run(patrol, farAway, 2);
        assertEquals("PROCURAR", patrol.brain().currentBehavior());
        assertTrue(team.attackers().isEmpty());

        run(patrol, farAway, 60 * 14);
        assertTrue(List.of("RETORNAR", "PATRULHAR").contains(patrol.brain().currentBehavior()));
    }

    @Test
    void noisesMakeTheRobotInvestigateTheNoiseNotThePlayer() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "#.######.#",
                "#........#",
                "##########");
        Enemy patrol = patrol(map, new GridPosition(1, 1), new GridPosition(1, 2));
        GridPosition noiseTile = new GridPosition(1, 6);
        GridPosition realPlayer = new GridPosition(3, 8);

        for (int step = 0; step < 5; step++) {
            update(patrol, realPlayer, List.of(noiseAt(noiseTile, 6.0)));
        }

        assertEquals("INVESTIGAR", patrol.brain().currentBehavior());
        assertEquals(noiseTile, patrol.destination());
    }

    @Test
    void robotWithoutAttackVacancyGoesToWatchTheExit() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "##########");
        team.setMaxAttackers(1);
        team.requestAttackPermit("OUTRO ROBÔ");
        Enemy patrol = patrol(map, new GridPosition(1, 1), new GridPosition(1, 8));

        run(patrol, new GridPosition(1, 5), 3);

        assertEquals("CERCAR SAÍDA", patrol.brain().currentBehavior());
        assertEquals(new GridPosition(1, 8), patrol.destination());
    }

    @Test
    void touchingThePlayerMakesTheRobotStopForAMoment() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "##########");
        Enemy patrol = patrol(map, new GridPosition(1, 1), new GridPosition(1, 8));
        run(patrol, new GridPosition(1, 4), 3);

        patrol.onPlayerContact();
        run(patrol, new GridPosition(1, 4), 1);

        assertEquals("ATACAR", patrol.brain().currentBehavior());
        assertTrue(patrol.agent().path().isEmpty());
        assertFalse(patrol.touchedPlayer());
    }

    @Test
    void radioReportArrivesWithDelayAndKeepsTheReportedPosition() {
        TileMap map = mapOf(
                "############",
                "#..........#",
                "#.########.#",
                "#..........#",
                "############");
        Enemy watcher = patrol(map, new GridPosition(1, 1), new GridPosition(1, 10));
        Enemy listener = Enemy.withAi(EnemyType.JUMPER, map, new GridPosition(3, 10),
                List.of(new GridPosition(3, 10), new GridPosition(3, 9)), new GridPosition(3, 1),
                AStarPathfinder.withEquipmentJumps(), team, log);
        GridPosition seenTile = new GridPosition(1, 4);

        for (int frame = 0; frame < 3; frame++) {
            now += FRAME;
            watcher.updateWithAi(FRAME, surroundings(seenTile, List.of()));
            listener.updateWithAi(FRAME, surroundings(seenTile, List.of()));
        }
        assertTrue(listener.memory().activeEvidence().isEmpty());

        for (int frame = 0; frame < 40; frame++) {
            now += FRAME;
            watcher.updateWithAi(FRAME, surroundings(seenTile, List.of()));
            listener.updateWithAi(FRAME, surroundings(seenTile, List.of()));
        }

        assertEquals(EvidenceOrigin.RADIO, listener.memory().activeEvidence().getFirst().origin());
        assertEquals(seenTile, listener.memory().activeEvidence().getFirst().position());
        assertFalse(listener.seesPlayer());
    }

    @Test
    void scoutTreeRoamsInterceptsAndRecovers() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "##########");
        Enemy scout = Enemy.withAi(EnemyType.INTERCEPTOR, map, new GridPosition(1, 1),
                List.of(new GridPosition(1, 1), new GridPosition(1, 8)), new GridPosition(1, 8),
                new AStarPathfinder(), team, log);
        update(scout, new GridPosition(30, 30), List.of());
        assertEquals("BT", scout.brain().technique());
        assertEquals("RONDAR", scout.brain().currentBehavior());

        run(scout, new GridPosition(1, 5), 3);
        assertEquals("INTERCEPTAR", scout.brain().currentBehavior());
        assertTrue(team.attackers().contains("INTERCEPTADOR"));

        scout.onPlayerContact();
        run(scout, new GridPosition(1, 5), 1);
        assertEquals("RECUAR", scout.brain().currentBehavior());
        assertFalse(team.attackers().contains("INTERCEPTADOR"));
    }

    @Test
    void guardStaysAtItsPostWithoutPerceivingAnything() {
        TileMap map = mapOf(
                "##########",
                "#........#",
                "#.######.#",
                "#........#",
                "##########");
        GridPosition post = new GridPosition(1, 1);
        Enemy guard = Enemy.withAi(EnemyType.CHASER, map, post, List.of(post), new GridPosition(3, 8),
                new AStarPathfinder(), team, log);

        run(guard, new GridPosition(3, 8), 120);

        assertEquals("VIGIAR", guard.brain().currentBehavior());
        assertEquals(post, guard.tile());
    }

    @Test
    void newRouteRequestedInTheAirWaitsForTheLanding() {
        TileMap map = mapOf(
                "#######",
                "#..E..#",
                "#######");
        Enemy jumper = Enemy.withAi(EnemyType.JUMPER, map, new GridPosition(1, 2),
                List.of(new GridPosition(1, 2), new GridPosition(1, 4)), new GridPosition(1, 5),
                AStarPathfinder.withEquipmentJumps(), team, log);
        GridPosition farAway = new GridPosition(30, 30);
        for (int frame = 0; frame < 120 && !jumper.agent().isJumping(); frame++) {
            update(jumper, farAway, List.of());
        }
        assertTrue(jumper.agent().isJumping());

        jumper.goTo(new GridPosition(1, 1));
        assertFalse(jumper.arrived());
        run(jumper, farAway, 120);

        assertFalse(jumper.agent().isJumping());
        GridPosition tile = jumper.tile();
        assertTrue(map.isWalkable(tile.row(), tile.column()));
    }

    private Enemy patrol(TileMap map, GridPosition start, GridPosition other) {
        return Enemy.withAi(EnemyType.PATROLLER, map, start, List.of(start, other), other,
                new AStarPathfinder(), team, log);
    }

    private void run(Enemy enemy, GridPosition player, int frames) {
        for (int frame = 0; frame < frames; frame++) {
            update(enemy, player, List.of());
        }
    }

    private void update(Enemy enemy, GridPosition player, List<NoiseEvent> noises) {
        now += FRAME;
        enemy.updateWithAi(FRAME, surroundings(player, noises));
    }

    private Surroundings surroundings(GridPosition player, List<NoiseEvent> noises) {
        return new Surroundings(now, (player.column() + 0.5) * TILE, (player.row() + 0.5) * TILE,
                player, MovementInput.NONE, noises, () -> nextId++);
    }

    private NoiseEvent noiseAt(GridPosition tile, double radiusTiles) {
        return new NoiseEvent(nextId++, (tile.column() + 0.5) * TILE, (tile.row() + 0.5) * TILE,
                radiusTiles * TILE, now, NoiseSource.FOOTSTEP);
    }

    static TileMap mapOf(String... rows) {
        TileType[][] tiles = new TileType[rows.length][rows[0].length()];
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < rows[row].length(); column++) {
                tiles[row][column] = switch (rows[row].charAt(column)) {
                    case '#' -> TileType.WALL;
                    case 'E' -> TileType.EQUIPMENT;
                    case 'D' -> TileType.DOOR;
                    default -> TileType.LAB_FLOOR;
                };
            }
        }
        return new TileMap(tiles, TILE, 1, 1);
    }

    private static final class CountingPathfinder implements Pathfinder {
        private final AStarPathfinder delegate = new AStarPathfinder();
        private int calls;

        @Override
        public List<GridPosition> findPath(TileMap tileMap, GridPosition start, GridPosition destination) {
            calls++;
            return delegate.findPath(tileMap, start, destination);
        }
    }
}
