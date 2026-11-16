package br.edu.unex.sentinela.ai.perception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HearingSensorTest {

    private final NoiseEvent footstep = new NoiseEvent(1, 100, 100, 80, 0.0, NoiseSource.FOOTSTEP);

    @Test
    void hearsNoiseInsideRadius() {
        HearingSensor sensor = new HearingSensor(1.0);
        assertTrue(sensor.hears(footstep, 100, 100));
        assertTrue(sensor.hears(footstep, 180, 100));
    }

    @Test
    void doesNotHearNoiseOutsideRadius() {
        HearingSensor sensor = new HearingSensor(1.0);
        assertFalse(sensor.hears(footstep, 181, 100));
        assertFalse(sensor.hears(footstep, 160, 160));
    }

    @Test
    void sensitivityScalesTheRadius() {
        assertTrue(new HearingSensor(1.5).hears(footstep, 220, 100));
        assertFalse(new HearingSensor(1.0).hears(footstep, 220, 100));
        assertTrue(new HearingSensor(0.5).hears(footstep, 140, 100));
        assertFalse(new HearingSensor(0.5).hears(footstep, 150, 100));
    }

    @Test
    void rejectsInvalidSensitivity() {
        assertThrows(IllegalArgumentException.class, () -> new HearingSensor(0.0));
        assertThrows(IllegalArgumentException.class, () -> new HearingSensor(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new HearingSensor(Double.POSITIVE_INFINITY));
    }
}
