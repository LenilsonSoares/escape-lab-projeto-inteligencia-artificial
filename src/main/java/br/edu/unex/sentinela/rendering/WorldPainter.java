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

/**
 * Desenha rota, destino e entidades nas coordenadas do mapa.
 */
final class WorldPainter {

    private static final Color PATH_HALO = Color.web("#45d7f2", 0.16);
    private static final Color PATH_OUTER = Color.web("#1696b7", 0.92);
    private static final Color PATH_CORE = Color.web("#8af5ff");
    private static final Color PATH_NODE = Color.web("#061923");
    private static final Color DESTINATION = Color.web("#ffd166");
    private static final Color DESTINATION_FILL = Color.web("#ffd166", 0.12);
    private static final Color UNREACHABLE = Color.web("#ef476f");
    private static final Color UNREACHABLE_FILL = Color.web("#ef476f", 0.12);

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

    void drawPlayer(Player player, RenderLayout layout) {
        double x = layout.screenX(player.x());
        double y = layout.screenY(player.y());
        double size = player.size();

        drawActorShadow(x, y, size);
        if (VisualAssets.PLAYER != null) {
            drawActorSprite(VisualAssets.PLAYER, x, y, size, playerGlow);
            return;
        }

        graphics.setEffect(playerGlow);
        graphics.setFill(PLAYER_OUTLINE);
        graphics.fillRect(x + 7.0, y + 11.0, size - 14.0, size - 13.0);
        graphics.setFill(PLAYER_BODY);
        graphics.fillRect(x + 9.0, y + 12.0, size - 18.0, size - 17.0);
        graphics.fillRect(x + 4.0, y + 14.0, 5.0, 11.0);
        graphics.fillRect(x + size - 9.0, y + 14.0, 5.0, 11.0);
        graphics.fillRect(x + 7.0, y + size - 7.0, 6.0, 5.0);
        graphics.fillRect(x + size - 13.0, y + size - 7.0, 6.0, 5.0);
        graphics.setEffect(null);

        graphics.setFill(PLAYER_OUTLINE);
        graphics.fillRect(x + 8.0, y + 2.0, size - 16.0, 12.0);
        graphics.fillRect(x + 6.0, y + 5.0, 2.0, 7.0);
        graphics.fillRect(x + size - 8.0, y + 5.0, 2.0, 7.0);
        graphics.setFill(PLAYER_LIGHT);
        graphics.fillRect(x + 10.0, y + 4.0, size - 20.0, 8.0);
        graphics.setFill(PLAYER_VISOR);
        graphics.fillRect(x + 11.0, y + 6.0, size - 22.0, 5.0);
        graphics.setFill(PLAYER_VISOR_LIGHT);
        graphics.fillRect(x + 13.0, y + 7.0, size - 27.0, 2.0);
    }

    void drawAgent(AutonomousAgent agent, RenderLayout layout) {
        double x = layout.screenX(agent.x());
        double y = layout.screenY(agent.y());
        double size = agent.size();

        drawActorShadow(x, y, size);
        if (VisualAssets.PATHFINDER_ROBOT != null) {
            drawActorSprite(VisualAssets.PATHFINDER_ROBOT, x, y, size, agentGlow);
            return;
        }

        graphics.setFill(AGENT_OUTLINE);
        graphics.fillRect(x + 2.0, y + 7.0, 4.0, size - 11.0);
        graphics.fillRect(x + size - 6.0, y + 7.0, 4.0, size - 11.0);

        graphics.setEffect(agentGlow);
        graphics.setFill(AGENT_OUTLINE);
        graphics.fillRect(x + 5.0, y + 3.0, size - 10.0, size - 6.0);
        graphics.setFill(AGENT_BODY);
        graphics.fillRect(x + 7.0, y + 5.0, size - 14.0, size - 10.0);
        graphics.setEffect(null);

        graphics.setFill(AGENT_LIGHT);
        graphics.fillRect(x + 9.0, y + 6.0, size - 18.0, 3.0);
        graphics.setFill(AGENT_SCREEN);
        graphics.fillRect(x + 8.0, y + 11.0, size - 16.0, 8.0);
        graphics.setFill(AGENT_INDICATOR_LIGHT);
        graphics.fillRect(x + 10.0, y + 13.0, 3.0, 3.0);
        graphics.fillRect(x + size - 13.0, y + 13.0, 3.0, 3.0);
        graphics.setFill(AGENT_OUTLINE);
        graphics.fillRect(x + 10.0, y + size - 6.0, size - 20.0, 2.0);
    }

