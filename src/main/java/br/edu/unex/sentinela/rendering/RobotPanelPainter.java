package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.ai.memory.AgentMemory;
import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.ai.perception.NoiseSource;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.game.AiMode;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.NavigationStatus;
import br.edu.unex.sentinela.navigation.AStarPathfinder;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Painel esquerdo: objetivo, estado de cada robô, números do A* e legenda. */
final class RobotPanelPainter {

    private static final double ROBOT_ROW = 32.0;
    private static final double LEGEND_ROW = 15.0;
    private static final double WIDE_ROW = 160.0;
    private static final Color DOOR_LOCKED = Color.web("#d63c4a");
    private static final Color DOOR_STRIPE = Color.web("#f2a541");
    private static final Color CARD = Color.web("#5ef0ff");
    private static final Color CONE = Color.web("#b58aff");
    private static final double[] CONE_X = {1.0, 12.0, 12.0};
    private static final double[] CONE_Y = {-3.0, -9.0, 3.0};

    private final PanelKit kit;
    private final GraphicsContext graphics;

    RobotPanelPainter(PanelKit kit) {
        this.kit = kit;
        this.graphics = kit.graphics();
    }

    void draw(GameWorld world, RenderLayout layout, boolean debugVisible, LaboratoryTheme theme) {
        double x = layout.leftRailX();
        double y = layout.mapY();
        double width = layout.railWidth();
        double height = layout.mapHeight();
        double padding = layout.compact() ? 10.0 : 14.0;
        double contentX = x + padding;
        double contentWidth = width - padding * 2.0;
        boolean fullAi = world.mode() == AiMode.FULL_AI;

        kit.techPanel(x, y, width, height, theme.accent());
        kit.panelTitle(
                fullAi ? "IA DOS ROBÔS" : "NAVEGAÇÃO A*",
                fullAi ? "PERCEPÇÃO, MEMÓRIA E DECISÃO" : "ATIVIDADES DE A* DINÂMICO",
                contentX, y, contentWidth, layout.compact());

        double cursor = drawObjective(world, contentX, y + 72.0, contentWidth);
        cursor = drawRobots(world, contentX, cursor, contentWidth);
        if (debugVisible) {
            cursor = drawAStar(world, contentX, cursor, contentWidth);
        }
        drawLegend(contentX, cursor, contentWidth);
        drawMapInfo(world, contentX, y + height, contentWidth, layout.compact());
    }

    private double drawObjective(GameWorld world, double x, double y, double width) {
        kit.sectionTitle("OBJETIVO", x, y, width);
        String objective = world.escapeCompleted()
                ? "SISTEMA DE SEGURANÇA DESLIGADO"
                : AiText.objective(world.cardPosition().isPresent(), world.cardCollected());
        Color color = world.cardCollected() || world.escapeCompleted() ? PanelKit.EXIT : PanelKit.TEXT;
        kit.text(objective, x, y + 17.0, width, kit.labelFont, color);
        kit.divider(x, y + 30.0, width);
        return y + 50.0;
    }

    private double drawRobots(GameWorld world, double x, double y, double width) {
        kit.sectionTitle("ROBÔS DE SEGURANÇA", x, y, width);
        double rowY = y + 20.0;
        for (Enemy enemy : world.enemies()) {
            drawRobotRow(enemy, x, rowY, width);
            rowY += ROBOT_ROW;
        }
        kit.divider(x, rowY - 10.0, width);
        return rowY + 10.0;
    }

    private void drawRobotRow(Enemy enemy, double x, double y, double width) {
        Color identity = AiPalette.identity(enemy.type());
        kit.swatch(identity, x, y);
        String technique = enemy.hasAi() ? enemy.brain().technique() : "A*";
        // No painel estreito o papel não cabe ao lado do nome; fica só a técnica.
        boolean narrow = width < WIDE_ROW;
        kit.text(enemy.type().displayName(), x + 18.0, y, narrow ? width - 46.0 : width * 0.52,
                kit.labelFont, identity);
        kit.rightText(narrow ? technique : enemy.type().role() + " · " + technique, x + width, y,
                narrow ? 26.0 : width * 0.42, kit.smallFont, PanelKit.MUTED);

        double lineY = y + 13.0;
        if (!enemy.hasAi()) {
            kit.text(navigationText(enemy.navigationStatus()), x + 18.0, lineY, width - 18.0,
                    kit.smallFont, PanelKit.TEXT);
            return;
        }
        AgentMemory memory = enemy.memory();
        Color levelColor = AiPalette.level(memory.level());
        kit.text(AiText.behavior(enemy.brain().currentBehavior()), x + 18.0, lineY, width * 0.44, kit.labelFont, levelColor);
        double barX = x + width * 0.56;
        double barWidth = width * 0.27;
        kit.bar(barX, lineY - 5.0, barWidth, 4.0, memory.suspicion() / 100.0, levelColor);
        kit.rightText("%d%%".formatted(Math.round(memory.suspicion())), x + width, lineY,
                width * 0.16, kit.labelFont, levelColor);
    }

