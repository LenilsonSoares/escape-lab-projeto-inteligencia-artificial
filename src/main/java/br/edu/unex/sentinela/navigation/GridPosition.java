package br.edu.unex.sentinela.navigation;

import java.util.Objects;

/**
 * Posição imutável de uma célula do tilemap.
 */
public record GridPosition(int row, int column) {

    /**
     * Soma as diferenças de linha e coluna, adequada ao movimento ortogonal.
     */
    public int manhattanDistanceTo(GridPosition other) {
        Objects.requireNonNull(other, "A outra posição não pode ser nula");
        return Math.abs(row - other.row) + Math.abs(column - other.column);
    }
}
