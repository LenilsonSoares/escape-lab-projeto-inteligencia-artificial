package br.edu.unex.sentinela.game;

/**
 * Tipos de tile usados para representar logicamente o laboratório.
 */
public enum TileType {

    LAB_FLOOR(true, false),
    WALL(false, false),
    // Equipamentos são baixos: bloqueiam a caminhada, mas podem ser saltados.
    EQUIPMENT(false, true),
    EXIT(true, false),
    // Porta de segurança: o TileMap libera a passagem depois que o cartão abre as portas.
    DOOR(false, false);

    private final boolean walkable;
    private final boolean jumpable;

    TileType(boolean walkable, boolean jumpable) {
        this.walkable = walkable;
        this.jumpable = jumpable;
    }

    public boolean isWalkable() {
        return walkable;
    }

    /** Indica se um agente saltador pode passar por cima deste tile. */
    public boolean isJumpable() {
        return jumpable;
    }
}
