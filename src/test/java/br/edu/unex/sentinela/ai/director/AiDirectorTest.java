package br.edu.unex.sentinela.ai.director;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AiDirectorTest {

    private static final double DELTA = 1e-9;

    private final AiDirector director = new AiDirector();
    private int time;

    @Test
    void startsWithInitialBudget() {
        assertEquals(2, director.attackBudget());
        assertEquals(0.0, director.pressure());
        assertEquals(0.0, director.smoothedPressure());
        assertEquals("SEM AJUSTE", director.lastAdjustment());
    }

    @Test
    void oneSampleWithMaximumPressureGivesSmoothedPointTwo() {
        highSeconds(1);

        assertEquals(1.0, director.pressure(), DELTA);
        assertEquals(0.2, director.smoothedPressure(), DELTA);
    }

    @Test
    void doesNotSampleBeforeOneSecond() {
        director.update(0.5, 0.5, 0, 1.0);
        assertEquals(0.0, director.pressure());

        director.update(0.5, 1.0, 0, 1.0);
        assertEquals(0.5, director.pressure(), DELTA);
    }

    @Test
    void longFrameProcessesSeveralSamples() {
        director.recordContact(0.0);
        director.recordContact(0.0);
        director.recordContact(0.0);

        director.update(3.0, 3.0, 0, 1.0);

        assertEquals(1.0 - Math.pow(0.8, 3), director.smoothedPressure(), DELTA);
    }

    @Test
    void sustainedHighPressureReducesBudget() {
        highSeconds(10);
        assertEquals(2, director.attackBudget());

        highSeconds(1);
        assertEquals(1, director.attackBudget());
        assertEquals("REDUZIU PARA 1 (PRESSÃO 0,91)", director.lastAdjustment());
    }

    @Test
    void oneSecondSpikeDoesNotAdjust() {
        mediumSeconds(30);
        highSeconds(1);

        assertEquals(1.0, director.pressure(), DELTA);
        assertEquals(0.6, director.smoothedPressure(), 0.01);
        assertEquals(2, director.attackBudget());
        assertEquals("SEM AJUSTE", director.lastAdjustment());
    }

    @Test
    void waitsTenSecondsBetweenAdjustments() {
        lowSeconds(10);
        assertEquals(3, director.attackBudget());

        highSeconds(11);
        assertEquals(2, director.attackBudget());

        highSeconds(9);
        assertEquals(2, director.attackBudget());

        highSeconds(1);
        assertEquals(1, director.attackBudget());
    }

    @Test
    void sustainedLowPressureIncreasesBudgetUpToThree() {
        lowSeconds(9);
        assertEquals(2, director.attackBudget());

        lowSeconds(1);
        assertEquals(3, director.attackBudget());
        assertEquals("AUMENTOU PARA 3 (PRESSÃO 0,00)", director.lastAdjustment());

        lowSeconds(60);
        assertEquals(3, director.attackBudget());
    }

    @Test
    void budgetNeverGoesBelowOne() {
        highSeconds(80);

        assertEquals(1, director.attackBudget());
    }

    @Test
    void damageCountsOnlyContactsFromLastTwentySeconds() {
        director.recordContact(0.0);
        director.recordContact(0.0);
        director.recordContact(0.0);

        director.update(1.0, 19.0, 1000, 0.0);
        assertEquals(0.5, director.pressure(), DELTA);

        director.update(1.0, 20.0, 1000, 0.0);
        assertEquals(0.0, director.pressure(), DELTA);
    }

    @Test
    void damageIsProportionalToContacts() {
        director.recordContact(0.0);

        director.update(1.0, 1.0, 1000, 0.0);

        assertEquals(0.5 / 3.0, director.pressure(), DELTA);
    }

    @Test
    void riskAndNearFractionAreLimitedToRange() {
        director.update(1.0, 1.0, 5000, -2.0);
        assertEquals(0.0, director.pressure(), DELTA);

        director.update(1.0, 2.0, -500, 4.0);
        assertEquals(0.5, director.pressure(), DELTA);
    }

    @Test
    void resetReturnsToInitialState() {
        highSeconds(11);

        director.reset();

        assertEquals(2, director.attackBudget());
        assertEquals(0.0, director.pressure());
        assertEquals(0.0, director.smoothedPressure());
        assertEquals("SEM AJUSTE", director.lastAdjustment());
    }

    @Test
    void rejectsInvalidDeltaTime() {
        assertThrows(IllegalArgumentException.class, () -> director.update(-1.0, 0.0, 0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> director.update(Double.NaN, 0.0, 0, 0.0));
    }

    private void highSeconds(int seconds) {
        for (int i = 0; i < seconds; i++) {
            time++;
            director.recordContact(time);
            director.recordContact(time);
            director.recordContact(time);
            director.update(1.0, time, 0, 1.0);
        }
    }

    private void mediumSeconds(int seconds) {
        for (int i = 0; i < seconds; i++) {
            time++;
            director.update(1.0, time, 0, 1.0);
        }
    }

    private void lowSeconds(int seconds) {
        for (int i = 0; i < seconds; i++) {
            time++;
            director.update(1.0, time, 1000, 0.0);
        }
    }
}
