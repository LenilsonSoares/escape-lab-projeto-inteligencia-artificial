package br.edu.unex.sentinela.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.unex.sentinela.input.MovementInput;
import org.junit.jupiter.api.Test;

class PlayerTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void movesUsingSpeedAndDeltaTime() {
        Player player = new Player(10.0, 20.0, 20.0, 100.0);

        player.update(new MovementInput(1.0, 0.0), 0.5, 500.0, 500.0);

        assertEquals(60.0, player.x(), EPSILON);
        assertEquals(20.0, player.y(), EPSILON);
    }

    @Test
    void movementIsIndependentFromNumberOfFrames() {
        Player singleFrame = new Player(10.0, 20.0, 20.0, 100.0);
        Player severalFrames = new Player(10.0, 20.0, 20.0, 100.0);
        MovementInput movement = new MovementInput(1.0, 0.0);

        singleFrame.update(movement, 0.5, 500.0, 500.0);
        for (int frame = 0; frame < 5; frame++) {
            severalFrames.update(movement, 0.1, 500.0, 500.0);
        }

        assertEquals(singleFrame.x(), severalFrames.x(), EPSILON);
        assertEquals(singleFrame.y(), severalFrames.y(), EPSILON);
    }

    @Test
    void normalizesDiagonalMovement() {
        Player player = new Player(100.0, 100.0, 20.0, 100.0);

        player.update(new MovementInput(1.0, 1.0), 1.0, 500.0, 500.0);

        double travelledDistance = Math.hypot(player.x() - 100.0, player.y() - 100.0);
        assertEquals(100.0, travelledDistance, EPSILON);
    }

    @Test
    void keepsPlayerInsideWorldBounds() {
        Player player = new Player(80.0, 80.0, 20.0, 100.0);

        player.update(new MovementInput(1.0, 1.0), 10.0, 100.0, 100.0);
        assertEquals(80.0, player.x(), EPSILON);
        assertEquals(80.0, player.y(), EPSILON);

        player.update(new MovementInput(-1.0, -1.0), 10.0, 100.0, 100.0);
        assertEquals(0.0, player.x(), EPSILON);
        assertEquals(0.0, player.y(), EPSILON);
    }

    @Test
    void rejectsNegativeDeltaTime() {
        Player player = new Player(0.0, 0.0, 20.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> player.update(MovementInput.NONE, -0.01, 100.0, 100.0)
        );
    }
}
