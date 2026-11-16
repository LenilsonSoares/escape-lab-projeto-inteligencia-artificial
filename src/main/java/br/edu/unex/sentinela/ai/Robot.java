package br.edu.unex.sentinela.ai;

import br.edu.unex.sentinela.ai.coop.TeamBlackboard;
import br.edu.unex.sentinela.ai.memory.AgentMemory;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;

/**
 * O que a FSM e a árvore de comportamento podem pedir a um robô.
 * Assim a decisão fica separada do movimento: o estado escolhe o destino e o
 * robô usa o A* para chegar lá.
 */
public interface Robot {

    String name();

    double now();

    TileMap map();

    AgentMemory memory();

    TeamBlackboard team();

    GridPosition tile();

    /** Calcula uma rota com A* até o destino. */
    void goTo(GridPosition target);

    GridPosition destination();

    boolean arrived();

    /** Verdadeiro quando o último goTo não encontrou caminho. */
    boolean routeFailed();

    void stopMoving();

    /** Custo do caminho até o destino, ou -1 se não houver caminho. */
    int pathCost(GridPosition target);

    void faceTowards(GridPosition target);

    /** Gira o olhar de um lado para o outro, como uma câmera de vigilância. */
    void lookAround(double deltaTime);

    /** Corre durante a perseguição e volta ao passo normal fora dela. */
    void setRunning(boolean running);

    /** Destino da perseguição conforme a tática do arquétipo. */
    GridPosition chaseTarget();

    boolean guardsPost();

    GridPosition post();

    List<GridPosition> routePoints();

    /** Ponto perto da porta de saída usado para cercar o jogador. */
    GridPosition exitWatchPoint();

    boolean touchedPlayer();

    void clearTouch();
}
