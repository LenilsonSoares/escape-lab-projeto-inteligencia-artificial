package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.game.NavigationStatus;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Desenha rota, destino e entidades nas coordenadas do mapa.
 */
final class WorldPainter {

    private static final double PLAYER_VISUAL_SIZE = 40.0;
    private static final double AGENT_VISUAL_SIZE = 40.0;
    private static final Color PATH_HALO = Color.web("#45d7f2", 0.16);
    private static final Color PATH_OUTER = Color.web("#1696b7", 0.92);
    private static final Color PATH_CORE = Color.web("#8af5ff");
    private static final Color PATH_PULSE = Color.web("#e5fdff", 0.96);
    private static final Color PATH_NODE = Color.web("#061923");
    private static final Color DESTINATION = Color.web("#ffd166");
    private static final Color DESTINATION_FILL = Color.web("#ffd166", 0.12);
    private static final Color UNREACHABLE = Color.web("#ef476f");
    private static final Color UNREACHABLE_FILL = Color.web("#ef476f", 0.12);
    private static final Color EXIT_BEACON = Color.web("#52e889");
    private static final Color EXIT_BEACON_FILL = Color.web("#52e889", 0.10);

    private static final Color ACTOR_SHADOW = Color.web("#01050b", 0.72);
    private static final Color PLAYER_OUTLINE = Color.web("#063b43");
    private static final Color PLAYER_BODY = Color.web("#1fc7a5");
    private static final Color PLAYER_LIGHT = Color.web("#72f1d2");
    private static final Color PLAYER_VISOR = Color.web("#071b2b");
    private static final Color PLAYER_VISOR_LIGHT = Color.web("#66ebff");

    private static final Color AGENT_OUTLINE = Color.web("#603b08");
    private static final Color AGENT_BODY = Color.web("#f4a61c");
    private static final Color AGENT_LIGHT = Color.web("#ffd166");
    private static final Color AGENT_SCREEN = Color.web("#171205");
    private static final Color AGENT_INDICATOR_LIGHT = Color.web("#61efff");

    private final GraphicsContext graphics;
    private final DropShadow destinationGlow;
    private final DropShadow unreachableGlow;
    private final DropShadow playerGlow;
    private final DropShadow agentGlow;
    private final ActorAnimationState playerAnimation = new ActorAnimationState();
    private final ActorAnimationState agentAnimation = new ActorAnimationState();

    WorldPainter(GraphicsContext graphics) {
        this.graphics = graphics;
        this.destinationGlow = new DropShadow(9.0, Color.web("#ffd166", 0.62));
        this.unreachableGlow = new DropShadow(9.0, Color.web("#ef476f", 0.62));
        this.playerGlow = new DropShadow(10.0, Color.web("#38d9a9", 0.56));
        this.agentGlow = new DropShadow(9.0, Color.web("#ffb703", 0.58));
    }

    void drawNavigation(
            List<GridPosition> path,
            GridPosition destination,
            NavigationStatus navigationStatus,
            double tileSize,
            RenderLayout layout
    ) {
        drawPath(path, tileSize, layout);
        drawDestination(destination, navigationStatus, tileSize, layout);
    }

    /**
     * Destaca a saída lógica do mapa sem interferir na colisão ou na navegação.
     */
    void drawExitBeacon(GridPosition exit, double tileSize, RenderLayout layout) {
        double scale = visualScale(layout);
        double displayedTileSize = tileSize * scale;
        double centerX = tileCenterX(exit, tileSize, layout);
        double centerY = tileCenterY(exit, tileSize, layout);
        double pulse = (Math.sin(visualSeconds() * 4.2) + 1.0) / 2.0;
        double radius = displayedTileSize * (0.42 + pulse * 0.13);

        graphics.save();
        graphics.setFill(EXIT_BEACON_FILL);
        graphics.fillOval(
                centerX - displayedTileSize * 0.68,
                centerY - displayedTileSize * 0.68,
                displayedTileSize * 1.36,
                displayedTileSize * 1.36
        );
        graphics.setStroke(Color.color(
                EXIT_BEACON.getRed(),
                EXIT_BEACON.getGreen(),
                EXIT_BEACON.getBlue(),
                0.42 + pulse * 0.45
        ));
        graphics.setLineWidth(Math.max(1.0, 1.5 * scale));
        graphics.strokeOval(
                centerX - radius,
                centerY - radius,
                radius * 2.0,
                radius * 2.0
        );

        double arrowOffset = (3.0 + pulse * 3.0) * scale;
        double arrowSize = 5.0 * scale;
        graphics.setFill(EXIT_BEACON);
        graphics.fillPolygon(
                new double[] {
                    centerX - arrowOffset - arrowSize,
                    centerX - arrowOffset,
                    centerX - arrowOffset - arrowSize
                },
                new double[] {
                    centerY - arrowSize,
                    centerY,
                    centerY + arrowSize
                },
                3
        );
        graphics.restore();
    }

