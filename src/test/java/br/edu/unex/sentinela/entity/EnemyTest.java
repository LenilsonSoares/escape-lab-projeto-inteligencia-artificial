package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {
    private TileMap map() {
        TileType[][] tiles = new TileType[5][8];
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                tiles[row][col] = row == 0 || row == 4 || col == 0 || col == 7
                        ? TileType.WALL : TileType.LAB_FLOOR;
            }
        }
        tiles[1][4] = TileType.WALL;
        return new TileMap(tiles, 40, 1, 1);
    }

    @Test
    void chaserTracksPlayerWithMinimumSearchInterval() {
        Enemy enemy = new Enemy(EnemyType.CHASER, map(), new GridPosition(3, 1),
                new GridPosition(1, 1), List.of(), new AStarPathfinder());
        enemy.update(0.1, new GridPosition(1, 2), MovementInput.NONE);
        assertEquals(new GridPosition(1, 1), enemy.destination());
        enemy.update(0.1, new GridPosition(1, 2), MovementInput.NONE);
        assertEquals(new GridPosition(1, 2), enemy.destination());
        assertEquals(enemy.destination(), enemy.agent().path().getLast());
    }

    @Test
    void patrollerCyclesThroughWaypointsIndependentlyOfPlayer() {
        List<GridPosition> points = List.of(new GridPosition(1, 1), new GridPosition(1, 3));
        Enemy enemy = new Enemy(EnemyType.PATROLLER, map(), new GridPosition(3, 1),
                new GridPosition(3, 6), points, new AStarPathfinder());
        Set<GridPosition> visited = new HashSet<>();
        int destinationChanges = 0;
        GridPosition previous = enemy.destination();
        for (int frame = 0; frame < 600; frame++) {
            enemy.update(1.0 / 60.0, new GridPosition(3, 6), MovementInput.NONE);
            visited.add(enemy.agent().currentGridPosition());
            if (!previous.equals(enemy.destination())) {
                destinationChanges++;
                previous = enemy.destination();
            }
        }
        assertTrue(visited.containsAll(points));
        assertTrue(destinationChanges >= 3);
        assertFalse(visited.contains(new GridPosition(1, 4)));
    }

    @Test
    void interceptorAimsTwoWalkableTilesAhead() {
        assertEquals(new GridPosition(2, 4), Enemy.interceptDestination(map(),
                new GridPosition(2, 2), new MovementInput(1, 0)));
        assertEquals(new GridPosition(2, 2), Enemy.interceptDestination(map(),
                new GridPosition(2, 2), MovementInput.NONE));
    }

    @Test
    void interceptorStopsPredictionBeforeWallsAndMapEdges() {
        assertEquals(new GridPosition(1, 3), Enemy.interceptDestination(map(),
                new GridPosition(1, 2), new MovementInput(1, 0)));
        assertEquals(new GridPosition(2, 1), Enemy.interceptDestination(map(),
                new GridPosition(2, 1), new MovementInput(-1, 0)));
    }

    @Test
    void interceptorUsesPredictedDestinationForItsPath() {
        Enemy enemy = new Enemy(EnemyType.INTERCEPTOR, map(), new GridPosition(3, 1),
                new GridPosition(2, 2), List.of(), new AStarPathfinder());
        enemy.update(0.2, new GridPosition(2, 2), new MovementInput(1, 0));
        assertEquals(new GridPosition(2, 4), enemy.destination());
        assertEquals(enemy.destination(), enemy.agent().path().getLast());
    }
}
