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
    private boolean debugVisible;
    private boolean startRequested;
    private boolean pauseToggleRequested;
    private int requestedMapNumber;
    private boolean modeToggleRequested;
    private int difficultyStep;

    public void attachTo(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (handleKeyPressed(event.getCode())) {
                event.consume();
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (handleKeyReleased(event.getCode())) {
                event.consume();
            }
        });
    }

    boolean handleKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.TAB) {
            if (pressedKeys.add(KeyCode.TAB)) {
                debugVisible = !debugVisible;
            }
            return true;
        }
        if (keyCode == KeyCode.ESCAPE) {
            if (pressedKeys.add(KeyCode.ESCAPE)) {
                pauseToggleRequested = true;
            }
            return true;
        }
        if (keyCode == KeyCode.ENTER) {
            if (pressedKeys.add(KeyCode.ENTER)) {
                startRequested = true;
            }
            return true;
        }
        if (keyCode == KeyCode.F2) {
            if (pressedKeys.add(KeyCode.F2)) {
                modeToggleRequested = true;
            }
            return true;
        }
        if (keyCode == KeyCode.SHIFT) {
            pressedKeys.add(KeyCode.SHIFT);
            return true;
        }
        int mapNumber = mapNumber(keyCode);
        if (mapNumber > 0) {
            if (pressedKeys.add(keyCode)) {
                requestedMapNumber = mapNumber;
            }
            return true;
        }
        if (MOVEMENT_KEYS.contains(keyCode)) {
            // No briefing, esquerda e direita também escolhem a dificuldade.
            if (pressedKeys.add(keyCode)) {
                if (keyCode == KeyCode.LEFT || keyCode == KeyCode.A) {
                    difficultyStep = -1;
                } else if (keyCode == KeyCode.RIGHT || keyCode == KeyCode.D) {
                    difficultyStep = 1;
                }
            }
            return true;
        }
        return false;
    }

    boolean handleKeyReleased(KeyCode keyCode) {
        if (keyCode == KeyCode.TAB
                || keyCode == KeyCode.ESCAPE
                || keyCode == KeyCode.ENTER
                || keyCode == KeyCode.F2
                || keyCode == KeyCode.SHIFT
                || mapNumber(keyCode) > 0
                || MOVEMENT_KEYS.contains(keyCode)) {
            pressedKeys.remove(keyCode);
            return true;
        }
        return false;
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

    /**
     * Indica se os painéis com os dados reais da execução devem aparecer.
     */
    public boolean debugVisible() {
        return debugVisible;
    }

    /**
     * Entrega uma única vez o número do mapa solicitado pelo teclado.
     */
    public int consumeRequestedMapNumber() {
        int mapNumber = requestedMapNumber;
        requestedMapNumber = 0;
        return mapNumber;
    }

    /**
     * Entrega uma única vez a solicitação para pausar ou continuar o jogo.
     */
    public boolean consumePauseToggleRequested() {
        boolean requested = pauseToggleRequested;
        pauseToggleRequested = false;
        return requested;
    }

    /**
     * Entrega uma única vez a solicitação para iniciar a simulação.
     */
    public boolean consumeStartRequested() {
        boolean requested = startRequested;
        startRequested = false;
        return requested;
    }

    /** SHIFT pressionado: o jogador anda agachado, mais devagar e com menos ruído. */
    public boolean sneaking() {
        return pressedKeys.contains(KeyCode.SHIFT);
    }

    /** Entrega uma única vez o pedido de troca entre IA completa e demonstração do A*. */
    public boolean consumeModeToggleRequested() {
        boolean requested = modeToggleRequested;
        modeToggleRequested = false;
        return requested;
    }

    /** Entrega uma única vez o pedido de troca de dificuldade (-1, 0 ou +1). */
    public int consumeDifficultyStep() {
        int step = difficultyStep;
        difficultyStep = 0;
        return step;
    }

    public void clear() {
        pressedKeys.clear();
        modeToggleRequested = false;
        difficultyStep = 0;
        startRequested = false;
        pauseToggleRequested = false;
        requestedMapNumber = 0;
    }

    private boolean isPressed(KeyCode primary, KeyCode alternative) {
        return pressedKeys.contains(primary) || pressedKeys.contains(alternative);
    }

    private static double axis(boolean negative, boolean positive) {
        return (positive ? 1.0 : 0.0) - (negative ? 1.0 : 0.0);
    }

    private static int mapNumber(KeyCode keyCode) {
        return switch (keyCode) {
            case DIGIT1, NUMPAD1 -> 1;
            case DIGIT2, NUMPAD2 -> 2;
            case DIGIT3, NUMPAD3 -> 3;
            default -> 0;
        };
    }
}
