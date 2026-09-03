package br.edu.unex.sentinela.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

class InputManagerTest {

    @Test
    void requestsPauseOnlyOnceForEachEscapePress() {
        InputManager inputManager = new InputManager();

        inputManager.handleKeyPressed(KeyCode.ESCAPE);

        assertTrue(inputManager.consumePauseToggleRequested());
        assertFalse(inputManager.consumePauseToggleRequested());

        inputManager.handleKeyPressed(KeyCode.ESCAPE);
        assertFalse(inputManager.consumePauseToggleRequested());

        inputManager.handleKeyReleased(KeyCode.ESCAPE);
        inputManager.handleKeyPressed(KeyCode.ESCAPE);
        assertTrue(inputManager.consumePauseToggleRequested());
    }

    @Test
    void clearsPendingPauseRequest() {
        InputManager inputManager = new InputManager();
        inputManager.handleKeyPressed(KeyCode.ESCAPE);

        inputManager.clear();

        assertFalse(inputManager.consumePauseToggleRequested());
    }

    @Test
    void requestsStartOnlyOnceForEachEnterPress() {
        InputManager inputManager = new InputManager();

        inputManager.handleKeyPressed(KeyCode.ENTER);

        assertTrue(inputManager.consumeStartRequested());
        assertFalse(inputManager.consumeStartRequested());
        inputManager.handleKeyPressed(KeyCode.ENTER);
        assertFalse(inputManager.consumeStartRequested());

        inputManager.handleKeyReleased(KeyCode.ENTER);
        inputManager.handleKeyPressed(KeyCode.ENTER);
        assertTrue(inputManager.consumeStartRequested());
    }

    @Test
    void clearsPendingStartRequest() {
        InputManager inputManager = new InputManager();
        inputManager.handleKeyPressed(KeyCode.ENTER);

        inputManager.clear();

        assertFalse(inputManager.consumeStartRequested());
    }
}
