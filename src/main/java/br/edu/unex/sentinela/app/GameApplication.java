package br.edu.unex.sentinela.app;

import br.edu.unex.sentinela.core.GameEngine;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.input.InputManager;
import br.edu.unex.sentinela.rendering.GameRenderer;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Monta a janela e conecta os componentes principais do jogo.
 */
public final class GameApplication extends Application {

    private static final double INITIAL_WIDTH = 1_280.0;
    private static final double INITIAL_HEIGHT = 720.0;
    private static final double MIN_VIEWPORT_WIDTH = 1_120.0;
    private static final double MIN_VIEWPORT_HEIGHT = 720.0;

    private GameEngine gameEngine;
    private boolean fullScreenKeyPressed;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(INITIAL_WIDTH, INITIAL_HEIGHT);
        StackPane root = new StackPane(canvas);

        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT, Color.web("#07111f"));
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                if (!fullScreenKeyPressed) {
                    stage.setFullScreen(!stage.isFullScreen());
                    fullScreenKeyPressed = true;
                }
                event.consume();
            }
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.F11) {
                fullScreenKeyPressed = false;
                event.consume();
            }
        });

        InputManager inputManager = new InputManager();
        inputManager.attachTo(scene);
        stage.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused) {
                inputManager.clear();
                fullScreenKeyPressed = false;
            }
        });

        GameWorld world = new GameWorld();
        GameRenderer renderer = new GameRenderer(canvas);
        gameEngine = new GameEngine(inputManager, world, renderer, new FrameMetrics());

        stage.setTitle("Escape Lab — Projeto de Inteligência Artificial");
        stage.setFullScreenExitHint("");
        stage.setMinWidth(MIN_VIEWPORT_WIDTH);
        stage.setMinHeight(MIN_VIEWPORT_HEIGHT);
        stage.setScene(scene);
        stage.show();
        Platform.runLater(() -> preserveMinimumViewport(stage, scene));

        gameEngine.start();
    }

    private void preserveMinimumViewport(Stage stage, Scene scene) {
        double windowBorderWidth = Math.max(0.0, stage.getWidth() - scene.getWidth());
        double titleBarHeight = Math.max(0.0, stage.getHeight() - scene.getHeight());

        stage.setMinWidth(MIN_VIEWPORT_WIDTH + windowBorderWidth);
        stage.setMinHeight(MIN_VIEWPORT_HEIGHT + titleBarHeight);
    }

    @Override
    public void stop() {
        if (gameEngine != null) {
            gameEngine.shutdown();
        }
    }
}
