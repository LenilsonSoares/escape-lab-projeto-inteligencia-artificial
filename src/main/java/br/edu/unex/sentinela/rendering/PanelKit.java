package br.edu.unex.sentinela.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Peças visuais comuns aos painéis laterais, em coordenadas lógicas do HUD. */
final class PanelKit {

    static final Color PANEL = Color.web("#081521", 0.96);
    static final Color PANEL_INNER = Color.web("#102638", 0.72);
    static final Color BORDER = Color.web("#28556d");
    static final Color ACCENT = Color.web("#45d7f2");
    static final Color TEXT = Color.web("#e9f5ff");
    static final Color MUTED = Color.web("#87a5b8");
    static final Color PLAYER = Color.web("#38d9a9");
    static final Color DESTINATION = Color.web("#ffd166");
    static final Color EXIT = Color.web("#52e889");
    static final Color DANGER = Color.web("#ef476f");
    static final Color SWATCH_BACKDROP = Color.web("#06121d");
    static final double LINE = 14.0;

    final Font titleFont = Font.font("Monospaced", FontWeight.BOLD, 18.0);
    final Font compactTitleFont = Font.font("Monospaced", FontWeight.BOLD, 14.0);
    final Font headingFont = Font.font("Monospaced", FontWeight.BOLD, 12.0);
    final Font valueFont = Font.font("Monospaced", FontWeight.NORMAL, 11.0);
    final Font compactValueFont = Font.font("Monospaced", FontWeight.NORMAL, 10.0);
    final Font labelFont = Font.font("System", FontWeight.BOLD, 9.0);
    final Font smallFont = Font.font("System", FontWeight.NORMAL, 9.0);

    private final GraphicsContext graphics;

    PanelKit(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    GraphicsContext graphics() {
        return graphics;
    }

    void techPanel(double x, double y, double width, double height, Color accent) {
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

    void panelTitle(String title, String subtitle, double x, double panelY, double width, boolean compact) {
        text(title, x, panelY + 27.0, width, compact ? compactTitleFont : titleFont, TEXT);
        text(subtitle, x, panelY + 45.0, width, smallFont, ACCENT);
    }

    void sectionTitle(String title, double x, double y, double width) {
        text(title, x, y, width, labelFont, ACCENT);
    }

    void divider(double x, double y, double width) {
        graphics.setFill(BORDER);
        graphics.fillRect(x, y, width, 1.0);
    }

    /** Rótulo à esquerda e valor colorido alinhado à direita, como nas linhas de diagnóstico. */
    void line(String label, String value, Color valueColor, double x, double y, double width) {
        text(label, x, y, width * 0.62, smallFont, TEXT);
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(valueColor);
        graphics.setFont(labelFont);
        graphics.fillText(value, x + width, y, width * 0.38);
    }

    void text(String value, double x, double y, double maxWidth, Font font, Color color) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(font);
        graphics.setFill(color);
        graphics.fillText(value, x, y, Math.max(1.0, maxWidth));
    }

    void rightText(String value, double rightX, double y, double maxWidth, Font font, Color color) {
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFont(font);
        graphics.setFill(color);
        graphics.fillText(value, rightX, y, Math.max(1.0, maxWidth));
    }

    void swatch(Color color, double x, double y) {
        graphics.setFill(SWATCH_BACKDROP);
        graphics.fillRect(x, y - 9.0, 12.0, 12.0);
        graphics.setFill(color);
        graphics.fillRect(x + 3.0, y - 6.0, 6.0, 6.0);
    }

    void bar(double x, double y, double width, double height, double fraction, Color color) {
        graphics.setFill(AiPalette.BAR_TRACK);
        graphics.fillRect(x, y, width, height);
        graphics.setFill(color);
        graphics.fillRect(x, y, width * Math.clamp(fraction, 0.0, 1.0), height);
    }
}
