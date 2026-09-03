package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.LaboratoryMap;
import java.util.Objects;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

/**
 * Centraliza a identidade visual de cada laboratório sem alterar seus tiles.
 */
enum LaboratoryTheme {

    ESCAPE_ROUTE("#45d7f2", "#1598ad", "#62f4ff", "#7cecff", "#50f2da"),
    DATA_CORE("#a78bfa", "#7357c9", "#c4b5fd", "#b9a7ff", "#9b87ff"),
    CONTAINMENT("#ff9f43", "#a94658", "#ffd166", "#ffc46b", "#ff7b54");

    private final Color accent;
    private final Color ambientTint;
    private final Color scanCore;
    private final Color particle;
    private final Color equipmentSignal;
    private final Paint scanBand;

    LaboratoryTheme(
            String accent,
            String ambientTint,
            String scanCore,
            String particle,
            String equipmentSignal
    ) {
        this.accent = Color.web(accent);
        this.ambientTint = Color.web(ambientTint, 0.055);
        this.scanCore = Color.web(scanCore, 0.17);
        this.particle = Color.web(particle);
        this.equipmentSignal = Color.web(equipmentSignal);
        this.scanBand = new LinearGradient(
                0.0,
                0.0,
                0.0,
                1.0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(0.45, withOpacity(this.accent, 0.025)),
                new Stop(0.5, withOpacity(this.accent, 0.14)),
                new Stop(0.55, withOpacity(this.accent, 0.025)),
                new Stop(1.0, Color.TRANSPARENT)
        );
    }

    static LaboratoryTheme forMap(LaboratoryMap map) {
        Objects.requireNonNull(map, "O mapa não pode ser nulo");
        return switch (map) {
            case ESCAPE_ROUTE -> ESCAPE_ROUTE;
            case DATA_CORE -> DATA_CORE;
            case CONTAINMENT -> CONTAINMENT;
        };
    }

    Color accent() {
        return accent;
    }

    Color ambientTint() {
        return ambientTint;
    }

    Color scanCore() {
        return scanCore;
    }

    Color particle() {
        return particle;
    }

    Color equipmentSignal() {
        return equipmentSignal;
    }

    Paint scanBand() {
        return scanBand;
    }

    Color accent(double opacity) {
        return withOpacity(accent, opacity);
    }

    private static Color withOpacity(Color color, double opacity) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }
}
