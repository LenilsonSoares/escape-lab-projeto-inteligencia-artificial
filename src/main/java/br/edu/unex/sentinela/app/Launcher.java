package br.edu.unex.sentinela.app;

import javafx.application.Application;

/**
 * Ponto de entrada independente da classe JavaFX.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(GameApplication.class, args);
    }
}
