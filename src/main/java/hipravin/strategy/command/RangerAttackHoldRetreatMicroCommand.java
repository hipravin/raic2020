package hipravin.strategy.command;

import hipravin.DebugOut;
import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static hipravin.model.Position2d.of;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class RangerAttackHoldRetreatMicroCommand extends Command {
    Integer rangerEntityId;
    Position2d attackPosition;
    Position2d retreatPosition;
    boolean workerHunter;

    public RangerAttackHoldRetreatMicroCommand(Integer rangerEntityId, Position2d attackPosition, Position2d retreatPosition) {
        this(rangerEntityId, attackPosition, retreatPosition, false);
    }

    public RangerAttackHoldRetreatMicroCommand(Integer rangerEntityId, Position2d attackPosition, Position2d retreatPosition, boolean workerHunter) {
        super(MAX_VAL, new HashSet<>());
        if (rangerEntityId != null) {
            this.getRelatedEntityIds().add(rangerEntityId);
        }
        this.rangerEntityId = rangerEntityId;
        this.attackPosition = attackPosition;
        this.retreatPosition = retreatPosition;
        this.workerHunter = workerHunter;
    }


    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (rangerEntityId == null) {
            rangerEntityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(rangerEntityId);
            if (rangerEntityId == null) {
                return false;
            }
        }

        return pgs.getEntityIdToCell().containsKey(rangerEntityId);
    }

    public int countEnemyRangersNearby(Position2d rangerPosition, int range,
                                       GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).isEnemyRanger()) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }
    public int countEnemySwordmansNearby(Position2d rangerPosition, int range,
                                       GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).test(c -> c.isUnit() && !c.isMyEntity() && c.getEntityType() == EntityType.MELEE_UNIT)) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }
    public int countEnemyTurretsNearby(Position2d rangerPosition, int range,
                                       GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger healthCounter = new AtomicInteger(0);

        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).test(c -> c.isBuilding()
                    && c.getEntityType() == EntityType.TURRET && c.getEntity().isActive() && !c.isMyEntity())) {
                healthCounter.addAndGet(pgs.at(p).getEntity().getHealth());
            }
        });

        return healthCounter.get() / 18;
    }

    public int countMyRangersNearby(Position2d rangerPosition, int range,
                                    GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).isMyRanger()) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }

    public int countRangersCloserToattackPoint(Position2d rangerPosition, int range,
                                               GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).isMyRanger() && rangerPosition.lenShiftSum(attackPosition) > p.lenShiftSum(attackPosition)) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }


    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (rangerEntityId == null) {
            rangerEntityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(rangerEntityId);
            if (rangerEntityId == null) {
                return false;
            }
        }

        //decide what to do

        return false;
    }


    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Position2d rp = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();

        AtomicInteger countSwitched = new AtomicInteger(0);

        if (rp.lenShiftSum(attackPosition) < 15) {
            Position2dUtil.iterAllPositionsInExactRange(rp, 3, p -> {
                if(pgs.at(p).test(c -> c.isMyRanger() && c.getRangerSwitchedAttackPositionTo() != null)) {
                    countSwitched.incrementAndGet();
                }
            });
        }

        if (rp.lenShiftSum(attackPosition) < 9 || countSwitched.get() > 0) {
            DebugOut.println("Reach attackPosition: " + attackPosition + ", nbs: " + countSwitched.get());

            Position2d currentAttackPosition = attackPosition;

            if (attackPosition.equals(strategyParams.attackPoints.get(0))) {
                attackPosition = pgs.findClosesEnemyArmy(rp).orElse(Position2dUtil.randomMapPosition());
            } else {
                attackPosition = strategyParams.attackPoints.get(0);
            }

            gameHistoryState.getOngoingCommands().forEach(c -> {
                if(c instanceof RangerAttackHoldRetreatMicroCommand) {
                    RangerAttackHoldRetreatMicroCommand mc = (RangerAttackHoldRetreatMicroCommand) c;
                    if(mc.getAttackPosition().equals(currentAttackPosition)) {
                        mc.setAttackPosition(attackPosition);
                    }
                }
            });
        }

        Cell rc = pgs.at(rp);

        int enemyCount = countEnemyRangersNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams)
                + countEnemySwordmansNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams)
                ;
        int myCount = countMyRangersNearby(rp, strategyParams.attackHoldMyRange, gameHistoryState, pgs, strategyParams);

        int turretHealth18 = countEnemyTurretsNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams);
        enemyCount += turretHealth18;

        if((pgs.isRound1() || pgs.isRound2()) && enemyCount > myCount) {
            updateRetreat(gameHistoryState, pgs, strategyParams, assignedActions);
        } else if(pgs.isRound1() && myCount < 5 && enemyCount > 0) {
            updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
        } else {

            if (rc.getAttackerCount(6) > 1 && rc.getPosition().lenShiftSum(retreatPosition) >= strategyParams.retreatStopRange) {
                DebugOut.println("Ranger retreat: " + rp);

                updateRetreat(gameHistoryState, pgs, strategyParams, assignedActions);
            } else if ((rc.getAttackerCount(6) == 1
                    || rc.getAttackerCount(7) == 1)

                    && (enemyCount > 1 || 2 * enemyCount > 3 *  myCount)) {
                DebugOut.println("Ranger hold: " + rp);

                updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
            } else {
                updateAttack(gameHistoryState, pgs, strategyParams, assignedActions);
            }
        }
    }

    public void updateAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.RANGER_RANGE,
                strategyParams.rangerDefaultAttackTargets));

        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(attackPosition.toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public void updateHold(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                           StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(0,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public void updateRetreat(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                              StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(0,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(retreatPosition.toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public void setAttackPosition(Position2d attackPosition) {
        this.attackPosition = attackPosition;
    }

    @Override
    public String toString() {
        return "RangerAttackHoldRetreatCommand{" +
                "rangerEntityId=" + rangerEntityId +
                ", attackPosition=" + attackPosition +
                ", retreatPosition=" + retreatPosition +
                '}';
    }

    public Position2d getAttackPosition() {
        return attackPosition;
    }

    public void setRetreatPosition(Position2d retreatPosition) {
        this.retreatPosition = retreatPosition;
    }
}
