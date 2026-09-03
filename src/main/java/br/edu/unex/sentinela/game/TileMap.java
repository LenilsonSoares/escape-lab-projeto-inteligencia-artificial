package br.edu.unex.sentinela.game;

/**
 * Matriz lógica de tiles e regras básicas de ocupação do mapa.
 */
public final class TileMap {

    public static final int LABORATORY_TILE_SIZE = 40;

    private final TileType[][] tiles;
    private final int tileSize;
    private final int playerStartRow;
    private final int playerStartColumn;

    public TileMap(
            TileType[][] tiles,
            int tileSize,
            int playerStartRow,
            int playerStartColumn
    ) {
        this.tiles = copyAndValidate(tiles);
        if (tileSize <= 0) {
            throw new IllegalArgumentException("O tamanho do tile deve ser maior que zero");
        }

        this.tileSize = tileSize;
        this.playerStartRow = playerStartRow;
        this.playerStartColumn = playerStartColumn;

        if (!isWalkable(playerStartRow, playerStartColumn)) {
            throw new IllegalArgumentException("A posição inicial deve estar em um tile transitável");
        }
    }

    public static TileMap createLaboratory() {
        return LaboratoryMap.ESCAPE_ROUTE.tileMap();
    }

    public int rows() {
        return tiles.length;
    }

    public int columns() {
        return tiles[0].length;
    }

    public int tileSize() {
        return tileSize;
    }

    public int pixelWidth() {
        return columns() * tileSize;
    }

    public int pixelHeight() {
        return rows() * tileSize;
    }

    public int playerStartRow() {
        return playerStartRow;
    }

    public int playerStartColumn() {
        return playerStartColumn;
    }

    public double playerStartX(double playerSize) {
        validatePlayerStart(playerSize);
        return centeredCoordinate(playerStartColumn, playerSize);
    }

    public double playerStartY(double playerSize) {
        validatePlayerStart(playerSize);
        return centeredCoordinate(playerStartRow, playerSize);
    }

    public TileType tileAt(int row, int column) {
        if (!contains(row, column)) {
            throw new IndexOutOfBoundsException("Posição fora dos limites do mapa");
        }
        return tiles[row][column];
    }

    public boolean isWalkable(int row, int column) {
        return contains(row, column) && tiles[row][column].isWalkable();
    }

    public boolean canOccupy(double x, double y, double width, double height) {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(width)
                || !Double.isFinite(height)
                || width <= 0.0
                || height <= 0.0) {
            return false;
        }

        double right = x + width;
        double bottom = y + height;
        if (!Double.isFinite(right)
                || !Double.isFinite(bottom)
                || right <= x
                || bottom <= y
                || x < 0.0
                || y < 0.0
                || right > pixelWidth()
                || bottom > pixelHeight()) {
            return false;
        }

        int firstColumn = (int) Math.floor(x / tileSize);
        // nextDown mantém uma borda exatamente sobre a grade no tile anterior.
        // Sem isso, um objeto encostado na parede pareceria ocupar dois tiles.
        int lastColumn = (int) Math.floor(Math.nextDown(right) / tileSize);
        int firstRow = (int) Math.floor(y / tileSize);
        int lastRow = (int) Math.floor(Math.nextDown(bottom) / tileSize);

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                if (!isWalkable(row, column)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean contains(int row, int column) {
        return row >= 0 && row < rows() && column >= 0 && column < columns();
    }

    private double centeredCoordinate(int gridCoordinate, double playerSize) {
        return gridCoordinate * tileSize + (tileSize - playerSize) / 2.0;
    }

    private void validatePlayerStart(double playerSize) {
        if (!Double.isFinite(playerSize) || playerSize <= 0.0) {
            throw new IllegalArgumentException("O tamanho do jogador deve ser maior que zero");
        }

        double x = centeredCoordinate(playerStartColumn, playerSize);
        double y = centeredCoordinate(playerStartRow, playerSize);
        if (!canOccupy(x, y, playerSize, playerSize)) {
            throw new IllegalStateException("O jogador não cabe na posição inicial do mapa");
        }
    }

    private static TileType[][] copyAndValidate(TileType[][] source) {
        if (source == null || source.length == 0 || source[0] == null || source[0].length == 0) {
            throw new IllegalArgumentException("A matriz de tiles não pode ser vazia");
        }

        int columns = source[0].length;
        TileType[][] copy = new TileType[source.length][columns];
        for (int row = 0; row < source.length; row++) {
            if (source[row] == null || source[row].length != columns) {
                throw new IllegalArgumentException("Todas as linhas do mapa devem ter o mesmo tamanho");
            }
            for (int column = 0; column < columns; column++) {
                if (source[row][column] == null) {
                    throw new IllegalArgumentException("A matriz não pode conter tiles nulos");
                }
                copy[row][column] = source[row][column];
            }
        }
        return copy;
    }
}
