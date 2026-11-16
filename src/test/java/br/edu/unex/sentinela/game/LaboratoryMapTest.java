package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.navigation.AStarPathfinder;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
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
    void keepsEveryWalkableAreaConnectedWithDoorsOpen() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.createTileMap();
            assertTrue(tileMap.openDoors(), laboratory.displayName());

            int[][] distances = distancesFromPlayer(tileMap);
            int reached = 0;
            for (int[] row : distances) {
                for (int distance : row) {
                    if (distance >= 0) {
                        reached++;
                    }
                }
            }

            assertEquals(countWalkableTiles(tileMap), reached, laboratory.displayName());
        }
    }

    @Test
    void locksTheExitUntilTheDoorsOpen() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.createTileMap();
            GridPosition exit = laboratory.exitPosition();

            assertEquals(-1, distancesFromPlayer(tileMap)[exit.row()][exit.column()],
                    laboratory.displayName());
            tileMap.openDoors();
            assertTrue(distancesFromPlayer(tileMap)[exit.row()][exit.column()] > 0,
                    laboratory.displayName());
        }
    }

    @Test
    void cardAndGuardPostAreReachableWithDoorsClosed() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.createTileMap();
            int[][] distances = distancesFromPlayer(tileMap);
            GridPosition card = laboratory.cardPosition().orElseThrow();
            GridPosition guard = laboratory.agentStart();

            assertTrue(distances[card.row()][card.column()] > 0, laboratory.displayName());
            assertTrue(distances[guard.row()][guard.column()] > 0, laboratory.displayName());
        }
    }

    @Test
    void placesCardFarFromPlayerAndNearTheGuard() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.tileMap();
            GridPosition card = laboratory.cardPosition().orElseThrow();
            GridPosition player = new GridPosition(tileMap.playerStartRow(), tileMap.playerStartColumn());
            GridPosition guard = laboratory.agentStart();
            int guardDistance = guard.manhattanDistanceTo(card);

            assertEquals(TileType.LAB_FLOOR, tileMap.tileAt(card.row(), card.column()));
            assertTrue(player.manhattanDistanceTo(card) >= 12, laboratory.displayName());
            assertTrue(guardDistance >= 2 && guardDistance <= 4, laboratory.displayName());
            assertTrue(distancesFrom(tileMap, guard)[card.row()][card.column()] <= 6,
                    laboratory.displayName());
        }
    }

    @Test
    void keepsDoorsAwayFromThePlayerStart() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.tileMap();
            GridPosition player = new GridPosition(tileMap.playerStartRow(), tileMap.playerStartColumn());
            for (int row = 0; row < tileMap.rows(); row++) {
                for (int column = 0; column < tileMap.columns(); column++) {
                    if (tileMap.tileAt(row, column) == TileType.DOOR) {
                        assertTrue(player.manhattanDistanceTo(new GridPosition(row, column)) > 2,
                                laboratory.displayName());
                    }
                }
            }
        }
    }

    @Test
    void createTileMapReturnsIndependentCopies() {
        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap first = laboratory.createTileMap();
            TileMap second = laboratory.createTileMap();

            assertNotSame(first, second);
            assertNotSame(laboratory.tileMap(), first);
            assertTrue(first.openDoors());

            assertFalse(second.doorsOpen());
            assertFalse(laboratory.tileMap().doorsOpen());
        }
    }

    @Test
    void spawnReferencePointsHaveNearbyFloorOutsideTheLockedArea() {
        List<GridPosition> references = List.of(
                new GridPosition(13, 18), new GridPosition(7, 10), new GridPosition(13, 10),
                new GridPosition(1, 18), new GridPosition(1, 1), new GridPosition(13, 1));

        for (LaboratoryMap laboratory : LaboratoryMap.values()) {
            TileMap tileMap = laboratory.createTileMap();
            int[][] distances = distancesFromPlayer(tileMap);
            for (GridPosition reference : references) {
                GridPosition nearest = nearestFloor(tileMap, reference);
                assertTrue(distances[nearest.row()][nearest.column()] >= 0,
                        laboratory.displayName() + " " + reference);
            }
        }
    }

    @Test
    void firstTwoMapsHaveThinEquipmentShortcutsForTheJumper() {
        assertTrue(countEquipmentShortcuts(LaboratoryMap.ESCAPE_ROUTE.tileMap()) >= 2);
        assertTrue(countEquipmentShortcuts(LaboratoryMap.DATA_CORE.tileMap()) >= 2);
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

    private static int[][] distancesFromPlayer(TileMap tileMap) {
        return distancesFrom(
                tileMap,
                new GridPosition(tileMap.playerStartRow(), tileMap.playerStartColumn())
        );
    }

    // Busca em largura: -1 marca os tiles que não podem ser alcançados a pé.
    private static int[][] distancesFrom(TileMap tileMap, GridPosition start) {
        int[][] distances = new int[tileMap.rows()][tileMap.columns()];
        for (int[] row : distances) {
            Arrays.fill(row, -1);
        }
        ArrayDeque<GridPosition> pending = new ArrayDeque<>();
        distances[start.row()][start.column()] = 0;
        pending.add(start);
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!pending.isEmpty()) {
            GridPosition position = pending.removeFirst();
            for (int[] direction : directions) {
                int row = position.row() + direction[0];
                int column = position.column() + direction[1];
                if (tileMap.isWalkable(row, column) && distances[row][column] < 0) {
                    distances[row][column] = distances[position.row()][position.column()] + 1;
                    pending.addLast(new GridPosition(row, column));
                }
            }
        }
        return distances;
    }

    private static GridPosition nearestFloor(TileMap tileMap, GridPosition reference) {
        GridPosition nearest = null;
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                GridPosition point = new GridPosition(row, column);
                if (tileMap.tileAt(row, column) == TileType.LAB_FLOOR
                        && (nearest == null
                        || reference.manhattanDistanceTo(point) < reference.manhattanDistanceTo(nearest))) {
                    nearest = point;
                }
            }
        }
        return nearest;
    }

    private static int countEquipmentShortcuts(TileMap tileMap) {
        int shortcuts = 0;
        for (int row = 1; row < tileMap.rows() - 1; row++) {
            for (int column = 1; column < tileMap.columns() - 1; column++) {
                if (tileMap.tileAt(row, column) != TileType.EQUIPMENT) {
                    continue;
                }
                boolean vertical = tileMap.isWalkable(row - 1, column)
                        && tileMap.isWalkable(row + 1, column)
                        && tileMap.tileAt(row, column - 1) == TileType.WALL
                        && tileMap.tileAt(row, column + 1) == TileType.WALL;
                boolean horizontal = tileMap.isWalkable(row, column - 1)
                        && tileMap.isWalkable(row, column + 1)
                        && tileMap.tileAt(row - 1, column) == TileType.WALL
                        && tileMap.tileAt(row + 1, column) == TileType.WALL;
                if (vertical || horizontal) {
                    shortcuts++;
                }
            }
        }
        return shortcuts;
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
