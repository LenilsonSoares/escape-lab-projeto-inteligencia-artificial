package br.edu.unex.sentinela.entity;

/**
 * Arquétipos de robô. Além da cor e da velocidade, cada um percebe e decide
 * de um jeito: o guarda vigia o cartão parado, a patrulha segue uma rota, o
 * batedor ronda e corre atrás de ruídos e o saltador pula equipamentos.
 */
public enum EnemyType {
    CHASER("PERSEGUIDOR", "GUARDA", "#ffb703", 100.0, false, 8.0, 70.0, 1.3),
    PATROLLER("PATRULHEIRO", "PATRULHA", "#b58aff", 80.0, false, 7.0, 90.0, 1.0),
    INTERCEPTOR("INTERCEPTADOR", "BATEDOR", "#ff5878", 120.0, false, 6.0, 110.0, 1.5),
    // Segue o jogador e pode saltar por cima de um equipamento para cortar caminho.
    JUMPER("SALTADOR", "SALTADOR", "#4fa8ff", 90.0, true, 7.0, 80.0, 1.0);

    private final String displayName;
    private final String role;
    private final String color;
    private final double speed;
    private final boolean jumpsOverEquipment;
    private final double visionRangeTiles;
    private final double fieldOfViewDegrees;
    private final double hearingSensitivity;

    EnemyType(String displayName, String role, String color, double speed, boolean jumpsOverEquipment,
            double visionRangeTiles, double fieldOfViewDegrees, double hearingSensitivity) {
        this.displayName = displayName;
        this.role = role;
        this.color = color;
        this.speed = speed;
        this.jumpsOverEquipment = jumpsOverEquipment;
        this.visionRangeTiles = visionRangeTiles;
        this.fieldOfViewDegrees = fieldOfViewDegrees;
        this.hearingSensitivity = hearingSensitivity;
    }

    public String displayName() { return displayName; }
    public String role() { return role; }
    public String color() { return color; }
    public double speed() { return speed; }
    public boolean jumpsOverEquipment() { return jumpsOverEquipment; }
    public double visionRangeTiles() { return visionRangeTiles; }
    public double fieldOfViewDegrees() { return fieldOfViewDegrees; }
    public double hearingSensitivity() { return hearingSensitivity; }
    public boolean guardsPost() { return this == CHASER; }
    public boolean usesBehaviorTree() { return this == INTERCEPTOR; }
}
