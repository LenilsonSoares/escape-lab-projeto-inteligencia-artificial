package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.LaboratoryMap;
import br.edu.unex.sentinela.game.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Desenha placas próprias para identificar as áreas de cada laboratório.
 */
final class LaboratoryLabelsPainter {

    private static final Color PANEL = Color.web("#03101a", 0.72);
    private static final Color TEXT = Color.web("#d9f8ff", 0.72);
    private final GraphicsContext graphics;

    LaboratoryLabelsPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(LaboratoryMap map, RenderLayout layout, LaboratoryTheme theme) {
        graphics.save();
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(Font.font(
                "Monospaced",
                FontWeight.BOLD,
                8.0 * layout.scale()
        ));
        for (SectorLabel label : labelsFor(map)) {
            drawLabel(label, layout, theme);
        }
        graphics.restore();
    }

    private void drawLabel(
            SectorLabel label,
            RenderLayout layout,
            LaboratoryTheme theme
    ) {
        double scale = layout.scale();
        double tileSize = TileMap.LABORATORY_TILE_SIZE * scale;
        double x = layout.mapX() + label.column() * tileSize;
        double baseline = layout.mapY() + label.row() * tileSize;
        double width = label.width() * scale;
        double height = 14.0 * scale;

        graphics.setFill(PANEL);
        graphics.fillRoundRect(
                x - 4.0 * scale,
                baseline - 10.0 * scale,
                width,
                height,
                3.0 * scale,
                3.0 * scale
        );
        graphics.setStroke(theme.accent(0.62));
        graphics.setLineWidth(Math.max(0.75, 0.75 * scale));
        graphics.strokeRoundRect(
                x - 4.0 * scale,
                baseline - 10.0 * scale,
                width,
                height,
                3.0 * scale,
                3.0 * scale
        );
        graphics.setFill(TEXT);
        graphics.fillText(label.text(), x, baseline, width - 6.0 * scale);
    }

    private static SectorLabel[] labelsFor(LaboratoryMap map) {
        return switch (map) {
            case ESCAPE_ROUTE -> new SectorLabel[] {
                new SectorLabel("BIO LAB", 1.2, 1.65, 64.0),
                new SectorLabel("DADOS", 9.15, 1.65, 54.0),
                new SectorLabel("CONTROLE", 16.1, 1.65, 72.0),
                new SectorLabel("SETOR 07", 6.3, 6.68, 70.0),
                new SectorLabel("PESQUISA", 6.1, 10.68, 70.0),
                new SectorLabel("SERVIDORES", 16.1, 10.68, 88.0)
            };
            case DATA_CORE -> new SectorLabel[] {
                new SectorLabel("ACESSO", 1.2, 1.65, 58.0),
                new SectorLabel("NÚCLEO DE DADOS", 7.8, 1.65, 116.0),
                new SectorLabel("ARQUIVOS", 15.5, 1.65, 72.0),
                new SectorLabel("REDE CENTRAL", 7.7, 6.68, 92.0),
                new SectorLabel("TERMINAIS", 2.0, 10.68, 82.0),
                new SectorLabel("SAÍDA", 16.4, 10.68, 54.0)
            };
            case CONTAINMENT -> new SectorLabel[] {
                new SectorLabel("CONTENÇÃO", 1.2, 1.65, 82.0),
                new SectorLabel("CÂMARAS", 8.2, 1.65, 68.0),
                new SectorLabel("SEGURANÇA", 15.6, 1.65, 82.0),
                new SectorLabel("CORREDOR", 7.9, 6.68, 74.0),
                new SectorLabel("ISOLAMENTO", 2.0, 10.68, 88.0),
                new SectorLabel("EVACUAÇÃO", 15.7, 10.68, 82.0)
            };
        };
    }

    private record SectorLabel(String text, double column, double row, double width) {
    }
}
