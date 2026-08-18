package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameWorldTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void startsPlayerAtConfiguredWalkableTile() {
        GameWorld world = new GameWorld(200.0, 100.0);
        TileMap tileMap = world.tileMap();

        assertEquals(
                tileMap.playerStartColumn(),
                (int) Math.floor(world.player().centerX() / tileMap.tileSize())
        );
        assertEquals(
                tileMap.playerStartRow(),
                (int) Math.floor(world.player().centerY() / tileMap.tileSize())
        );
        assertTrue(tileMap.canOccupy(
                world.player().x(),
                world.player().y(),
                world.player().size(),
                world.player().size()
        ));
    }

    @Test
    void resizingViewportDoesNotChangePositionInsideMap() {
        GameWorld world = new GameWorld(400.0, 300.0);
        double initialX = world.player().x();
        double initialY = world.player().y();

        world.resize(100.0, 80.0);

        assertEquals(initialX, world.player().x(), EPSILON);
        assertEquals(initialY, world.player().y(), EPSILON);
        assertEquals(100.0, world.width(), EPSILON);
        assertEquals(80.0, world.height(), EPSILON);
    }

    @Test
    void rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(0.0, 100.0));
        assertThrows(IllegalArgumentException.class, () -> new GameWorld(100.0, Double.NaN));
    }
}
