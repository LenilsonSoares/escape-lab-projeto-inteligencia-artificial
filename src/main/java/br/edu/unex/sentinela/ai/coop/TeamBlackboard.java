package br.edu.unex.sentinela.ai.coop;

import br.edu.unex.sentinela.ai.memory.Evidence;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Blackboard da equipe de robôs: rádio com atraso, reservas de tiles de busca
 * e permissões de ataque (apostila "IA para agentes", seções 9 e 10).
 */
public final class TeamBlackboard {

    public static final double DEFAULT_RADIO_DELAY = 0.6;
    public static final int DEFAULT_MAX_ATTACKERS = 2;

    private static final double REPORT_LIFETIME = 30.0;

    private final double radioDelay;
    private final List<RadioReport> reports = new ArrayList<>();
    private final Set<Long> publishedIds = new HashSet<>();
    private final Map<GridPosition, Reservation> reservations = new HashMap<>();
    private final Set<String> attackers = new LinkedHashSet<>();
    private Evidence latestReport;
    private int maxAttackers = DEFAULT_MAX_ATTACKERS;

    public TeamBlackboard() {
        this(DEFAULT_RADIO_DELAY);
    }

    public TeamBlackboard(double radioDelaySeconds) {
        if (!Double.isFinite(radioDelaySeconds) || radioDelaySeconds < 0) {
            throw new IllegalArgumentException("O atraso do rádio deve ser finito e não negativo");
        }
        this.radioDelay = radioDelaySeconds;
    }

    /** Publica um relato no instante {@code now}. Retorna falso se o eventId já foi publicado. */
    public boolean report(Evidence evidence, double now) {
        // O mesmo ruído pode chegar por dois rádios: o evento original só entra uma vez.
        if (!publishedIds.add(evidence.eventId())) {
            return false;
        }
        reports.removeIf(report -> now - report.publishedAt() > REPORT_LIFETIME);
        // A evidência é guardada como veio, com o instante original. Se a data
        // fosse renovada a cada repasse, a pista nunca seria esquecida.
        reports.add(new RadioReport(evidence, now, new HashSet<>()));
        latestReport = evidence;
        return true;
    }

    /** Relatos já entregáveis a {@code receiver} e ainda não entregues a ele (marca como entregues). */
    public List<Evidence> deliveries(String receiver, double now) {
        List<Evidence> result = new ArrayList<>();
        for (RadioReport report : reports) {
            boolean arrived = now >= report.publishedAt() + radioDelay;
            boolean isAuthor = report.evidence().reporter().equals(receiver);
            if (arrived && !isAuthor && report.receivers().add(receiver)) {
                result.add(report.evidence());
            }
        }
        return result;
    }

    /** Relato publicado mais recentemente, para depuração. */
    public Optional<Evidence> latestReport() {
        return Optional.ofNullable(latestReport);
    }

    /** Reserva se o tile estiver livre, vencido ou já for do mesmo dono (renova o prazo). */
    public boolean tryReserve(GridPosition position, String owner, double now, double durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("A duração da reserva deve ser positiva");
        }
        if (isReservedByOther(position, owner, now)) {
            return false;
        }
        reservations.put(position, new Reservation(owner, now + durationSeconds));
        return true;
    }

    /** Libera somente se {@code owner} ainda for o proprietário. */
    public void release(GridPosition position, String owner) {
        Reservation reservation = reservations.get(position);
        if (reservation != null && reservation.owner().equals(owner)) {
            reservations.remove(position);
        }
    }

    public void releaseAll(String owner) {
        reservations.values().removeIf(reservation -> reservation.owner().equals(owner));
    }

    public boolean isReservedByOther(GridPosition position, String owner, double now) {
        Reservation reservation = reservations.get(position);
        return reservation != null && !reservation.expired(now) && !reservation.owner().equals(owner);
    }

    /** Reservas ainda válidas: tile para proprietário. */
    public Map<GridPosition, String> activeReservations(double now) {
        Map<GridPosition, String> active = new HashMap<>();
        reservations.forEach((position, reservation) -> {
            if (!reservation.expired(now)) {
                active.put(position, reservation.owner());
            }
        });
        return Map.copyOf(active);
    }

    /** Limite de perseguidores simultâneos (mínimo 1). Não revoga permissões já concedidas. */
    public void setMaxAttackers(int maxAttackers) {
        if (maxAttackers < 1) {
            throw new IllegalArgumentException("O limite de perseguidores deve ser pelo menos 1");
        }
        this.maxAttackers = maxAttackers;
    }

    public int maxAttackers() {
        return maxAttackers;
    }

    /** Concede (ou confirma) a permissão; recusa quando o limite já está ocupado por outros. */
    public boolean requestAttackPermit(String owner) {
        if (attackers.contains(owner)) {
            return true;
        }
        // Limita quantos robôs atacam ao mesmo tempo.
        if (attackers.size() < maxAttackers) {
            attackers.add(owner);
            return true;
        }
        return false;
    }

    public void releaseAttackPermit(String owner) {
        attackers.remove(owner);
    }

    public boolean hasAttackPermit(String owner) {
        return attackers.contains(owner);
    }

    public Set<String> attackers() {
        return Set.copyOf(attackers);
    }

    /** Limpa relatos, reservas e permissões (troca de mapa). Mantém maxAttackers. */
    public void clear() {
        reports.clear();
        publishedIds.clear();
        reservations.clear();
        attackers.clear();
        latestReport = null;
    }

    private record RadioReport(Evidence evidence, double publishedAt, Set<String> receivers) {
    }

    private record Reservation(String owner, double expiresAt) {

        boolean expired(double now) {
            return now >= expiresAt;
        }
    }
}