    void drawPlayer(Player player, RenderLayout layout) {
        playerAnimation.sample(player.x(), player.y());
        double scale = visualScale(layout);
        playerGlow.setRadius(10.0 * scale);
        double logicalSize = VisualAssets.PLAYER == null ? player.size() : PLAYER_VISUAL_SIZE;
        double x = layout.screenX(player.centerX() - logicalSize / 2.0);
        double y = layout.screenY(player.centerY() - logicalSize / 2.0);
        double size = logicalSize * scale;
        double bob = Math.rint(playerAnimation.moving()
                ? Math.abs(Math.sin(visualSeconds() * 9.0)) * 1.15 * scale
                : 0.0);
        double spriteY = Math.rint(y - bob);

        drawActorShadow(x, y, size, bob, scale);
        if (VisualAssets.PLAYER != null) {
            drawActorSprite(
                    VisualAssets.PLAYER,
                    x,
                    spriteY,
                    size,
                    playerGlow,
                    playerAnimation.facingLeft()
            );
            return;
        }

        graphics.save();
        graphics.translate(x + (playerAnimation.facingLeft() ? size : 0.0), spriteY);
        graphics.scale(playerAnimation.facingLeft() ? -scale : scale, scale);
        drawFallbackPlayer(player.size());
        graphics.restore();
    }

    private void drawFallbackPlayer(double size) {
        graphics.setEffect(playerGlow);
        graphics.setFill(PLAYER_OUTLINE);
        graphics.fillRect(7.0, 11.0, size - 14.0, size - 13.0);
        graphics.setFill(PLAYER_BODY);
        graphics.fillRect(9.0, 12.0, size - 18.0, size - 17.0);
        graphics.fillRect(4.0, 14.0, 5.0, 11.0);
        graphics.fillRect(size - 9.0, 14.0, 5.0, 11.0);
        graphics.fillRect(7.0, size - 7.0, 6.0, 5.0);
        graphics.fillRect(size - 13.0, size - 7.0, 6.0, 5.0);
        graphics.setEffect(null);

        graphics.setFill(PLAYER_OUTLINE);
        graphics.fillRect(8.0, 2.0, size - 16.0, 12.0);
        graphics.fillRect(6.0, 5.0, 2.0, 7.0);
        graphics.fillRect(size - 8.0, 5.0, 2.0, 7.0);
        graphics.setFill(PLAYER_LIGHT);
        graphics.fillRect(10.0, 4.0, size - 20.0, 8.0);
        graphics.setFill(PLAYER_VISOR);
        graphics.fillRect(11.0, 6.0, size - 22.0, 5.0);
        graphics.setFill(PLAYER_VISOR_LIGHT);
        graphics.fillRect(13.0, 7.0, size - 27.0, 2.0);
    }

    void drawAgent(AutonomousAgent agent, RenderLayout layout) {
        agentAnimation.sample(agent.x(), agent.y());
        double scale = visualScale(layout);
        agentGlow.setRadius(9.0 * scale);
        double logicalSize = VisualAssets.PATHFINDER_ROBOT == null
                ? agent.size()
                : AGENT_VISUAL_SIZE;
        double x = layout.screenX(agent.centerX() - logicalSize / 2.0);
        double y = layout.screenY(agent.centerY() - logicalSize / 2.0);
        double size = logicalSize * scale;
        double bob = Math.rint(agentAnimation.moving()
                ? Math.abs(Math.sin(visualSeconds() * 7.2 + Math.PI)) * 1.25 * scale
                : 0.0);
        double spriteY = Math.rint(y - bob);

        drawActorShadow(x, y, size, bob, scale);
        if (VisualAssets.PATHFINDER_ROBOT != null) {
            drawActorSprite(
                    VisualAssets.PATHFINDER_ROBOT,
                    x,
                    spriteY,
                    size,
                    agentGlow,
                    agentAnimation.facingLeft()
            );
            drawAgentSignal(x, spriteY, size, scale);
            return;
        }

        graphics.save();
        graphics.translate(x + (agentAnimation.facingLeft() ? size : 0.0), spriteY);
        graphics.scale(agentAnimation.facingLeft() ? -scale : scale, scale);
        drawFallbackAgent(agent.size());
        graphics.restore();
    }

