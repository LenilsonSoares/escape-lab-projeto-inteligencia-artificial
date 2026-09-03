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

        assertEquals(1.0, layout.scale(), EPSILON);
        assertEquals(240.0, layout.mapX(), EPSILON);
        assertEquals(60.0, layout.mapY(), EPSILON);
        assertEquals(800.0, layout.mapWidth(), EPSILON);
        assertEquals(600.0, layout.mapHeight(), EPSILON);
        assertEquals(216.0, layout.railWidth(), EPSILON);
        assertEquals(12.0, layout.leftRailX(), EPSILON);
        assertEquals(1_052.0, layout.rightRailX(), EPSILON);
        assertEquals(12.0, layout.headerY(), EPSILON);
        assertEquals(672.0, layout.footerY(), EPSILON);
        assertEquals(12.0, layout.gap(), EPSILON);
        assertEquals(36.0, layout.barHeight(), EPSILON);
        assertFalse(layout.compact());
    }

    @Test
    void keepsCompactPanelsOutsideMapAtMinimumWidth() {
        RenderLayout layout = RenderLayout.calculate(
                1_120.0,
                720.0,
                TileMap.createLaboratory()
        );

        assertEquals(1.0, layout.scale(), EPSILON);
        assertEquals(136.0, layout.railWidth(), EPSILON);
        assertTrue(layout.compact());
        assertPanelsDoNotOverlapMap(layout, 1_120.0);
    }

    @Test
    void usesIntermediateScaleWhenWindowIsLargerThanBaseSize() {
        RenderLayout layout = RenderLayout.calculate(
                1_600.0,
                900.0,
                TileMap.createLaboratory()
        );

        assertEquals(1.25, layout.scale(), EPSILON);
        assertEquals(300.0, layout.mapX(), EPSILON);
        assertEquals(75.0, layout.mapY(), EPSILON);
        assertEquals(1_000.0, layout.mapWidth(), EPSILON);
        assertEquals(750.0, layout.mapHeight(), EPSILON);
        assertPanelsDoNotOverlapMap(layout, 1_600.0);
    }

    @Test
    void enlargesMapAndPanelsProportionallyAtFullHd() {
        RenderLayout layout = RenderLayout.calculate(
                1_920.0,
                1_080.0,
                TileMap.createLaboratory()
        );

        assertEquals(1.5, layout.scale(), EPSILON);
        assertEquals(360.0, layout.mapX(), EPSILON);
        assertEquals(90.0, layout.mapY(), EPSILON);
        assertEquals(1_200.0, layout.mapWidth(), EPSILON);
        assertEquals(900.0, layout.mapHeight(), EPSILON);
        assertEquals(324.0, layout.railWidth(), EPSILON);
        assertEquals(18.0, layout.leftRailX(), EPSILON);
        assertEquals(1_578.0, layout.rightRailX(), EPSILON);
        assertEquals(18.0, layout.headerY(), EPSILON);
        assertEquals(1_008.0, layout.footerY(), EPSILON);
        assertEquals(18.0, layout.gap(), EPSILON);
        assertEquals(54.0, layout.barHeight(), EPSILON);
        assertEquals(600.0, layout.screenX(160.0), EPSILON);
        assertEquals(150.0, layout.screenY(40.0), EPSILON);
        assertEquals(240.0, layout.logicalCoordinates().mapX(), EPSILON);
        assertEquals(216.0, layout.logicalCoordinates().railWidth(), EPSILON);
        assertFalse(layout.compact());
        assertPanelsDoNotOverlapMap(layout, 1_920.0);
    }

    @Test
    void usesIntegerPixelArtScaleAtFourK() {
        RenderLayout layout = RenderLayout.calculate(
                3_840.0,
                2_160.0,
                TileMap.createLaboratory()
        );

        assertEquals(3.0, layout.scale(), EPSILON);
        assertEquals(720.0, layout.mapX(), EPSILON);
        assertEquals(180.0, layout.mapY(), EPSILON);
        assertEquals(2_400.0, layout.mapWidth(), EPSILON);
        assertEquals(1_800.0, layout.mapHeight(), EPSILON);
        assertEquals(648.0, layout.railWidth(), EPSILON);
        assertEquals(36.0, layout.leftRailX(), EPSILON);
        assertEquals(3_156.0, layout.rightRailX(), EPSILON);
        assertEquals(36.0, layout.headerY(), EPSILON);
        assertEquals(2_016.0, layout.footerY(), EPSILON);
        assertEquals(36.0, layout.gap(), EPSILON);
        assertEquals(108.0, layout.barHeight(), EPSILON);
        assertEquals(1_200.0, layout.screenX(160.0), EPSILON);
        assertEquals(300.0, layout.screenY(40.0), EPSILON);
        assertEquals(240.0, layout.logicalCoordinates().mapX(), EPSILON);
        assertEquals(216.0, layout.logicalCoordinates().railWidth(), EPSILON);
        assertFalse(layout.compact());
        assertPanelsDoNotOverlapMap(layout, 3_840.0);
    }

    private static void assertPanelsDoNotOverlapMap(RenderLayout layout, double viewportWidth) {
        assertTrue(layout.leftRailX() + layout.railWidth() <= layout.mapX() - layout.gap());
        assertTrue(layout.rightRailX() >= layout.mapX() + layout.mapWidth() + layout.gap());
        assertTrue(layout.leftRailX() >= layout.outerMargin());
        assertTrue(layout.rightRailX() + layout.railWidth()
                <= viewportWidth - layout.outerMargin());
    }
}
