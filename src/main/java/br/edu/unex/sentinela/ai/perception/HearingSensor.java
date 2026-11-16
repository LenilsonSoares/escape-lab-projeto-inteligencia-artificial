package br.edu.unex.sentinela.ai.perception;

import java.util.Objects;

/**
 * Audição do robô. Um ruído é ouvido quando a distância até a origem é menor
 * ou igual a {@code raio do ruído × sensibilidade}.
 */
public final class HearingSensor {

    private final double sensitivity;

    public HearingSensor(double sensitivity) {
        if (!Double.isFinite(sensitivity) || sensitivity <= 0.0) {
            throw new IllegalArgumentException("A sensibilidade da audição deve ser positiva");
        }
        this.sensitivity = sensitivity;
    }

    public double sensitivity() {
        return sensitivity;
    }

    public boolean hears(NoiseEvent noise, double listenerX, double listenerY) {
        Objects.requireNonNull(noise, "O ruído não pode ser nulo");
        double distance = Math.hypot(noise.x() - listenerX, noise.y() - listenerY);
        return distance <= noise.radius() * sensitivity;
    }
}
