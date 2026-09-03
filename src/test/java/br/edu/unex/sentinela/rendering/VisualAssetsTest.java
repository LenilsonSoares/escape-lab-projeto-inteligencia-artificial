package br.edu.unex.sentinela.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class VisualAssetsTest {

    private static final List<String> ASSET_PATHS = List.of(
            "/br/edu/unex/sentinela/assets/lab-floor.png",
            "/br/edu/unex/sentinela/assets/lab-floor-alt.png",
            "/br/edu/unex/sentinela/assets/lab-wall.png",
            "/br/edu/unex/sentinela/assets/lab-wall-alt.png",
            "/br/edu/unex/sentinela/assets/lab-terminal.png",
            "/br/edu/unex/sentinela/assets/lab-bio-pod.png",
            "/br/edu/unex/sentinela/assets/lab-console.png",
            "/br/edu/unex/sentinela/assets/lab-reactor.png",
            "/br/edu/unex/sentinela/assets/player.png",
            "/br/edu/unex/sentinela/assets/pathfinder-robot.png"
    );

    @Test
    void loadsAllVisualAssetsWithExpectedSize() throws IOException {
        for (String path : ASSET_PATHS) {
            var resource = getClass().getResource(path);
            assertNotNull(resource, () -> "Asset não encontrado: " + path);

            var image = ImageIO.read(resource);
            assertNotNull(image, () -> "Asset inválido: " + path);
            assertEquals(80, image.getWidth(), () -> "Largura incorreta: " + path);
            assertEquals(80, image.getHeight(), () -> "Altura incorreta: " + path);
        }
    }
}
