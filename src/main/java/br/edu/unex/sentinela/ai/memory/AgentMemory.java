package br.edu.unex.sentinela.ai.memory;

import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Conhecimento local de um robô: o que ele percebeu, não a posição real do jogador.
 *
 * <p>A suspeita S vai de 0 a 100. Visão a menos de 6 tiles confirma o combate e,
 * já em combate, o contato vale até 8 tiles (histerese 6/8). Visão mais distante é
 * ambígua e sobe S aos poucos. Cada ruído ou relato de rádio soma uma vez só por
 * eventId, e sem estímulo S cai com o tempo. Os níveis também têm histerese
 * (25/15 e 60/40). Baseado na apostila "IA para agentes", seções 1 e 2, e na Aula 07.
 */
public final class AgentMemory {

    public static final double CONFIRM_DISTANCE_TILES = 6.0;
    public static final double LOSE_DISTANCE_TILES = 8.0;
    public static final double AMBIGUOUS_RATE = 25.0;
    public static final double DECAY_RATE = 10.0;
    public static final double NOISE_INCREMENT = 12.0;
    public static final double RADIO_INCREMENT = 25.0;
    public static final double ATTENTION_ENTER = 25.0;
    public static final double ATTENTION_EXIT = 15.0;
    public static final double INVESTIGATION_ENTER = 60.0;
    public static final double INVESTIGATION_EXIT = 40.0;
    public static final double MIN_EVIDENCE_CONFIDENCE = 0.15;
    public static final int MAX_ACTIVE_EVIDENCE = 5;

    private static final double MAX_SUSPICION = 100.0;
    private static final double CONFIRMED_CONFIDENCE = 1.0;
    private static final double AMBIGUOUS_CONFIDENCE = 0.6;

    private final String owner;
    private final List<Evidence> evidence = new ArrayList<>();
    // Ids já recebidos, inclusive os invalidados, para o mesmo ruído não contar duas vezes.
    private final Set<Long> knownIds = new HashSet<>();

    private double suspicion;
    private AlertLevel level;
    private boolean inCombat;
    private boolean seesPlayer;
    private GridPosition lastSeenPosition;
    private double lastSeenTime;
    private boolean newStimulus;
    private boolean lostAtDistance;
    private long nextVisionId;
    private long visionId;

    public AgentMemory(String owner) {
        this.owner = Objects.requireNonNull(owner, "O dono da memória não pode ser nulo");
        reset();
    }

    public String owner() {
        return owner;
    }

