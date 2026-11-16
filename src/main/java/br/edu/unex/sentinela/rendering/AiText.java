package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import java.util.Locale;

/** Textos curtos mostrados na tela, sempre montados a partir de dados reais. */
final class AiText {

    private static final Locale BRAZIL = Locale.of("pt", "BR");

    private AiText() {
    }

    static String robotLabel(String name, String behavior) {
        return name + " · " + behavior;
    }

    // A árvore de comportamento informa "-" antes de escolher a primeira ação.
    static String behavior(String current) {
        return "-".equals(current) ? "AGUARDANDO" : current;
    }

    static String alertIcon(AlertLevel level) {
        return switch (level) {
            case ROUTINE -> "";
            case ATTENTION, INVESTIGATION -> "?";
            case COMBAT -> "!";
        };
    }

    static String objective(boolean mapHasCard, boolean cardCollected) {
        if (mapHasCard && !cardCollected) {
            return "PEGUE O CARTÃO DE ACESSO";
        }
        return mapHasCard ? "PORTA ABERTA: VÁ PARA A SAÍDA" : "ALCANCE A SAÍDA VERDE";
    }

    static String seconds(double value) {
        return oneDecimal(value) + "s";
    }

    static String oneDecimal(double value) {
        return String.format(BRAZIL, "%.1f", value);
    }

    static String decimal(double value) {
        return String.format(BRAZIL, "%.2f", value);
    }

    static String tile(GridPosition position) {
        return "L%02d C%02d".formatted(position.row(), position.column());
    }

    static String transition(DecisionLog.Entry entry) {
        String from = "-".equals(entry.from()) ? "INÍCIO" : entry.from();
        return from + "→" + behavior(entry.to());
    }

    static String decision(DecisionLog.Entry entry) {
        return "%s %s %s (%s)".formatted(seconds(entry.time()), entry.robot(),
                transition(entry), entry.reason());
    }
}
