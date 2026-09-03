package br.edu.unex.sentinela.navigation;

import br.edu.unex.sentinela.game.TileMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Calcula o menor caminho em quatro direções usando A* e heurística Manhattan.
 */
public final class AStarPathfinder implements Pathfinder {

    private static final int[][] DIRECTIONS = {
        {-1, 0},
        {0, 1},
        {1, 0},
        {0, -1}
    };

    private static final Comparator<SearchNode> NODE_ORDER = Comparator
            .comparingInt(SearchNode::estimatedTotalCost)
            .thenComparingInt(SearchNode::heuristicCost)
            .thenComparingInt(node -> node.position().row())
            .thenComparingInt(node -> node.position().column());

    @Override
    public List<GridPosition> findPath(
            TileMap tileMap,
            GridPosition start,
            GridPosition destination
    ) {
        Objects.requireNonNull(tileMap, "O tilemap não pode ser nulo");
        Objects.requireNonNull(start, "A posição inicial não pode ser nula");
        Objects.requireNonNull(destination, "O destino não pode ser nulo");
        requireNavigable(tileMap, start, "A posição inicial");
        requireNavigable(tileMap, destination, "O destino");

        // A fila aberta prioriza o menor f = g + h. A lista fechada impede
        // que posições já analisadas sejam processadas novamente.
        PriorityQueue<SearchNode> openNodes = new PriorityQueue<>(NODE_ORDER);
        Set<GridPosition> closedNodes = new HashSet<>();
        Map<GridPosition, Integer> pathCosts = new HashMap<>();
        Map<GridPosition, GridPosition> previousPositions = new HashMap<>();

        int initialHeuristic = start.manhattanDistanceTo(destination);
        pathCosts.put(start, 0);
        openNodes.add(new SearchNode(start, 0, initialHeuristic));

        while (!openNodes.isEmpty()) {
            SearchNode current = openNodes.remove();
            int bestKnownCost = pathCosts.getOrDefault(current.position(), Integer.MAX_VALUE);
            // Uma mesma posição pode entrar mais de uma vez na fila. Entradas
            // antigas são ignoradas quando já existe um custo g menor.
            if (current.pathCost() != bestKnownCost
                    || !closedNodes.add(current.position())) {
                continue;
            }

            if (current.position().equals(destination)) {
                return reconstructPath(previousPositions, start, destination);
            }

            for (GridPosition neighbor : neighborsOf(current.position())) {
                if (closedNodes.contains(neighbor)
                        || !tileMap.isWalkable(neighbor.row(), neighbor.column())) {
                    continue;
                }

                // Todos os movimentos ortogonais possuem custo unitário.
                int newCost = current.pathCost() + 1;
                int knownCost = pathCosts.getOrDefault(neighbor, Integer.MAX_VALUE);
                if (newCost >= knownCost) {
                    continue;
                }

                pathCosts.put(neighbor, newCost);
                previousPositions.put(neighbor, current.position());
                int heuristic = neighbor.manhattanDistanceTo(destination);
                openNodes.add(new SearchNode(neighbor, newCost, heuristic));
            }
        }

        // Fila vazia significa que não existe rota transitável entre os pontos.
        return List.of();
    }

    private static List<GridPosition> neighborsOf(GridPosition position) {
        List<GridPosition> neighbors = new ArrayList<>(DIRECTIONS.length);
        for (int[] direction : DIRECTIONS) {
            neighbors.add(new GridPosition(
                    position.row() + direction[0],
                    position.column() + direction[1]
            ));
        }
        return neighbors;
    }

    private static List<GridPosition> reconstructPath(
            Map<GridPosition, GridPosition> previousPositions,
            GridPosition start,
            GridPosition destination
    ) {
        List<GridPosition> path = new ArrayList<>();
        GridPosition current = destination;
        path.add(current);

        while (!current.equals(start)) {
            current = previousPositions.get(current);
            path.add(current);
        }

        // Os pais levam do destino à origem; a inversão devolve a ordem percorrida.
        Collections.reverse(path);
        return List.copyOf(path);
    }

    private static void requireNavigable(
            TileMap tileMap,
            GridPosition position,
            String description
    ) {
        if (!tileMap.isWalkable(position.row(), position.column())) {
            throw new IllegalArgumentException(description + " deve estar em um tile transitável");
        }
    }

    private record SearchNode(
            GridPosition position,
            int pathCost,
            int heuristicCost
    ) {

        private int estimatedTotalCost() {
            return pathCost + heuristicCost;
        }
    }
}
