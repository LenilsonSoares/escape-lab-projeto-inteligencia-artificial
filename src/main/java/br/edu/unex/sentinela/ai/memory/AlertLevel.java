package br.edu.unex.sentinela.ai.memory;

/** Níveis legíveis da suspeita contínua (0 a 100). */
public enum AlertLevel {
    ROUTINE("ROTINA"),
    ATTENTION("ATENÇÃO"),
    INVESTIGATION("INVESTIGAÇÃO"),
    COMBAT("COMBATE");

    private final String label;

    AlertLevel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
