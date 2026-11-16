package br.edu.unex.sentinela.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import org.junit.jupiter.api.Test;

class DifficultyTest {

    @Test
    void stepsStopAtTheEnds() {
        assertEquals(Difficulty.EASY, Difficulty.EASY.step(-1));
        assertEquals(Difficulty.NORMAL, Difficulty.EASY.step(1));
        assertEquals(Difficulty.HARD, Difficulty.HARD.step(1));
    }

    @Test
    void harderLevelsAreFasterSeeFartherAndProtectLess() {
        assertTrue(Difficulty.EASY.robotSpeed() < Difficulty.HARD.robotSpeed());
        assertTrue(Difficulty.EASY.visionRange() < Difficulty.HARD.visionRange());
        assertTrue(Difficulty.EASY.hitProtection() > Difficulty.HARD.hitProtection());
        assertTrue(Difficulty.EASY.maxAttackers() < Difficulty.HARD.maxAttackers());
    }

    @Test
    void worldStartsNormalAndAppliesTheChosenLevel() {
        GameWorld world = new GameWorld();
        assertEquals(Difficulty.NORMAL, world.difficulty());

        world.changeDifficulty(1);

        assertEquals(Difficulty.HARD, world.difficulty());
        assertEquals(3, world.director().attackBudget());
        assertEquals(3, world.team().maxAttackers());
        Enemy patrol = world.enemies().stream().filter(enemy -> enemy.type() == EnemyType.PATROLLER)
                .findFirst().orElseThrow();
        assertEquals(EnemyType.PATROLLER.visionRangeTiles() * 1.2, patrol.vision().rangeTiles(), 1.0e-9);
        assertEquals(EnemyType.PATROLLER.speed() * 1.15, patrol.agent().currentSpeed(), 1.0e-9);
    }

    @Test
    void easyLevelKeepsTheDirectorBetweenOneAndTwoVacancies() {
        GameWorld world = new GameWorld();

        world.changeDifficulty(-1);

        assertEquals(Difficulty.EASY, world.difficulty());
        assertEquals(1, world.director().attackBudget());
        assertEquals(2, world.director().maxBudget());
    }
}
