package br.edu.unex.sentinela.ai.perception;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisionSensorTest {

    private static final int TILE = 40;

    private static TileMap map() {
        String[] rows = {
            "##########",
            "#........#",
            "#...E.#..#",
            "#........#",
            "##########"
        };
        TileType[][] tiles = new TileType[rows.length][rows[0].length()];
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < rows[row].length(); column++) {
                char symbol = rows[row].charAt(column);
                tiles[row][column] = symbol == '#' ? TileType.WALL
                        : symbol == 'E' ? TileType.EQUIPMENT
                        : TileType.LAB_FLOOR;
            }
        }
        return new TileMap(tiles, TILE, 1, 1);
    }

    private static double center(int gridCoordinate) {
        return gridCoordinate * TILE + TILE / 2.0;
    }

    @Test
    void distanceIsMeasuredInTiles() {
        assertEquals(5.0, VisionSensor.distanceTiles(map(), 0, 0, 120, 160), 1e-9);
    }

    @Test
    void seesTargetInsideRangeAndCone() {
        VisionSensor sensor = new VisionSensor(8, 90);
        assertTrue(sensor.canSee(map(), center(1), center(2), 0.0, center(3), center(2)));
    }

    @Test
    void doesNotSeeTargetOutOfRange() {
        VisionSensor sensor = new VisionSensor(3, 90);
        assertFalse(sensor.inRangeAndAngle(map(), center(1), center(2), 0.0, center(5), center(2)));
        assertTrue(sensor.inRangeAndAngle(map(), center(1), center(2), 0.0, center(4), center(2)));
    }

    @Test
    void doesNotSeeTargetOutsideTheCone() {
        VisionSensor narrow = new VisionSensor(8, 90);
        VisionSensor wide = new VisionSensor(8, 200);
        assertFalse(narrow.inRangeAndAngle(map(), center(3), center(2), 0.0, center(1), center(2)));
        assertFalse(narrow.inRangeAndAngle(map(), center(1), center(3), 0.0, center(1), center(1)));
        assertTrue(wide.inRangeAndAngle(map(), center(1), center(3), 0.0, center(1), center(1)));
    }

    @Test
    void coneWorksAcrossThePiBoundary() {
        VisionSensor sensor = new VisionSensor(8, 90);
        double facingLeft = Math.PI - 0.1;
        assertTrue(sensor.inRangeAndAngle(map(), center(5), center(2), facingLeft, center(5) - 80, center(2) - 20));
    }

    @Test
    void targetOnTopOfTheRobotIsVisible() {
        VisionSensor sensor = new VisionSensor(8, 30);
        assertTrue(sensor.canSee(map(), center(2), center(1), Math.PI, center(2), center(1)));
    }

    @Test
    void wallBlocksVision() {
        VisionSensor sensor = new VisionSensor(8, 90);
        assertFalse(VisionSensor.hasLineOfSight(map(), center(1), center(2), center(7), center(2)));
        assertTrue(sensor.inRangeAndAngle(map(), center(1), center(2), 0.0, center(7), center(2)));
        assertFalse(sensor.canSee(map(), center(1), center(2), 0.0, center(7), center(2)));
    }

    @Test
    void equipmentDoesNotBlockVision() {
        VisionSensor sensor = new VisionSensor(8, 90);
        assertTrue(VisionSensor.hasLineOfSight(map(), center(1), center(2), center(5), center(2)));
        assertTrue(sensor.canSee(map(), center(1), center(2), 0.0, center(5), center(2)));
    }

    @Test
    void polygonHasOnePointMoreThanRays() {
        VisionSensor sensor = new VisionSensor(3, 90);
        assertEquals(5, sensor.visibilityPolygon(map(), center(1), center(2), 0.0, 4).length);
        assertEquals(2, sensor.visibilityPolygon(map(), center(1), center(2), 0.0, 1).length);
    }

    @Test
    void polygonRaysStopAtWallsAndRange() {
        VisionSensor sensor = new VisionSensor(8, 90);
        TileMap map = map();
        double x = center(1);
        double y = center(2);
        double[][] polygon = sensor.visibilityPolygon(map, x, y, 0.0, 4);

        double[] middle = polygon[2];
        assertTrue(middle[0] < 6 * TILE && middle[0] >= 6 * TILE - TILE / 8.0);
        assertEquals(y, middle[1], 1e-9);
        for (double[] point : polygon) {
            assertFalse(map.blocksVision((int) Math.floor(point[1] / TILE), (int) Math.floor(point[0] / TILE)));
            assertTrue(Math.hypot(point[0] - x, point[1] - y) <= 8 * TILE + 1e-9);
        }
    }

    @Test
    void polygonReachesFullRangeWhenNothingBlocks() {
        VisionSensor sensor = new VisionSensor(2, 10);
        double[][] polygon = sensor.visibilityPolygon(map(), center(1), center(1), 0.0, 2);
        assertEquals(center(1) + 2 * TILE, polygon[1][0], 1e-9);
        assertEquals(center(1), polygon[1][1], 1e-9);
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new VisionSensor(0, 90));
        assertThrows(IllegalArgumentException.class, () -> new VisionSensor(5, 0));
        assertThrows(IllegalArgumentException.class, () -> new VisionSensor(5, 361));
        assertThrows(IllegalArgumentException.class, () -> new VisionSensor(Double.NaN, 90));
        VisionSensor sensor = new VisionSensor(5, 90);
        assertThrows(IllegalArgumentException.class,
                () -> sensor.visibilityPolygon(map(), center(1), center(1), 0.0, 0));
    }
}
