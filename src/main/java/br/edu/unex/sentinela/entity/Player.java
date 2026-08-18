package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.input.MovementInput;

/**
 * Entidade controlável do jogador.
 */
public final class Player {

    private static final double MAX_COLLISION_STEP = 1.0;

    private double x;
    private double y;
    private final double size;
    private final double speed;

    public Player(double x, double y, double size, double speed) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("A posição inicial deve ser finita");
        }
        if (!Double.isFinite(size) || size <= 0.0) {
            throw new IllegalArgumentException("O tamanho deve ser maior que zero");
        }
        if (!Double.isFinite(speed) || speed < 0.0) {
            throw new IllegalArgumentException("A velocidade não pode ser negativa");
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
    }

    public void update(
            MovementInput input,
            double deltaTime,
            TileMap tileMap
    ) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("Delta time não pode ser negativo");
        }

        double horizontal = input.horizontal();
        double vertical = input.vertical();
        double directionLength = Math.hypot(horizontal, vertical);

        if (directionLength > 0.0) {
            double movementX = (horizontal / directionLength) * speed * deltaTime;
            double movementY = (vertical / directionLength) * speed * deltaTime;
            move(movementX, movementY, tileMap);
        }
    }

    private void move(double movementX, double movementY, TileMap tileMap) {
        if (!Double.isFinite(movementX) || !Double.isFinite(movementY)) {
            throw new IllegalArgumentException("O deslocamento do jogador deve ser finito");
        }

        double maximumStep = Math.min(MAX_COLLISION_STEP, tileMap.tileSize() / 2.0);
        double longestMovement = Math.max(Math.abs(movementX), Math.abs(movementY));
        int steps = Math.max(1, (int) Math.ceil(longestMovement / maximumStep));
        double stepX = movementX / steps;
        double stepY = movementY / steps;

        for (int currentStep = 0; currentStep < steps; currentStep++) {
            tryMoveTo(x + stepX, y, tileMap);
            tryMoveTo(x, y + stepY, tileMap);
        }
    }

    private void tryMoveTo(double nextX, double nextY, TileMap tileMap) {
        if (tileMap.canOccupy(nextX, nextY, size, size)) {
            x = nextX;
            y = nextY;
        }
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double size() {
        return size;
    }

    public double centerX() {
        return x + size / 2.0;
    }

    public double centerY() {
        return y + size / 2.0;
    }
}
