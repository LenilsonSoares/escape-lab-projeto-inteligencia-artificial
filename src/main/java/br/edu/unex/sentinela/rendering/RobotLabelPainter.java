package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.ai.memory.AgentMemory;
import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.game.GameWorld;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/** Etiqueta acima de cada robô com nome, estado atual, suspeita e ícone de alerta. */
final class RobotLabelPainter {

    private static final Font LABEL_FONT = Font.font("Monospaced", FontWeight.BOLD, 8.0);
    private static final Font ICON_FONT = Font.font("Monospaced", FontWeight.BOLD, 10.0);
    private static final double PADDING = 4.0;
    private static final double HEIGHT = 16.0;
    private static final double BEHIND_PLAYER_ALPHA = 0.4;
    private static final double ICON_WIDTH = 11.0;
    private static final double SPRITE_HALF = 20.0;
    private static final double MARGIN = 2.0;

    private final GraphicsContext graphics;
    // Os textos se repetem (4 robôs × poucos estados), então a largura medida fica guardada.
    private final Map<String, Double> textWidths = new HashMap<>();

    RobotLabelPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(GameWorld world, RenderLayout layout, boolean debugVisible) {
        if (!debugVisible) {
            drawAlertsOnly(world, layout);
            return;
        }
        double mapWidth = world.tileMap().pixelWidth();
        List<Label> labels = new ArrayList<>();
        for (Enemy enemy : world.enemies()) {
            if (enemy.hasAi()) {
                labels.add(place(enemy, mapWidth));
            }
        }
        separate(labels);
        for (Label label : labels) {
            drawLabel(label, world.player(), layout);
        }
    }

    private Label place(Enemy enemy, double mapWidth) {
        String prefix = AiText.robotLabel(enemy.type().displayName(), "");
        String behavior = AiText.behavior(enemy.brain().currentBehavior());
        double width = textWidth(prefix) + textWidth(behavior) + PADDING * 2.0;
        String icon = AiText.alertIcon(enemy.memory().level());
        double iconSpace = icon.isEmpty() ? 0.0 : ICON_WIDTH + MARGIN;
        AutonomousAgent agent = enemy.agent();
        double lift = Math.sin(agent.jumpProgress() * Math.PI) * ActorPainter.JUMP_HEIGHT;
        double left = Math.clamp(agent.centerX() - (width + iconSpace) / 2.0,
                MARGIN, mapWidth - width - iconSpace - MARGIN);
        double top = Math.max(MARGIN, agent.centerY() - SPRITE_HALF - lift - HEIGHT - 3.0);
        return new Label(enemy, prefix, behavior, icon, left, top, width, iconSpace);
    }

    // Robôs próximos teriam etiquetas uma sobre a outra: a de cima sobe até ficar livre.
    private static void separate(List<Label> labels) {
        labels.sort(Comparator.comparingDouble(Label::top).reversed());
        for (int index = 1; index < labels.size(); index++) {
            Label current = labels.get(index);
            for (int other = 0; other < index; other++) {
                if (current.overlaps(labels.get(other))) {
                    current.top = labels.get(other).top - HEIGHT - MARGIN;
                    other = -1;
                }
            }
            if (current.top < MARGIN) {
                current.top = MARGIN;
            }
        }
    }

    private void drawLabel(Label label, Player player, RenderLayout layout) {
        AgentMemory memory = label.enemy.memory();
        Color identity = AiPalette.identity(label.enemy.type());
        Color levelColor = AiPalette.level(memory.level());
        double left = label.left;
        double top = label.top;
        double width = label.width;

        graphics.save();
        graphics.translate(layout.mapX(), layout.mapY());
        graphics.scale(layout.scale(), layout.scale());
        // A etiqueta fica translúcida quando passa por cima do jogador.
        if (overlapsPlayer(player, left, top, width + label.iconSpace)) {
            graphics.setGlobalAlpha(BEHIND_PLAYER_ALPHA);
        }
        drawPill(left, top, width, identity);
        graphics.setFont(LABEL_FONT);
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(identity);
        graphics.fillText(label.prefix, left + PADDING, top + 9.0);
        graphics.setFill(PanelKit.TEXT);
        graphics.fillText(label.behavior, left + PADDING + textWidth(label.prefix), top + 9.0);
        drawSuspicionBar(left + PADDING, top + 12.0, width - PADDING * 2.0,
                memory.suspicion() / 100.0, levelColor);
        if (!label.icon.isEmpty()) {
            drawIcon(label.icon, left + width + MARGIN, top, levelColor);
        }
        graphics.restore();
    }

