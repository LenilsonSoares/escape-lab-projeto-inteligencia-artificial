package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.AutonomousAgent;
import br.edu.unex.sentinela.entity.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/** Desenha e anima visualmente o jogador e o agente autônomo. */
final class ActorPainter {

    private static final double PLAYER_VISUAL_SIZE = 40.0;
    private static final double AGENT_VISUAL_SIZE = 40.0;
    private static final Color ACTOR_SHADOW = Color.web("#01050b", 0.72);
    private static final Color PLAYER_LOCATOR = Color.web("#38d9a9", 0.62);
    private static final Color AGENT_LOCATOR = Color.web("#ffb703", 0.62);

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
    private final DropShadow playerGlow = new DropShadow(10.0, Color.web("#38d9a9", 0.56));
    private final DropShadow agentGlow = new DropShadow(9.0, Color.web("#ffb703", 0.58));
    private final ActorAnimationState playerAnimation = new ActorAnimationState();
    private final ActorAnimationState agentAnimation = new ActorAnimationState();

    ActorPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void drawPlayer(Player player, RenderLayout layout) {
        playerAnimation.sample(player.x(), player.y());
        double scale = layout.scale();
        playerGlow.setRadius(10.0 * scale);
        double logicalSize = VisualAssets.PLAYER == null ? player.size() : PLAYER_VISUAL_SIZE;
        double x = layout.screenX(player.centerX() - logicalSize / 2.0);
        double y = layout.screenY(player.centerY() - logicalSize / 2.0);
        double size = logicalSize * scale;
        double bob = Math.rint(playerAnimation.moving()
                ? Math.abs(Math.sin(visualSeconds() * 9.0)) * 1.15 * scale
                : 0.0);
        double spriteY = Math.rint(y - bob);

        drawLocator(x, y, size, scale, PLAYER_LOCATOR);
        drawShadow(x, y, size, bob, scale);
        if (VisualAssets.PLAYER != null) {
            drawSprite(VisualAssets.PLAYER, x, spriteY, size, playerGlow,
                    playerAnimation.facingLeft(), playerAnimation.walkFrame(), scale);
            return;
        }

        graphics.save();
        graphics.translate(x + (playerAnimation.facingLeft() ? size : 0.0), spriteY);
        graphics.scale(playerAnimation.facingLeft() ? -scale : scale, scale);
        drawFallbackPlayer(player.size());
        graphics.restore();
    }

    void drawAgent(AutonomousAgent agent, RenderLayout layout) {
        agentAnimation.sample(agent.x(), agent.y());
        double scale = layout.scale();
        agentGlow.setRadius(9.0 * scale);
        double logicalSize = VisualAssets.PATHFINDER_ROBOT == null
                ? agent.size() : AGENT_VISUAL_SIZE;
        double x = layout.screenX(agent.centerX() - logicalSize / 2.0);
        double y = layout.screenY(agent.centerY() - logicalSize / 2.0);
        double size = logicalSize * scale;
        double bob = Math.rint(agentAnimation.moving()
                ? Math.abs(Math.sin(visualSeconds() * 7.2 + Math.PI)) * 1.25 * scale
                : 0.0);
        double spriteY = Math.rint(y - bob);

        drawLocator(x, y, size, scale, AGENT_LOCATOR);
        drawShadow(x, y, size, bob, scale);
        if (VisualAssets.PATHFINDER_ROBOT != null) {
            drawSprite(VisualAssets.PATHFINDER_ROBOT, x, spriteY, size, agentGlow,
                    agentAnimation.facingLeft(), agentAnimation.walkFrame(), scale);
            drawAgentSignal(x, spriteY, size, scale);
            return;
        }

        graphics.save();
        graphics.translate(x + (agentAnimation.facingLeft() ? size : 0.0), spriteY);
        graphics.scale(agentAnimation.facingLeft() ? -scale : scale, scale);
        drawFallbackAgent(agent.size());
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
        graphics.fillRect(Math.rint(x + size / 2.0 - lightSize / 2.0),
                Math.rint(y + 2.0 * scale), lightSize, lightSize);
        graphics.setGlobalAlpha(1.0);
    }

    private void drawShadow(double x, double y, double size, double bob, double scale) {
        double shadowWidth = size - 8.0 * scale;
        double shadowHeight = 6.0 * scale;
        double bobDistance = Math.abs(bob) / Math.max(scale, 0.001);
        graphics.setFill(ACTOR_SHADOW);
        graphics.setGlobalAlpha(Math.max(0.48, 0.76 - bobDistance * 0.08));
        graphics.fillOval(x + (size - shadowWidth) / 2.0,
                y + size - shadowHeight * 0.55, shadowWidth, shadowHeight);
        graphics.setGlobalAlpha(1.0);
    }

    private void drawLocator(double x, double y, double size, double scale, Color color) {
        double width = size * 0.72;
        double height = 8.0 * scale;
        graphics.setStroke(color);
        graphics.setLineWidth(Math.max(1.0, scale));
        graphics.strokeOval(x + (size - width) / 2.0,
                y + size - height * 0.72, width, height);
    }

    private void drawSprite(Image sprite, double x, double y, double size,
            DropShadow glow, boolean facingLeft, int animationFrame, double scale) {
        double stepOffset = switch (animationFrame) {
            case 1 -> -scale;
            case 3 -> scale;
            default -> 0.0;
        };
        double heightFactor = animationFrame % 2 == 0 ? 1.0 : 0.97;
        double animatedHeight = size * heightFactor;

        graphics.save();
        graphics.translate(x + stepOffset + (facingLeft ? size : 0.0),
                y + size - animatedHeight);
        graphics.scale(facingLeft ? -1.0 : 1.0, 1.0);
        graphics.setEffect(glow);
        graphics.drawImage(sprite, 0.0, 0.0, size, animatedHeight);
        graphics.setEffect(null);
        graphics.restore();
    }

    private static double visualSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
