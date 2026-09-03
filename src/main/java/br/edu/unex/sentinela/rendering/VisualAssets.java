package br.edu.unex.sentinela.rendering;

import javafx.scene.image.Image;

/**
 * Carrega uma única vez as imagens usadas pela renderização.
 */
final class VisualAssets {

    private static final String BASE_PATH = "/br/edu/unex/sentinela/assets/";

    static final Image FLOOR = load("lab-floor.png");
    static final Image FLOOR_ALT = load("lab-floor-alt.png");
    static final Image WALL = load("lab-wall.png");
    static final Image WALL_ALT = load("lab-wall-alt.png");
    static final Image TERMINAL = load("lab-terminal.png");
    static final Image BIO_POD = load("lab-bio-pod.png");
    static final Image CONSOLE = load("lab-console.png");
    static final Image REACTOR = load("lab-reactor.png");
    static final Image PLAYER = load("player.png");
    static final Image PATHFINDER_ROBOT = load("pathfinder-robot.png");

    private VisualAssets() {
    }

    private static Image load(String fileName) {
        var resource = VisualAssets.class.getResource(BASE_PATH + fileName);
        if (resource == null) {
            return null;
        }

        Image image = new Image(resource.toExternalForm(), false);
        return image.isError() ? null : image;
    }
}
