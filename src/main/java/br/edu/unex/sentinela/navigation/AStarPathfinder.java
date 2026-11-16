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
 *
 * <p>No modo com saltos, o agente também pode pular por cima de um único
 * equipamento quando existe piso livre do outro lado. O salto avança dois
 * tiles e custa {@value #JUMP_COST}; andar um tile custa {@value #WALK_COST}.
 * Como o salto custa mais de 1 por tile percorrido, a distância Manhattan
 * continua admissível: nunca superestima o custo real até o destino.
 */
public final class AStarPathfinder implements Pathfinder {

    public static final int WALK_COST = 1;
    public static final int JUMP_COST = 3;

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

    /** Números da última busca, mostrados no painel de depuração. */
    public record SearchStats(Set<GridPosition> visited, int pathCost, int heuristic, boolean found) {
        public static final SearchStats NONE = new SearchStats(Set.of(), 0, 0, false);
    }

    private final boolean equipmentJumps;
    private SearchStats lastSearch = SearchStats.NONE;

    /** A* comum: somente passos ortogonais de custo 1. */
    public AStarPathfinder() {
        this(false);
    }

    private AStarPathfinder(boolean equipmentJumps) {
        this.equipmentJumps = equipmentJumps;
    }

    /** A* do saltador: também considera saltos sobre um equipamento. */
    public static AStarPathfinder withEquipmentJumps() {
        return new AStarPathfinder(true);
    }

    public boolean allowsEquipmentJumps() {
        return equipmentJumps;
    }

    public SearchStats lastSearch() {
        return lastSearch;
    }

    /**
     * Soma o custo de uma rota: passo comum = {@value #WALK_COST},
     * salto de dois tiles = {@value #JUMP_COST}.
     */
    public static int costOf(List<GridPosition> path) {
        Objects.requireNonNull(path, "A rota não pode ser nula");
        int cost = 0;
        for (int index = 1; index < path.size(); index++) {
            cost += isJump(path.get(index - 1), path.get(index)) ? JUMP_COST : WALK_COST;
        }
        return cost;
    }

    /** Dois pontos consecutivos a dois tiles de distância formam um salto. */
    public static boolean isJump(GridPosition from, GridPosition to) {
        return from.manhattanDistanceTo(to) == 2
                && (from.row() == to.row() || from.column() == to.column());
    }

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
                lastSearch = new SearchStats(Set.copyOf(closedNodes), current.pathCost(), initialHeuristic, true);
                return reconstructPath(previousPositions, start, destination);
            }

            for (Move move : movesFrom(tileMap, current.position())) {
                GridPosition neighbor = move.destination();
                if (closedNodes.contains(neighbor)) {
                    continue;
                }

                // g(n) acumula o custo do movimento: 1 ao andar, 3 ao saltar.
                int newCost = current.pathCost() + move.cost();
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
        lastSearch = new SearchStats(Set.copyOf(closedNodes), 0, initialHeuristic, false);
        return List.of();
    }

    private List<Move> movesFrom(TileMap tileMap, GridPosition position) {
        List<Move> moves = new ArrayList<>(DIRECTIONS.length * 2);
        for (int[] direction : DIRECTIONS) {
            int row = position.row() + direction[0];
            int column = position.column() + direction[1];
            if (tileMap.isWalkable(row, column)) {
                moves.add(new Move(new GridPosition(row, column), WALK_COST));
            } else if (equipmentJumps && tileMap.isJumpable(row, column)) {
                // Salto: passa por cima do equipamento e pousa no tile seguinte.
                int landingRow = row + direction[0];
                int landingColumn = column + direction[1];
                if (tileMap.isWalkable(landingRow, landingColumn)) {
                    moves.add(new Move(new GridPosition(landingRow, landingColumn), JUMP_COST));
                }
            }
        }
        return moves;
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

    private record Move(GridPosition destination, int cost) {
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
