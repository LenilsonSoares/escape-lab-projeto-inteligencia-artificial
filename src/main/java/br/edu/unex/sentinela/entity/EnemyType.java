package br.edu.unex.sentinela.entity;

/** Tipos de robô, com identificação visual e velocidades próprias. */
public enum EnemyType {
    CHASER("PERSEGUIDOR", "#ffb703", 100.0),
    PATROLLER("PATRULHEIRO", "#b58aff", 80.0),
    INTERCEPTOR("INTERCEPTADOR", "#ff5878", 120.0);

    private final String displayName;
    private final String color;
    private final double speed;

    EnemyType(String displayName, String color, double speed) {
        this.displayName = displayName;
        this.color = color;
        this.speed = speed;
    }

    public String displayName() { return displayName; }
    public String color() { return color; }
    public double speed() { return speed; }
}
