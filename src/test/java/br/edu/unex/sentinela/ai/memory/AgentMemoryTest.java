package br.edu.unex.sentinela.ai.memory;

import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentMemoryTest {

    private static final GridPosition SPOT = new GridPosition(3, 4);

    private static VisualContact contact(double distanceTiles) {
        return new VisualContact(SPOT, distanceTiles);
    }

    private static Evidence noise(long id, double time) {
        return new Evidence(id, SPOT, time, EvidenceOrigin.HEARING, 0.9, "ROBO");
    }

    private static Evidence radio(long id, double time) {
        return new Evidence(id, SPOT, time, EvidenceOrigin.RADIO, 0.5, "PARCEIRO");
    }

    @Test
    void startsInRoutineWithoutSighting() {
        AgentMemory memory = new AgentMemory("R1");
        assertEquals(0.0, memory.suspicion());
        assertEquals(AlertLevel.ROUTINE, memory.level());
        assertFalse(memory.inCombat());
        assertFalse(memory.seesPlayer());
        assertTrue(memory.lastSeenPosition().isEmpty());
        assertEquals(Double.NEGATIVE_INFINITY, memory.lastSeenTime());
    }

    @Test
    void closeSightingConfirmsCombat() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.016, 1.0, contact(5.9));
        assertTrue(memory.inCombat());
        assertTrue(memory.seesPlayer());
        assertEquals(100.0, memory.suspicion());
        assertEquals(AlertLevel.COMBAT, memory.level());
    }

    @Test
    void distantSightingRaisesSuspicionGradually() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.5, 0.5, contact(7.0));
        assertEquals(12.5, memory.suspicion(), 1e-9);
        assertTrue(memory.seesPlayer());
        assertFalse(memory.inCombat());
        assertEquals(AlertLevel.ROUTINE, memory.level());

        memory.update(0.5, 1.0, contact(7.0));
        assertEquals(AlertLevel.ATTENTION, memory.level());
        memory.update(1.0, 2.0, contact(7.0));
        memory.update(0.5, 2.5, contact(7.0));
        assertEquals(62.5, memory.suspicion(), 1e-9);
        assertEquals(AlertLevel.INVESTIGATION, memory.level());
        memory.update(1.0, 3.5, contact(7.0));
        assertFalse(memory.inCombat());
        memory.update(0.5, 4.0, contact(7.0));
        assertTrue(memory.inCombat());
        assertEquals(AlertLevel.COMBAT, memory.level());
    }

    @Test
    void ambiguousSuspicionDoesNotDependOnFrameRate() {
        AgentMemory fast = new AgentMemory("R1");
        for (int frame = 1; frame <= 60; frame++) {
            fast.update(1.0 / 60.0, frame / 60.0, contact(7.0));
        }
        AgentMemory slow = new AgentMemory("R2");
        slow.update(1.0, 1.0, contact(7.0));

        assertEquals(25.0, slow.suspicion(), 1e-9);
        assertEquals(slow.suspicion(), fast.suspicion(), 1e-9);
    }

    @Test
    void combatUsesSixAndEightTileHysteresis() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.1, 0.0, contact(5.9));
        assertTrue(memory.inCombat());
        memory.update(0.1, 0.1, contact(7.5));
        assertTrue(memory.inCombat());
        memory.update(0.1, 0.2, contact(8.5));
        assertFalse(memory.inCombat());
        assertTrue(memory.seesPlayer());
        assertEquals(AlertLevel.INVESTIGATION, memory.level());
        memory.update(0.1, 0.3, contact(8.5));
        assertFalse(memory.inCombat());
        memory.update(0.1, 0.4, contact(7.5));
        assertFalse(memory.inCombat());
        memory.update(0.1, 0.5, contact(5.5));
        assertTrue(memory.inCombat());
    }

    @Test
    void sevenAndHalfTilesDoesNotConfirmWithoutCombat() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.1, 0.1, contact(7.5));
        assertFalse(memory.inCombat());
        assertEquals(2.5, memory.suspicion(), 1e-9);
    }

    @Test
    void sameNoiseCountsOnlyOnce() {
        AgentMemory memory = new AgentMemory("R1");
        assertTrue(memory.hear(noise(1, 0.0)));
        assertFalse(memory.hear(noise(1, 0.5)));
        assertEquals(AgentMemory.NOISE_INCREMENT, memory.suspicion());
        assertEquals(1, memory.activeEvidence().size());
    }

    @Test
    void radioReportAddsMoreSuspicion() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(radio(7, 0.0));
        assertEquals(AgentMemory.RADIO_INCREMENT, memory.suspicion());
    }

    @Test
    void suspicionDecaysWithoutStimulus() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(noise(1, 0.0));
        memory.update(1.0, 1.0, null);
        assertEquals(12.0, memory.suspicion(), 1e-9);
        memory.update(0.5, 1.5, null);
        assertEquals(7.0, memory.suspicion(), 1e-9);
        memory.update(5.0, 6.5, null);
        assertEquals(0.0, memory.suspicion());
    }

    @Test
    void attentionLevelUsesHysteresis() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(noise(1, 0.0));
        memory.hear(noise(2, 0.0));
        assertEquals(AlertLevel.ROUTINE, memory.level());
        memory.hear(radio(3, 0.0));
        assertEquals(49.0, memory.suspicion(), 1e-9);
        assertEquals(AlertLevel.ATTENTION, memory.level());

        memory.update(1.0, 1.0, null);
        memory.update(3.0, 4.0, null);
        assertEquals(19.0, memory.suspicion(), 1e-9);
        assertEquals(AlertLevel.ATTENTION, memory.level());
        memory.update(0.3, 4.3, null);
        assertEquals(AlertLevel.ATTENTION, memory.level());
        memory.update(0.15, 4.45, null);
        assertEquals(14.5, memory.suspicion(), 1e-9);
        assertEquals(AlertLevel.ROUTINE, memory.level());
    }

    @Test
    void investigationLevelUsesHysteresis() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(radio(1, 0.0));
        memory.hear(radio(2, 0.0));
        assertEquals(AlertLevel.ATTENTION, memory.level());
        memory.hear(radio(3, 0.0));
        assertEquals(AlertLevel.INVESTIGATION, memory.level());

        memory.update(1.0, 1.0, null);
        memory.update(3.0, 4.0, null);
        assertEquals(45.0, memory.suspicion(), 1e-9);
        assertEquals(AlertLevel.INVESTIGATION, memory.level());
        memory.update(0.6, 4.6, null);
        assertEquals(AlertLevel.ATTENTION, memory.level());
        memory.hear(noise(4, 4.6));
        assertEquals(51.0, memory.suspicion(), 1e-9);
        assertEquals(AlertLevel.ATTENTION, memory.level());
    }

    @Test
    void losingContactFreezesLastSeenPosition() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.1, 2.0, contact(4.0));
        memory.update(0.1, 3.0, null);
        assertFalse(memory.seesPlayer());
        assertFalse(memory.inCombat());
        assertEquals(AlertLevel.INVESTIGATION, memory.level());
        assertEquals(SPOT, memory.lastSeenPosition().orElseThrow());
        assertEquals(2.0, memory.lastSeenTime());
    }

    @Test
    void sightingKeepsOneVisionEvidencePerEpisode() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.1, 1.0, contact(5.0));
        memory.update(0.1, 1.1, new VisualContact(new GridPosition(3, 5), 5.0));
        List<Evidence> active = memory.activeEvidence();
        assertEquals(1, active.size());
        Evidence vision = active.get(0);
        assertTrue(vision.eventId() < 0);
        assertEquals(EvidenceOrigin.VISION, vision.origin());
        assertEquals(new GridPosition(3, 5), vision.position());
        assertEquals(1.1, vision.time());
        assertEquals(1.0, vision.initialConfidence());

        memory.update(0.1, 1.2, null);
        memory.update(0.1, 1.3, contact(5.0));
        assertEquals(2, memory.activeEvidence().size());
        assertNotEquals(vision.eventId(), memory.activeEvidence().get(0).eventId());
    }

    @Test
    void ambiguousSightingHasLowerConfidence() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.1, 1.0, contact(7.0));
        assertEquals(0.6, memory.activeEvidence().get(0).initialConfidence());
    }

    @Test
    void evidenceLosesConfidenceOverTime() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(noise(1, 0.0));
        assertEquals(0.33, memory.bestEvidence(5.0).orElseThrow().confidenceAt(5.0), 0.01);
        assertTrue(memory.bestEvidence(20.0).isEmpty());
    }

    @Test
    void bestEvidenceIsTheMostConfidentNow() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(noise(1, 0.0));
        memory.hear(radio(2, 4.0));
        assertEquals(2, memory.bestEvidence(5.0).orElseThrow().eventId());
        assertEquals(1, memory.bestEvidence(0.0).orElseThrow().eventId());
    }

    @Test
    void invalidatedEvidenceIsNotUsedNorHeardAgain() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(noise(1, 0.0));
        memory.hear(radio(2, 0.0));
        memory.invalidate(1);
        assertEquals(1, memory.activeEvidence().size());
        assertEquals(2, memory.bestEvidence(0.0).orElseThrow().eventId());
        assertFalse(memory.hear(noise(1, 1.0)));
    }

    @Test
    void invalidateNearRemovesCloseEvidence() {
        AgentMemory memory = new AgentMemory("R1");
        memory.hear(new Evidence(1, new GridPosition(5, 7), 0.0, EvidenceOrigin.HEARING, 0.9, "R1"));
        memory.hear(new Evidence(2, new GridPosition(5, 8), 0.0, EvidenceOrigin.HEARING, 0.9, "R1"));
        memory.invalidateNear(new GridPosition(5, 5), 2);
        assertEquals(1, memory.activeEvidence().size());
        assertEquals(2, memory.activeEvidence().get(0).eventId());
    }

    @Test
    void keepsAtMostFiveEvidences() {
        AgentMemory memory = new AgentMemory("R1");
        for (int id = 1; id <= 7; id++) {
            memory.hear(noise(id, id));
        }
        List<Evidence> active = memory.activeEvidence();
        assertEquals(AgentMemory.MAX_ACTIVE_EVIDENCE, active.size());
        assertEquals(List.of(7L, 6L, 5L, 4L, 3L), active.stream().map(Evidence::eventId).toList());
    }

    @Test
    void resetForgetsEverything() {
        AgentMemory memory = new AgentMemory("R1");
        memory.update(0.1, 1.0, contact(3.0));
        memory.hear(noise(1, 1.0));
        memory.reset();
        assertEquals(0.0, memory.suspicion());
        assertEquals(AlertLevel.ROUTINE, memory.level());
        assertFalse(memory.inCombat());
        assertFalse(memory.seesPlayer());
        assertTrue(memory.activeEvidence().isEmpty());
        assertTrue(memory.lastSeenPosition().isEmpty());
        assertEquals(Double.NEGATIVE_INFINITY, memory.lastSeenTime());
        assertTrue(memory.hear(noise(1, 2.0)));
    }
}