    private void drawPath(List<GridPosition> path, double tileSize, RenderLayout layout) {
        if (path.isEmpty()) {
            return;
        }

        drawPathSegments(path, tileSize, layout, PATH_HALO, 12.0);
        drawPathSegments(path, tileSize, layout, PATH_OUTER, 5.0);
        drawPathSegments(path, tileSize, layout, PATH_CORE, 2.0);

        for (GridPosition position : path) {
            drawPathNode(position, tileSize, layout);
        }
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
        double startX = tileX(start, tileSize, layout) + tileSize / 2.0;
        double startY = tileY(start, tileSize, layout) + tileSize / 2.0;
        double endX = tileX(end, tileSize, layout) + tileSize / 2.0;
        double endY = tileY(end, tileSize, layout) + tileSize / 2.0;

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

    private void drawPathNode(GridPosition position, double tileSize, RenderLayout layout) {
        double centerX = tileX(position, tileSize, layout) + tileSize / 2.0;
        double centerY = tileY(position, tileSize, layout) + tileSize / 2.0;
        graphics.setFill(PATH_NODE);
        graphics.fillRect(centerX - 4.0, centerY - 4.0, 8.0, 8.0);
        graphics.setFill(PATH_CORE);
        graphics.fillRect(centerX - 2.0, centerY - 2.0, 4.0, 4.0);
    }

    private void drawDestination(
            GridPosition destination,
            NavigationStatus navigationStatus,
            double tileSize,
            RenderLayout layout
    ) {
        double x = tileX(destination, tileSize, layout) + 5.0;
        double y = tileY(destination, tileSize, layout) + 5.0;
        double markerSize = tileSize - 10.0;
        boolean unreachable = navigationStatus == NavigationStatus.NO_PATH;
        Color marker = unreachable ? UNREACHABLE : DESTINATION;

        graphics.setFill(unreachable ? UNREACHABLE_FILL : DESTINATION_FILL);
        graphics.fillRect(x, y, markerSize, markerSize);
        graphics.setEffect(unreachable ? unreachableGlow : destinationGlow);
        graphics.setFill(marker);
        drawTargetCorners(x, y, markerSize);
        graphics.fillRect(x + markerSize / 2.0 - 3.0, y + markerSize / 2.0 - 3.0, 6.0, 6.0);
        graphics.setEffect(null);
    }

    private void drawTargetCorners(double x, double y, double size) {
        double corner = 9.0;
        double thickness = 3.0;
        graphics.fillRect(x, y, corner, thickness);
        graphics.fillRect(x, y, thickness, corner);
        graphics.fillRect(x + size - corner, y, corner, thickness);
        graphics.fillRect(x + size - thickness, y, thickness, corner);
        graphics.fillRect(x, y + size - thickness, corner, thickness);
        graphics.fillRect(x, y + size - corner, thickness, corner);
        graphics.fillRect(x + size - corner, y + size - thickness, corner, thickness);
        graphics.fillRect(x + size - thickness, y + size - corner, thickness, corner);
    }

    private void drawActorShadow(double x, double y, double size) {
        graphics.setFill(ACTOR_SHADOW);
        graphics.fillRect(x + 4.0, y + size - 3.0, size - 8.0, 5.0);
    }

    private void drawActorSprite(
            Image sprite,
            double x,
            double y,
            double size,
            DropShadow glow
    ) {
        graphics.setEffect(glow);
        graphics.drawImage(sprite, x, y, size, size);
        graphics.setEffect(null);
    }

    private static double tileX(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.mapX() + position.column() * tileSize;
    }

    private static double tileY(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.mapY() + position.row() * tileSize;
    }
}
