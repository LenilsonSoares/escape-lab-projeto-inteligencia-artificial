package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TileMapTest {

    @Test
    void usesExpandedLaboratoryDimensions() {
        TileMap tileMap = TileMap.createLaboratory();

        assertEquals(15, tileMap.rows());
        assertEquals(20, tileMap.columns());
        assertEquals(800, tileMap.pixelWidth());
        assertEquals(600, tileMap.pixelHeight());
    }

    @Test
    void keepsAllWalkableTilesConnectedWithDoorsOpen() {
        TileMap tileMap = TileMap.createLaboratory().copy();
        assertTrue(tileMap.openDoors());
        boolean[][] visited = new boolean[tileMap.rows()][tileMap.columns()];
        ArrayDeque<int[]> pending = new ArrayDeque<>();
        int walkableTiles = 0;

        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.isWalkable(row, column)) {
                    walkableTiles++;
                }
            }
        }

        pending.add(new int[]{tileMap.playerStartRow(), tileMap.playerStartColumn()});
        visited[tileMap.playerStartRow()][tileMap.playerStartColumn()] = true;
        int reachedTiles = 0;
        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        while (!pending.isEmpty()) {
            int[] position = pending.removeFirst();
            reachedTiles++;

            for (int[] direction : directions) {
                int nextRow = position[0] + direction[0];
                int nextColumn = position[1] + direction[1];
                if (tileMap.isWalkable(nextRow, nextColumn) && !visited[nextRow][nextColumn]) {
                    visited[nextRow][nextColumn] = true;
                    pending.addLast(new int[]{nextRow, nextColumn});
                }
            }
        }

        assertEquals(walkableTiles, reachedTiles);
    }

    @Test
    void containsEveryTileType() {
        TileMap tileMap = TileMap.createLaboratory();
        Set<TileType> typesInMap = EnumSet.noneOf(TileType.class);

        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                typesInMap.add(tileMap.tileAt(row, column));
            }
        }

        assertEquals(EnumSet.allOf(TileType.class), typesInMap);
    }

    @Test
    void containsWalkableAndBlockedAreas() {
        TileMap tileMap = TileMap.createLaboratory();
        boolean hasWalkableTile = false;
        boolean hasBlockedTile = false;

        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.tileAt(row, column).isWalkable()) {
                    hasWalkableTile = true;
                } else {
                    hasBlockedTile = true;
                }
            }
        }

        assertTrue(hasWalkableTile);
        assertTrue(hasBlockedTile);
    }

    @Test
    void placesPlayerStartOnWalkableTile() {
        TileMap tileMap = TileMap.createLaboratory();
        double playerSize = 34.0;

        assertTrue(tileMap.isWalkable(tileMap.playerStartRow(), tileMap.playerStartColumn()));
        assertTrue(tileMap.canOccupy(
                tileMap.playerStartX(playerSize),
                tileMap.playerStartY(playerSize),
                playerSize,
                playerSize
        ));
        assertFalse(tileMap.tileAt(0, 0).isWalkable());
    }

    @Test
    void rejectsAnAreaThatCannotAdvancePastTheMapEdge() {
        TileMap tileMap = TileMap.createLaboratory();

        assertFalse(tileMap.canOccupy(
                tileMap.pixelWidth(),
                tileMap.tileSize(),
                1.0,
                1.0
        ));
    }

    @Test
    void wallsAndClosedDoorsBlockVisionButEquipmentDoesNot() {
        TileMap tileMap = new TileMap(new TileType[][]{
                {TileType.LAB_FLOOR, TileType.WALL, TileType.EQUIPMENT, TileType.DOOR}
        }, 10, 0, 0);

        assertFalse(tileMap.blocksVision(0, 0));
        assertTrue(tileMap.blocksVision(0, 1));
        assertFalse(tileMap.blocksVision(0, 2));
        assertTrue(tileMap.blocksVision(0, 3));
        assertTrue(tileMap.blocksVision(0, 4));

        tileMap.openDoors();

        assertFalse(tileMap.blocksVision(0, 3));
        assertTrue(tileMap.blocksVision(0, 1));
    }

    @Test
    void laboratoryCopyHasSameTilesAndClosedDoors() {
        TileMap original = TileMap.createLaboratory();
        TileMap copy = original.copy();

        assertTrue(original.hasDoors());
        assertFalse(copy.doorsOpen());
        assertEquals(original.playerStartRow(), copy.playerStartRow());
        assertEquals(original.playerStartColumn(), copy.playerStartColumn());
        for (int row = 0; row < original.rows(); row++) {
            for (int column = 0; column < original.columns(); column++) {
                assertEquals(original.tileAt(row, column), copy.tileAt(row, column));
            }
        }
    }
}
