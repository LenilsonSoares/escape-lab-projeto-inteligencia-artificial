package br.edu.unex.sentinela.rendering;

/**
 * Guarda somente informações visuais derivadas do deslocamento de uma entidade.
 */
final class ActorAnimationState {

    private static final double MOVEMENT_EPSILON = 1.0e-4;

    private boolean initialized;
    private boolean moving;
    private boolean facingLeft;
    private double previousX;
    private double previousY;

    void sample(double x, double y) {
        if (!initialized) {
            previousX = x;
            previousY = y;
            initialized = true;
            moving = false;
            return;
        }

        double movementX = x - previousX;
        double movementY = y - previousY;
        moving = Math.hypot(movementX, movementY) > MOVEMENT_EPSILON;
        if (Math.abs(movementX) > MOVEMENT_EPSILON) {
            facingLeft = movementX < 0.0;
        }

        previousX = x;
        previousY = y;
    }

    boolean moving() {
        return moving;
    }

    boolean facingLeft() {
        return facingLeft;
    }
}
