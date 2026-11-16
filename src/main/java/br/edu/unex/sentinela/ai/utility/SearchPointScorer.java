package br.edu.unex.sentinela.ai.utility;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Utility AI da busca: dá uma nota para cada ponto onde o jogador pode estar.
 * P = 0,45 × evidência + 0,35 × não verificado - 0,20 × custo (todos de 0 a 1).
 * Antes de pontuar, descarta pontos sem caminho e pontos reservados por outro robô.
 */
public final class SearchPointScorer {

    public static final int SEARCH_RADIUS = 6;
    static final double EVIDENCE_WEIGHT = 0.45;
    static final double UNVERIFIED_WEIGHT = 0.35;
    static final double COST_WEIGHT = 0.20;
    static final double MAX_COST = 20.0;
    private static final int MAX_CANDIDATES = 12;

    public List<ScoredPoint> rank(Robot robot, GridPosition clue, Set<GridPosition> visited) {
        List<ScoredPoint> ranking = new ArrayList<>();
        for (GridPosition candidate : candidates(robot.map(), clue)) {
            if (robot.team().isReservedByOther(candidate, robot.name(), robot.now())) {
                continue;
            }
            int cost = robot.pathCost(candidate);
            if (cost < 0) {
                continue;
            }
            double evidence = 1.0 - (double) candidate.manhattanDistanceTo(clue) / SEARCH_RADIUS;
            double unverified = visited.contains(candidate) ? 0.1 : 1.0;
            double normalizedCost = Math.min(1.0, cost / MAX_COST);
            ranking.add(new ScoredPoint(candidate, evidence, unverified, normalizedCost,
                    score(evidence, unverified, normalizedCost)));
        }
        ranking.sort(Comparator.comparingDouble(ScoredPoint::score).reversed());
        return ranking;
    }

    static double score(double evidence, double unverified, double cost) {
        return EVIDENCE_WEIGHT * evidence + UNVERIFIED_WEIGHT * unverified - COST_WEIGHT * cost;
    }

    /**
     * Candidatos: a própria pista e os cruzamentos e becos próximos dela, que são
     * os lugares por onde o jogador provavelmente saiu ou onde pode ter se escondido.
     */
    static List<GridPosition> candidates(TileMap map, GridPosition clue) {
        List<GridPosition> result = new ArrayList<>();
        if (map.isWalkable(clue.row(), clue.column())) {
            result.add(clue);
        }
        for (int row = clue.row() - SEARCH_RADIUS; row <= clue.row() + SEARCH_RADIUS; row++) {
            for (int column = clue.column() - SEARCH_RADIUS; column <= clue.column() + SEARCH_RADIUS; column++) {
                GridPosition point = new GridPosition(row, column);
                if (point.equals(clue) || point.manhattanDistanceTo(clue) > SEARCH_RADIUS
                        || !map.isWalkable(row, column)) {
                    continue;
                }
                int exits = walkableNeighbours(map, point);
                if (exits >= 3 || exits == 1) {
                    result.add(point);
                }
            }
        }
        // Limita o número de buscas A*: fica com os mais próximos da pista.
        result.sort(Comparator.comparingInt(point -> point.manhattanDistanceTo(clue)));
        return result.subList(0, Math.min(MAX_CANDIDATES, result.size()));
    }

    private static int walkableNeighbours(TileMap map, GridPosition point) {
        int count = 0;
        if (map.isWalkable(point.row() - 1, point.column())) count++;
        if (map.isWalkable(point.row() + 1, point.column())) count++;
        if (map.isWalkable(point.row(), point.column() - 1)) count++;
        if (map.isWalkable(point.row(), point.column() + 1)) count++;
        return count;
    }
}
