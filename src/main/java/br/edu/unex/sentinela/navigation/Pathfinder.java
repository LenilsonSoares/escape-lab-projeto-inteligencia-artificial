package br.edu.unex.sentinela.navigation;

import br.edu.unex.sentinela.game.TileMap;
import java.util.List;

/**
 * Contrato para algoritmos que calculam uma rota em um tilemap.
 */
@FunctionalInterface
public interface Pathfinder {

    List<GridPosition> findPath(
            TileMap tileMap,
            GridPosition start,
            GridPosition destination
    );
}
