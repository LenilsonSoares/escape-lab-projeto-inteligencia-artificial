package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.input.MovementInput;

/**
 * Mantém o estado atual do mundo e aplica suas regras de atualização.
 */
public final class GameWorld {

    private static final double PLAYER_SIZE = 34.0;
    private static final double PLAYER_SPEED = 230.0;

    private final Player player;

    private double width;
    private double height;

    public GameWorld(double width, double height) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
        this.player = new Player(
                (width - PLAYER_SIZE) / 2.0,
                (height - PLAYER_SIZE) / 2.0,
                PLAYER_SIZE,
                PLAYER_SPEED
        );
    }

    public void update(double deltaTime, MovementInput movementInput) {
        player.update(movementInput, deltaTime, width, height);
    }

    public void resize(double width, double height) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
        player.keepInside(width, height);
    }

    public Player player() {
        return player;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    private static double requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " deve ser maior que zero");
        }
        return value;
    }
}