    // Sem TAB a tela fica limpa: só o "?" ou o "!" sobre quem desconfiou ou viu o jogador.
    private void drawAlertsOnly(GameWorld world, RenderLayout layout) {
        for (Enemy enemy : world.enemies()) {
            if (!enemy.hasAi()) {
                continue;
            }
            String icon = AiText.alertIcon(enemy.memory().level());
            if (icon.isEmpty()) {
                continue;
            }
            AutonomousAgent agent = enemy.agent();
            double lift = Math.sin(agent.jumpProgress() * Math.PI) * ActorPainter.JUMP_HEIGHT;
            double x = agent.centerX() - ICON_WIDTH / 2.0;
            double y = Math.max(MARGIN, agent.centerY() - SPRITE_HALF - lift - HEIGHT - 2.0);
            graphics.save();
            graphics.translate(layout.mapX(), layout.mapY());
            graphics.scale(layout.scale(), layout.scale());
            drawIcon(icon, x, y, AiPalette.level(enemy.memory().level()));
            graphics.restore();
        }
    }

    private void drawPill(double x, double y, double width, Color identity) {
        graphics.setFill(AiPalette.LABEL_BACKGROUND);
        graphics.fillRoundRect(x, y, width, HEIGHT, 4.0, 4.0);
        graphics.setStroke(identity);
        graphics.setLineWidth(0.8);
        graphics.strokeRoundRect(x, y, width, HEIGHT, 4.0, 4.0);
    }

    private static boolean overlapsPlayer(Player player, double left, double top, double width) {
        return player.x() < left + width
                && player.x() + player.size() > left
                && player.y() < top + HEIGHT
                && player.y() + player.size() > top;
    }

    private void drawSuspicionBar(double x, double y, double width, double fraction, Color color) {
        graphics.setFill(AiPalette.BAR_TRACK);
        graphics.fillRect(x, y, width, 2.0);
        graphics.setFill(color);
        graphics.fillRect(x, y, width * Math.clamp(fraction, 0.0, 1.0), 2.0);
    }

    private void drawIcon(String icon, double x, double y, Color color) {
        graphics.setFill(color);
        graphics.fillRoundRect(x, y, ICON_WIDTH, HEIGHT, 3.0, 3.0);
        graphics.setFill(AiPalette.ICON_TEXT);
        graphics.setFont(ICON_FONT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.fillText(icon, x + ICON_WIDTH / 2.0, y + 12.0);
    }

    private static final class Label {
        final Enemy enemy;
        final String prefix;
        final String behavior;
        final String icon;
        final double left;
        final double width;
        final double iconSpace;
        double top;

        Label(Enemy enemy, String prefix, String behavior, String icon,
                double left, double top, double width, double iconSpace) {
            this.enemy = enemy;
            this.prefix = prefix;
            this.behavior = behavior;
            this.icon = icon;
            this.left = left;
            this.top = top;
            this.width = width;
            this.iconSpace = iconSpace;
        }

        double top() {
            return top;
        }

        boolean overlaps(Label other) {
            return left < other.left + other.width + other.iconSpace
                    && left + width + iconSpace > other.left
                    && top < other.top + HEIGHT
                    && top + HEIGHT > other.top;
        }
    }

    private double textWidth(String value) {
        return textWidths.computeIfAbsent(value, key -> {
            Text text = new Text(key);
            text.setFont(LABEL_FONT);
            return text.getLayoutBounds().getWidth();
        });
    }
}
