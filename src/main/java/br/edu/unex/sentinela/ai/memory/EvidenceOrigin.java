package br.edu.unex.sentinela.ai.memory;

/** Como o robô obteve a evidência. */
public enum EvidenceOrigin {
    VISION("VISÃO"),
    HEARING("AUDIÇÃO"),
    RADIO("RÁDIO");

    private final String label;

    EvidenceOrigin(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
