package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.game.TileMap;
import org.junit.jupiter.api.Test;

class RenderLayoutTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void centersMapAndUsesFullSidePanelsAtInitialSize() {
        RenderLayout layout = RenderLayout.calculate(
                1_280.0,
                720.0,
                TileMap.createLaboratory()
        );

        assertEquals(240.0, layout.mapX(), EPSILON);
        assertEquals(60.0, layout.mapY(), EPSILON);
        assertEquals(216.0, layout.railWidth(), EPSILON);
        assertEquals(12.0, layout.leftRailX(), EPSILON);
        assertEquals(1_052.0, layout.rightRailX(), EPSILON);
        assertEquals(12.0, layout.headerY(), EPSILON);
        assertEquals(672.0, layout.footerY(), EPSILON);
        assertFalse(layout.compact());
    }

    @Test
    void keepsCompactPanelsOutsideMapAtMinimumWidth() {
        RenderLayout layout = RenderLayout.calculate(
                1_120.0,
                720.0,
                TileMap.createLaboratory()
        );

        assertEquals(136.0, layout.railWidth(), EPSILON);
        assertTrue(layout.compact());
        assertTrue(layout.leftRailX() + layout.railWidth() <= layout.mapX() - RenderLayout.GAP);
        assertTrue(layout.rightRailX() >= layout.mapX() + layout.mapWidth() + RenderLayout.GAP);
        assertTrue(layout.rightRailX() + layout.railWidth() <= 1_120.0 - RenderLayout.OUTER_MARGIN);
    }
}
