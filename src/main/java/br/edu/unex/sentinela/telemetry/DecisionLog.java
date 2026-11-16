package br.edu.unex.sentinela.telemetry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Guarda as últimas decisões dos robôs para o painel de depuração. */
public final class DecisionLog {

    public record Entry(double time, String robot, String from, String to, String reason) {
    }

    private static final int CAPACITY = 8;
    private final Deque<Entry> entries = new ArrayDeque<>();

    public void add(double time, String robot, String from, String to, String reason) {
        if (entries.size() == CAPACITY) {
            entries.removeLast();
        }
        entries.addFirst(new Entry(time, robot, from, to, reason));
    }

    /** Da decisão mais recente para a mais antiga. */
    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public void clear() {
        entries.clear();
    }
}
