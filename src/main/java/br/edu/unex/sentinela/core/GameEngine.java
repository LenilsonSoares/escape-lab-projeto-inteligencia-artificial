package br.edu.unex.sentinela.core;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.input.InputManager;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.rendering.GameRenderer;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.animation.AnimationTimer;

/**
 * Coordena o ciclo input -> update -> render da aplicação.
 */
public final class GameEngine extends AnimationTimer {

    static final double MAX_DELTA_SECONDS = 0.05;
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;

    private final InputManager inputManager;
    private final GameWorld world;
    private final GameRenderer renderer;
    private final FrameMetrics metrics;

    private long previousFrameTime;
    private MovementInput currentInput = MovementInput.NONE;
    private boolean sessionStarted;
    private boolean paused;

    public GameEngine(
            InputManager inputManager,
            GameWorld world,
            GameRenderer renderer,
            FrameMetrics metrics
    ) {
        this.inputManager = inputManager;
        this.world = world;
        this.renderer = renderer;
        this.metrics = metrics;
    }

    @Override
    public void handle(long now) {
        if (!hasRenderableViewport()) {
            previousFrameTime = now;
            return;
        }

        if (previousFrameTime == 0L) {
            previousFrameTime = now;
            render();
            return;
        }

        double frameTime = calculateFrameTime(now);
        double simulationDeltaTime = Math.min(frameTime, MAX_DELTA_SECONDS);
        previousFrameTime = now;

        processInput();
        update(simulationDeltaTime, frameTime);
        render();
    }

    private double calculateFrameTime(long now) {
        double elapsedSeconds = (now - previousFrameTime) / NANOSECONDS_PER_SECOND;
        return Math.max(0.0, elapsedSeconds);
    }

    private void processInput() {
        if (inputManager.consumeStartRequested()) {
            sessionStarted = true;
            paused = false;
        }
        if (sessionStarted && inputManager.consumePauseToggleRequested()) {
            paused = !paused;
            currentInput = MovementInput.NONE;
        } else if (!sessionStarted) {
            inputManager.consumePauseToggleRequested();
        }
        int requestedMapNumber = inputManager.consumeRequestedMapNumber();
        if (requestedMapNumber > 0) {
            world.selectMap(requestedMapNumber);
        }
        currentInput = inputManager.movementInput();
    }

    private void update(double simulationDeltaTime, double frameTime) {
        if (sessionStarted && !paused) {
            world.update(simulationDeltaTime, currentInput);
        }
        metrics.recordFrame(frameTime);
    }

    private void render() {
        renderer.render(
                world,
                metrics,
                inputManager.debugVisible(),
                sessionStarted,
                paused
        );
    }

    private boolean hasRenderableViewport() {
        return renderer.viewportWidth() > 0.0 && renderer.viewportHeight() > 0.0;
    }

    public void shutdown() {
        stop();
        inputManager.clear();
        previousFrameTime = 0L;
        sessionStarted = false;
        paused = false;
    }
}
