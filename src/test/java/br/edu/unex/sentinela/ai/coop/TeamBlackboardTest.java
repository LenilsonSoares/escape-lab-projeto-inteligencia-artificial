package br.edu.unex.sentinela.ai.coop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.ai.memory.Evidence;
import br.edu.unex.sentinela.ai.memory.EvidenceOrigin;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TeamBlackboardTest {

    private static final GridPosition TILE = new GridPosition(3, 4);

    private final TeamBlackboard blackboard = new TeamBlackboard(0.6);

    @Test
    void doesNotDeliverBeforeRadioDelay() {
        blackboard.report(evidence(1, "A", 10.0), 10.0);

        assertTrue(blackboard.deliveries("B", 10.5).isEmpty());
        assertEquals(1, blackboard.deliveries("B", 10.6).size());
    }

    @Test
    void doesNotDeliverReportToItsAuthor() {
        blackboard.report(evidence(1, "A", 0.0), 0.0);

        assertTrue(blackboard.deliveries("A", 5.0).isEmpty());
    }

    @Test
    void sameEventIsPublishedAndDeliveredOnlyOnce() {
        assertTrue(blackboard.report(evidence(7, "A", 1.0), 1.0));
        assertFalse(blackboard.report(evidence(7, "C", 1.0), 1.2));

        assertEquals(1, blackboard.deliveries("B", 2.0).size());
        assertTrue(blackboard.deliveries("B", 3.0).isEmpty());
    }

    @Test
    void deliveryKeepsOriginalEvidence() {
        Evidence original = evidence(3, "A", 4.0);
        blackboard.report(original, 5.0);

        List<Evidence> received = blackboard.deliveries("B", 9.0);

        assertEquals(1, received.size());
        assertSame(original, received.get(0));
        assertEquals(4.0, received.get(0).time());
        assertEquals("A", received.get(0).reporter());
    }

    @Test
    void deliversInPublicationOrderToEachReceiver() {
        blackboard.report(evidence(1, "A", 0.0), 0.0);
        blackboard.report(evidence(2, "A", 0.5), 0.5);

        List<Evidence> forB = blackboard.deliveries("B", 2.0);
        List<Evidence> forC = blackboard.deliveries("C", 2.0);

        assertEquals(List.of(1L, 2L), forB.stream().map(Evidence::eventId).toList());
        assertEquals(2, forC.size());
    }

    @Test
    void latestReportStartsEmptyAndFollowsPublications() {
        assertTrue(blackboard.latestReport().isEmpty());

        blackboard.report(evidence(1, "A", 0.0), 0.0);
        blackboard.report(evidence(2, "B", 1.0), 1.0);

        assertEquals(2L, blackboard.latestReport().orElseThrow().eventId());
    }

    @Test
    void rejectsInvalidRadioDelay() {
        assertThrows(IllegalArgumentException.class, () -> new TeamBlackboard(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new TeamBlackboard(Double.NaN));
    }

    @Test
    void reservedTileConflictsWithOtherRobot() {
        assertTrue(blackboard.tryReserve(TILE, "A", 0.0, 5.0));

        assertFalse(blackboard.tryReserve(TILE, "B", 1.0, 5.0));
        assertTrue(blackboard.isReservedByOther(TILE, "B", 1.0));
        assertFalse(blackboard.isReservedByOther(TILE, "A", 1.0));
    }

    @Test
    void expiredReservationCanBeTakenByOther() {
        blackboard.tryReserve(TILE, "A", 0.0, 5.0);

        assertFalse(blackboard.isReservedByOther(TILE, "B", 5.0));
        assertTrue(blackboard.tryReserve(TILE, "B", 5.0, 5.0));
        assertEquals(Map.of(TILE, "B"), blackboard.activeReservations(6.0));
    }

    @Test
    void ownerRenewsReservation() {
        blackboard.tryReserve(TILE, "A", 0.0, 5.0);

        assertTrue(blackboard.tryReserve(TILE, "A", 4.0, 5.0));
        assertTrue(blackboard.isReservedByOther(TILE, "B", 8.0));
    }

    @Test
    void onlyOwnerReleasesReservation() {
        blackboard.tryReserve(TILE, "A", 0.0, 5.0);

        blackboard.release(TILE, "B");
        assertTrue(blackboard.isReservedByOther(TILE, "B", 1.0));

        blackboard.release(TILE, "A");
        assertFalse(blackboard.isReservedByOther(TILE, "B", 1.0));
    }

    @Test
    void releaseAllRemovesOnlyOwnerReservations() {
        GridPosition other = new GridPosition(5, 5);
        GridPosition third = new GridPosition(6, 6);
        blackboard.tryReserve(TILE, "A", 0.0, 5.0);
        blackboard.tryReserve(other, "A", 0.0, 5.0);
        blackboard.tryReserve(third, "B", 0.0, 5.0);

        blackboard.releaseAll("A");

        assertEquals(Map.of(third, "B"), blackboard.activeReservations(1.0));
    }

    @Test
    void activeReservationsIgnoresExpiredAndIsImmutable() {
        blackboard.tryReserve(TILE, "A", 0.0, 2.0);
        blackboard.tryReserve(new GridPosition(1, 1), "B", 0.0, 10.0);

        Map<GridPosition, String> active = blackboard.activeReservations(3.0);

        assertEquals(Map.of(new GridPosition(1, 1), "B"), active);
        assertThrows(UnsupportedOperationException.class, () -> active.put(TILE, "C"));
    }

    @Test
    void reservationNeedsPositiveDuration() {
        assertThrows(IllegalArgumentException.class, () -> blackboard.tryReserve(TILE, "A", 0.0, 0.0));
    }

    @Test
    void attackPermitsRespectLimit() {
        assertEquals(2, blackboard.maxAttackers());
        assertTrue(blackboard.requestAttackPermit("A"));
        assertTrue(blackboard.requestAttackPermit("B"));

        assertFalse(blackboard.requestAttackPermit("C"));
        assertFalse(blackboard.hasAttackPermit("C"));
        assertEquals(Set.of("A", "B"), blackboard.attackers());
    }

    @Test
    void repeatedRequestDoesNotUseAnotherSlot() {
        blackboard.setMaxAttackers(1);

        assertTrue(blackboard.requestAttackPermit("A"));
        assertTrue(blackboard.requestAttackPermit("A"));
        assertEquals(Set.of("A"), blackboard.attackers());
    }

    @Test
    void reducingLimitKeepsCurrentAttackers() {
        blackboard.requestAttackPermit("A");
        blackboard.requestAttackPermit("B");

        blackboard.setMaxAttackers(1);

        assertTrue(blackboard.hasAttackPermit("A"));
        assertTrue(blackboard.hasAttackPermit("B"));
        assertFalse(blackboard.requestAttackPermit("C"));

        blackboard.releaseAttackPermit("A");
        assertFalse(blackboard.requestAttackPermit("C"));

        blackboard.releaseAttackPermit("B");
        assertTrue(blackboard.requestAttackPermit("C"));
    }

    @Test
    void releasingPermitOpensSlot() {
        blackboard.requestAttackPermit("A");
        blackboard.requestAttackPermit("B");

        blackboard.releaseAttackPermit("A");

        assertTrue(blackboard.requestAttackPermit("C"));
        assertThrows(UnsupportedOperationException.class, () -> blackboard.attackers().add("D"));
    }

    @Test
    void limitMustBeAtLeastOne() {
        assertThrows(IllegalArgumentException.class, () -> blackboard.setMaxAttackers(0));
    }

    @Test
    void clearKeepsAttackLimit() {
        blackboard.setMaxAttackers(3);
        blackboard.report(evidence(1, "A", 0.0), 0.0);
        blackboard.tryReserve(TILE, "A", 0.0, 5.0);
        blackboard.requestAttackPermit("A");

        blackboard.clear();

        assertEquals(3, blackboard.maxAttackers());
        assertTrue(blackboard.deliveries("B", 1.0).isEmpty());
        assertTrue(blackboard.latestReport().isEmpty());
        assertTrue(blackboard.activeReservations(1.0).isEmpty());
        assertTrue(blackboard.attackers().isEmpty());
        assertTrue(blackboard.report(evidence(1, "A", 2.0), 2.0));
    }

    private static Evidence evidence(long id, String reporter, double time) {
        return new Evidence(id, new GridPosition(2, 2), time, EvidenceOrigin.HEARING, 0.8, reporter);
    }
}
