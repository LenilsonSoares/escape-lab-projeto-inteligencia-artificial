package br.edu.unex.sentinela.telemetry;

/**
 * Agrega métricas simples do game loop para o painel de depuração.
 */
public final class FrameMetrics {

    private static final double FPS_SAMPLE_SECONDS = 0.25;

    private double deltaTime;
    private double sampleDuration;
    private int framesInSample;
    private double framesPerSecond;

    public void recordFrame(double deltaTime) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0.0) {
            throw new IllegalArgumentException("Delta time não pode ser negativo");
        }

        this.deltaTime = deltaTime;
        sampleDuration += deltaTime;
        framesInSample++;

        if (sampleDuration >= FPS_SAMPLE_SECONDS) {
            framesPerSecond = framesInSample / sampleDuration;
            sampleDuration = 0.0;
            framesInSample = 0;
        }
    }

    public double deltaTime() {
        return deltaTime;
    }

    public double framesPerSecond() {
        return framesPerSecond;
    }
}
