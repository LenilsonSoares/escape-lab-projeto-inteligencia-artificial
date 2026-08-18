package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GameWorldTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void startsPlayerAtCenterOfWorld() {
        GameWorld world = new GameWorld(200.0, 100.0);

        assertEquals(100.0, world.player().centerX(), EPSILON);
        assertEquals(50.0, world.player().centerY(), EPSILON);
    }

    @Test
    void resizingKeepsPlayerInsideVisibleArea() {
        GameWorld world = new GameWorld(400.0, 300.0);

        world.resize(100.0, 80.0);

        assertEquals(66.0, world.player().x(), EPSILON);
        assertEquals(46.0, world.player().y(), EPSILON);
    }

    @Test
    void rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(0.0, 100.0));
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(100.0, Double.NaN));
    }
}
