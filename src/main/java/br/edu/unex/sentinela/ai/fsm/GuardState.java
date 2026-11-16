package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;

/** Rotina do robô guarda: fica no posto e gira o olhar. */
public final class GuardState implements EnemyState {

    private boolean paused;

    @Override
    public String name() {
        return "VIGIAR";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(false);
        robot.goTo(robot.post());
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (Reactions.reactToThreat(robot, machine) || Reactions.reactToClue(robot, machine)) {
            return;
        }
        if (Reactions.payAttention(robot)) {
            paused = true;
            return;
        }
        if (paused) {
            paused = false;
            robot.goTo(robot.post());
        }
        if (robot.arrived() || robot.routeFailed()) {
            robot.lookAround(deltaTime);
        }
    }
}