    private double drawAStar(GameWorld world, double x, double y, double width) {
        Enemy focus = DebugOverlayPainter.aStarFocus(world.enemies());
        AStarPathfinder.SearchStats search = focus.lastSearch();
        Color identity = AiPalette.identity(focus.type());
        kit.sectionTitle("A* · " + focus.type().displayName(), x, y, width);
        double lineY = y + 18.0;
        kit.line("DESTINO", AiText.tile(focus.destination()), identity, x, lineY, width);
        kit.line("NÓS VISITADOS", Integer.toString(search.visited().size()), PanelKit.ACCENT,
                x, lineY + PanelKit.LINE, width);
        kit.line("CUSTO g", Integer.toString(search.pathCost()), PanelKit.TEXT,
                x, lineY + PanelKit.LINE * 2, width);
        kit.line("HEURÍSTICA h", Integer.toString(search.heuristic()), PanelKit.TEXT,
                x, lineY + PanelKit.LINE * 3, width);
        kit.line("f = g + h", Integer.toString(search.pathCost() + search.heuristic()),
                PanelKit.DESTINATION, x, lineY + PanelKit.LINE * 4, width);
        kit.line("PASSOS DA ROTA", routeSteps(focus, search),
                search.found() ? PanelKit.TEXT : PanelKit.DANGER, x, lineY + PanelKit.LINE * 5, width);
        double end = lineY + PanelKit.LINE * 5;
        kit.divider(x, end + 10.0, width);
        return end + 30.0;
    }

    private void drawLegend(double x, double y, double width) {
        kit.sectionTitle("LEGENDA", x, y, width);
        double rowY = y + 18.0;
        kit.swatch(PanelKit.PLAYER, x, rowY);
        kit.text("JOGADOR", x + 20.0, rowY, width - 20.0, kit.smallFont, PanelKit.TEXT);
        drawConeIcon(x, rowY + LEGEND_ROW);
        kit.text("CAMPO DE VISÃO", x + 20.0, rowY + LEGEND_ROW, width - 20.0, kit.smallFont, PanelKit.TEXT);
        drawWaveIcon(x, rowY + LEGEND_ROW * 2);
        kit.text("ONDA DE SOM (RUÍDO)", x + 20.0, rowY + LEGEND_ROW * 2, width - 20.0,
                kit.smallFont, PanelKit.TEXT);
        drawSuspicionIcon(x, rowY + LEGEND_ROW * 3);
        kit.text("SUSPEITA  ? ALERTA  ! COMBATE", x + 20.0, rowY + LEGEND_ROW * 3, width - 20.0,
                kit.smallFont, PanelKit.TEXT);
        drawDoorIcon(x, rowY + LEGEND_ROW * 4);
        kit.text("PORTA TRANCADA", x + 20.0, rowY + LEGEND_ROW * 4, width - 20.0, kit.smallFont, PanelKit.TEXT);
        kit.swatch(CARD, x, rowY + LEGEND_ROW * 5);
        kit.text("CARTÃO DE ACESSO", x + 20.0, rowY + LEGEND_ROW * 5, width - 20.0,
                kit.smallFont, PanelKit.TEXT);
        kit.swatch(PanelKit.EXIT, x, rowY + LEGEND_ROW * 6);
        kit.text("SAÍDA", x + 20.0, rowY + LEGEND_ROW * 6, width - 20.0, kit.smallFont, PanelKit.TEXT);
    }

    private void drawConeIcon(double x, double y) {
        graphics.save();
        graphics.translate(x, y);
        graphics.setFill(CONE);
        graphics.setGlobalAlpha(0.55);
        graphics.fillPolygon(CONE_X, CONE_Y, 3);
        graphics.restore();
    }

    private void drawWaveIcon(double x, double y) {
        graphics.setStroke(AiPalette.noise(NoiseSource.FOOTSTEP));
        graphics.setLineWidth(1.0);
        graphics.strokeOval(x + 3.0, y - 6.0, 6.0, 6.0);
        graphics.setGlobalAlpha(0.5);
        graphics.strokeOval(x, y - 9.0, 12.0, 12.0);
        graphics.setGlobalAlpha(1.0);
    }

    private void drawSuspicionIcon(double x, double y) {
        kit.bar(x, y - 5.0, 12.0, 4.0, 0.6, AiPalette.level(AlertLevel.INVESTIGATION));
    }

    private void drawDoorIcon(double x, double y) {
        graphics.setFill(DOOR_LOCKED);
        graphics.fillRect(x + 2.0, y - 9.0, 8.0, 12.0);
        graphics.setFill(DOOR_STRIPE);
        graphics.fillRect(x + 2.0, y - 5.0, 8.0, 2.0);
    }

    private void drawMapInfo(GameWorld world, double x, double panelBottom, double width, boolean compact) {
        kit.text("MAPA %d  •  %s".formatted(world.currentMap().number(), world.currentMap().displayName()),
                x, panelBottom - 36.0, width, kit.smallFont, PanelKit.MUTED);
        kit.text("%d × %d  •  TILE %d px".formatted(world.tileMap().rows(), world.tileMap().columns(),
                        world.tileMap().tileSize()),
                x, panelBottom - 18.0, width, compact ? kit.compactValueFont : kit.valueFont, PanelKit.TEXT);
    }

    // O agente esvazia a rota quando chega ou para; aí a rota já foi percorrida.
    private static String routeSteps(Enemy enemy, AStarPathfinder.SearchStats search) {
        if (!search.found()) {
            return "SEM ROTA";
        }
        int steps = enemy.agent().path().size() - 1;
        return steps > 0 ? Integer.toString(steps) : "CONCLUÍDA";
    }

    private static String navigationText(NavigationStatus status) {
        return switch (status) {
            case MOVING -> "EM ROTA";
            case DESTINATION_REACHED -> "DESTINO ALCANÇADO";
            case NO_PATH -> "SEM ROTA";
        };
    }
}
