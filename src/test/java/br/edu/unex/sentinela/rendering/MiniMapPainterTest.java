package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import org.junit.jupiter.api.Test;

class MiniMapPainterTest {

    private static final double EPSILON = 0.000_001;

    @Test
    void preservesTileMapProportions() {
        TileMap tileMap = TileMap.createLaboratory();

        assertEquals(141.0, MiniMapPainter.proportionalHeight(tileMap, 188.0), EPSILON);
        assertEquals(87.0, MiniMapPainter.proportionalHeight(tileMap, 116.0), EPSILON);
    }

    @Test
    void fitsWidthToWholePixelsPerTile() {
        TileMap tileMap = TileMap.createLaboratory();

        assertEquals(180.0, MiniMapPainter.fittedWidth(tileMap, 188.0), EPSILON);
        assertEquals(100.0, MiniMapPainter.fittedWidth(tileMap, 116.0), EPSILON);
    }

    @Test
    void doorChangesColorWhenOpened() {
        assertNotEquals(
                MiniMapPainter.tileColor(TileType.DOOR, false),
                MiniMapPainter.tileColor(TileType.DOOR, true)
        );
        assertEquals(
                MiniMapPainter.tileColor(TileType.WALL, false),
                MiniMapPainter.tileColor(TileType.WALL, true)
        );
    }
}
