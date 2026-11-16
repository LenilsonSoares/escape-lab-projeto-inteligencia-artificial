package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.ai.perception.NoiseSource;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.game.GameWorld;
import org.junit.jupiter.api.Test;

class PerceptionPainterTest {

    private static final double EPSILON = 0.000_001;

    @Test
    void soundWaveGrowsUntilNoiseRadiusInOneSecond() {
        assertEquals(0.0, PerceptionPainter.waveRadius(140.0, PerceptionPainter.waveProgress(0.0)), EPSILON);
        assertEquals(105.0, PerceptionPainter.waveRadius(140.0, PerceptionPainter.waveProgress(0.5)), EPSILON);
        assertEquals(140.0, PerceptionPainter.waveRadius(140.0, PerceptionPainter.waveProgress(1.0)), EPSILON);
        assertEquals(1.0, PerceptionPainter.waveProgress(3.0), EPSILON);
        assertEquals(0.0, PerceptionPainter.waveProgress(-0.2), EPSILON);
    }

    @Test
    void hearingRangeUsesFootstepRadiusAndSensitivity() {
        assertEquals(140.0, DebugOverlayPainter.hearingRadius(EnemyType.PATROLLER, 40.0), EPSILON);
        assertEquals(210.0, DebugOverlayPainter.hearingRadius(EnemyType.INTERCEPTOR, 40.0), EPSILON);
    }

    @Test
    void aStarPanelFollowsTheGuardWhileNoRobotHasARoute() {
        GameWorld world = new GameWorld();

        assertSame(world.enemies().getFirst(), DebugOverlayPainter.aStarFocus(world.enemies()));
    }

    @Test
    void aStarPanelShowsTheFirstRobotWithARealRoute() {
        GameWorld world = new GameWorld();
        world.toggleMode();

        assertTrue(world.enemies().getFirst().lastSearch().pathCost() > 0);
        assertSame(world.enemies().getFirst(), DebugOverlayPainter.aStarFocus(world.enemies()));
    }

    @Test
    void everyRobotLevelAndNoiseHasAColor() {
        for (EnemyType type : EnemyType.values()) {
            assertNotNull(AiPalette.identity(type));
        }
        for (AlertLevel level : AlertLevel.values()) {
            assertNotNull(AiPalette.level(level));
        }
        for (NoiseSource source : NoiseSource.values()) {
            assertNotNull(AiPalette.noise(source));
        }
        assertTrue(AiPalette.level(AlertLevel.COMBAT).getRed() > AiPalette.level(AlertLevel.COMBAT).getGreen());
    }
}
