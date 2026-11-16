package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.ai.perception.NoiseEvent;
import br.edu.unex.sentinela.ai.perception.NoiseSource;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameWorldAiTest {

    private static final double FRAME = 1.0 / 60.0;

    @Test
    void startsWithFullAiAndRobotsDoNotKnowWhereThePlayerIs() {
        GameWorld world = new GameWorld();
        GridPosition playerTile = new GridPosition(world.tileMap().playerStartRow(),
                world.tileMap().playerStartColumn());

        assertEquals(AiMode.FULL_AI, world.mode());
        assertTrue(world.enemies().stream().allMatch(Enemy::hasAi));
        assertTrue(world.enemies().stream().noneMatch(enemy -> playerTile.equals(enemy.destination())));
    }

    @Test
    void togglingTheModeSwitchesToTheNavigationDemoAndBack() {
        GameWorld world = new GameWorld();

        world.toggleMode();
        assertEquals(AiMode.NAVIGATION_DEMO, world.mode());
        assertTrue(world.enemies().stream().noneMatch(Enemy::hasAi));

        world.toggleMode();
        assertEquals(AiMode.FULL_AI, world.mode());
        assertTrue(world.enemies().stream().allMatch(Enemy::hasAi));
    }

    @Test
    void walkingMakesFootstepNoiseAndSneakingIsQuieterAndSlower() {
        GameWorld walking = new GameWorld();
        GameWorld sneaking = new GameWorld();
        double startX = walking.player().x();

        for (int frame = 0; frame < 30; frame++) {
            walking.update(FRAME, new MovementInput(1.0, 0.0), false);
            sneaking.update(FRAME, new MovementInput(1.0, 0.0), true);
        }

        NoiseEvent step = footstep(walking);
        NoiseEvent quietStep = footstep(sneaking);
        assertEquals(3.5 * 40, step.radius(), 1.0e-9);
        assertEquals(1.5 * 40, quietStep.radius(), 1.0e-9);
        assertEquals((walking.player().x() - startX) / 2.0, sneaking.player().x() - startX, 1.0e-6);
    }

    @Test
    void standingStillMakesNoNoise() {
        GameWorld world = new GameWorld();

        for (int frame = 0; frame < 60; frame++) {
            world.update(FRAME, MovementInput.NONE);
        }

        assertTrue(world.recentNoises().isEmpty());
    }

    @Test
    void reachingZeroPointsCapturesThePlayerAndRestartBeginsANewRun() {
        // Na demonstração do A* os robôs vão direto ao jogador; 100 pontos acabam no primeiro toque.
        GameWorld world = new GameWorld(new AStarPathfinder(), AiMode.NAVIGATION_DEMO, 100);

        for (int frame = 0; frame < 3_000 && !world.captured(); frame++) {
            world.update(FRAME, MovementInput.NONE);
        }
        assertTrue(world.captured());
        assertEquals(0, world.score());

        double x = world.player().x();
        world.update(1.0, new MovementInput(1.0, 0.0));
        assertEquals(x, world.player().x(), 1.0e-9);

        world.restart();
        assertFalse(world.captured());
        assertEquals(100, world.score());
        assertEquals(0, world.penalties());
        assertEquals(LaboratoryMap.ESCAPE_ROUTE, world.currentMap());
    }

    @Test
    void touchingAndReleasingOnTheRobotEdgeDoesNotDrainThePoints() {
        GameWorld world = new GameWorld(new AStarPathfinder(), AiMode.NAVIGATION_DEMO, 1_000);
        for (int frame = 0; frame < 3_000 && world.penalties() == 0; frame++) {
            world.update(FRAME, MovementInput.NONE);
        }
        assertEquals(1, world.penalties());

        // O jogador treme na borda do robô durante 1 segundo.
        for (int frame = 0; frame < 60; frame++) {
            world.update(FRAME, new MovementInput(frame % 2 == 0 ? 1.0 : -1.0, 0.0));
        }

        assertTrue(world.penalties() <= world.enemies().size());
    }

    @Test
    void afterATouchThePlayerIsProtectedForTwoSeconds() {
        GameWorld world = new GameWorld(new AStarPathfinder(), AiMode.NAVIGATION_DEMO, 1_000);
        for (int frame = 0; frame < 3_000 && world.penalties() == 0; frame++) {
            world.update(FRAME, MovementInput.NONE);
        }
        assertTrue(world.playerProtected());

        for (int frame = 0; frame < 110; frame++) {
            world.update(FRAME, new MovementInput(frame % 2 == 0 ? 1.0 : -1.0, 0.0));
        }
        assertEquals(1, world.penalties());

        // Passados os 2 s, a proteção acaba (ou um novo toque já começou outra).
        for (int frame = 0; frame < 30; frame++) {
            world.update(FRAME, MovementInput.NONE);
        }
        assertTrue(!world.playerProtected() || world.penalties() > 1);
    }

    @Test
    void cardPickupOpensTheDoorsAndMakesNoise() {
        GameWorld world = new GameWorld();
        GridPosition card = world.cardPosition().orElseThrow();
        GridPosition exit = world.currentMap().exitPosition();
        assertFalse(world.tileMap().isWalkable(exit.row() , exit.column() - 1));

        walkTo(world, card);

        assertTrue(world.cardCollected());
        assertTrue(world.tileMap().doorsOpen());
        List<NoiseSource> sources = world.recentNoises().stream().map(NoiseEvent::source).toList();
        assertTrue(sources.contains(NoiseSource.CARD_PICKUP));
        assertTrue(sources.contains(NoiseSource.DOOR));
    }

    @Test
    void eachMapStartsWithTheDoorsClosedAgain() {
        GameWorld world = new GameWorld();
        walkTo(world, world.cardPosition().orElseThrow());
        assertTrue(world.cardCollected());

        world.selectMap(2);
        world.selectMap(1);

        assertFalse(world.cardCollected());
    }

    private static NoiseEvent footstep(GameWorld world) {
        return world.recentNoises().stream()
                .filter(noise -> noise.source() == NoiseSource.FOOTSTEP)
                .findFirst().orElseThrow();
    }

    private static void walkTo(GameWorld world, GridPosition target) {
        int tile = world.tileMap().tileSize();
        GridPosition start = new GridPosition((int) (world.player().centerY() / tile),
                (int) (world.player().centerX() / tile));
        List<GridPosition> route = new AStarPathfinder().findPath(world.tileMap(), start, target);
        for (GridPosition waypoint : route) {
            double targetX = (waypoint.column() + 0.5) * tile;
            double targetY = (waypoint.row() + 0.5) * tile;
            for (int frame = 0; frame < 100; frame++) {
                double dx = targetX - world.player().centerX();
                double dy = targetY - world.player().centerY();
                if (Math.abs(dx) < 1.0e-9 && Math.abs(dy) < 1.0e-9) {
                    break;
                }
                double deltaTime = Math.min(FRAME, (Math.abs(dx) + Math.abs(dy)) / 230.0);
                world.update(deltaTime, new MovementInput(Math.signum(dx), Math.signum(dy)));
            }
        }
    }
}
