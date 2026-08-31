package br.edu.unex.sentinela.navigation;

import br.edu.unex.sentinela.game.TileMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Calcula o menor caminho em quatro direções usando A* e heurística Manhattan.
 */
public final class AStarPathfinder {

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

        PriorityQueue<SearchNode> openNodes = new PriorityQueue<>(NODE_ORDER);
        Map<GridPosition, Integer> pathCosts = new HashMap<>();
        Map<GridPosition, GridPosition> previousPositions = new HashMap<>();

        int initialHeuristic = start.manhattanDistanceTo(destination);
        pathCosts.put(start, 0);
        openNodes.add(new SearchNode(start, 0, initialHeuristic));

        while (!openNodes.isEmpty()) {
            SearchNode current = openNodes.remove();
            int bestKnownCost = pathCosts.getOrDefault(current.position(), Integer.MAX_VALUE);
            if (current.pathCost() != bestKnownCost) {
                continue;
            }

            if (current.position().equals(destination)) {
                return reconstructPath(previousPositions, start, destination);
            }

            for (GridPosition neighbor : neighborsOf(current.position())) {
                if (!tileMap.isWalkable(neighbor.row(), neighbor.column())) {
                    continue;
                }

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
