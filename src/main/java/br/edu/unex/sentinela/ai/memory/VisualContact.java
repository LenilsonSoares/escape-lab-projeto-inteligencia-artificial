package br.edu.unex.sentinela.ai.memory;

import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.Objects;

/** Resultado da visão neste quadro: tile onde o jogador foi visto e distância em tiles. */
public record VisualContact(GridPosition position, double distanceTiles) {

    public VisualContact {
        Objects.requireNonNull(position, "A posição vista não pode ser nula");
    }
}
