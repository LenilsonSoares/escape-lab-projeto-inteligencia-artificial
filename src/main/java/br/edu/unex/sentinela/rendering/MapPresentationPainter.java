package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.GameWorld;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Mostra avisos visuais de entrada no setor e de conclusão da fuga.
 */
final class MapPresentationPainter {

    private static final double INTRO_DURATION_SECONDS = 2.2;
    private static final double INTRO_FADE_SECONDS = 0.55;
    private static final Color PANEL = Color.web("#04101b", 0.94);
    private static final Color BORDER = Color.web("#45d7f2");
    private static final Color TEXT = Color.web("#e9f5ff");
    private static final Color MUTED = Color.web("#87a5b8");
    private static final Color SUCCESS = Color.web("#52e889");

    private final GraphicsContext graphics;
    private final Font titleFont = Font.font("Monospaced", FontWeight.BOLD, 18.0);
    private final Font headingFont = Font.font("Monospaced", FontWeight.BOLD, 12.0);
    private final Font valueFont = Font.font("Monospaced", FontWeight.NORMAL, 10.0);

    private int displayedMapNumber = -1;
    private long introStartedAt;
    private boolean previouslyStarted;

    MapPresentationPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(
            GameWorld world,
            RenderLayout layout,
            boolean sessionStarted,
            boolean paused
    ) {
        long now = System.nanoTime();
        if (displayedMapNumber != world.currentMap().number()) {
            displayedMapNumber = world.currentMap().number();
            introStartedAt = now;
        }
        if (sessionStarted && !previouslyStarted) {
            introStartedAt = now;
        }
        previouslyStarted = sessionStarted;

        graphics.save();
        graphics.scale(layout.scale(), layout.scale());
        RenderLayout logicalLayout = layout.logicalCoordinates();
        if (!sessionStarted) {
            drawBriefing(logicalLayout, now);
        } else if (world.escapeCompleted()) {
            drawCompletion(logicalLayout);
        } else if (paused) {
            drawPause(logicalLayout);
        } else {
            drawIntroduction(
                    world,
                    logicalLayout,
                    LaboratoryTheme.forMap(world.currentMap()),
                    now
            );
        }
        graphics.restore();
    }

    private void drawBriefing(RenderLayout layout, long now) {
        double width = 470.0;
        double height = 260.0;
        double x = layout.mapX() + (layout.mapWidth() - width) / 2.0;
        double y = layout.mapY() + (layout.mapHeight() - height) / 2.0;
        double pulse = (Math.sin(now / 1_000_000_000.0 * 3.0) + 1.0) / 2.0;

        graphics.setFill(Color.web("#020811", 0.68));
        graphics.fillRect(layout.mapX(), layout.mapY(), layout.mapWidth(), layout.mapHeight());
        drawPanel(x, y, width, height, BORDER);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(BORDER);
        graphics.setFont(headingFont);
        graphics.fillText("PROTOCOLO DE NAVEGAÇÃO AUTÔNOMA", x + width / 2.0, y + 31.0);
        graphics.setFill(TEXT);
        graphics.setFont(Font.font("Monospaced", FontWeight.BOLD, 28.0));
        graphics.fillText("ESCAPE LAB", x + width / 2.0, y + 70.0);

        graphics.setFill(MUTED);
        graphics.setFont(valueFont);
        graphics.fillText("OBJETIVO", x + width / 2.0, y + 101.0);
        graphics.setFill(TEXT);
        graphics.fillText(
                "ATRAVESSE OS TRÊS SETORES E ALCANCE AS SAÍDAS VERDES",
                x + width / 2.0,
                y + 124.0,
                width - 36.0
        );

        graphics.setFill(MUTED);
        graphics.fillText(
                "WASD/SETAS  •  MOVER     ESC  •  PAUSAR     TAB  •  DEBUG",
                x + width / 2.0,
                y + 162.0,
                width - 30.0
        );
        graphics.fillText(
                "O ROBÔ RECALCULA A ROTA QUANDO O DESTINO MUDA",
                x + width / 2.0,
                y + 188.0,
                width - 36.0
        );

        graphics.setGlobalAlpha(0.58 + pulse * 0.42);
        graphics.setFill(SUCCESS);
        graphics.setFont(headingFont);
        graphics.fillText("PRESSIONE ENTER PARA INICIAR", x + width / 2.0, y + 226.0);
        graphics.setGlobalAlpha(1.0);
    }

