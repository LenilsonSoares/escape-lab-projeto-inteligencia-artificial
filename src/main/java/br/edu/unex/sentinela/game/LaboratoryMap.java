package br.edu.unex.sentinela.game;

import br.edu.unex.sentinela.navigation.GridPosition;

/**
 * Catálogo dos laboratórios usados para demonstrar a navegação.
 */
public enum LaboratoryMap {

    ESCAPE_ROUTE(
            "ROTA DE FUGA",
            "####################",
            "#...P.........#....#",
            "#.EE...#...EE.#.EE.#",
            "#.EE...#...EE.#.EE.#",
            "#......#...........#",
            "###.##.#.##.####.#.#",
            "#..................#",
            "#.###.###.###.#.##.#",
            "#..................#",
            "###.#.###.#.####.###",
            "#...#.....#........#",
            "#.E.#.EEE.#.E.#.EE.#",
            "#.E.........E...EE.#",
            "#A..#.....#........X",
            "####################"
    ),
    DATA_CORE(
            "NÚCLEO DE DADOS",
            "####################",
            "#P.......#.........X",
            "#.EEE....#..EEE....#",
            "#........#.........#",
            "#..#####.#..#####..#",
            "#........#.........#",
            "####.##########.####",
            "#..................#",
            "#.####..######..####",
            "#....#..........#..#",
            "#....#..EEEE....#..#",
            "#....#..........#..#",
            "#.EE....######.....#",
            "#.................A#",
            "####################"
    ),
    CONTAINMENT(
            "CONTENÇÃO",
            "####################",
            "#....E......E.....P#",
            "#.#####.######.###.#",
            "#.....#........#...#",
            "###.#.########.#.###",
            "#...#....E.....#...#",
            "#.######....######.#",
            "#..................#",
            "#.######....######.#",
            "#...#....E.....#...#",
            "###.#.########.#.###",
            "#...#........#.....#",
            "#.###.######.#####.#",
            "#A....E......E.....X",
            "####################"
    );

    private final String displayName;
    private final TileMap tileMap;
    private final GridPosition agentStart;
    private final GridPosition exitPosition;

    LaboratoryMap(String displayName, String... rows) {
        this.displayName = displayName;
        ParsedMap parsedMap = parse(rows);
        this.tileMap = parsedMap.tileMap();
        this.agentStart = parsedMap.agentStart();
        this.exitPosition = parsedMap.exitPosition();
    }

    public String displayName() {
        return displayName;
    }

    public int number() {
        return ordinal() + 1;
    }

    public TileMap tileMap() {
        return tileMap;
    }

    public GridPosition agentStart() {
        return agentStart;
    }

    public GridPosition exitPosition() {
        return exitPosition;
    }

    public static LaboratoryMap fromNumber(int number) {
        if (number < 1 || number > values().length) {
            throw new IllegalArgumentException("O número do mapa deve estar entre 1 e 3");
        }
        return values()[number - 1];
    }

    private static ParsedMap parse(String[] rows) {
        if (rows.length < 15) {
            throw new IllegalArgumentException("O laboratório deve ter pelo menos 15 linhas");
        }

        int columns = rows[0].length();
        if (columns < 15) {
            throw new IllegalArgumentException("O laboratório deve ter pelo menos 15 colunas");
        }
        TileType[][] tiles = new TileType[rows.length][columns];
        GridPosition playerStart = null;
        GridPosition agentStart = null;
        GridPosition exitPosition = null;

        for (int row = 0; row < rows.length; row++) {
            if (rows[row].length() != columns) {
                throw new IllegalArgumentException("Todas as linhas do mapa devem ter o mesmo tamanho");
            }
            for (int column = 0; column < columns; column++) {
                char symbol = rows[row].charAt(column);
                tiles[row][column] = tileType(symbol);
                if (symbol == 'P') {
                    playerStart = uniqueStart(playerStart, row, column, "jogador");
                } else if (symbol == 'A') {
                    agentStart = uniqueStart(agentStart, row, column, "agente");
                } else if (symbol == 'X') {
                    exitPosition = uniqueStart(exitPosition, row, column, "saída");
                }
            }
        }

        if (playerStart == null || agentStart == null || exitPosition == null) {
            throw new IllegalArgumentException("O mapa precisa dos pontos P, A e X");
        }

        TileMap tileMap = new TileMap(
                tiles,
                TileMap.LABORATORY_TILE_SIZE,
                playerStart.row(),
                playerStart.column()
        );
        return new ParsedMap(tileMap, agentStart, exitPosition);
    }

    private static TileType tileType(char symbol) {
        // Legenda: ponto = piso, # = parede, E = equipamento e X = saída.
        // P e A marcam apenas as posições iniciais sobre um piso transitável.
        return switch (symbol) {
            case '.', 'P', 'A' -> TileType.LAB_FLOOR;
            case 'X' -> TileType.EXIT;
            case '#' -> TileType.WALL;
            case 'E' -> TileType.EQUIPMENT;
            default -> throw new IllegalArgumentException("Símbolo desconhecido no mapa: " + symbol);
        };
    }

    private static GridPosition uniqueStart(
            GridPosition current,
            int row,
            int column,
            String description
    ) {
        if (current != null) {
            throw new IllegalArgumentException("O mapa possui mais de um início para o " + description);
        }
        return new GridPosition(row, column);
    }

    private record ParsedMap(
            TileMap tileMap,
            GridPosition agentStart,
            GridPosition exitPosition
    ) {
    }
}
