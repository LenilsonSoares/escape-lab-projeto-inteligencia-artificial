package br.edu.unex.sentinela.ai.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.ai.coop.TeamBlackboard;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SearchPointScorerTest {

    private final TeamBlackboard team = new TeamBlackboard();
    private final TileMap map = mapOf(
            "#########",
            "#.......#",
            "#.##.##.#",
            "#.......#",
            "#########");
    private final Enemy robot = Enemy.withAi(EnemyType.PATROLLER, map, new GridPosition(1, 1),
            List.of(new GridPosition(1, 1), new GridPosition(3, 7)), new GridPosition(3, 7),
            new AStarPathfinder(), team, new DecisionLog());

    @Test
    void scoreFollowsTheWeightedFormula() {
        assertEquals(0.80, SearchPointScorer.score(1.0, 1.0, 0.0), 1.0e-9);
        assertEquals(0.45 * 0.5 + 0.35 * 0.1 - 0.20 * 0.5, SearchPointScorer.score(0.5, 0.1, 0.5), 1.0e-9);
    }

    @Test
    void clueItselfComesFirstWhenNothingWasVerified() {
        GridPosition clue = new GridPosition(1, 4);

        List<ScoredPoint> ranking = new SearchPointScorer().rank(robot, clue, Set.of());

        assertEquals(clue, ranking.getFirst().position());
        assertEquals(1.0, ranking.getFirst().evidence(), 1.0e-9);
    }

    @Test
    void visitedPointsLoseScore() {
        GridPosition clue = new GridPosition(1, 4);

        List<ScoredPoint> ranking = new SearchPointScorer().rank(robot, clue, Set.of(clue));

        assertFalse(ranking.getFirst().position().equals(clue));
    }

    @Test
    void pointsReservedByAnotherRobotAreFilteredOut() {
        GridPosition clue = new GridPosition(1, 4);
        team.tryReserve(clue, "OUTRO", 0.0, 10.0);

        List<ScoredPoint> ranking = new SearchPointScorer().rank(robot, clue, Set.of());

        assertTrue(ranking.stream().noneMatch(point -> point.position().equals(clue)));
    }

    @Test
    void candidatesAreJunctionsAndDeadEndsNearTheClue() {
        List<GridPosition> candidates = SearchPointScorer.candidates(map, new GridPosition(1, 4));

        assertTrue(candidates.contains(new GridPosition(2, 4)) || candidates.contains(new GridPosition(1, 4)));
        assertTrue(candidates.stream().allMatch(point -> map.isWalkable(point.row(), point.column())));
        assertTrue(candidates.size() <= 12);
    }

    private static TileMap mapOf(String... rows) {
        TileType[][] tiles = new TileType[rows.length][rows[0].length()];
        for (int row = 0; row < rows.length; row++) {
            for (int column = 0; column < rows[row].length(); column++) {
                tiles[row][column] = rows[row].charAt(column) == '#' ? TileType.WALL : TileType.LAB_FLOOR;
            }
        }
        return new TileMap(tiles, 40, 1, 1);
    }
}
