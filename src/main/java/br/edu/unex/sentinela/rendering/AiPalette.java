package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.ai.perception.NoiseSource;
import br.edu.unex.sentinela.entity.EnemyType;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.paint.Color;

/** Cores da IA criadas uma única vez, para não converter texto em cor a cada quadro. */
final class AiPalette {

    static final Color COMBAT_CONE = Color.web("#ff3b4f");
    static final Color VISITED_NODE = Color.web("#45d7f2");
    static final Color GRID_LINE = Color.web("#9fd8ff");
    static final Color LABEL_BACKGROUND = Color.web("#03101a", 0.90);
    static final Color BAR_TRACK = Color.web("#1d3443");
    static final Color ICON_TEXT = Color.web("#120a02");

    private static final Map<EnemyType, Color> IDENTITY = new EnumMap<>(EnemyType.class);
    private static final Map<AlertLevel, Color> LEVEL = new EnumMap<>(AlertLevel.class);
    private static final Map<NoiseSource, Color> NOISE = new EnumMap<>(NoiseSource.class);

    static {
        for (EnemyType type : EnemyType.values()) {
            IDENTITY.put(type, Color.web(type.color()));
        }
        LEVEL.put(AlertLevel.ROUTINE, Color.web("#52e889"));
        LEVEL.put(AlertLevel.ATTENTION, Color.web("#ffd166"));
        LEVEL.put(AlertLevel.INVESTIGATION, Color.web("#ff9f43"));
        LEVEL.put(AlertLevel.COMBAT, Color.web("#ef476f"));
        NOISE.put(NoiseSource.FOOTSTEP, Color.web("#d8f1ff"));
        NOISE.put(NoiseSource.CARD_PICKUP, Color.web("#5ef0ff"));
        NOISE.put(NoiseSource.DOOR, Color.web("#ffb347"));
    }

    private AiPalette() {
    }

    static Color identity(EnemyType type) {
        return IDENTITY.get(type);
    }

    static Color level(AlertLevel level) {
        return LEVEL.get(level);
    }

    static Color noise(NoiseSource source) {
        return NOISE.get(source);
    }
}
