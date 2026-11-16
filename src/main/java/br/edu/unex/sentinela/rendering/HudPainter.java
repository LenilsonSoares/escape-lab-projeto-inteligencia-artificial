package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.AiMode;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Apresenta cabeçalho, rodapé e os painéis laterais fora do tilemap.
 */
final class HudPainter {

    private static final double MINIMUM_RAIL_WIDTH = 120.0;
    private static final Color DEMO_MODE = Color.web("#ffb703");
    private static final String CONTROLS =
            "WASD MOVER  •  SHIFT AGACHAR (MENOS RUÍDO)  •  ESC PAUSA  •  TAB DEBUG  •  F2 MODO  •  1 2 3 MAPA";

    private final GraphicsContext graphics;
    private final PanelKit kit;
    private final MapProgressPainter mapProgressPainter;
    private final RobotPanelPainter robotPanelPainter;
    private final DiagnosticsPanelPainter diagnosticsPanelPainter;

    HudPainter(GraphicsContext graphics) {
        this.graphics = graphics;
        this.kit = new PanelKit(graphics);
        this.mapProgressPainter = new MapProgressPainter(graphics);
        this.robotPanelPainter = new RobotPanelPainter(kit);
        this.diagnosticsPanelPainter = new DiagnosticsPanelPainter(kit);
    }

    void draw(
            GameWorld world,
            FrameMetrics metrics,
            RenderLayout layout,
            boolean debugVisible
    ) {
        double scale = layout.scale();
        RenderLayout logicalLayout = layout.logicalCoordinates();
        LaboratoryTheme theme = LaboratoryTheme.forMap(world.currentMap());

        graphics.save();
        graphics.scale(scale, scale);
        drawHeader(world, logicalLayout, theme);
        if (logicalLayout.railWidth() >= MINIMUM_RAIL_WIDTH) {
            robotPanelPainter.draw(world, logicalLayout, debugVisible, theme);
            diagnosticsPanelPainter.draw(world, metrics, logicalLayout, debugVisible);
        }
        drawFooter(logicalLayout, debugVisible, theme);
        graphics.restore();
    }

    private void drawHeader(GameWorld world, RenderLayout layout, LaboratoryTheme theme) {
        kit.techPanel(layout.mapX(), layout.headerY(), layout.mapWidth(), RenderLayout.BAR_HEIGHT, theme.accent());
        kit.text("ESCAPE LAB • %04d PTS".formatted(world.score()), layout.mapX() + 14.0,
                layout.headerY() + 24.0, layout.mapWidth() * 0.36, kit.titleFont, PanelKit.TEXT);

        mapProgressPainter.draw(world, layout, theme);

        boolean fullAi = world.mode() == AiMode.FULL_AI;
        String mode = fullAi ? AiMode.FULL_AI.label() : AiMode.NAVIGATION_DEMO.label() + " (F2)";
        kit.rightText(mode + " • " + world.difficulty().label(),
                layout.mapX() + layout.mapWidth() - 14.0, layout.headerY() + 23.0,
                layout.mapWidth() * 0.34, kit.headingFont, fullAi ? theme.accent() : DEMO_MODE);
    }

    private void drawFooter(RenderLayout layout, boolean debugVisible, LaboratoryTheme theme) {
        kit.techPanel(layout.mapX(), layout.footerY(), layout.mapWidth(), RenderLayout.BAR_HEIGHT, PanelKit.BORDER);
        kit.text(CONTROLS, layout.mapX() + 14.0, layout.footerY() + 23.0, layout.mapWidth() * 0.80,
                kit.compactValueFont, PanelKit.TEXT);
        kit.rightText(debugVisible ? "DEBUG LIGADO" : "DEBUG OCULTO",
                layout.mapX() + layout.mapWidth() - 14.0, layout.footerY() + 23.0,
                layout.mapWidth() * 0.16, kit.compactValueFont, debugVisible ? theme.routeCore() : PanelKit.MUTED);
    }
}
