package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.NavigationStatus;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Apresenta controles, navegação e métricas reais fora do tilemap.
 */
final class HudPainter {

    private static final Color PANEL = Color.web("#081521", 0.96);
    private static final Color PANEL_INNER = Color.web("#102638", 0.72);
    private static final Color BORDER = Color.web("#28556d");
    private static final Color ACCENT = Color.web("#45d7f2");
    private static final Color TEXT = Color.web("#e9f5ff");
    private static final Color MUTED = Color.web("#87a5b8");
    private static final Color PLAYER = Color.web("#38d9a9");
    private static final Color AGENT = Color.web("#ffb703");
    private static final Color DESTINATION = Color.web("#ffd166");
    private static final Color EXIT = Color.web("#52e889");
    private static final Color DANGER = Color.web("#ef476f");
    private static final Color LEGEND_BACKDROP = Color.web("#06121d");
    private static final Color MOVING_BACKGROUND = Color.web("#45d7f2", 0.14);
    private static final Color REACHED_BACKGROUND = Color.web("#38d9a9", 0.14);
    private static final Color NO_PATH_BACKGROUND = Color.web("#ef476f", 0.14);

    private final GraphicsContext graphics;
    private final MiniMapPainter miniMapPainter;
    private final MapProgressPainter mapProgressPainter;
    private final Font titleFont = Font.font("Monospaced", FontWeight.BOLD, 18.0);
    private final Font compactTitleFont = Font.font("Monospaced", FontWeight.BOLD, 14.0);
    private final Font headingFont = Font.font("Monospaced", FontWeight.BOLD, 12.0);
    private final Font valueFont = Font.font("Monospaced", FontWeight.NORMAL, 11.0);
    private final Font compactValueFont = Font.font("Monospaced", FontWeight.NORMAL, 10.0);
    private final Font labelFont = Font.font("System", FontWeight.BOLD, 9.0);
    private final Font smallFont = Font.font("System", FontWeight.NORMAL, 9.0);

    HudPainter(GraphicsContext graphics) {
        this.graphics = graphics;
        this.miniMapPainter = new MiniMapPainter(graphics);
        this.mapProgressPainter = new MapProgressPainter(graphics);
    }

    void draw(
            GameWorld world,
            FrameMetrics metrics,
            RenderLayout layout,
            boolean debugVisible,
            boolean sessionStarted
    ) {
        double scale = layout.scale();
        RenderLayout logicalLayout = layout.logicalCoordinates();

        graphics.save();
        graphics.scale(scale, scale);
        drawHeader(world, logicalLayout, LaboratoryTheme.forMap(world.currentMap()));
        if (debugVisible && logicalLayout.railWidth() >= 120.0) {
            drawNavigationPanel(world, logicalLayout, sessionStarted);
            drawTelemetryPanel(world, metrics, logicalLayout);
        }
        drawFooter(logicalLayout, debugVisible);
        graphics.restore();
    }

    private void drawHeader(
            GameWorld world,
            RenderLayout layout,
            LaboratoryTheme theme
    ) {
        drawTechPanel(
                layout.mapX(),
                layout.headerY(),
                layout.mapWidth(),
                RenderLayout.BAR_HEIGHT,
                theme.accent()
        );

        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(TEXT);
        graphics.setFont(titleFont);
        graphics.fillText(
                "ESCAPE LAB",
                layout.mapX() + 14.0,
                layout.headerY() + 24.0
        );

        mapProgressPainter.draw(world, layout, theme);

        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(theme.accent());
        graphics.setFont(headingFont);
        graphics.fillText(
                "%s  •  A* DINÂMICO".formatted(world.currentMap().displayName()),
                layout.mapX() + layout.mapWidth() - 14.0,
                layout.headerY() + 23.0,
                layout.mapWidth() * 0.34
        );
    }

