package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.input.MovementInput;

/**
 * Entidade controlável do jogador.
 */
public final class Player {

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
            double worldWidth,
            double worldHeight
    ) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("Delta time não pode ser negativo");
        }

        double horizontal = input.horizontal();
        double vertical = input.vertical();
        double directionLength = Math.hypot(horizontal, vertical);

        if (directionLength > 0.0) {
            x += (horizontal / directionLength) * speed * deltaTime;
            y += (vertical / directionLength) * speed * deltaTime;
        }

        keepInside(worldWidth, worldHeight);
    }

    public void keepInside(double worldWidth, double worldHeight) {
        double maximumX = Math.max(0.0, worldWidth - size);
        double maximumY = Math.max(0.0, worldHeight - size);
        x = Math.clamp(x, 0.0, maximumX);
        y = Math.clamp(y, 0.0, maximumY);
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
