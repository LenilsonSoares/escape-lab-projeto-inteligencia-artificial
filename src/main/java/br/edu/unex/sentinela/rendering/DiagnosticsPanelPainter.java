package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.ai.coop.TeamBlackboard;
import br.edu.unex.sentinela.ai.director.AiDirector;
import br.edu.unex.sentinela.ai.memory.Evidence;
import br.edu.unex.sentinela.ai.utility.ScoredPoint;
import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.game.AiMode;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import java.util.List;
import java.util.Optional;
import javafx.scene.paint.Color;

/** Painel direito: diretor de IA, blackboard, Utility AI e decisões recentes. */
final class DiagnosticsPanelPainter {

    private static final int DECISIONS_SHOWN = 4;
    private static final int SEARCH_POINTS_SHOWN = 3;
    private static final double SECTION_GAP = 22.0;

    private final PanelKit kit;
    private final MiniMapPainter miniMapPainter;

    DiagnosticsPanelPainter(PanelKit kit) {
        this.kit = kit;
        this.miniMapPainter = new MiniMapPainter(kit.graphics());
    }

    void draw(GameWorld world, FrameMetrics metrics, RenderLayout layout, boolean debugVisible) {
        double x = layout.rightRailX();
        double y = layout.mapY();
        double width = layout.railWidth();
        double height = layout.mapHeight();
        double padding = layout.compact() ? 10.0 : 14.0;
        double contentX = x + padding;
        double contentWidth = width - padding * 2.0;

        kit.techPanel(x, y, width, height, PanelKit.PLAYER);
        kit.panelTitle("DIAGNÓSTICO", "DADOS REAIS DA EXECUÇÃO", contentX, y, contentWidth, layout.compact());

        double cursor = y + 72.0;
        if (world.mode() == AiMode.FULL_AI) {
            cursor = drawDirector(world.director(), contentX, cursor, contentWidth);
            if (debugVisible) {
                cursor = drawBlackboard(world, contentX, cursor, contentWidth);
                cursor = drawUtility(world.enemies(), contentX, cursor, contentWidth);
            }
            drawDecisions(world.decisions(), contentX, cursor, contentWidth);
        } else {
            if (debugVisible) {
                cursor = drawMiniMap(world, contentX, cursor, contentWidth, layout.compact());
            }
            drawNavigationDemo(world, contentX, cursor, contentWidth);
        }
        kit.text("FPS %s  •  %s ms".formatted(
                        AiText.oneDecimal(metrics.framesPerSecond()),
                        AiText.decimal(metrics.deltaTime() * 1_000.0)),
                contentX, y + height - 16.0, contentWidth, kit.smallFont, PanelKit.MUTED);
    }

    private double drawDirector(AiDirector director, double x, double y, double width) {
        kit.sectionTitle("DIRETOR DE IA", x, y, width);
        double lineY = y + 18.0;
        kit.line("PRESSÃO q", AiText.decimal(director.pressure()), pressureColor(director.pressure()),
                x, lineY, width);
        kit.line("PRESSÃO SUAVIZADA", AiText.decimal(director.smoothedPressure()),
                pressureColor(director.smoothedPressure()), x, lineY + PanelKit.LINE, width);
        kit.line("VAGAS DE ATAQUE", "%d / %d".formatted(director.attackBudget(), director.maxBudget()),
                PanelKit.DESTINATION, x, lineY + PanelKit.LINE * 2, width);
        kit.text(director.lastAdjustment(), x, lineY + PanelKit.LINE * 3, width, kit.smallFont, PanelKit.MUTED);
        return closeSection(x, lineY + PanelKit.LINE * 3, width);
    }

