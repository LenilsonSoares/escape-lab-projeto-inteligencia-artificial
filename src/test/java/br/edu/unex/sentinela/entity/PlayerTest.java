package br.edu.unex.sentinela.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.input.MovementInput;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class PlayerTest {

    private static final double EPSILON = 1.0e-9;
    private static final TileMap OPEN_MAP = createOpenMap();

    @Test
    void movesUsingSpeedAndDeltaTime() {
        Player player = new Player(10.0, 20.0, 20.0, 100.0);

        player.update(new MovementInput(1.0, 0.0), 0.5, OPEN_MAP);

        assertEquals(60.0, player.x(), EPSILON);
        assertEquals(20.0, player.y(), EPSILON);
    }

    @Test
    void movementIsIndependentFromNumberOfFrames() {
        Player singleFrame = new Player(10.0, 20.0, 20.0, 100.0);
        Player severalFrames = new Player(10.0, 20.0, 20.0, 100.0);
        MovementInput movement = new MovementInput(1.0, 0.0);

        singleFrame.update(movement, 0.5, OPEN_MAP);
        for (int frame = 0; frame < 5; frame++) {
            severalFrames.update(movement, 0.1, OPEN_MAP);
        }

        assertEquals(singleFrame.x(), severalFrames.x(), EPSILON);
        assertEquals(singleFrame.y(), severalFrames.y(), EPSILON);
    }

    @Test
    void normalizesDiagonalMovement() {
        Player player = new Player(100.0, 100.0, 20.0, 100.0);

        player.update(new MovementInput(1.0, 1.0), 1.0, OPEN_MAP);

        double movementX = player.x() - 100.0;
        double movementY = player.y() - 100.0;
        double travelledDistance = Math.hypot(movementX, movementY);

        assertTrue(movementX > 0.0);
        assertTrue(movementY > 0.0);
        assertEquals(movementX, movementY, EPSILON);
        assertEquals(100.0, travelledDistance, EPSILON);
    }

    @Test
    void keepsPlayerInsideMapBounds() {
        TileMap map = createOpenMap(3, 3);
        Player upperLeftPlayer = new Player(0.0, 0.0, 20.0, 100.0);
        Player lowerRightPlayer = new Player(100.0, 100.0, 20.0, 100.0);

        upperLeftPlayer.update(new MovementInput(-1.0, -1.0), 10.0, map);
        lowerRightPlayer.update(new MovementInput(1.0, 1.0), 10.0, map);

        assertEquals(0.0, upperLeftPlayer.x(), EPSILON);
        assertEquals(0.0, upperLeftPlayer.y(), EPSILON);
        assertEquals(100.0, lowerRightPlayer.x(), EPSILON);
        assertEquals(100.0, lowerRightPlayer.y(), EPSILON);
    }

    @Test
    void rejectsNegativeDeltaTime() {
        Player player = new Player(0.0, 0.0, 20.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> player.update(MovementInput.NONE, -0.01, OPEN_MAP)
        );
    }

    @Test
    void doesNotMoveThroughWall() {
        TileMap map = createMapWithBlockedTile(TileType.WALL);
        Player singleFrame = new Player(45.0, 45.0, 30.0, 80.0);
        Player severalFrames = new Player(45.0, 45.0, 30.0, 80.0);

        singleFrame.update(new MovementInput(1.0, 0.0), 1.0, map);
        for (int frame = 0; frame < 80; frame++) {
            severalFrames.update(new MovementInput(1.0, 0.0), 0.0125, map);
        }

        assertEquals(50.0, singleFrame.x(), EPSILON);
        assertEquals(singleFrame.x(), severalFrames.x(), EPSILON);
        assertEquals(45.0, singleFrame.y(), EPSILON);
    }

    @Test
    void doesNotMoveThroughEquipment() {
        TileMap map = createMapWithBlockedTile(TileType.EQUIPMENT);
        Player player = new Player(45.0, 45.0, 30.0, 80.0);

        player.update(new MovementInput(1.0, 0.0), 1.0, map);

        assertEquals(50.0, player.x(), EPSILON);
        assertEquals(45.0, player.y(), EPSILON);
    }

    @Test
    void checksBothAxesDuringLargeDiagonalMovement() {
        TileType[][] tiles = openTiles(5, 5);
        tiles[1][1] = TileType.WALL;
        TileMap map = new TileMap(tiles, 40, 0, 0);
        Player player = new Player(3.0, 3.0, 34.0, 120.0);

        player.update(new MovementInput(1.0, 1.0), 1.0, map);

        assertTrue(player.x() >= 80.0);
        assertTrue(player.y() > 3.0);
        assertTrue(player.y() < 40.0);
    }

    private static TileMap createOpenMap() {
        return createOpenMap(15, 15);
    }

    private static TileMap createOpenMap(int rows, int columns) {
        return new TileMap(openTiles(rows, columns), 40, 0, 0);
    }

    private static TileMap createMapWithBlockedTile(TileType blockedTile) {
        TileType[][] tiles = openTiles(5, 5);
        tiles[1][2] = blockedTile;
        return new TileMap(tiles, 40, 1, 1);
    }

    private static TileType[][] openTiles(int rows, int columns) {
        TileType[][] tiles = new TileType[rows][columns];
        for (TileType[] row : tiles) {
            Arrays.fill(row, TileType.LAB_FLOOR);
        }
        return tiles;
    }
}
