package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import br.edu.unex.sentinela.game.LaboratoryMap;
import org.junit.jupiter.api.Test;

class LaboratoryThemeTest {

    @Test
    void associatesEachMapWithItsVisualTheme() {
        assertEquals(
                LaboratoryTheme.ESCAPE_ROUTE,
                LaboratoryTheme.forMap(LaboratoryMap.ESCAPE_ROUTE)
        );
        assertEquals(
                LaboratoryTheme.DATA_CORE,
                LaboratoryTheme.forMap(LaboratoryMap.DATA_CORE)
        );
        assertEquals(
                LaboratoryTheme.CONTAINMENT,
                LaboratoryTheme.forMap(LaboratoryMap.CONTAINMENT)
        );
    }

    @Test
    void usesADifferentAccentForEveryLaboratory() {
        assertNotEquals(LaboratoryTheme.ESCAPE_ROUTE.accent(), LaboratoryTheme.DATA_CORE.accent());
        assertNotEquals(LaboratoryTheme.DATA_CORE.accent(), LaboratoryTheme.CONTAINMENT.accent());
        assertNotEquals(LaboratoryTheme.CONTAINMENT.accent(), LaboratoryTheme.ESCAPE_ROUTE.accent());
        assertNotEquals(
                LaboratoryTheme.ESCAPE_ROUTE.routeCore(),
                LaboratoryTheme.DATA_CORE.routeCore()
        );
        assertNotEquals(
                LaboratoryTheme.DATA_CORE.routeCore(),
                LaboratoryTheme.CONTAINMENT.routeCore()
        );
    }
}
