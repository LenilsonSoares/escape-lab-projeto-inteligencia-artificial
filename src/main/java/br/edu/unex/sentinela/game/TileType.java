package br.edu.unex.sentinela.game;

/**
 * Tipos de tile usados para representar logicamente o laboratório.
 */
public enum TileType {

    LAB_FLOOR(true),
    WALL(false),
    EQUIPMENT(false);

    private final boolean walkable;

    TileType(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
