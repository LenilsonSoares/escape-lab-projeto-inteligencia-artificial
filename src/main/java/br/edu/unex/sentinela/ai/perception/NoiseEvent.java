package br.edu.unex.sentinela.ai.perception;

import java.util.Objects;

/**
 * Ruído emitido no mundo. Coordenadas e raio estão em pixels do mapa;
 * {@code time} é o instante da simulação em segundos.
 */
public record NoiseEvent(long id, double x, double y, double radius, double time, NoiseSource source) {

    public NoiseEvent {
        Objects.requireNonNull(source, "A origem do ruído não pode ser nula");
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(time)) {
            throw new IllegalArgumentException("Posição e instante do ruído devem ser finitos");
        }
        if (!Double.isFinite(radius) || radius <= 0.0) {
            throw new IllegalArgumentException("O raio do ruído deve ser positivo");
        }
    }
}
