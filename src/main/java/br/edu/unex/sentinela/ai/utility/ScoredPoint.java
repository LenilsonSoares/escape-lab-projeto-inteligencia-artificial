package br.edu.unex.sentinela.ai.utility;

import br.edu.unex.sentinela.navigation.GridPosition;

/** Ponto de busca com cada fator da nota, para mostrar no painel de depuração. */
public record ScoredPoint(GridPosition position, double evidence, double unverified,
        double cost, double score) {
}
