package br.edu.unex.sentinela.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FrameMetricsTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    void recordsDeltaTimeAndCalculatesFramesPerSecond() {
        FrameMetrics metrics = new FrameMetrics();

        for (int frame = 0; frame < 5; frame++) {
            metrics.recordFrame(0.05);
        }

        assertEquals(0.05, metrics.deltaTime(), EPSILON);
        assertEquals(20.0, metrics.framesPerSecond(), EPSILON);
    }

    @Test
    void rejectsNegativeDeltaTime() {
        FrameMetrics metrics = new FrameMetrics();

        assertThrows(IllegalArgumentException.class, () -> metrics.recordFrame(-0.01));
    }
}
