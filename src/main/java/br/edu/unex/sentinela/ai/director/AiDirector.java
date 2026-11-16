package br.edu.unex.sentinela.ai.director;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Diretor de IA com dificuldade dinâmica (apostila "IA para agentes", seções 11 e 12).
 * Só controla quantos robôs podem perseguir ao mesmo tempo (entre 1 e 3).
 */
public final class AiDirector {

    public static final int MIN_BUDGET = 1;
    public static final int MAX_BUDGET = 3;
    public static final int INITIAL_BUDGET = 2;

    private static final double CONTACT_WINDOW = 20.0;
    private static final double MIN_TIME_BETWEEN_ADJUSTMENTS = 10.0;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final List<Double> contacts = new ArrayList<>();
    private int minBudget = MIN_BUDGET;
    private int startBudget = INITIAL_BUDGET;
    private int maxBudget = MAX_BUDGET;
    private int budget;
    private double pressure;
    private double smoothedPressure;
    private String lastAdjustment;
    private double sampleTimer;
    private int highSeconds;
    private int lowSeconds;
    private double lastAdjustmentTime;

    public AiDirector() {
        reset();
    }

    /** Faixa de vagas do nível de dificuldade escolhido; recomeça o diretor. */
    public void setBudgetRange(int min, int start, int max) {
        if (min < 1 || start < min || max < start) {
            throw new IllegalArgumentException("Faixa de vagas inválida");
        }
        minBudget = min;
        startBudget = start;
        maxBudget = max;
        reset();
    }

    public int maxBudget() {
        return maxBudget;
    }

    /** Registra um contato (dano) no instante {@code now}. */
    public void recordContact(double now) {
        contacts.add(now);
    }

    /** Faz uma amostra da pressão a cada 1 s de simulação acumulado. */
    public void update(double deltaTime, double now, int score, double nearThreatFraction) {
        if (!Double.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("O intervalo de tempo deve ser finito e não negativo");
        }
        sampleTimer += deltaTime;
        while (sampleTimer >= 1.0) {
            sampleTimer -= 1.0;
            sample(now, score, nearThreatFraction);
        }
    }

    private void sample(double now, int score, double nearThreatFraction) {
        contacts.removeIf(time -> time <= now - CONTACT_WINDOW);
        double damage = Math.min(1.0, contacts.size() / 3.0);
        double risk = clamp(1.0 - score / 1000.0);
        double near = clamp(nearThreatFraction);

        // q = 0,50D + 0,30R + 0,20A e média suavizada para um pico não mudar tudo.
        pressure = 0.5 * damage + 0.3 * risk + 0.2 * near;
        smoothedPressure = 0.2 * pressure + 0.8 * smoothedPressure;

        // Histerese de duração: a pressão precisa ficar alta ou baixa por segundos seguidos.
        if (smoothedPressure > 0.75) {
            highSeconds++;
            lowSeconds = 0;
        } else if (smoothedPressure < 0.35) {
            lowSeconds++;
            highSeconds = 0;
        } else {
            highSeconds = 0;
            lowSeconds = 0;
        }

        if (now - lastAdjustmentTime < MIN_TIME_BETWEEN_ADJUSTMENTS) {
            return;
        }
        if (highSeconds >= 5 && budget > minBudget) {
            budget--;
            registerAdjustment("REDUZIU", now);
        } else if (lowSeconds >= 10 && budget < maxBudget) {
            budget++;
            registerAdjustment("AUMENTOU", now);
        }
    }

    private void registerAdjustment(String action, double now) {
        lastAdjustment = String.format(PT_BR, "%s PARA %d (PRESSÃO %.2f)", action, budget, smoothedPressure);
        highSeconds = 0;
        lowSeconds = 0;
        lastAdjustmentTime = now;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /** Última pressão bruta q calculada. */
    public double pressure() {
        return pressure;
    }

    public double smoothedPressure() {
        return smoothedPressure;
    }

    public int attackBudget() {
        return budget;
    }

    /** Texto da última decisão, ex.: "REDUZIU PARA 1 (PRESSÃO 0,81)"; "SEM AJUSTE" no início. */
    public String lastAdjustment() {
        return lastAdjustment;
    }

    /** Volta ao estado inicial (nova partida). */
    public void reset() {
        contacts.clear();
        budget = startBudget;
        pressure = 0;
        smoothedPressure = 0;
        lastAdjustment = "SEM AJUSTE";
        sampleTimer = 0;
        highSeconds = 0;
        lowSeconds = 0;
        // Sem ajuste anterior: o primeiro pode acontecer assim que as durações forem atingidas.
        lastAdjustmentTime = Double.NEGATIVE_INFINITY;
    }
}
