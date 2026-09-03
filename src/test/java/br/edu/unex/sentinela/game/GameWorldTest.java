package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.navigation.Pathfinder;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameWorldTest {

    private static final double EPSILON = 1.0e-9;
    private static final double PLAYER_TEST_SPEED = 230.0;

    @Test
    void startsPlayerAtConfiguredWalkableTile() {
        GameWorld world = new GameWorld();
        TileMap tileMap = world.tileMap();

        assertEquals(
                tileMap.playerStartColumn(),
                (int) Math.floor(world.player().centerX() / tileMap.tileSize())
        );
        assertEquals(
                tileMap.playerStartRow(),
                (int) Math.floor(world.player().centerY() / tileMap.tileSize())
        );
        assertTrue(tileMap.canOccupy(
                world.player().x(),
                world.player().y(),
                world.player().size(),
                world.player().size()
        ));
    }

    @Test
    void startsWithFirstLaboratoryMap() {
        GameWorld world = new GameWorld();

        assertEquals(LaboratoryMap.ESCAPE_ROUTE, world.currentMap());
    }

    @Test
    void rejectsNullPathfinder() {
        assertThrows(NullPointerException.class, () -> new GameWorld(null));
    }

    @Test
    void createsValidRouteToInitialPlayerTile() {
        GameWorld world = new GameWorld();
        List<GridPosition> path = world.navigationPath();
        GridPosition initialPlayerTile = new GridPosition(
                world.tileMap().playerStartRow(),
                world.tileMap().playerStartColumn()
        );

        assertFalse(path.isEmpty());
        assertEquals(new GridPosition(13, 1), path.get(0));
        assertEquals(initialPlayerTile, path.get(path.size() - 1));
        assertEquals(initialPlayerTile, world.navigationDestination());
        assertEquals(NavigationStatus.MOVING, world.navigationStatus());
        assertFalse(world.autonomousAgent().hasReachedDestination());
        assertTrue(world.tileMap().canOccupy(
                world.autonomousAgent().x(),
                world.autonomousAgent().y(),
                world.autonomousAgent().size(),
                world.autonomousAgent().size()
        ));
    }

    @Test
    void autonomousAgentStopsAtPlayerTile() {
        GameWorld world = new GameWorld();

        for (int frame = 0;
                frame < 1_000 && world.navigationStatus() == NavigationStatus.MOVING;
                frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
            assertTrue(world.tileMap().canOccupy(
                    world.autonomousAgent().x(),
                    world.autonomousAgent().y(),
                    world.autonomousAgent().size(),
                    world.autonomousAgent().size()
            ));
        }

        double destinationX = world.autonomousAgent().x();
        double destinationY = world.autonomousAgent().y();
        world.update(1.0, MovementInput.NONE);
        GridPosition destination = world.navigationDestination();
        double expectedX = destination.column() * world.tileMap().tileSize()
                + (world.tileMap().tileSize() - world.autonomousAgent().size()) / 2.0;
        double expectedY = destination.row() * world.tileMap().tileSize()
                + (world.tileMap().tileSize() - world.autonomousAgent().size()) / 2.0;

        assertEquals(NavigationStatus.DESTINATION_REACHED, world.navigationStatus());
        assertTrue(world.autonomousAgent().hasReachedDestination());
        assertEquals(expectedX, destinationX, EPSILON);
        assertEquals(expectedY, destinationY, EPSILON);
        assertEquals(destinationX, world.autonomousAgent().x(), EPSILON);
        assertEquals(destinationY, world.autonomousAgent().y(), EPSILON);
    }

    @Test
    void resumesWhenPlayerChangesTileAfterBeingReached() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);

        for (int frame = 0;
                frame < 1_000 && world.navigationStatus() == NavigationStatus.MOVING;
                frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
        }
        assertEquals(NavigationStatus.DESTINATION_REACHED, world.navigationStatus());
        assertEquals(1, pathfinder.calls());

        world.update(0.05, new MovementInput(1.0, 0.0));
        world.update(0.05, new MovementInput(1.0, 0.0));

        assertEquals(2, pathfinder.calls());
        assertEquals(new GridPosition(1, 5), world.navigationDestination());
        assertEquals(NavigationStatus.MOVING, world.navigationStatus());
        assertFalse(world.autonomousAgent().hasReachedDestination());
    }

    @Test
    void doesNotRecalculateWhilePlayerRemainsInSameTile() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);

        for (int frame = 0; frame < 120; frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
        }
        world.update(0.01, new MovementInput(1.0, 0.0));

        assertEquals(1, pathfinder.calls());
        assertEquals(new GridPosition(1, 4), world.navigationDestination());
    }

    @Test
    void recalculatesOnceWhenPlayerEntersAnotherTile() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);

        for (int frame = 0; frame < 3; frame++) {
            world.update(0.05, new MovementInput(1.0, 0.0));
        }
        assertEquals(1, pathfinder.calls());

        world.update(0.05, new MovementInput(1.0, 0.0));

        GridPosition newDestination = new GridPosition(1, 5);
        assertEquals(2, pathfinder.calls());
        assertEquals(newDestination, pathfinder.lastDestination());
        assertEquals(pathfinder.lastStart(), world.navigationPath().get(0));
        assertEquals(
                newDestination,
                world.navigationPath().get(world.navigationPath().size() - 1)
        );

        for (int frame = 0; frame < 120; frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
        }
        assertEquals(2, pathfinder.calls());
    }

    @Test
    void agentKeepsMovingWhilePlayerChangesDestination() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);
        double initialAgentY = world.autonomousAgent().y();

        for (int frame = 0; frame < 60; frame++) {
            world.update(1.0 / 60.0, new MovementInput(1.0, 0.0));
        }

        assertTrue(pathfinder.calls() >= 4);
        assertTrue(world.autonomousAgent().y() < initialAgentY - 60.0);
    }

    @Test
    void recalculatesFromCurrentAgentTile() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);

        world.update(0.80, MovementInput.NONE);
        world.update(0.05, new MovementInput(1.0, 0.0));
        GridPosition currentAgentTile = world.autonomousAgent().currentGridPosition();
        assertFalse(currentAgentTile.equals(new GridPosition(13, 1)));

        world.update(0.05, new MovementInput(1.0, 0.0));

        assertEquals(2, pathfinder.calls());
        assertEquals(currentAgentTile, pathfinder.lastStart());
        assertEquals(currentAgentTile, world.navigationPath().get(0));
    }

    @Test
    void stopsOnMissingRouteAndResumesAfterAnotherDestination() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);
        pathfinder.failNextSearch();

        for (int frame = 0; frame < 4; frame++) {
            world.update(0.05, new MovementInput(1.0, 0.0));
        }

        assertEquals(2, pathfinder.calls());
        assertEquals(NavigationStatus.NO_PATH, world.navigationStatus());
        assertTrue(world.navigationPath().isEmpty());
        assertFalse(world.autonomousAgent().hasReachedDestination());
        double stoppedX = world.autonomousAgent().x();
        double stoppedY = world.autonomousAgent().y();

        world.update(0.20, MovementInput.NONE);

        assertEquals(2, pathfinder.calls());
        assertEquals(stoppedX, world.autonomousAgent().x(), EPSILON);
        assertEquals(stoppedY, world.autonomousAgent().y(), EPSILON);

        for (int frame = 0; frame < 4; frame++) {
            world.update(0.05, new MovementInput(-1.0, 0.0));
        }

        assertEquals(3, pathfinder.calls());
        assertFalse(world.navigationPath().isEmpty());
        assertEquals(NavigationStatus.MOVING, world.navigationStatus());
        assertEquals(new GridPosition(1, 4), pathfinder.lastDestination());
    }

    @Test
    void changesLaboratoryAndRecreatesAValidInitialRoute() {
        GameWorld world = new GameWorld();

        world.selectMap(2);

        assertEquals(LaboratoryMap.DATA_CORE, world.currentMap());
        assertEquals(LaboratoryMap.DATA_CORE.tileMap(), world.tileMap());
        assertEquals(LaboratoryMap.DATA_CORE.agentStart(), world.navigationPath().get(0));
        assertEquals(
                new GridPosition(
                        world.tileMap().playerStartRow(),
                        world.tileMap().playerStartColumn()
                ),
                world.navigationDestination()
        );
        assertFalse(world.navigationPath().isEmpty());
    }

    @Test
    void doesNotReloadLaboratoryThatIsAlreadyActive() {
        RecordingPathfinder pathfinder = new RecordingPathfinder();
        GameWorld world = new GameWorld(pathfinder);

        world.selectMap(1);

        assertEquals(LaboratoryMap.ESCAPE_ROUTE, world.currentMap());
        assertEquals(1, pathfinder.calls());
    }

    @Test
    void loadsNextLaboratoryWhenPlayerReachesExit() {
        GameWorld world = new GameWorld();

        movePlayerToExit(world);

        assertEquals(LaboratoryMap.DATA_CORE, world.currentMap());
        assertFalse(world.escapeCompleted());
    }

    @Test
    void finishesEscapeAtExitOfThirdLaboratory() {
        GameWorld world = new GameWorld();
        world.selectMap(3);

        movePlayerToExit(world);
        double playerX = world.player().x();
        double playerY = world.player().y();
        world.update(1.0, new MovementInput(-1.0, 0.0));

        assertEquals(LaboratoryMap.CONTAINMENT, world.currentMap());
        assertTrue(world.escapeCompleted());
        assertEquals(playerX, world.player().x(), EPSILON);
        assertEquals(playerY, world.player().y(), EPSILON);

        world.selectMap(1);
        assertFalse(world.escapeCompleted());
    }

    private static void movePlayerToExit(GameWorld world) {
        LaboratoryMap expectedMap = world.currentMap();
        GridPosition start = playerPosition(world);
        List<GridPosition> route = new AStarPathfinder().findPath(
                world.tileMap(),
                start,
                expectedMap.exitPosition()
        );

        assertFalse(route.isEmpty());
        for (int index = 1; index < route.size() && world.currentMap() == expectedMap; index++) {
            movePlayerToWaypoint(world, expectedMap, route.get(index));
        }
    }

    private static void movePlayerToWaypoint(
            GameWorld world,
            LaboratoryMap expectedMap,
            GridPosition waypoint
    ) {
        double targetX = (waypoint.column() + 0.5) * world.tileMap().tileSize();
        double targetY = (waypoint.row() + 0.5) * world.tileMap().tileSize();

        for (int frame = 0; frame < 100 && world.currentMap() == expectedMap; frame++) {
            double distanceX = targetX - world.player().centerX();
            double distanceY = targetY - world.player().centerY();
            if (Math.abs(distanceX) <= EPSILON && Math.abs(distanceY) <= EPSILON) {
                return;
            }

            double distance = Math.abs(distanceX) + Math.abs(distanceY);
            double deltaTime = Math.min(1.0 / 60.0, distance / PLAYER_TEST_SPEED);
            world.update(
                    deltaTime,
                    new MovementInput(Math.signum(distanceX), Math.signum(distanceY))
            );
        }
    }

    private static GridPosition playerPosition(GameWorld world) {
        return new GridPosition(
                (int) Math.floor(world.player().centerY() / world.tileMap().tileSize()),
                (int) Math.floor(world.player().centerX() / world.tileMap().tileSize())
        );
    }

    private static final class RecordingPathfinder implements Pathfinder {

        private final AStarPathfinder delegate = new AStarPathfinder();
        private int calls;
        private boolean failNextSearch;
        private GridPosition lastStart;
        private GridPosition lastDestination;

        @Override
        public List<GridPosition> findPath(
                TileMap tileMap,
                GridPosition start,
                GridPosition destination
        ) {
            calls++;
            lastStart = start;
            lastDestination = destination;
            if (failNextSearch) {
                failNextSearch = false;
                return List.of();
            }
            return delegate.findPath(tileMap, start, destination);
        }

        int calls() {
            return calls;
        }

        GridPosition lastStart() {
            return lastStart;
        }

        GridPosition lastDestination() {
            return lastDestination;
        }

        void failNextSearch() {
            failNextSearch = true;
        }
    }
}
