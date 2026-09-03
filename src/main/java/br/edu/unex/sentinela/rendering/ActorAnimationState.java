package br.edu.unex.sentinela.rendering;

/**
 * Guarda somente informações visuais derivadas do deslocamento de uma entidade.
 */
final class ActorAnimationState {

    private static final double MOVEMENT_EPSILON = 1.0e-4;
    private static final double DISTANCE_PER_FRAME = 4.0;

    private boolean initialized;
    private boolean moving;
    private boolean facingLeft;
    private double previousX;
    private double previousY;
    private double traveledDistance;

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
        double movementDistance = Math.hypot(movementX, movementY);
        moving = movementDistance > MOVEMENT_EPSILON;
        if (moving) {
            traveledDistance += movementDistance;
        }
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

    int walkFrame() {
        if (!moving) {
            return 0;
        }
        return (int) (traveledDistance / DISTANCE_PER_FRAME) % 4;
    }
}
