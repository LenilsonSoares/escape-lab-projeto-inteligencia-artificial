package br.edu.unex.sentinela.game;

/**
 * Nível escolhido no briefing. Muda poucas coisas: velocidade e visão dos robôs,
 * proteção depois de um toque e a faixa de vagas de ataque em que o diretor de IA
 * continua ajustando a dificuldade sozinho.
 */
public enum Difficulty {
    EASY("FÁCIL", 0.85, 0.8, 3.0, 1, 1, 2),
    NORMAL("NORMAL", 1.0, 1.0, 2.0, 1, 2, 3),
    HARD("DIFÍCIL", 1.15, 1.2, 1.5, 2, 3, 3);

    private final String label;
    private final double robotSpeed;
    private final double visionRange;
    private final double hitProtection;
    private final int minAttackers;
    private final int startAttackers;
    private final int maxAttackers;

    Difficulty(String label, double robotSpeed, double visionRange, double hitProtection,
            int minAttackers, int startAttackers, int maxAttackers) {
        this.label = label;
        this.robotSpeed = robotSpeed;
        this.visionRange = visionRange;
        this.hitProtection = hitProtection;
        this.minAttackers = minAttackers;
        this.startAttackers = startAttackers;
        this.maxAttackers = maxAttackers;
    }

    /** Nível vizinho (-1 mais fácil, +1 mais difícil), parando nas pontas. */
    public Difficulty step(int direction) {
        int index = Math.clamp(ordinal() + Integer.signum(direction), 0, values().length - 1);
        return values()[index];
    }

    public String label() { return label; }
    public double robotSpeed() { return robotSpeed; }
    public double visionRange() { return visionRange; }
    public double hitProtection() { return hitProtection; }
    public int minAttackers() { return minAttackers; }
    public int startAttackers() { return startAttackers; }
    public int maxAttackers() { return maxAttackers; }
}
