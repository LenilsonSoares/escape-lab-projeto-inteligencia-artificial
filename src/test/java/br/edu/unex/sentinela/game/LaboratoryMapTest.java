package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LaboratoryMapTest {

    @Test
    void providesThreeLaboratoriesWithRequiredDimensionsAndTileTypes() {
        assertEquals(3, LaboratoryMap.values().length);

        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.tileMap();
            Set<TileType> types = EnumSet.noneOf(TileType.class);
            for (int row = 0; row < tileMap.rows(); row++) {
                for (int column = 0; column < tileMap.columns(); column++) {
                    types.add(tileMap.tileAt(row, column));
                }
            }

            assertEquals(15, tileMap.rows());
            assertEquals(20, tileMap.columns());
            assertEquals(EnumSet.allOf(TileType.class), types);
            assertEquals(
                    TileType.EXIT,
                    tileMap.tileAt(
                            laboratory.exitPosition().row(),
                            laboratory.exitPosition().column()
                    )
            );
            assertTrue(tileMap.isWalkable(
                    laboratory.exitPosition().row(),
                    laboratory.exitPosition().column()
            ));
        }
    }

    @Test
    void keepsEveryWalkableAreaConnectedInAllMaps() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.tileMap();
            boolean[][] visited = new boolean[tileMap.rows()][tileMap.columns()];
            ArrayDeque<int[]> pending = new ArrayDeque<>();
            pending.add(new int[]{tileMap.playerStartRow(), tileMap.playerStartColumn()});
            visited[tileMap.playerStartRow()][tileMap.playerStartColumn()] = true;
            int reached = 0;

            while (!pending.isEmpty()) {
                int[] position = pending.removeFirst();
                reached++;
                visit(tileMap, visited, pending, position[0] - 1, position[1]);
                visit(tileMap, visited, pending, position[0] + 1, position[1]);
                visit(tileMap, visited, pending, position[0], position[1] - 1);
                visit(tileMap, visited, pending, position[0], position[1] + 1);
            }

            assertEquals(countWalkableTiles(tileMap), reached, laboratory.displayName());
        }
    }

    @Test
    void calculatesAnInitialRouteInEveryLaboratory() {
        AStarPathfinder pathfinder = new AStarPathfinder();

        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.tileMap();
            var path = pathfinder.findPath(
                    tileMap,
                    laboratory.agentStart(),
                    new GridPosition(
                            tileMap.playerStartRow(),
                            tileMap.playerStartColumn()
                    )
            );

            assertFalse(path.isEmpty(), laboratory.displayName());
            assertEquals(laboratory.agentStart(), path.get(0));
            assertEquals(
                    new GridPosition(tileMap.playerStartRow(), tileMap.playerStartColumn()),
                    path.get(path.size() - 1)
            );
        }
    }

    @Test
    void selectsMapsByTheirDisplayedNumber() {
        assertEquals(LaboratoryMap.ESCAPE_ROUTE, LaboratoryMap.fromNumber(1));
        assertEquals(LaboratoryMap.DATA_CORE, LaboratoryMap.fromNumber(2));
        assertEquals(LaboratoryMap.CONTAINMENT, LaboratoryMap.fromNumber(3));
    }

    private static void visit(
            TileMap tileMap,
            boolean[][] visited,
            ArrayDeque<int[]> pending,
            int row,
            int column
    ) {
        if (tileMap.isWalkable(row, column) && !visited[row][column]) {
            visited[row][column] = true;
            pending.addLast(new int[]{row, column});
        }
    }

    private static int countWalkableTiles(TileMap tileMap) {
        int count = 0;
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.isWalkable(row, column)) {
                    count++;
                }
            }
        }
        return count;
    }
}
