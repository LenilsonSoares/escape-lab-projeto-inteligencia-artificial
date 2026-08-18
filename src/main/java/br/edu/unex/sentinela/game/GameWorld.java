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
    private final TileMap tileMap;

    private double width;
    private double height;

    public GameWorld(double width, double height) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
        this.tileMap = TileMap.createLaboratory();
        this.player = new Player(
                tileMap.playerStartX(PLAYER_SIZE),
                tileMap.playerStartY(PLAYER_SIZE),
                PLAYER_SIZE,
                PLAYER_SPEED
        );
    }

    public void update(double deltaTime, MovementInput movementInput) {
        player.update(movementInput, deltaTime, tileMap);
    }

    public void resize(double width, double height) {
        this.width = requirePositive(width, "width");
        this.height = requirePositive(height, "height");
    }

    public Player player() {
        return player;
    }

    public TileMap tileMap() {
        return tileMap;
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
