package br.edu.unex.sentinela.ai.bt;

/** Guarda qual ação rodou no último tick, para a depuração e para cancelar ações. */
public final class Trace {

    private Action activeAction;

    void ran(Action action) {
        activeAction = action;
    }

    Action activeAction() {
        return activeAction;
    }

    void clear() {
        activeAction = null;
    }
}