    /**
     * Atualiza a memória no quadro. {@code contact} é nulo quando o robô não vê o jogador.
     * Cada episódio de contato tem uma evidência de VISÃO própria, com id negativo.
     */
    public void update(double deltaTime, double now, VisualContact contact) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("O deltaTime deve ser zero ou positivo");
        }

        if (contact != null) {
            see(deltaTime, now, contact);
        } else {
            seesPlayer = false;
            inCombat = false;
            lostAtDistance = false;
            if (!newStimulus) {
                suspicion = Math.max(0.0, suspicion - DECAY_RATE * deltaTime);
            }
        }

        newStimulus = false;
        updateLevel();
    }

    private void see(double deltaTime, double now, VisualContact contact) {
        boolean newEpisode = !seesPlayer;
        seesPlayer = true;
        lastSeenPosition = contact.position();
        lastSeenTime = now;

        double distance = contact.distanceTiles();
        if (distance < CONFIRM_DISTANCE_TILES || (inCombat && distance <= LOSE_DISTANCE_TILES)) {
            inCombat = true;
            suspicion = MAX_SUSPICION;
        } else {
            // Passou de 8 tiles em combate: perde o contato e, neste mesmo episódio,
            // só volta ao combate chegando a menos de 6 tiles.
            if (inCombat) {
                inCombat = false;
                lostAtDistance = true;
            }
            suspicion = Math.min(MAX_SUSPICION, suspicion + AMBIGUOUS_RATE * deltaTime);
            if (suspicion >= MAX_SUSPICION && !lostAtDistance) {
                inCombat = true;
            }
        }

        if (newEpisode) {
            visionId = nextVisionId--;
            knownIds.add(visionId);
        }
        double confidence = inCombat ? CONFIRMED_CONFIDENCE : AMBIGUOUS_CONFIDENCE;
        evidence.removeIf(item -> item.eventId() == visionId);
        addEvidence(new Evidence(visionId, contact.position(), now, EvidenceOrigin.VISION, confidence, owner));
    }

    /** Registra ruído (HEARING) ou relato (RADIO). Retorna falso se o eventId já era conhecido. */
    public boolean hear(Evidence clue) {
        Objects.requireNonNull(clue, "A evidência não pode ser nula");
        if (!knownIds.add(clue.eventId())) {
            return false;
        }
        addEvidence(clue);
        double increment = clue.origin() == EvidenceOrigin.RADIO ? RADIO_INCREMENT : NOISE_INCREMENT;
        suspicion = Math.min(MAX_SUSPICION, suspicion + increment);
        newStimulus = true;
        updateLevel();
        return true;
    }

    public double suspicion() {
        return suspicion;
    }

    public AlertLevel level() {
        return level;
    }

    /** Verdadeiro enquanto o contato visual confirmado continua (histerese 6/8). */
    public boolean inCombat() {
        return inCombat;
    }

    /** Verdadeiro somente se houve contato visual (confirmado ou ambíguo) no último update. */
    public boolean seesPlayer() {
        return seesPlayer;
    }

    public Optional<GridPosition> lastSeenPosition() {
        return Optional.ofNullable(lastSeenPosition);
    }

    /** Instante do último avistamento; Double.NEGATIVE_INFINITY se nunca viu. */
    public double lastSeenTime() {
        return lastSeenTime;
    }

    /** Pista válida de maior confiança atual, ignorando as fracas demais. */
    public Optional<Evidence> bestEvidence(double now) {
        return evidence.stream()
                .filter(item -> item.confidenceAt(now) >= MIN_EVIDENCE_CONFIDENCE)
                .max(Comparator.comparingDouble(item -> item.confidenceAt(now)));
    }

    /** Marca como verificada a pista com esse id. */
    public void invalidate(long eventId) {
        evidence.removeIf(item -> item.eventId() == eventId);
    }

    /** Invalida toda pista a até {@code radius} tiles (Manhattan) de {@code area}. */
    public void invalidateNear(GridPosition area, int radius) {
        Objects.requireNonNull(area, "A área não pode ser nula");
        evidence.removeIf(item -> item.position().manhattanDistanceTo(area) <= radius);
    }

    /** Evidências ativas, da mais recente para a mais antiga. */
    public List<Evidence> activeEvidence() {
        List<Evidence> copy = new ArrayList<>(evidence);
        copy.sort(Comparator.comparingDouble(Evidence::time).reversed());
        return List.copyOf(copy);
    }

    /** Esquece tudo (troca de mapa). */
    public void reset() {
        evidence.clear();
        knownIds.clear();
        suspicion = 0.0;
        level = AlertLevel.ROUTINE;
        inCombat = false;
        seesPlayer = false;
        lastSeenPosition = null;
        lastSeenTime = Double.NEGATIVE_INFINITY;
        newStimulus = false;
        lostAtDistance = false;
        nextVisionId = -1;
        visionId = 0;
    }

    private void addEvidence(Evidence clue) {
        evidence.add(clue);
        while (evidence.size() > MAX_ACTIVE_EVIDENCE) {
            Evidence oldest = evidence.stream().min(Comparator.comparingDouble(Evidence::time)).orElseThrow();
            evidence.remove(oldest);
        }
    }

    private void updateLevel() {
        if (inCombat) {
            level = AlertLevel.COMBAT;
            return;
        }
        // Ao sair do combate, S ainda está alto: parte de INVESTIGAÇÃO e desce pela histerese.
        AlertLevel current = level == AlertLevel.COMBAT ? AlertLevel.INVESTIGATION : level;
        level = switch (current) {
            case ROUTINE -> suspicion >= INVESTIGATION_ENTER ? AlertLevel.INVESTIGATION
                    : suspicion >= ATTENTION_ENTER ? AlertLevel.ATTENTION
                    : AlertLevel.ROUTINE;
            case ATTENTION -> suspicion >= INVESTIGATION_ENTER ? AlertLevel.INVESTIGATION
                    : suspicion <= ATTENTION_EXIT ? AlertLevel.ROUTINE
                    : AlertLevel.ATTENTION;
            default -> suspicion > INVESTIGATION_EXIT ? AlertLevel.INVESTIGATION
                    : suspicion <= ATTENTION_EXIT ? AlertLevel.ROUTINE
                    : AlertLevel.ATTENTION;
        };
    }
}