    private void drawNavigationPanel(
            GameWorld world,
            RenderLayout layout,
            boolean sessionStarted
    ) {
        double x = layout.leftRailX();
        double y = layout.mapY();
        double width = layout.railWidth();
        double height = layout.mapHeight();
        double padding = layout.compact() ? 10.0 : 14.0;
        double contentX = x + padding;
        double contentWidth = width - padding * 2.0;

        drawTechPanel(x, y, width, height, ACCENT);
        drawPanelTitle(
                "NAVEGAÇÃO A*",
                world.escapeCompleted() ? "SEQUÊNCIA FINALIZADA" : "DESTINO DINÂMICO",
                contentX,
                y,
                contentWidth,
                layout.compact()
        );
        if (world.escapeCompleted()) {
            drawCompletionBadge(contentX, y + 56.0, contentWidth, layout.compact());
        } else {
            drawStatusBadge(
                    world.navigationStatus(),
                    contentX,
                    y + 56.0,
                    contentWidth,
                    layout.compact(),
                    sessionStarted
            );
        }

        drawSectionTitle("OBJETIVO", contentX, y + 112.0, contentWidth);
        drawInstruction(
                world.escapeCompleted() ? "FUGA CONCLUÍDA" : "ALCANCE A SAÍDA VERDE",
                contentX,
                y + 136.0,
                contentWidth
        );
        drawInstruction(
                world.escapeCompleted() ? "MAPAS FINALIZADOS" : "PARA AVANÇAR O MAPA",
                contentX,
                y + 151.0,
                contentWidth
        );

        drawDivider(contentX, y + 170.0, contentWidth);
        drawSectionTitle(
                world.escapeCompleted() ? "RESULTADO FINAL" : "ROTA ATUAL",
                contentX,
                y + 196.0,
                contentWidth
        );
        GridPosition destination = world.escapeCompleted()
                ? world.currentMap().exitPosition()
                : world.navigationDestination();
        GridPosition agentPosition = world.autonomousAgent().currentGridPosition();
        GridPosition playerPosition = playerPosition(world);
        drawMetric(
                world.escapeCompleted() ? "DESTINO FINAL" : "DESTINO",
                "L%02d  C%02d".formatted(destination.row(), destination.column()),
                contentX,
                y + 220.0,
                contentWidth,
                layout.compact()
        );
        drawMetric(
                world.escapeCompleted() ? "JOGADOR" : "AGENTE",
                world.escapeCompleted()
                        ? "L%02d  C%02d".formatted(playerPosition.row(), playerPosition.column())
                        : "L%02d  C%02d".formatted(agentPosition.row(), agentPosition.column()),
                contentX,
                y + 268.0,
                contentWidth,
                layout.compact()
        );
        drawMetric(
                "CAMINHO",
                world.escapeCompleted()
                        ? "CONCLUÍDO"
                        : "%02d PASSOS".formatted(Math.max(0, world.navigationPath().size() - 1)),
                contentX,
                y + 316.0,
                contentWidth,
                layout.compact()
        );

        drawDivider(contentX, y + 358.0, contentWidth);
        drawSectionTitle("LEGENDA", contentX, y + 384.0, contentWidth);
        drawLegendItem(PLAYER, "JOGADOR", contentX, y + 414.0, contentWidth);
        drawLegendItem(AGENT, "ROBÔ A*", contentX, y + 444.0, contentWidth);
        drawLegendItem(ACCENT, "ROTA CALCULADA", contentX, y + 474.0, contentWidth);
        drawLegendItem(EXIT, "SAÍDA VERDE", contentX, y + 504.0, contentWidth);

        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(MUTED);
        graphics.setFont(smallFont);
        graphics.fillText(
                "MAPA %d  •  %s".formatted(
                        world.currentMap().number(),
                        world.currentMap().displayName()
                ),
                contentX,
                y + height - 55.0,
                contentWidth
        );
        graphics.setFill(TEXT);
        graphics.setFont(layout.compact() ? compactValueFont : valueFont);
        graphics.fillText(
                "%d × %d  •  TILE %d px".formatted(
                        world.tileMap().rows(),
                        world.tileMap().columns(),
                        world.tileMap().tileSize()
                ),
                contentX,
                y + height - 33.0,
                contentWidth
        );
    }

