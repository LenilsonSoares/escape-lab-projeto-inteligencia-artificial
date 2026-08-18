package br.edu.unex.sentinela.input;

/**
 * Fotografia imutável dos eixos de movimento em um quadro.
 */
public record MovementInput(double horizontal, double vertical) {

    public static final MovementInput NONE = new MovementInput(0.0, 0.0);

    public MovementInput {
        if (!Double.isFinite(horizontal) || !Double.isFinite(vertical)) {
            throw new IllegalArgumentException("Os eixos de movimento devem ser finitos");
        }
        horizontal = Math.clamp(horizontal, -1.0, 1.0);
        vertical = Math.clamp(vertical, -1.0, 1.0);
    }
}
