package br.edu.unex.sentinela.navigation;

import br.edu.unex.sentinela.game.TileMap;
import java.util.List;

/**
 * Contrato para algoritmos que calculam uma rota em um tilemap.
 */
@FunctionalInterface
public interface Pathfinder {

    /**
     * Retorna a rota incluindo origem e destino, ou uma lista vazia quando
     * não existe caminho entre os pontos. Pontos consecutivos são vizinhos
     * ortogonais, exceto nos saltos sobre equipamentos, que avançam dois tiles.
     */
    List<GridPosition> findPath(
            TileMap tileMap,
            GridPosition start,
            GridPosition destination
    );
}
