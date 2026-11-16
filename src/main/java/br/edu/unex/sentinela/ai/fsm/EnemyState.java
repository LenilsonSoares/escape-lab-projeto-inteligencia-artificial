package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;

/**
 * Um estado da FSM. O enter prepara o estado (normalmente pede a rota ao A*),
 * o update roda a cada quadro e decide as transições, e o exit faz a limpeza.
 */
public interface EnemyState {

    String name();

    default void enter(Robot robot) {
    }

    void update(Robot robot, StateMachine machine, double deltaTime);

    default void exit(Robot robot) {
    }
}
