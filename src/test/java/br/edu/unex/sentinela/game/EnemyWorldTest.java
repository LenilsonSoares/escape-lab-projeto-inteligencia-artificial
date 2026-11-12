package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.input.MovementInput;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnemyWorldTest {
    @Test
    void allMapsSpawnThreeDifferentNavigableEnemies() {
        GameWorld world = new GameWorld();
        for (int map = 1; map <= 3; map++) {
            world.selectMap(map);
            assertEquals(3, world.enemies().size());
            assertEquals(3, new HashSet<>(world.enemies().stream().map(Enemy::type).toList()).size());
            assertEquals(3, new HashSet<>(world.enemies().stream()
                    .map(enemy -> enemy.agent().currentGridPosition()).toList()).size());
            for (int frame = 0; frame < 1_200; frame++) {
                world.update(1.0 / 60.0, MovementInput.NONE);
                for (Enemy enemy : world.enemies()) {
                    assertTrue(world.tileMap().canOccupy(enemy.agent().x(), enemy.agent().y(),
                            enemy.agent().size(), enemy.agent().size()));
                }
            }
        }
    }

    @Test
    void contactDeductsPointsOnceAndKeepsPenaltyAcrossMaps() {
        GameWorld world = new GameWorld();
        assertEquals(1_000, world.score());
        for (int frame = 0; frame < 1_200; frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
        }
        int penalties = world.penalties();
        int score = world.score();
        assertTrue(penalties >= 2);
        assertEquals(Math.max(0, 1_000 - penalties * 100), score);
        for (int frame = 0; frame < 120; frame++) {
            world.update(1.0 / 60.0, MovementInput.NONE);
        }
        assertEquals(score, world.score());
        assertEquals(penalties, world.penalties());
        world.selectMap(2);
        assertEquals(score, world.score());
        assertEquals(penalties, world.penalties());
    }
}
