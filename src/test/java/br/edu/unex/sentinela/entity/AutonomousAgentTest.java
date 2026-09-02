package br.edu.unex.sentinela.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AutonomousAgentTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void startsCenteredAndFollowsWaypointsInOrder() {
        TileMap tileMap = openMap(4, 4);
        List<GridPosition> path = List.of(
                new GridPosition(2, 0),
                new GridPosition(2, 1),
                new GridPosition(1, 1),
                new GridPosition(1, 2)
        );
        AutonomousAgent agent = new AutonomousAgent(tileMap, path, 20.0, 40.0);

        assertPosition(agent, 10.0, 90.0);
        assertFalse(agent.hasReachedDestination());

        agent.update(1.0);
        assertPosition(agent, 50.0, 90.0);

        agent.update(1.0);
        assertPosition(agent, 50.0, 50.0);

        agent.update(1.0);
        assertPosition(agent, 90.0, 50.0);
        assertTrue(agent.hasReachedDestination());
    }

    @Test
    void staysAtDestinationAfterCompletingPath() {
        TileMap tileMap = openMap(3, 3);
        AutonomousAgent agent = new AutonomousAgent(
                tileMap,
                List.of(
                        new GridPosition(0, 0),
                        new GridPosition(0, 1),
                        new GridPosition(1, 1)
                ),
                20.0,
                40.0
        );

        agent.update(10.0);
        assertPosition(agent, 50.0, 50.0);
        double destinationX = agent.x();
        double destinationY = agent.y();
        agent.update(10.0);

        assertTrue(agent.hasReachedDestination());
        assertEquals(destinationX, agent.x(), EPSILON);
        assertEquals(destinationY, agent.y(), EPSILON);
        assertTrue(tileMap.canOccupy(agent.x(), agent.y(), agent.size(), agent.size()));
    }

    @Test
    void usesRemainingDistanceAfterReachingWaypoint() {
        TileMap tileMap = openMap(4, 4);
        AutonomousAgent agent = new AutonomousAgent(
                tileMap,
                List.of(
                        new GridPosition(2, 0),
                        new GridPosition(2, 1),
                        new GridPosition(1, 1)
                ),
                20.0,
                60.0
        );

        agent.update(1.0);

        assertPosition(agent, 50.0, 70.0);
        assertFalse(agent.hasReachedDestination());
    }

    @Test
    void replacesPathWithoutTeleporting() {
        TileMap tileMap = openMap(5, 5);
        AutonomousAgent agent = new AutonomousAgent(
                tileMap,
                List.of(
                        new GridPosition(2, 0),
                        new GridPosition(2, 1),
                        new GridPosition(2, 2)
                ),
                20.0,
                40.0
        );
        agent.update(0.5);
        double previousX = agent.x();
        double previousY = agent.y();
        List<GridPosition> newPath = List.of(
                new GridPosition(2, 1),
                new GridPosition(1, 1),
                new GridPosition(1, 2)
        );

        agent.replacePath(newPath);

        assertEquals(previousX, agent.x(), EPSILON);
        assertEquals(previousY, agent.y(), EPSILON);
        assertEquals(newPath, agent.path());
        assertFalse(agent.hasReachedDestination());

        agent.update(3.0);
        assertPosition(agent, 90.0, 50.0);
        assertTrue(agent.hasReachedDestination());
    }

    @Test
    void continuesCurrentSegmentWhenFirstStepRemainsTheSame() {
        TileMap tileMap = openMap(4, 4);
        AutonomousAgent agent = new AutonomousAgent(
                tileMap,
                List.of(
                        new GridPosition(2, 0),
                        new GridPosition(2, 1),
                        new GridPosition(2, 2)
                ),
                20.0,
                40.0
        );
        agent.update(0.25);
        double previousX = agent.x();
        double previousY = agent.y();

        agent.replacePath(List.of(
                new GridPosition(2, 0),
                new GridPosition(2, 1),
                new GridPosition(1, 1)
        ));
        agent.update(0.25);

        assertEquals(previousX + 10.0, agent.x(), EPSILON);
        assertEquals(previousY, agent.y(), EPSILON);
    }

    @Test
    void stopsAndResumesWithAnotherPath() {
        TileMap tileMap = openMap(3, 3);
        AutonomousAgent agent = new AutonomousAgent(
                tileMap,
                List.of(
                        new GridPosition(1, 0),
                        new GridPosition(1, 1)
                ),
                20.0,
                40.0
        );

        agent.stop();
        agent.update(2.0);

        assertTrue(agent.path().isEmpty());
        assertFalse(agent.hasReachedDestination());
        assertPosition(agent, 10.0, 50.0);

        agent.replacePath(List.of(
                new GridPosition(1, 0),
                new GridPosition(0, 0)
        ));
        agent.update(1.0);

        assertPosition(agent, 10.0, 10.0);
        assertTrue(agent.hasReachedDestination());
    }

    @Test
    void rejectsRoutesWithBlockedOrNonAdjacentPoints() {
        TileType floor = TileType.LAB_FLOOR;
        TileType wall = TileType.WALL;
        TileType[][] tiles = {
            {floor, wall, floor},
            {floor, floor, floor},
            {floor, floor, floor}
        };
        TileMap tileMap = new TileMap(tiles, 40, 0, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new AutonomousAgent(
                        tileMap,
                        List.of(new GridPosition(0, 0), new GridPosition(0, 1)),
                        20.0,
                        40.0
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new AutonomousAgent(
                        tileMap,
                        List.of(new GridPosition(0, 0), new GridPosition(0, 2)),
                        20.0,
                        40.0
                )
        );
    }

    @Test
    void rejectsNegativeDeltaTime() {
        TileMap tileMap = openMap(2, 2);
        AutonomousAgent agent = new AutonomousAgent(
                tileMap,
                List.of(new GridPosition(0, 0)),
                20.0,
                40.0
        );

        assertThrows(IllegalArgumentException.class, () -> agent.update(-0.01));
    }

    private static void assertPosition(AutonomousAgent agent, double x, double y) {
        assertEquals(x, agent.x(), EPSILON);
        assertEquals(y, agent.y(), EPSILON);
    }

    private static TileMap openMap(int rows, int columns) {
        TileType[][] tiles = new TileType[rows][columns];
        for (TileType[] row : tiles) {
            Arrays.fill(row, TileType.LAB_FLOOR);
        }
        return new TileMap(tiles, 40, 0, 0);
    }
}