    private double drawBlackboard(GameWorld world, double x, double y, double width) {
        TeamBlackboard team = world.team();
        kit.sectionTitle("BLACKBOARD (RÁDIO)", x, y, width);
        double lineY = y + 18.0;
        Optional<Evidence> report = team.latestReport();
        if (report.isPresent()) {
            Evidence evidence = report.get();
            kit.line("ÚLTIMO RELATO", "HÁ " + AiText.seconds(evidence.ageAt(world.time())), PanelKit.TEXT,
                    x, lineY, width);
            kit.text(evidence.reporter(), x + 8.0, lineY + PanelKit.LINE, width * 0.6, kit.labelFont,
                    reporterColor(evidence.reporter()));
            kit.rightText(AiText.tile(evidence.position()), x + width, lineY + PanelKit.LINE, width * 0.4,
                    kit.labelFont, PanelKit.DESTINATION);
        } else {
            kit.line("ÚLTIMO RELATO", "NENHUM", PanelKit.MUTED, x, lineY, width);
            kit.text("SEM PISTAS NO RÁDIO", x + 8.0, lineY + PanelKit.LINE, width, kit.smallFont, PanelKit.MUTED);
        }
        kit.line("RESERVAS ATIVAS", Integer.toString(team.activeReservations(world.time()).size()),
                PanelKit.ACCENT, x, lineY + PanelKit.LINE * 2, width);
        kit.line("COM VAGA DE ATAQUE", "%d / %d".formatted(team.attackers().size(), team.maxAttackers()),
                PanelKit.DANGER, x, lineY + PanelKit.LINE * 3, width);
        String attackers = team.attackers().isEmpty() ? "NINGUÉM" : String.join(", ", team.attackers());
        kit.text(attackers, x + 8.0, lineY + PanelKit.LINE * 4, width - 8.0, kit.smallFont, PanelKit.MUTED);
        return closeSection(x, lineY + PanelKit.LINE * 4, width);
    }

    private double drawUtility(List<Enemy> enemies, double x, double y, double width) {
        Optional<Enemy> searcher = enemies.stream()
                .filter(enemy -> !enemy.searchRanking().isEmpty())
                .findFirst();
        double lineY = y + 18.0;
        if (searcher.isEmpty()) {
            kit.sectionTitle("UTILITY AI · BUSCA", x, y, width);
            kit.text("NENHUM ROBÔ PROCURANDO", x, lineY, width, kit.smallFont, PanelKit.MUTED);
            return closeSection(x, lineY, width);
        }
        Enemy enemy = searcher.get();
        kit.sectionTitle("UTILITY AI · " + enemy.type().displayName(), x, y, width);
        drawUtilityRow("PONTO", "EVID", "N.VER", "CUSTO", "NOTA", PanelKit.MUTED, x, lineY, width);
        List<ScoredPoint> ranking = enemy.searchRanking();
        int shown = Math.min(SEARCH_POINTS_SHOWN, ranking.size());
        for (int index = 0; index < shown; index++) {
            ScoredPoint point = ranking.get(index);
            drawUtilityRow(AiText.tile(point.position()), AiText.decimal(point.evidence()),
                    AiText.decimal(point.unverified()), AiText.decimal(point.cost()),
                    AiText.decimal(point.score()), index == 0 ? PanelKit.DESTINATION : PanelKit.TEXT,
                    x, lineY + PanelKit.LINE * (index + 1), width);
        }
        return closeSection(x, lineY + PanelKit.LINE * shown, width);
    }

    private void drawUtilityRow(String point, String evidence, String unverified, String cost,
            String score, Color color, double x, double y, double width) {
        double column = width * 0.17;
        kit.text(point, x, y, width * 0.32, kit.smallFont, color);
        kit.rightText(evidence, x + width * 0.32 + column, y, column, kit.smallFont, color);
        kit.rightText(unverified, x + width * 0.32 + column * 2, y, column, kit.smallFont, color);
        kit.rightText(cost, x + width * 0.32 + column * 3, y, column, kit.smallFont, color);
        kit.rightText(score, x + width, y, column, kit.labelFont, color);
    }

