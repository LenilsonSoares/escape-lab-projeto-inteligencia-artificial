package br.edu.unex.sentinela.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MovementInputTest {

    @Test
    void limitsAxesToExpectedRange() {
        MovementInput input = new MovementInput(3.0, -4.0);

        assertEquals(1.0, input.horizontal());
        assertEquals(-1.0, input.vertical());
    }

    @Test
    void rejectsNonFiniteAxes() {
        assertThrows(IllegalArgumentException.class, () -> new MovementInput(Double.NaN, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new MovementInput(0.0, Double.POSITIVE_INFINITY));
    }
}