    private void drawTelemetryPanel(
            GameWorld world,
            FrameMetrics metrics,
            RenderLayout layout
    ) {
        double x = layout.rightRailX();
        double y = layout.mapY();
        double width = layout.railWidth();
        double height = layout.mapHeight();
        double padding = layout.compact() ? 10.0 : 14.0;
        double contentX = x + padding;
        double contentWidth = width - padding * 2.0;

        drawTechPanel(x, y, width, height, PLAYER);
        drawPanelTitle("DIAGNÓSTICO", "DADOS DA EXECUÇÃO", contentX, y, contentWidth, layout.compact());

        drawSectionTitle(
                "MAPA LÓGICO %d × %d".formatted(
                        world.tileMap().rows(),
                        world.tileMap().columns()
                ),
                contentX,
                y + 78.0,
                contentWidth
        );
        double maximumMiniMapWidth = Math.min(
                contentWidth,
                layout.compact() ? 136.0 : 188.0
        );
        double miniMapWidth = MiniMapPainter.fittedWidth(
                world.tileMap(),
                maximumMiniMapWidth
        );
        double miniMapHeight = MiniMapPainter.proportionalHeight(world.tileMap(), miniMapWidth);
        double miniMapX = contentX + (contentWidth - miniMapWidth) / 2.0;
        double miniMapY = y + 92.0;
        miniMapPainter.draw(world, miniMapX, miniMapY, miniMapWidth, miniMapHeight);

        double executionY = miniMapY + miniMapHeight + 24.0;
        drawDivider(contentX, executionY - 12.0, contentWidth);
        drawSectionTitle("EXECUÇÃO", contentX, executionY + 8.0, contentWidth);
        drawSystemLine(
                "FPS",
                "%5.1f".formatted(metrics.framesPerSecond()),
                ACCENT,
                contentX,
                executionY + 36.0,
                contentWidth
        );
        drawSystemLine(
                "DELTA",
                "%5.2f ms".formatted(metrics.deltaTime() * 1_000.0),
                ACCENT,
                contentX,
                executionY + 62.0,
                contentWidth
        );
        GridPosition playerPosition = playerPosition(world);
        GridPosition agentPosition = world.autonomousAgent().currentGridPosition();
        GridPosition destination = world.escapeCompleted()
                ? world.currentMap().exitPosition()
                : world.navigationDestination();
        drawSystemLine(
                "JOGADOR",
                "L%02d C%02d".formatted(playerPosition.row(), playerPosition.column()),
                PLAYER,
                contentX,
                executionY + 88.0,
                contentWidth
        );
        drawSystemLine(
                "ROBÔ",
                "L%02d C%02d".formatted(agentPosition.row(), agentPosition.column()),
                AGENT,
                contentX,
                executionY + 114.0,
                contentWidth
        );
        drawSystemLine(
                "DESTINO",
                "L%02d C%02d".formatted(destination.row(), destination.column()),
                DESTINATION,
                contentX,
                executionY + 140.0,
                contentWidth
        );
        drawSystemLine(
                "CAMINHO",
                world.escapeCompleted()
                        ? "FINALIZADO"
                        : "%02d PASSOS".formatted(Math.max(0, world.navigationPath().size() - 1)),
                world.escapeCompleted() ? EXIT : ACCENT,
                contentX,
                executionY + 166.0,
                contentWidth
        );

        double systemsY = executionY + 192.0;
        drawDivider(contentX, systemsY - 12.0, contentWidth);
        drawSectionTitle("SISTEMAS", contentX, systemsY + 8.0, contentWidth);
        drawSystemLine("TILEMAP", "ATIVO", PLAYER, contentX, systemsY + 36.0, contentWidth);
        drawSystemLine("A* DINÂMICO", "ATIVO", ACCENT, contentX, systemsY + 62.0, contentWidth);
        drawSystemLine("COLISÕES", "ATIVO", PLAYER, contentX, systemsY + 88.0, contentWidth);

        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(MUTED);
        graphics.setFont(smallFont);
        graphics.fillText("DADOS REAIS DA EXECUÇÃO", contentX, y + height - 20.0, contentWidth);
    }

    private void drawFooter(RenderLayout layout, boolean debugVisible) {
        drawTechPanel(
                layout.mapX(),
                layout.footerY(),
                layout.mapWidth(),
                RenderLayout.BAR_HEIGHT,
                BORDER
        );

        graphics.setFont(valueFont);
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(TEXT);
        graphics.fillText(
                "WASD/SETAS • MOVER   |   ESC • PAUSA   |   1 2 3 • DEMO   |   TAB   |   F11",
                layout.mapX() + 14.0,
                layout.footerY() + 23.0
        );

        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(MUTED);
        graphics.fillText(
                debugVisible
                        ? "CIANO: ROTA  •  VERDE: SAÍDA"
                        : "PAINÉIS DE DEBUG OCULTOS",
                layout.mapX() + layout.mapWidth() - 14.0,
                layout.footerY() + 23.0
        );
    }

