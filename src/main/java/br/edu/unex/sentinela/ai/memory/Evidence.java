package br.edu.unex.sentinela.ai.memory;

import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.Objects;

/**
 * Pista datada sobre o jogador: onde foi percebido, quando, por qual sentido,
 * com que confiança inicial e quem a observou. A confiança envelhece com
 * c(t) = c0 × exp(-λ × idade), com λ = 0,20 por segundo.
 */
public record Evidence(long eventId, GridPosition position, double time, EvidenceOrigin origin,
        double initialConfidence, String reporter) {

    public static final double DECAY_PER_SECOND = 0.20;

    public Evidence {
        Objects.requireNonNull(position, "A posição da evidência não pode ser nula");
        Objects.requireNonNull(origin, "A origem da evidência não pode ser nula");
        Objects.requireNonNull(reporter, "O autor da evidência não pode ser nulo");
        if (!Double.isFinite(time)) {
            throw new IllegalArgumentException("O instante da evidência deve ser finito");
        }
        if (!(initialConfidence >= 0.0 && initialConfidence <= 1.0)) {
            throw new IllegalArgumentException("A confiança deve estar entre 0 e 1");
        }
    }

    public double ageAt(double now) {
        return Math.max(0.0, now - time);
    }

    public double confidenceAt(double now) {
        return initialConfidence * Math.exp(-DECAY_PER_SECOND * ageAt(now));
    }

    /** Mesma pista retransmitida: preserva id, posição e instante originais. */
    public Evidence relayedAs(EvidenceOrigin newOrigin, double confidence) {
        return new Evidence(eventId, position, time, newOrigin, confidence, reporter);
    }
}
