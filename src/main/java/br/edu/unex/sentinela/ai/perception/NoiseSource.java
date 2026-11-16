package br.edu.unex.sentinela.ai.perception;

/** Origem de um ruído no laboratório. */
public enum NoiseSource {
    FOOTSTEP("PASSOS"),
    CARD_PICKUP("CARTÃO"),
    DOOR("PORTA");

    private final String label;

    NoiseSource(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
