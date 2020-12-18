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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static hipravin.model.Position2d.of;
import static hipravin.strategy.StrategyParams.MAX_VAL;

public class RangerAttackHoldRetreatMicroCommand extends Command {
    Integer rangerEntityId;
    Position2d attackPosition;
    Position2d retreatPosition;
    int xshift = 0;
    int yshift = 0;

    boolean decidedToAttackThisTurn = false;
    Map<Integer, Position2d> lastPositions = new HashMap<>();


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
        if (StrategyParams.randomAttackPositionShift > 0) {
            int shift = StrategyParams.randomAttackPositionShift;
            this.xshift = -shift + GameHistoryAndSharedState.random.nextInt(2 * shift);
            this.yshift = -shift + GameHistoryAndSharedState.random.nextInt(2 * shift);
        }

    }


    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        decidedToAttackThisTurn = false;

        if (rangerEntityId == null) {
            rangerEntityId = pgs.getMyUnitSpawned(EntityType.RANGED_UNIT).orElse(null);
            this.getRelatedEntityIds().add(rangerEntityId);
            if (rangerEntityId == null) {
                return false;
            }
        }

        Cell c = pgs.getEntityIdToCell().get(rangerEntityId);
        if (c == null) {
            return false;
        }

        lastPositions.put(pgs.curTick(), c.getPosition());
        lastPositions.entrySet().removeIf(e -> e.getKey().equals(pgs.curTick() - StrategyParams.RANGER_POS_HIST));

        return true;
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

        return healthCounter.get() / 30;
    }
    public int countEnemyTurretsInRange6(Position2d rangerPosition,
                                       GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);

        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, 6, p -> {
            if (pgs.at(p).test(c -> c.isBuilding()
                    && c.getEntityType() == EntityType.TURRET && c.getEntity().isActive() && !c.isMyEntity())) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
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
                if (pgs.at(p).test(c -> c.isMyRanger() && c.getRangerSwitchedAttackPositionTo() != null)) {
                    countSwitched.incrementAndGet();
                }
            });
        }

        if (pgs.at(rp).getTotalNearAttackerCount() == 0 && (rp.lenShiftSum(attackPosition) < 7 || countSwitched.get() > 0)) {
            DebugOut.println("Reach attackPosition and cleared: " + attackPosition + ", nbs: " + countSwitched.get());

            Position2d currentAttackPosition = attackPosition;

            if (attackPosition.equals(strategyParams.attackPoints.get(0))) {
                attackPosition = pgs.findClosesEnemyArmy(rp).orElse(Position2dUtil.randomMapPosition());
            } else {
                attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);
//                attackPosition = strategyParams.attackPoints.get(0);
            }

            gameHistoryState.getOngoingCommands().forEach(c -> {
                if (c instanceof RangerAttackHoldRetreatMicroCommand) {
                    RangerAttackHoldRetreatMicroCommand mc = (RangerAttackHoldRetreatMicroCommand) c;
                    if (mc.getAttackPosition().equals(currentAttackPosition)) {
                        mc.setAttackPosition(attackPosition);
                    }
                }
            });
        }

        Cell rc = pgs.at(rp);

        int enemyCount = countEnemyRangersNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams)
                + countEnemySwordmansNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams);
        int myCount = countMyRangersNearby(rp, strategyParams.attackHoldMyRange, gameHistoryState, pgs, strategyParams);

//        int turretHealthDivided = countEnemyTurretsNearby(rp, strategyParams.attackHoldEnemyRange, gameHistoryState, pgs, strategyParams);
        int turretsRange6 = countEnemyTurretsInRange6(rp, gameHistoryState, pgs, strategyParams);
        enemyCount += turretsRange6 * 4;

        if (((rc.getAttackerCount(6) >= 1
                || rc.getAttackerCount(7) == 1)
                && (3 * enemyCount > 4 * myCount))
                || (turretsRange6 > 0 &&  enemyCount > myCount)
        ) {
            DebugOut.println("Ranger hold: " + rp);

            updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
        } else {
            updateAttack(gameHistoryState, pgs, strategyParams, assignedActions);
        }
    }





    public void updateAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        decidedToAttackThisTurn = true;

        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.RANGER_RANGE,
                strategyParams.rangerDefaultAttackTargets));

        autoAttack.setAttackAction(attackAction);

        Position2d attackShifted = Position2dUtil.crop(attackPosition.shift(xshift, yshift));

        attackShifted = findBetterMoveTo(currentParsedGameState, attackShifted, gameHistoryState, strategyParams);

        autoAttack.setMoveAction(new MoveAction(attackShifted.toVec2dInt(), true, true));

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

    public Position2d findBetterMoveTo(ParsedGameState pgs, Position2d attackPosition, GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {

        if (!strategyParams.useRangerFollow) {
            return attackPosition;
        }
        Position2d cur = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();

        RangerAttackHoldRetreatMicroCommand prevToSameTarget = null;
        for (Command ongoingCommand : gameHistoryAndSharedState.getOngoingCommands()) {
            if (ongoingCommand == this) {
                break;
            }

            if (ongoingCommand instanceof RangerAttackHoldRetreatMicroCommand) {
                RangerAttackHoldRetreatMicroCommand rahmc = (RangerAttackHoldRetreatMicroCommand) ongoingCommand;

                if (attackPosition.lenShiftSum(rahmc.getAttackPosition()) < 5) {
                    prevToSameTarget = rahmc;
                }
            }
        }


        if (prevToSameTarget != null) {
            Position2d prevToSameTargetPos = pgs.getEntityIdToCell().get(prevToSameTarget.rangerEntityId).getPosition();
            if (prevToSameTargetPos.lenShiftSum(attackPosition) < cur.lenShiftSum(attackPosition)
                    && prevToSameTarget.decidedToAttackThisTurn
                    && pgs.at(cur).getTotalNearAttackerCount() == 0
                    && pgs.getRangersMovedSinceLastTickReversed().containsKey(prevToSameTargetPos)
                    && pgs.getRangersMovedSinceLastTickReversed().get(prevToSameTargetPos).lenShiftSum(attackPosition) > prevToSameTargetPos.lenShiftSum(attackPosition)
                    && prevToSameTargetPos.lenShiftSum(cur) < 5
                    && !isStuck(cur, gameHistoryAndSharedState, pgs, strategyParams)
            ) {
                return prevToSameTargetPos;
            }
        }

        return attackPosition;
    }

    @Override
    public void computeLenToTarget(ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState) {
        Position2d cur = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();
        setLenToTarget(cur.lenShiftSum(attackPosition));
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

    boolean isStuck(Position2d currentPosition, GameHistoryAndSharedState gameHistoryState,
                    ParsedGameState pgs, StrategyParams strategyParams) {

        if (lastPositions.size() < StrategyParams.RANGER_POS_HIST) {
            return false;
        }

        int countSame = (int) lastPositions.values().stream()
                .filter(lp -> lp.equals(currentPosition))
                .count();


        return countSame > 1;
    }
}