    private void drawIntroduction(
            GameWorld world,
            RenderLayout layout,
            LaboratoryTheme theme,
            long now
    ) {
        double elapsed = (now - introStartedAt) / 1_000_000_000.0;
        if (elapsed >= INTRO_DURATION_SECONDS) {
            return;
        }

        double remaining = INTRO_DURATION_SECONDS - elapsed;
        double opacity = Math.min(1.0, remaining / INTRO_FADE_SECONDS);
        double width = 300.0;
        double height = 76.0;
        double x = layout.mapX() + (layout.mapWidth() - width) / 2.0;
        double y = layout.mapY() + 18.0;

        graphics.setGlobalAlpha(opacity);
        drawPanel(x, y, width, height, theme.accent());
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(theme.accent());
        graphics.setFont(headingFont);
        graphics.fillText(
                "MAPA %02d / 03".formatted(world.currentMap().number()),
                x + width / 2.0,
                y + 21.0
        );
        graphics.setFill(TEXT);
        graphics.setFont(titleFont);
        graphics.fillText(
                world.currentMap().displayName(),
                x + width / 2.0,
                y + 45.0,
                width - 24.0
        );
        graphics.setFill(MUTED);
        graphics.setFont(valueFont);
        graphics.fillText("LOCALIZE A SAÍDA VERDE", x + width / 2.0, y + 64.0);
        graphics.setGlobalAlpha(1.0);
    }

    private void drawCompletion(RenderLayout layout) {
        double width = 390.0;
        double height = 128.0;
        double x = layout.mapX() + (layout.mapWidth() - width) / 2.0;
        double y = layout.mapY() + (layout.mapHeight() - height) / 2.0;

        graphics.setFill(Color.web("#020811", 0.58));
        graphics.fillRect(layout.mapX(), layout.mapY(), layout.mapWidth(), layout.mapHeight());
        drawPanel(x, y, width, height, SUCCESS);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(SUCCESS);
        graphics.setFont(headingFont);
        graphics.fillText("PROTOCOLO DE EVACUAÇÃO", x + width / 2.0, y + 28.0);
        graphics.setFill(TEXT);
        graphics.setFont(titleFont);
        graphics.fillText("FUGA CONCLUÍDA", x + width / 2.0, y + 61.0);
        graphics.setFill(MUTED);
        graphics.setFont(valueFont);
        graphics.fillText(
                "OS TRÊS SETORES FORAM FINALIZADOS",
                x + width / 2.0,
                y + 87.0
        );
        graphics.fillText(
                "PRESSIONE 1 PARA REINICIAR A DEMONSTRAÇÃO",
                x + width / 2.0,
                y + 106.0
        );
    }

    private void drawPause(RenderLayout layout) {
        double width = 340.0;
        double height = 112.0;
        double x = layout.mapX() + (layout.mapWidth() - width) / 2.0;
        double y = layout.mapY() + (layout.mapHeight() - height) / 2.0;

        graphics.setFill(Color.web("#020811", 0.50));
        graphics.fillRect(layout.mapX(), layout.mapY(), layout.mapWidth(), layout.mapHeight());
        drawPanel(x, y, width, height, BORDER);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(BORDER);
        graphics.setFont(headingFont);
        graphics.fillText("SISTEMA EM ESPERA", x + width / 2.0, y + 27.0);
        graphics.setFill(TEXT);
        graphics.setFont(titleFont);
        graphics.fillText("JOGO PAUSADO", x + width / 2.0, y + 59.0);
        graphics.setFill(MUTED);
        graphics.setFont(valueFont);
        graphics.fillText("PRESSIONE ESC PARA CONTINUAR", x + width / 2.0, y + 87.0);
    }

    private void drawPanel(double x, double y, double width, double height, Color accent) {
        graphics.setFill(PANEL);
        graphics.fillRoundRect(x, y, width, height, 8.0, 8.0);
        graphics.setStroke(accent);
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(x, y, width, height, 8.0, 8.0);
        graphics.setFill(accent);
        graphics.fillRect(x + 12.0, y, 44.0, 2.0);
        graphics.fillRect(x + width - 56.0, y + height - 2.0, 44.0, 2.0);
    }
}
