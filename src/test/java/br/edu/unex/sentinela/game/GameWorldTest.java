package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
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

    @Test
    void createsValidRouteForAutonomousAgent() {
        GameWorld world = new GameWorld(960.0, 640.0);
        List<GridPosition> path = world.navigationPath();

        assertEquals(25, path.size());
        assertEquals(new GridPosition(13, 1), path.get(0));
        assertEquals(new GridPosition(1, 13), path.get(path.size() - 1));
        assertFalse(world.autonomousAgent().hasReachedDestination());
        assertTrue(world.tileMap().canOccupy(
                world.autonomousAgent().x(),
                world.autonomousAgent().y(),
                world.autonomousAgent().size(),
                world.autonomousAgent().size()
        ));
    }

    @Test
    void autonomousAgentStopsAtConfiguredDestination() {
        GameWorld world = new GameWorld(960.0, 640.0);

        for (int frame = 0;
                frame < 1_000 && !world.autonomousAgent().hasReachedDestination();
                frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
            assertTrue(world.tileMap().canOccupy(
                    world.autonomousAgent().x(),
                    world.autonomousAgent().y(),
                    world.autonomousAgent().size(),
                    world.autonomousAgent().size()
            ));
        }

        double destinationX = world.autonomousAgent().x();
        double destinationY = world.autonomousAgent().y();
        world.update(1.0, MovementInput.NONE);

        assertTrue(world.autonomousAgent().hasReachedDestination());
        assertEquals(526.0, destinationX, EPSILON);
        assertEquals(46.0, destinationY, EPSILON);
        assertEquals(destinationX, world.autonomousAgent().x(), EPSILON);
        assertEquals(destinationY, world.autonomousAgent().y(), EPSILON);
    }
}