    private void drawDecisions(DecisionLog log, double x, double y, double width) {
        kit.sectionTitle("DECISÕES RECENTES", x, y, width);
        List<DecisionLog.Entry> entries = log.entries();
        double rowY = y + 18.0;
        if (entries.isEmpty()) {
            kit.text("NENHUMA TRANSIÇÃO AINDA", x, rowY, width, kit.smallFont, PanelKit.MUTED);
            return;
        }
        for (int index = 0; index < Math.min(DECISIONS_SHOWN, entries.size()); index++) {
            DecisionLog.Entry entry = entries.get(index);
            kit.text(AiText.seconds(entry.time()), x, rowY, 40.0, kit.smallFont, PanelKit.MUTED);
            kit.text(entry.robot(), x + 40.0, rowY, width - 40.0, kit.labelFont, reporterColor(entry.robot()));
            kit.text(AiText.transition(entry), x + 8.0, rowY + 12.0, width - 8.0, kit.labelFont, PanelKit.TEXT);
            kit.text("(" + entry.reason() + ")", x + 8.0, rowY + 24.0, width - 8.0, kit.smallFont, PanelKit.MUTED);
            rowY += 42.0;
        }
    }

    private double drawMiniMap(GameWorld world, double x, double y, double width, boolean compact) {
        kit.sectionTitle("MAPA LÓGICO %d × %d".formatted(world.tileMap().rows(), world.tileMap().columns()),
                x, y, width);
        double miniMapWidth = MiniMapPainter.fittedWidth(world.tileMap(), Math.min(width, compact ? 136.0 : 188.0));
        double miniMapHeight = MiniMapPainter.proportionalHeight(world.tileMap(), miniMapWidth);
        miniMapPainter.draw(world, x + (width - miniMapWidth) / 2.0, y + 12.0, miniMapWidth, miniMapHeight);
        return closeSection(x, y + 12.0 + miniMapHeight, width);
    }

    private void drawNavigationDemo(GameWorld world, double x, double y, double width) {
        Enemy chaser = world.enemies().getFirst();
        kit.sectionTitle("EXECUÇÃO", x, y, width);
        double lineY = y + 18.0;
        kit.line("ROBÔ", AiText.tile(chaser.agent().currentGridPosition()),
                AiPalette.identity(chaser.type()), x, lineY, width);
        kit.line("DESTINO", AiText.tile(world.navigationDestination()), PanelKit.DESTINATION,
                x, lineY + PanelKit.LINE, width);
        kit.line("CAMINHO", "%02d PASSOS".formatted(Math.max(0, world.navigationPath().size() - 1)),
                PanelKit.ACCENT, x, lineY + PanelKit.LINE * 2, width);
        kit.line("PONTOS", "%04d".formatted(world.score()), PanelKit.PLAYER, x, lineY + PanelKit.LINE * 3, width);
        kit.line("PENALIDADES", Integer.toString(world.penalties()), PanelKit.DESTINATION,
                x, lineY + PanelKit.LINE * 4, width);
        kit.line("CONTATO", "-100 PTS", PanelKit.DANGER, x, lineY + PanelKit.LINE * 5, width);
    }

    private double closeSection(double x, double lastLineY, double width) {
        kit.divider(x, lastLineY + 10.0, width);
        return lastLineY + 10.0 + SECTION_GAP;
    }

    // Mesmos limites da histerese do diretor: acima de 0,75 reduz, abaixo de 0,35 aumenta.
    private static Color pressureColor(double pressure) {
        if (pressure > 0.75) {
            return PanelKit.DANGER;
        }
        return pressure < 0.35 ? PanelKit.EXIT : PanelKit.DESTINATION;
    }

    private static Color reporterColor(String robotName) {
        for (EnemyType type : EnemyType.values()) {
            if (type.displayName().equals(robotName)) {
                return AiPalette.identity(type);
            }
        }
        return PanelKit.TEXT;
    }
}
