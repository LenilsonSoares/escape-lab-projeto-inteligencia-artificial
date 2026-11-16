package br.edu.unex.sentinela.ai.fsm;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.ai.memory.Evidence;
import java.util.Optional;

/**
 * Transições comuns a vários estados, sempre na mesma ordem de prioridade:
 * primeiro a ameaça (jogador visto), depois as pistas.
 */
final class Reactions {

    private Reactions() {
    }

    static boolean reactToThreat(Robot robot, StateMachine machine) {
        if (!robot.memory().inCombat()) {
            return false;
        }
        if (robot.team().requestAttackPermit(robot.name())) {
            machine.changeState(new ChaseState(), "VIU O JOGADOR");
        } else {
            machine.changeState(new CoverExitState(), "SEM VAGA DE ATAQUE");
        }
        return true;
    }

    static boolean reactToClue(Robot robot, StateMachine machine) {
        if (robot.memory().level().compareTo(AlertLevel.INVESTIGATION) < 0) {
            return false;
        }
        Optional<Evidence> clue = robot.memory().bestEvidence(robot.now());
        if (clue.isEmpty()) {
            return false;
        }
        machine.changeState(new InvestigateState(), "PISTA: " + clue.get().origin().label());
        return true;
    }

    /** Em ATENÇÃO o robô para e olha para a origem da pista, sem sair da rotina. */
    static boolean payAttention(Robot robot) {
        if (robot.memory().level() != AlertLevel.ATTENTION) {
            return false;
        }
        Optional<Evidence> clue = robot.memory().bestEvidence(robot.now());
        if (clue.isEmpty()) {
            return false;
        }
        robot.stopMoving();
        robot.faceTowards(clue.get().position());
        return true;
    }

    static EnemyState routineOf(Robot robot) {
        return robot.guardsPost() ? new GuardState() : new PatrolState();
    }
}
