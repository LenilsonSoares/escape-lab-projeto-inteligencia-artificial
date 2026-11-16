package br.edu.unex.sentinela.ai.perception;

import br.edu.unex.sentinela.game.TileMap;
import java.util.Objects;

/**
 * Visão do robô: alcance em tiles, ângulo do cone e oclusão por obstáculos.
 *
 * <p>Ângulos em radianos no sistema da tela: 0 aponta para a direita (+x) e
 * PI/2 para baixo (+y). A oclusão consulta {@link TileMap#blocksVision}:
 * paredes e portas fechadas bloqueiam; equipamentos são baixos e não bloqueiam.
 */
public final class VisionSensor {

    /** Os raios andam 1/8 de tile por passo: fino o bastante para não "pular" uma parede. */
    private static final double STEPS_PER_TILE = 8.0;

    private final double rangeTiles;
    private final double fieldOfViewDegrees;

    /** @param fieldOfViewDegrees abertura total do cone (ex.: 90 = 45 para cada lado). */
    public VisionSensor(double rangeTiles, double fieldOfViewDegrees) {
        if (!Double.isFinite(rangeTiles) || rangeTiles <= 0.0) {
            throw new IllegalArgumentException("O alcance da visão deve ser positivo");
        }
        if (!Double.isFinite(fieldOfViewDegrees) || fieldOfViewDegrees <= 0.0 || fieldOfViewDegrees > 360.0) {
            throw new IllegalArgumentException("O ângulo de visão deve estar entre 0 (exclusivo) e 360 graus");
        }
        this.rangeTiles = rangeTiles;
        this.fieldOfViewDegrees = fieldOfViewDegrees;
    }

    public double rangeTiles() {
        return rangeTiles;
    }

    public double fieldOfViewDegrees() {
        return fieldOfViewDegrees;
    }

    /** Distância euclidiana em tiles entre dois pontos em pixels. */
    public static double distanceTiles(TileMap map, double fromX, double fromY, double toX, double toY) {
        Objects.requireNonNull(map, "O mapa não pode ser nulo");
        return Math.hypot(toX - fromX, toY - fromY) / map.tileSize();
    }

    /** Alvo dentro do alcance e do cone, sem considerar obstáculos. */
    public boolean inRangeAndAngle(TileMap map, double fromX, double fromY, double facing,
            double targetX, double targetY) {
        double distance = distanceTiles(map, fromX, fromY, targetX, targetY);
        if (distance > rangeTiles) {
            return false;
        }
        if (distance == 0.0) {
            // Alvo exatamente na posição do robô: não há direção, conta como visto.
            return true;
        }
        double angleToTarget = Math.atan2(targetY - fromY, targetX - fromX);
        double difference = normalizeAngle(angleToTarget - facing);
        double halfFieldOfView = Math.toRadians(fieldOfViewDegrees) / 2.0;
        return Math.abs(difference) <= halfFieldOfView;
    }

    /** Alcance + cone + linha de visão livre. */
    public boolean canSee(TileMap map, double fromX, double fromY, double facing,
            double targetX, double targetY) {
        return inRangeAndAngle(map, fromX, fromY, facing, targetX, targetY)
                && hasLineOfSight(map, fromX, fromY, targetX, targetY);
    }

    /** Verdadeiro se nenhum tile que bloqueia a visão estiver entre os dois pontos. */
    public static boolean hasLineOfSight(TileMap map, double x0, double y0, double x1, double y1) {
        Objects.requireNonNull(map, "O mapa não pode ser nulo");
        double stepSize = map.tileSize() / STEPS_PER_TILE;
        double length = Math.hypot(x1 - x0, y1 - y0);
        int steps = (int) Math.ceil(length / stepSize);

        // Amostra o segmento do início ao fim (inclusive) em passos de no máximo 1/8 de tile.
        for (int step = 0; step <= steps; step++) {
            double t = steps == 0 ? 1.0 : (double) step / steps;
            double x = x0 + (x1 - x0) * t;
            double y = y0 + (y1 - y0) * t;
            if (blocksAt(map, x, y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Polígono do campo de visão para depuração: {@code rays + 1} pontos
     * {x, y} distribuídos no cone, cada um parando no primeiro obstáculo ou no alcance.
     * A origem não entra na lista (quem desenha a acrescenta).
     */
    public double[][] visibilityPolygon(TileMap map, double x, double y, double facing, int rays) {
        Objects.requireNonNull(map, "O mapa não pode ser nulo");
        if (rays < 1) {
            throw new IllegalArgumentException("O polígono precisa de pelo menos um raio");
        }
        double fieldOfView = Math.toRadians(fieldOfViewDegrees);
        double startAngle = facing - fieldOfView / 2.0;
        double rangePixels = rangeTiles * map.tileSize();
        double stepSize = map.tileSize() / STEPS_PER_TILE;
        int steps = (int) Math.ceil(rangePixels / stepSize);

        double[][] points = new double[rays + 1][];
        for (int ray = 0; ray <= rays; ray++) {
            double angle = startAngle + fieldOfView * ray / rays;
            double directionX = Math.cos(angle);
            double directionY = Math.sin(angle);

            // Avança pelo raio e guarda o último ponto livre antes de bater num obstáculo.
            double lastFreeX = x;
            double lastFreeY = y;
            for (int step = 1; step <= steps; step++) {
                double distance = Math.min(step * stepSize, rangePixels);
                double pointX = x + directionX * distance;
                double pointY = y + directionY * distance;
                if (blocksAt(map, pointX, pointY)) {
                    break;
                }
                lastFreeX = pointX;
                lastFreeY = pointY;
            }
            points[ray] = new double[] {lastFreeX, lastFreeY};
        }
        return points;
    }

    /** Converte um ponto em pixels para o tile correspondente e pergunta se ele bloqueia a visão. */
    private static boolean blocksAt(TileMap map, double x, double y) {
        int row = (int) Math.floor(y / map.tileSize());
        int column = (int) Math.floor(x / map.tileSize());
        return map.blocksVision(row, column);
    }

    /** Leva um ângulo para o intervalo [-PI, PI]. */
    private static double normalizeAngle(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }
}
