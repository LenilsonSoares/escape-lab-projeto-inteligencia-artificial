package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Mantém em memória a imagem dos tiles estáticos do mapa atual.
 */
final class LaboratoryTileCache {

    private TileMap cachedTileMap;
    private LaboratoryTheme cachedTheme;
    private double cachedScale;
    private WritableImage cachedTiles;

    WritableImage imageFor(TileMap tileMap, LaboratoryTheme theme, double scale) {
        if (tileMap == cachedTileMap
                && theme == cachedTheme
                && scale == cachedScale
                && cachedTiles != null) {
            return cachedTiles;
        }

        int width = (int) Math.rint(tileMap.pixelWidth() * scale);
        int height = (int) Math.rint(tileMap.pixelHeight() * scale);
        Canvas cacheCanvas = new Canvas(width, height);
        GraphicsContext cacheGraphics = cacheCanvas.getGraphicsContext2D();
        cacheGraphics.setImageSmoothing(false);
        cacheGraphics.scale(scale, scale);
        new LaboratoryTilePainter(cacheGraphics, theme).draw(tileMap, 0.0, 0.0);

        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);
        cachedTiles = new WritableImage(width, height);
        cacheCanvas.snapshot(snapshotParameters, cachedTiles);
        cachedTileMap = tileMap;
        cachedTheme = theme;
        cachedScale = scale;
        return cachedTiles;
    }
}