    private void drawPanelTitle(
            String title,
            String subtitle,
            double x,
            double panelY,
            double width,
            boolean compact
    ) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(TEXT);
        graphics.setFont(compact ? compactTitleFont : titleFont);
        graphics.fillText(title, x, panelY + 27.0, width);
        graphics.setFill(ACCENT);
        graphics.setFont(smallFont);
        graphics.fillText(subtitle, x, panelY + 45.0, width);
    }

    private void drawStatusBadge(
            NavigationStatus navigationStatus,
            double x,
            double y,
            double width,
            boolean compact,
            boolean sessionStarted
    ) {
        Color color = statusColor(navigationStatus);
        graphics.setFill(statusBackground(navigationStatus));
        graphics.fillRect(x, y, width, 28.0);
        graphics.setFill(color);
        graphics.fillRect(x, y, 3.0, 28.0);
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(compact ? compactValueFont : headingFont);
        graphics.fillText(
                sessionStarted ? statusText(navigationStatus) : "AGUARDANDO INÍCIO",
                x + 10.0,
                y + 19.0,
                width - 14.0
        );
    }

    private void drawCompletionBadge(double x, double y, double width, boolean compact) {
        graphics.setFill(REACHED_BACKGROUND);
        graphics.fillRect(x, y, width, 28.0);
        graphics.setFill(EXIT);
        graphics.fillRect(x, y, 3.0, 28.0);
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(compact ? compactValueFont : headingFont);
        graphics.fillText("FUGA CONCLUÍDA", x + 10.0, y + 19.0, width - 14.0);
    }

    private void drawInstruction(String text, double x, double y, double width) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(TEXT);
        graphics.setFont(smallFont);
        graphics.fillText(text, x, y, width);
    }

    private void drawMetric(
            String label,
            String value,
            double x,
            double y,
            double width,
            boolean compact
    ) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(MUTED);
        graphics.setFont(labelFont);
        graphics.fillText(label, x, y, width);
        graphics.setFill(TEXT);
        graphics.setFont(compact ? compactValueFont : valueFont);
        graphics.fillText(value, x, y + 20.0, width);
    }

    private void drawLegendItem(Color color, String text, double x, double y, double width) {
        graphics.setFill(LEGEND_BACKDROP);
        graphics.fillRect(x, y - 9.0, 12.0, 12.0);
        graphics.setFill(color);
        graphics.fillRect(x + 3.0, y - 6.0, 6.0, 6.0);
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(TEXT);
        graphics.setFont(smallFont);
        graphics.fillText(text, x + 20.0, y, width - 20.0);
    }

    private void drawSystemLine(
            String label,
            String value,
            Color color,
            double x,
            double y,
            double width
    ) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(TEXT);
        graphics.setFont(smallFont);
        graphics.fillText(label, x, y, width - 42.0);
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(color);
        graphics.setFont(labelFont);
        graphics.fillText(value, x + width, y);
    }

    private void drawSectionTitle(String text, double x, double y, double width) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(ACCENT);
        graphics.setFont(labelFont);
        graphics.fillText(text, x, y, width);
    }

    private void drawDivider(double x, double y, double width) {
        graphics.setFill(BORDER);
        graphics.fillRect(x, y, width, 1.0);
    }

    private void drawTechPanel(
            double x,
            double y,
            double width,
            double height,
            Color accent
    ) {
        graphics.setFill(PANEL);
        graphics.fillRoundRect(x, y, width, height, 10.0, 10.0);
        graphics.setStroke(BORDER);
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(x + 0.5, y + 0.5, width - 1.0, height - 1.0, 10.0, 10.0);
        graphics.setFill(PANEL_INNER);
        graphics.fillRect(x + 5.0, y + 5.0, width - 10.0, 2.0);
        graphics.setFill(accent);
        graphics.fillRect(x + 12.0, y + 5.0, Math.min(54.0, width - 24.0), 2.0);
    }

    private static String statusText(NavigationStatus navigationStatus) {
        return switch (navigationStatus) {
            case MOVING -> "EM ROTA";
            case DESTINATION_REACHED -> "DESTINO ALCANÇADO";
            case NO_PATH -> "SEM ROTA";
        };
    }

    private static GridPosition playerPosition(GameWorld world) {
        int row = (int) Math.floor(world.player().centerY() / world.tileMap().tileSize());
        int column = (int) Math.floor(world.player().centerX() / world.tileMap().tileSize());
        return new GridPosition(row, column);
    }

    private static Color statusColor(NavigationStatus navigationStatus) {
        return switch (navigationStatus) {
            case MOVING -> ACCENT;
            case DESTINATION_REACHED -> PLAYER;
            case NO_PATH -> DANGER;
        };
    }

    private static Color statusBackground(NavigationStatus navigationStatus) {
        return switch (navigationStatus) {
            case MOVING -> MOVING_BACKGROUND;
            case DESTINATION_REACHED -> REACHED_BACKGROUND;
            case NO_PATH -> NO_PATH_BACKGROUND;
        };
    }
}
