package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DoorTest {

    private static final TileType F = TileType.LAB_FLOOR;
    private static final TileType D = TileType.DOOR;

    private static TileMap corridorWithDoor() {
        return new TileMap(new TileType[][]{{F, D, F}}, 10, 0, 0);
    }

    @Test
    void closedDoorBlocksWalkingJumpingAndVision() {
        TileMap tileMap = corridorWithDoor();

        assertFalse(tileMap.doorsOpen());
        assertFalse(tileMap.isWalkable(0, 1));
        assertFalse(tileMap.isJumpable(0, 1));
        assertTrue(tileMap.blocksVision(0, 1));
        assertFalse(tileMap.canOccupy(12.0, 2.0, 6.0, 6.0));
    }

    @Test
    void openDoorWorksLikeFloor() {
        TileMap tileMap = corridorWithDoor();

        assertTrue(tileMap.openDoors());

        assertTrue(tileMap.doorsOpen());
        assertTrue(tileMap.isWalkable(0, 1));
        assertFalse(tileMap.isJumpable(0, 1));
        assertFalse(tileMap.blocksVision(0, 1));
        assertTrue(tileMap.canOccupy(12.0, 2.0, 6.0, 6.0));
    }

    @Test
    void opensDoorsOnlyOnce() {
        TileMap tileMap = corridorWithDoor();

        assertTrue(tileMap.openDoors());
        assertFalse(tileMap.openDoors());
    }

    @Test
    void mapWithoutDoorsCannotOpenDoors() {
        TileMap tileMap = new TileMap(new TileType[][]{{F, F, F}}, 10, 0, 0);

        assertFalse(tileMap.hasDoors());
        assertFalse(tileMap.openDoors());
        assertFalse(tileMap.doorsOpen());
    }

    @Test
    void copyStartsWithClosedDoorsAndIsIndependent() {
        TileMap original = corridorWithDoor();
        original.openDoors();

        TileMap copy = original.copy();

        assertNotSame(original, copy);
        assertTrue(copy.hasDoors());
        assertFalse(copy.doorsOpen());
        assertFalse(copy.isWalkable(0, 1));
        assertTrue(original.isWalkable(0, 1));
    }

    @Test
    void doorTypeIsNotWalkableNorJumpableByItself() {
        assertFalse(TileType.DOOR.isWalkable());
        assertFalse(TileType.DOOR.isJumpable());
    }
}
