package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Robot;

/** Nó da árvore. O tick recebe o tempo do quadro e registra o caminho percorrido. */
public interface Node {

    String name();

    Status tick(Robot robot, double deltaTime, Trace trace);
}
