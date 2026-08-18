package br.edu.unex.sentinela.input;

import java.util.EnumSet;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Captura o estado do teclado sem depender da repetição de teclas do sistema.
 */
public final class InputManager {

    private static final Set<KeyCode> MOVEMENT_KEYS = EnumSet.of(
            KeyCode.W,
            KeyCode.A,
            KeyCode.S,
            KeyCode.D,
            KeyCode.UP,
            KeyCode.LEFT,
            KeyCode.DOWN,
            KeyCode.RIGHT
    );

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

    public void attachTo(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (MOVEMENT_KEYS.contains(event.getCode())) {
                pressedKeys.add(event.getCode());
                event.consume();
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (MOVEMENT_KEYS.contains(event.getCode())) {
                pressedKeys.remove(event.getCode());
                event.consume();
            }
        });
    }

    public MovementInput movementInput() {
        double horizontal = axis(
                isPressed(KeyCode.A, KeyCode.LEFT),
                isPressed(KeyCode.D, KeyCode.RIGHT)
        );
        double vertical = axis(
                isPressed(KeyCode.W, KeyCode.UP),
                isPressed(KeyCode.S, KeyCode.DOWN)
        );
        return new MovementInput(horizontal, vertical);
    }

    public void clear() {
        pressedKeys.clear();
    }

    private boolean isPressed(KeyCode primary, KeyCode alternative) {
        return pressedKeys.contains(primary) || pressedKeys.contains(alternative);
    }

    private static double axis(boolean negative, boolean positive) {
        return (positive ? 1.0 : 0.0) - (negative ? 1.0 : 0.0);
    }
}