    private void drawFallbackAgent(double size) {
        graphics.setFill(AGENT_OUTLINE);
        graphics.fillRect(2.0, 7.0, 4.0, size - 11.0);
        graphics.fillRect(size - 6.0, 7.0, 4.0, size - 11.0);

        graphics.setEffect(agentGlow);
        graphics.setFill(AGENT_OUTLINE);
        graphics.fillRect(5.0, 3.0, size - 10.0, size - 6.0);
        graphics.setFill(AGENT_BODY);
        graphics.fillRect(7.0, 5.0, size - 14.0, size - 10.0);
        graphics.setEffect(null);

        graphics.setFill(AGENT_LIGHT);
        graphics.fillRect(9.0, 6.0, size - 18.0, 3.0);
        graphics.setFill(AGENT_SCREEN);
        graphics.fillRect(8.0, 11.0, size - 16.0, 8.0);
        graphics.setFill(AGENT_INDICATOR_LIGHT);
        graphics.fillRect(10.0, 13.0, 3.0, 3.0);
        graphics.fillRect(size - 13.0, 13.0, 3.0, 3.0);
        graphics.setFill(AGENT_OUTLINE);
        graphics.fillRect(10.0, size - 6.0, size - 20.0, 2.0);
    }

    private void drawAgentSignal(double x, double y, double size, double scale) {
        double pulse = (Math.sin(visualSeconds() * 5.5) + 1.0) / 2.0;
        double lightSize = Math.max(2.0, 2.0 * scale);
        graphics.setGlobalAlpha(0.55 + pulse * 0.45);
        graphics.setFill(AGENT_INDICATOR_LIGHT);
        graphics.fillRect(
                Math.rint(x + size / 2.0 - lightSize / 2.0),
                Math.rint(y + 2.0 * scale),
                lightSize,
                lightSize
        );
        graphics.setGlobalAlpha(1.0);
    }

    private void drawPath(List<GridPosition> path, double tileSize, RenderLayout layout) {
        if (path.isEmpty()) {
            return;
        }

        double scale = visualScale(layout);
        drawPathSegments(path, tileSize, layout, PATH_HALO, 9.0 * scale);
        drawPathSegments(path, tileSize, layout, PATH_OUTER, 4.0 * scale);
        drawPathSegments(path, tileSize, layout, PATH_CORE, 2.0 * scale);
        drawMovingPathPulse(path, tileSize, layout, scale);

        for (GridPosition position : path) {
            drawPathNode(position, tileSize, layout, scale);
        }
    }

    /**
     * Cria um fluxo luminoso sobre a rota já calculada. É apenas uma animação
     * visual: os pontos e a ordem do caminho continuam vindo do A*.
     */
    private void drawMovingPathPulse(
            List<GridPosition> path,
            double tileSize,
            RenderLayout layout,
            double scale
    ) {
        graphics.save();
        graphics.setStroke(PATH_PULSE);
        graphics.setLineWidth(1.5 * scale);
        graphics.setLineCap(StrokeLineCap.BUTT);
        graphics.setLineDashes(5.0 * scale, 10.0 * scale);
        double dashCycle = 15.0 * scale;
        double dashOffset = (visualSeconds() * 28.0 * scale) % dashCycle;
        graphics.setLineDashOffset(-dashOffset);

        for (int index = 1; index < path.size(); index++) {
            GridPosition start = path.get(index - 1);
            GridPosition end = path.get(index);
            graphics.strokeLine(
                    tileCenterX(start, tileSize, layout),
                    tileCenterY(start, tileSize, layout),
                    tileCenterX(end, tileSize, layout),
                    tileCenterY(end, tileSize, layout)
            );
        }
        graphics.restore();
    }

    private void drawPathSegments(
            List<GridPosition> path,
            double tileSize,
            RenderLayout layout,
            Color color,
            double thickness
    ) {
        graphics.setFill(color);
        for (int index = 1; index < path.size(); index++) {
            drawPathSegment(
                    path.get(index - 1),
                    path.get(index),
                    tileSize,
                    layout,
                    thickness
            );
        }
    }

    private void drawPathSegment(
            GridPosition start,
            GridPosition end,
            double tileSize,
            RenderLayout layout,
            double thickness
    ) {
        double startX = tileCenterX(start, tileSize, layout);
        double startY = tileCenterY(start, tileSize, layout);
        double endX = tileCenterX(end, tileSize, layout);
        double endY = tileCenterY(end, tileSize, layout);

        if (start.row() == end.row()) {
            double left = Math.min(startX, endX);
            graphics.fillRect(
                    left,
                    startY - thickness / 2.0,
                    Math.abs(endX - startX),
                    thickness
            );
        } else {
            double top = Math.min(startY, endY);
            graphics.fillRect(
                    startX - thickness / 2.0,
                    top,
                    thickness,
                    Math.abs(endY - startY)
            );
        }
    }

