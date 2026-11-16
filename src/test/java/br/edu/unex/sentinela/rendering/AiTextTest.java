package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.navigation.GridPosition;
import br.edu.unex.sentinela.telemetry.DecisionLog;
import org.junit.jupiter.api.Test;

class AiTextTest {

    @Test
    void joinsRobotNameAndStateInLabel() {
        assertEquals("PATRULHEIRO · PERSEGUIR", AiText.robotLabel("PATRULHEIRO", "PERSEGUIR"));
    }

    @Test
    void showsQuestionMarkWhileSuspiciousAndExclamationInCombat() {
        assertEquals("", AiText.alertIcon(AlertLevel.ROUTINE));
        assertEquals("?", AiText.alertIcon(AlertLevel.ATTENTION));
        assertEquals("?", AiText.alertIcon(AlertLevel.INVESTIGATION));
        assertEquals("!", AiText.alertIcon(AlertLevel.COMBAT));
    }

    @Test
    void objectiveChangesAfterTheCardIsCollected() {
        assertEquals("PEGUE O CARTÃO DE ACESSO", AiText.objective(true, false));
        assertEquals("PORTA ABERTA: VÁ PARA A SAÍDA", AiText.objective(true, true));
        assertEquals("ALCANCE A SAÍDA VERDE", AiText.objective(false, false));
    }

    @Test
    void formatsDecisionWithBrazilianDecimalComma() {
        DecisionLog.Entry entry = new DecisionLog.Entry(12.44, "PATRULHEIRO", "PATRULHAR", "PERSEGUIR",
                "VIU O JOGADOR");

        assertEquals("12,4s PATRULHEIRO PATRULHAR→PERSEGUIR (VIU O JOGADOR)", AiText.decision(entry));
    }

    @Test
    void namesTheFirstActionOfTheBehaviorTree() {
        DecisionLog.Entry entry = new DecisionLog.Entry(0.0, "INTERCEPTADOR", "-", "RONDAR", "RAMO DA ÁRVORE");

        assertEquals("INÍCIO→RONDAR", AiText.transition(entry));
        assertEquals("AGUARDANDO", AiText.behavior("-"));
        assertEquals("VIGIAR", AiText.behavior("VIGIAR"));
    }

    @Test
    void writesTilesAsRowAndColumn() {
        assertEquals("L03 C11", AiText.tile(new GridPosition(3, 11)));
        assertEquals("0,42", AiText.decimal(0.4249));
    }
}
