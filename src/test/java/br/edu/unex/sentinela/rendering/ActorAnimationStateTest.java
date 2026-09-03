package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ActorAnimationStateTest {

    @Test
    void startsStoppedAndFacingRight() {
        ActorAnimationState animation = new ActorAnimationState();

        animation.sample(10.0, 20.0);

        assertFalse(animation.moving());
        assertFalse(animation.facingLeft());
    }

    @Test
    void detectsMovementAndHorizontalDirection() {
        ActorAnimationState animation = new ActorAnimationState();
        animation.sample(10.0, 20.0);

        animation.sample(8.0, 20.0);

        assertTrue(animation.moving());
        assertTrue(animation.facingLeft());
    }

    @Test
    void preservesDirectionDuringVerticalMovementAndStopsWithoutDisplacement() {
        ActorAnimationState animation = new ActorAnimationState();
        animation.sample(10.0, 20.0);
        animation.sample(8.0, 20.0);

        animation.sample(8.0, 18.0);
        assertTrue(animation.moving());
        assertTrue(animation.facingLeft());

        animation.sample(8.0, 18.0);
        assertFalse(animation.moving());
        assertTrue(animation.facingLeft());
    }

    @Test
    void advancesWalkingFrameAccordingToTraveledDistance() {
        ActorAnimationState animation = new ActorAnimationState();
        animation.sample(0.0, 0.0);

        animation.sample(4.0, 0.0);
        assertEquals(1, animation.walkFrame());

        animation.sample(8.0, 0.0);
        assertEquals(2, animation.walkFrame());

        animation.sample(8.0, 0.0);
        assertEquals(0, animation.walkFrame());
    }
}
