package br.edu.unex.sentinela.ai.bt;

import br.edu.unex.sentinela.ai.Robot;
import br.edu.unex.sentinela.ai.memory.AlertLevel;
import br.edu.unex.sentinela.ai.memory.Evidence;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;

/**
 * Árvore do batedor (interceptador). A ordem dos ramos é a prioridade:
 * recuar depois do contato, interceptar, investigar pistas e, por fim, rondar.
 * O batedor investiga já no nível ATENÇÃO, por isso segue ruídos antes dos outros.
 */
public final class ScoutTree {

    private ScoutTree() {
    }

    public static Node build() {
        RecoverAction recover = new RecoverAction();
        return new Selector("RAIZ",
                new Sequence("REAGIR AO CONTATO",
                        new Condition("TOCOU O JOGADOR?", robot -> robot.touchedPlayer() || recover.recovering),
                        recover),
                new Sequence("INTERCEPTAR",
                        new Condition("CONTATO CONFIRMADO?", robot -> robot.memory().inCombat()),
                        new Condition("TEM VAGA DE ATAQUE?",
                                robot -> robot.team().requestAttackPermit(robot.name())),
                        new InterceptAction()),
                new Sequence("INVESTIGAR",
                        new Condition("PISTA VÁLIDA?", ScoutTree::hasClue),
                        new InvestigateAction()),
                new RoamAction());
    }

    private static boolean hasClue(Robot robot) {
        return robot.memory().level().compareTo(AlertLevel.ATTENTION) >= 0
                && robot.memory().bestEvidence(robot.now()).isPresent();
    }

    static final class RecoverAction extends Action {

        static final double RECOVERY_TIME = 1.2;
        private boolean recovering;
        private double timer;

        RecoverAction() {
            super("RECUAR");
        }

        @Override
        protected Status run(Robot robot, double deltaTime) {
            if (!recovering) {
                recovering = true;
                timer = 0.0;
                robot.clearTouch();
                robot.stopMoving();
            }
            timer += deltaTime;
            if (timer < RECOVERY_TIME) {
                return Status.RUNNING;
            }
            recovering = false;
            return Status.SUCCESS;
        }

        @Override
        protected void abort(Robot robot) {
            recovering = false;
        }
    }

    static final class InterceptAction extends Action {

        static final double REPATH_INTERVAL = 0.5;
        private boolean started;
        private double timer;

        InterceptAction() {
            super("INTERCEPTAR");
        }

        @Override
        protected Status run(Robot robot, double deltaTime) {
            if (!started) {
                started = true;
                timer = 0.0;
                robot.setRunning(true);
                robot.goTo(robot.chaseTarget());
                return Status.RUNNING;
            }
            timer += deltaTime;
            if (timer >= REPATH_INTERVAL) {
                timer = 0.0;
                GridPosition target = robot.chaseTarget();
                if (!target.equals(robot.destination())) {
                    robot.goTo(target);
                }
            }
            return Status.RUNNING;
        }

        @Override
        protected void abort(Robot robot) {
            started = false;
            robot.setRunning(false);
            robot.team().releaseAttackPermit(robot.name());
        }
    }

    static final class InvestigateAction extends Action {

        static final double LOOK_TIME = 2.0;
        private Evidence clue;
        private double lookTimer;

        InvestigateAction() {
            super("INVESTIGAR PISTA");
        }

        @Override
        protected Status run(Robot robot, double deltaTime) {
            Evidence best = robot.memory().bestEvidence(robot.now()).orElse(null);
            if (best == null) {
                clue = null;
                return Status.FAILURE;
            }
            if (clue == null || !best.position().equals(clue.position())) {
                clue = best;
                lookTimer = 0.0;
                robot.goTo(clue.position());
            }
            if (robot.routeFailed()) {
                robot.memory().invalidate(clue.eventId());
                clue = null;
                return Status.FAILURE;
            }
            if (!robot.arrived()) {
                return Status.RUNNING;
            }
            lookTimer += deltaTime;
            robot.lookAround(deltaTime);
            if (lookTimer < LOOK_TIME) {
                return Status.RUNNING;
            }
            robot.memory().invalidateNear(clue.position(), 1);
            clue = null;
            return Status.SUCCESS;
        }

        @Override
        protected void abort(Robot robot) {
            clue = null;
        }
    }

    static final class RoamAction extends Action {

        private int index;
        private boolean started;

        RoamAction() {
            super("RONDAR");
        }

        @Override
        protected Status run(Robot robot, double deltaTime) {
            List<GridPosition> points = robot.routePoints();
            if (!started) {
                started = true;
                robot.goTo(points.get(index));
            } else if (robot.arrived() || robot.routeFailed()) {
                index = (index + 1) % points.size();
                robot.goTo(points.get(index));
            }
            return Status.RUNNING;
        }

        @Override
        protected void abort(Robot robot) {
            // Ao voltar a rondar, refaz a rota até o ponto em que parou.
            started = false;
        }
    }
}
