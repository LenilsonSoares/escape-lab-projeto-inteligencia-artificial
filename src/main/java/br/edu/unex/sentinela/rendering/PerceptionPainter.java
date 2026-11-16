package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.ai.perception.NoiseEvent;
import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Mostra o que os robôs veem (cones) e os ruídos que o jogador provoca. */
final class PerceptionPainter {

    static final int VISION_RAYS = 24;
    static final double NOISE_LIFETIME = 1.0;
    private static final double CONE_ALPHA = 0.13;
    private static final double CONE_SEEING_ALPHA = 0.30;
    private static final double LABEL_MIN_RADIUS = 24.0;
    private static final Font NOISE_FONT = Font.font("Monospaced", FontWeight.BOLD, 7.0);

    private final GraphicsContext graphics;
    private final double[] polygonX = new double[VISION_RAYS + 2];
    private final double[] polygonY = new double[VISION_RAYS + 2];

    PerceptionPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void drawVisionCones(GameWorld world, RenderLayout layout) {
        for (Enemy enemy : world.enemies()) {
            if (enemy.hasAi()) {
                drawVisionCone(enemy, world.tileMap(), layout);
            }
        }
        graphics.setGlobalAlpha(1.0);
    }

    void drawNoiseWaves(GameWorld world, RenderLayout layout) {
        for (NoiseEvent noise : world.recentNoises()) {
            drawNoiseWave(noise, world.time() - noise.time(), layout);
        }
    }

    static double waveProgress(double age) {
        return Math.clamp(age / NOISE_LIFETIME, 0.0, 1.0);
    }

    // Cresce rápido no início e desacelera, como uma onda que perde força.
    static double waveRadius(double maxRadius, double progress) {
        double remaining = 1.0 - progress;
        return maxRadius * (1.0 - remaining * remaining);
    }

    private void drawVisionCone(Enemy enemy, TileMap map, RenderLayout layout) {
        AutonomousAgent agent = enemy.agent();
        double[][] points = enemy.vision().visibilityPolygon(
                map, agent.centerX(), agent.centerY(), enemy.facing(), VISION_RAYS);
        polygonX[0] = layout.screenX(agent.centerX());
        polygonY[0] = layout.screenY(agent.centerY());
        for (int index = 0; index < points.length; index++) {
            polygonX[index + 1] = layout.screenX(points[index][0]);
            polygonY[index + 1] = layout.screenY(points[index][1]);
        }

        Color color = enemy.memory().inCombat()
                ? AiPalette.COMBAT_CONE
                : AiPalette.identity(enemy.type());
        double alpha = enemy.seesPlayer() ? CONE_SEEING_ALPHA : CONE_ALPHA;
        int count = points.length + 1;
        graphics.setFill(color);
        graphics.setGlobalAlpha(alpha);
        graphics.fillPolygon(polygonX, polygonY, count);
        graphics.setStroke(color);
        graphics.setLineWidth(Math.max(1.0, layout.scale()));
        graphics.setGlobalAlpha(Math.min(1.0, alpha + 0.30));
        graphics.strokePolygon(polygonX, polygonY, count);
    }

    private void drawNoiseWave(NoiseEvent noise, double age, RenderLayout layout) {
        double progress = waveProgress(age);
        if (progress >= 1.0) {
            return;
        }
        double fade = 1.0 - progress;
        double scale = layout.scale();
        double centerX = layout.screenX(noise.x());
        double centerY = layout.screenY(noise.y());
        double radius = waveRadius(noise.radius(), progress) * scale;
        Color color = AiPalette.noise(noise.source());

        graphics.setFill(color);
        graphics.setGlobalAlpha(0.07 * fade);
        graphics.fillOval(centerX - radius, centerY - radius, radius * 2.0, radius * 2.0);
        graphics.setStroke(color);
        graphics.setLineWidth(2.0 * scale);
        graphics.setGlobalAlpha(0.65 * fade);
        graphics.strokeOval(centerX - radius, centerY - radius, radius * 2.0, radius * 2.0);
        double inner = radius * 0.62;
        graphics.setLineWidth(scale);
        graphics.setGlobalAlpha(0.35 * fade);
        graphics.strokeOval(centerX - inner, centerY - inner, inner * 2.0, inner * 2.0);

        // O rótulo acompanha a borda de cima da onda, longe do jogador.
        if (radius > LABEL_MIN_RADIUS * scale) {
            graphics.setGlobalAlpha(Math.min(1.0, 1.4 * fade));
            drawNoiseLabel(noise, centerX, centerY - radius - 2.0 * scale, scale, color);
        }
        graphics.setGlobalAlpha(1.0);
    }

    private void drawNoiseLabel(NoiseEvent noise, double x, double y, double scale, Color color) {
        graphics.save();
        graphics.translate(x, y);
        graphics.scale(scale, scale);
        graphics.setFill(AiPalette.LABEL_BACKGROUND);
        graphics.fillRect(-18.0, -7.0, 36.0, 9.0);
        graphics.setFill(color);
        graphics.setFont(NOISE_FONT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.fillText(noise.source().label(), 0.0, 0.5, 34.0);
        graphics.restore();
    }
}
