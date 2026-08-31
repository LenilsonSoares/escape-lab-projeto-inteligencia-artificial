package br.edu.unex.sentinela.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AStarPathfinderTest {

    private final AStarPathfinder pathfinder = new AStarPathfinder();

    @Test
    void findsStraightPathInStartToDestinationOrder() {
        TileMap tileMap = openMap(3, 5);
        GridPosition start = new GridPosition(1, 0);
        GridPosition destination = new GridPosition(1, 4);

        List<GridPosition> path = pathfinder.findPath(tileMap, start, destination);

        assertEquals(List.of(
                new GridPosition(1, 0),
                new GridPosition(1, 1),
                new GridPosition(1, 2),
                new GridPosition(1, 3),
                new GridPosition(1, 4)
        ), path);
        assertThrows(
                UnsupportedOperationException.class,
                () -> path.add(new GridPosition(0, 0))
        );
    }

    @Test
    void calculatesManhattanDistanceForOrthogonalMovement() {
        GridPosition start = new GridPosition(1, 2);
        GridPosition destination = new GridPosition(4, 6);

        assertEquals(7, start.manhattanDistanceTo(destination));
    }

    @Test
    void findsShortestDetourAroundBlockedTiles() {
        TileType floor = TileType.LAB_FLOOR;
        TileType wall = TileType.WALL;
        TileType equipment = TileType.EQUIPMENT;
        TileType[][] tiles = {
            {wall, wall, wall, wall, wall},
            {wall, floor, equipment, floor, wall},
            {wall, floor, wall, floor, wall},
            {wall, floor, floor, floor, wall},
            {wall, wall, wall, wall, wall}
        };
        TileMap tileMap = new TileMap(tiles, 40, 1, 1);

        List<GridPosition> path = pathfinder.findPath(
                tileMap,
                new GridPosition(1, 1),
                new GridPosition(1, 3)
        );

        assertEquals(List.of(
                new GridPosition(1, 1),
                new GridPosition(2, 1),
                new GridPosition(3, 1),
                new GridPosition(3, 2),
                new GridPosition(3, 3),
                new GridPosition(2, 3),
                new GridPosition(1, 3)
        ), path);
    }

    @Test
    void findsShortestValidPathInLaboratory() {
        TileMap tileMap = TileMap.createLaboratory();
        GridPosition start = new GridPosition(13, 1);
        GridPosition destination = new GridPosition(1, 13);

        List<GridPosition> path = pathfinder.findPath(tileMap, start, destination);

        assertEquals(25, path.size());
        assertEquals(start, path.get(0));
        assertEquals(destination, path.get(path.size() - 1));
        assertValidPath(tileMap, path);
    }

    @Test
    void returnsEmptyWhenDestinationIsUnreachable() {
        TileType floor = TileType.LAB_FLOOR;
        TileType wall = TileType.WALL;
        TileType[][] tiles = {
            {floor, wall, floor},
            {wall, wall, wall},
            {floor, floor, floor}
        };
        TileMap tileMap = new TileMap(tiles, 40, 0, 0);

        List<GridPosition> path = pathfinder.findPath(
                tileMap,
                new GridPosition(0, 0),
                new GridPosition(0, 2)
        );

        assertTrue(path.isEmpty());
    }

    @Test
    void returnsOnlyStartWhenItIsAlreadyTheDestination() {
        TileMap tileMap = openMap(2, 2);
        GridPosition position = new GridPosition(1, 1);

        List<GridPosition> path = pathfinder.findPath(tileMap, position, position);

        assertEquals(List.of(position), path);
        assertThrows(UnsupportedOperationException.class, () -> path.add(position));
    }

    @Test
    void rejectsBlockedOrOutOfBoundsEndpoints() {
        TileMap tileMap = TileMap.createLaboratory();
        GridPosition floor = new GridPosition(1, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> pathfinder.findPath(tileMap, new GridPosition(0, 0), floor)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pathfinder.findPath(tileMap, floor, new GridPosition(2, 2))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pathfinder.findPath(tileMap, new GridPosition(-1, 1), floor)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> pathfinder.findPath(tileMap, floor, new GridPosition(tileMap.rows(), 1))
        );
    }

    private static void assertValidPath(TileMap tileMap, List<GridPosition> path) {
        for (int index = 0; index < path.size(); index++) {
            GridPosition position = path.get(index);
            assertTrue(tileMap.isWalkable(position.row(), position.column()));
            if (index > 0) {
                assertEquals(1, path.get(index - 1).manhattanDistanceTo(position));
            }
        }
    }

    private static TileMap openMap(int rows, int columns) {
        TileType[][] tiles = new TileType[rows][columns];
        for (TileType[] row : tiles) {
            Arrays.fill(row, TileType.LAB_FLOOR);
        }
        return new TileMap(tiles, 40, 0, 0);
    }
}
