package br.edu.unex.sentinela.game;

/** Modo dos robôs. A demonstração do A* reproduz as atividades de navegação já entregues. */
public enum AiMode {
    FULL_AI("IA COMPLETA"),
    NAVIGATION_DEMO("DEMONSTRAÇÃO A*");

    private final String label;

    AiMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