    private void drawPathNode(
            GridPosition position,
            double tileSize,
            RenderLayout layout,
            double scale
    ) {
        double centerX = tileCenterX(position, tileSize, layout);
        double centerY = tileCenterY(position, tileSize, layout);
        double outerSize = Math.max(4.0, 6.0 * scale);
        double coreSize = Math.max(2.0, 3.0 * scale);
        graphics.setFill(PATH_NODE);
        graphics.fillRect(
                centerX - outerSize / 2.0,
                centerY - outerSize / 2.0,
                outerSize,
                outerSize
        );
        graphics.setFill(PATH_CORE);
        graphics.fillRect(
                centerX - coreSize / 2.0,
                centerY - coreSize / 2.0,
                coreSize,
                coreSize
        );
    }

    private void drawDestination(
            GridPosition destination,
            NavigationStatus navigationStatus,
            double tileSize,
            RenderLayout layout
    ) {
        double scale = visualScale(layout);
        destinationGlow.setRadius(9.0 * scale);
        unreachableGlow.setRadius(9.0 * scale);
        double displayedTileSize = tileSize * scale;
        double pulse = (Math.sin(visualSeconds() * 4.0) + 1.0) / 2.0;
        double inset = (5.5 - pulse * 1.5) * scale;
        double x = tileX(destination, tileSize, layout) + inset;
        double y = tileY(destination, tileSize, layout) + inset;
        double markerSize = displayedTileSize - inset * 2.0;
        boolean unreachable = navigationStatus == NavigationStatus.NO_PATH;
        Color marker = unreachable ? UNREACHABLE : DESTINATION;

        graphics.setFill(unreachable ? UNREACHABLE_FILL : DESTINATION_FILL);
        graphics.fillRect(x, y, markerSize, markerSize);
        graphics.setEffect(unreachable ? unreachableGlow : destinationGlow);
        graphics.setFill(marker);
        drawTargetCorners(x, y, markerSize, scale);
        double centerMark = 5.0 * scale;
        graphics.fillRect(
                x + markerSize / 2.0 - centerMark / 2.0,
                y + markerSize / 2.0 - centerMark / 2.0,
                centerMark,
                centerMark
        );
        graphics.setEffect(null);

        graphics.setStroke(Color.color(
                marker.getRed(),
                marker.getGreen(),
                marker.getBlue(),
                0.2 + pulse * 0.28
        ));
        graphics.setLineWidth(Math.max(1.0, scale));
        double ringInset = (2.0 + pulse * 3.0) * scale;
        graphics.strokeOval(
                x - ringInset,
                y - ringInset,
                markerSize + ringInset * 2.0,
                markerSize + ringInset * 2.0
        );
    }

    private void drawTargetCorners(double x, double y, double size, double scale) {
        double corner = 9.0 * scale;
        double thickness = Math.max(2.0, 3.0 * scale);
        graphics.fillRect(x, y, corner, thickness);
        graphics.fillRect(x, y, thickness, corner);
        graphics.fillRect(x + size - corner, y, corner, thickness);
        graphics.fillRect(x + size - thickness, y, thickness, corner);
        graphics.fillRect(x, y + size - thickness, corner, thickness);
        graphics.fillRect(x, y + size - corner, thickness, corner);
        graphics.fillRect(x + size - corner, y + size - thickness, corner, thickness);
        graphics.fillRect(x + size - thickness, y + size - corner, thickness, corner);
    }

    private void drawActorShadow(
            double x,
            double y,
            double size,
            double bob,
            double scale
    ) {
        double shadowWidth = size - 8.0 * scale;
        double shadowHeight = 6.0 * scale;
        double bobDistance = Math.abs(bob) / Math.max(scale, 0.001);
        graphics.setFill(ACTOR_SHADOW);
        graphics.setGlobalAlpha(Math.max(0.48, 0.76 - bobDistance * 0.08));
        graphics.fillOval(
                x + (size - shadowWidth) / 2.0,
                y + size - shadowHeight * 0.55,
                shadowWidth,
                shadowHeight
        );
        graphics.setGlobalAlpha(1.0);
    }

    private void drawActorSprite(
            Image sprite,
            double x,
            double y,
            double size,
            DropShadow glow,
            boolean facingLeft
    ) {
        graphics.save();
        graphics.translate(x + (facingLeft ? size : 0.0), y);
        graphics.scale(facingLeft ? -1.0 : 1.0, 1.0);
        graphics.setEffect(glow);
        graphics.drawImage(sprite, 0.0, 0.0, size, size);
        graphics.setEffect(null);
        graphics.restore();
    }

    private static double tileX(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenX(position.column() * tileSize);
    }

    private static double tileY(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenY(position.row() * tileSize);
    }

    private static double tileCenterX(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenX((position.column() + 0.5) * tileSize);
    }

    private static double tileCenterY(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenY((position.row() + 0.5) * tileSize);
    }

    /**
     * Aplica a mesma escala inteira ou de meio passo usada pelo mapa. Assim a
     * pintura continua correta tanto no tamanho base quanto em telas 4K.
     */
    private static double visualScale(RenderLayout layout) {
        return layout.scale();
    }

    private static double visualSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
