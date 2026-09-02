package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.unex.sentinela.game.TileMap;
import org.junit.jupiter.api.Test;

class MiniMapPainterTest {

    private static final double EPSILON = 0.000_001;

    @Test
    void preservesTileMapProportions() {
        TileMap tileMap = TileMap.createLaboratory();

        assertEquals(141.0, MiniMapPainter.proportionalHeight(tileMap, 188.0), EPSILON);
        assertEquals(87.0, MiniMapPainter.proportionalHeight(tileMap, 116.0), EPSILON);
    }
}
