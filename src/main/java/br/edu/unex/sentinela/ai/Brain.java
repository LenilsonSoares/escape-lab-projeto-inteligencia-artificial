package br.edu.unex.sentinela.ai;

/** Técnica de decisão usada por um robô (FSM ou árvore de comportamento). */
public interface Brain {

    void update(double deltaTime);

    /** Nome do estado ou da ação ativa, mostrado acima do robô. */
    String currentBehavior();

    String technique();
}
