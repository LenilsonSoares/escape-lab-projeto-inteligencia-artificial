package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;

/**
 * Cooperação: o robô viu o jogador, mas as vagas de ataque já estão ocupadas.
 * Em vez de todos correrem atrás dele, este vai vigiar a porta de saída.
 */
public final class CoverExitState implements EnemyState {

    static final double GIVE_UP_TIME = 6.0;
    private double timeWithoutContact;

    @Override
    public String name() {
        return "CERCAR SAÍDA";
    }

    @Override
    public void enter(Robot robot) {
        robot.setRunning(true);
        robot.goTo(robot.exitWatchPoint());
    }

    @Override
    public void update(Robot robot, StateMachine machine, double deltaTime) {
        if (robot.touchedPlayer()) {
            machine.changeState(new AttackState(), "TOCOU O JOGADOR");
            return;
        }
        if (robot.memory().inCombat()) {
            timeWithoutContact = 0.0;
            if (robot.team().requestAttackPermit(robot.name())) {
                machine.changeState(new ChaseState(), "VAGA DE ATAQUE LIBERADA");
            }
            return;
        }
        timeWithoutContact += deltaTime;
        if (robot.arrived() || robot.routeFailed()) {
            robot.setRunning(false);
            robot.lookAround(deltaTime);
        }
        if (timeWithoutContact >= GIVE_UP_TIME) {
            machine.changeState(new ReturnState(), "SAÍDA VIGIADA");
        }
    }
}
