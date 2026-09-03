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
    private WritableImage cachedTiles;

    WritableImage imageFor(TileMap tileMap) {
        if (tileMap == cachedTileMap && cachedTiles != null) {
            return cachedTiles;
        }

        Canvas cacheCanvas = new Canvas(tileMap.pixelWidth(), tileMap.pixelHeight());
        GraphicsContext cacheGraphics = cacheCanvas.getGraphicsContext2D();
        cacheGraphics.setImageSmoothing(false);
        new LaboratoryTilePainter(cacheGraphics).draw(tileMap, 0.0, 0.0);

        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);
        cachedTiles = new WritableImage(tileMap.pixelWidth(), tileMap.pixelHeight());
        cacheCanvas.snapshot(snapshotParameters, cachedTiles);
        cachedTileMap = tileMap;
        return cachedTiles;
    }
}
