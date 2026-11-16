package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;

/** Depois de tocar o jogador, o robô para um instante, o que dá chance de fuga. */
public final class AttackState implements EnemyState {

    static final double RECOVERY_TIME = 1.2;
    private double timer;

    @Override
    public String name() {
        return "ATACAR";
    }

    @Override
    public void enter(Robot robot) {
        robot.stopMoving();
        robot.clearTouch();
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        timer += deltaTime;
        if (timer < RECOVERY_TIME) {
            return;
        }
        if (!Reactions.reactToThreat(robot, machine)) {
            machine.changeState(new SearchState(), "JOGADOR FUGIU");
        }
    }
}
