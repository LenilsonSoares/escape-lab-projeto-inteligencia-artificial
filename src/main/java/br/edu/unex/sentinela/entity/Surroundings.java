package br.edu.unex.sentinela.entity;

import br.edu.unex.sentinela.ai.perception.NoiseEvent;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * O que existe ao redor do robô neste quadro. O robô só usa a posição do
 * jogador se os sensores dele conseguirem perceber o jogador.
 */
public record Surroundings(double now, double playerX, double playerY, GridPosition playerTile,
        MovementInput playerMovement, List<NoiseEvent> noises, LongSupplier eventIds) {
}
