package hipravin.strategy.command;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.SpawnRangersStrategy;
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
    Position2d switchAttackPosition = null;

    boolean randomMover = false;


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

    public boolean isAttackPosition() {
        return attackPosition.x > 50 || attackPosition.y > 50;
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

    public int countMyRangersCLoseRange6(Position2d rangerPosition,
                                    GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        List<Position2d> r6 = Position2dUtil.upRightLeftDownDiagsFiltered(rangerPosition, p -> {
            return  pgs.at(p).test(c ->c.isMyRanger() && c.getAttackerCount(6) > 0);
        });

        return r6.size();
    }
    public int countMyRangersCLoseRange7(Position2d rangerPosition,
                                    GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        List<Position2d> r7 = Position2dUtil.upRightLeftDownDiagsFiltered(rangerPosition, p -> {
            return  pgs.at(p).test(c ->c.isMyRanger() && c.getAttackerCount(7) > 0);
        });

        return r7.size();
    }

    public int countMyRangersNearbyTurretRange6(Position2d rangerPosition, int range,
                                                GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).test(c -> c.isMyRanger() && c.getTurretAttackerCount(6) > 0)) {
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


    public Optional<Position2d> betterToAttackWorkersOutSide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if (pgs.isRound1() || pgs.isRound2()) {
            return Optional.empty();
        }

        Position2d rp = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();

        Optional<Position2d> workerToAttack = pgs.getOppWorkers().values().stream()
                .filter(c -> Position2dUtil.isPositionOutside(c.getPosition(), strategyParams.outsideLine))
                .filter(c -> c.getRange5enemyWorkers() >= strategyParams.workerAttackSwitchMinWorkers)
                .filter(c -> c.getPosition().lenShiftSum(rp) * 1.2 < rp.lenShiftSum(attackPosition))
                .filter(c -> {

                    int enemies = SpawnRangersStrategy.countEnemyArmyNearby(c.getPosition(), strategyParams.workerHarrassClosenessRange, gameHistoryState, pgs, strategyParams);
                    int allies = SpawnRangersStrategy.rangersThatAlreadyDefendingNearby(c.getPosition(), pgs, gameHistoryState, strategyParams).size();

                    return allies == 0 ||
                            allies < enemies * strategyParams.workerHarrasOverCountRatio;
                })
                .min(Comparator.comparingInt(c -> c.getPosition().lenShiftSum(rp)))
                .map(Cell::getPosition);
        if (workerToAttack.isPresent()) {

            Integer closestRanger = gameHistoryState.getRangerCommands().entrySet()
                    .stream()
                    .filter(rce -> rce.getValue().getAttackPosition().lenShiftSum(workerToAttack.get()) >= strategyParams.workerHarrassClosenessRange)
                    .min(Comparator.comparingInt(rce -> rce.getKey().lenShiftSum(workerToAttack.get())))
                    .map(rce -> rce.getValue().rangerEntityId).orElse(null);

            if (closestRanger != null && closestRanger.equals(rangerEntityId)) {
                return workerToAttack;
            }
        }
        return Optional.empty();
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

        if (randomMover) {
            Optional<Position2d> enemyPos = SpawnRangersStrategy.nearestEnemyEntityToPosition(rp, pgs);
            if (enemyPos.isPresent()) {
                attackPosition = enemyPos.get();
            }
        }

        Position2d betterToAttackWorkersPos = betterToAttackWorkersOutSide(gameHistoryState, pgs, strategyParams).orElse(null);
        if (betterToAttackWorkersPos != null) {
            DebugOut.println("Decided to attack workers: " + rp + " -> " + betterToAttackWorkersPos + " workers5:" + pgs.at(betterToAttackWorkersPos).getRange5enemyWorkers());
            attackPosition = betterToAttackWorkersPos;
        }

        if (//pgs.at(rp).getTotalNearAttackerCount() == 0
            //  && pgs.at(attackPosition).getTotalNearAttackerCount() == 0
                (rp.lenShiftSum(attackPosition) < 6 || countSwitched.get() > 0)) {
            DebugOut.println("Reach attackPosition and cleared: " + attackPosition + ", nbs: " + countSwitched.get());

            Position2d currentAttackPosition = attackPosition;
            Optional<Position2d> nearestEnemyEntity = SpawnRangersStrategy.nearestEnemyEntityToPosition(rp, pgs);
            if (switchAttackPosition != null) {
                attackPosition = switchAttackPosition;
                switchAttackPosition = null;
            } else if (nearestEnemyEntity
                    .map(p -> p.lenShiftSum(rp) <= 20).orElse(false)) {
                attackPosition = nearestEnemyEntity.orElse(Position2dUtil.randomMapPosition());
            } else if (attackPosition.equals(strategyParams.attackPoints.get(0))) {
                attackPosition = nearestEnemyEntity.orElse(Position2dUtil.randomMapPosition());
            } else if (!attackPosition.equals(strategyParams.attackPoints.get(0))) {
                attackPosition = strategyParams.attackPoints.get(0);
            } else {
                attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);
                randomMover = true;
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
        int myCount = countMyRangersNearby(rp, strategyParams.attackHoldMyRange, gameHistoryState, pgs, strategyParams) + 1;
        int myCountTurretsRange6 = countMyRangersNearbyTurretRange6(rp, strategyParams.attackHoldMyRange, gameHistoryState, pgs, strategyParams);

        int myCountNearRange6 = countMyRangersCLoseRange6(rc.getPosition(), gameHistoryState, pgs, strategyParams);
        int myCountNearRange7 = countMyRangersCLoseRange7(rc.getPosition(), gameHistoryState, pgs, strategyParams);


        int turretsRange6 = countEnemyTurretsInRange6(rp, gameHistoryState, pgs, strategyParams);
        enemyCount += turretsRange6 * 4;

        Position2d p61 = find61(rp, gameHistoryState, pgs);
        Position2d better6 = findBetter6(rp, gameHistoryState, pgs);



        if((rc.getAttackerCount(6) >= 1 || rc.getAttackerCount(7) >=1)  && myCount <= 1 && enemyCount > 1) {
            updateRetreat(gameHistoryState, pgs, strategyParams, assignedActions);
        } else if (rc.getAttackerCount(6) > 1 && p61 != null && !strategyParams.neverHold) {
            DebugOut.println("Ranger move 61: " + rp + "->" + p61);
            updateAttackPos(p61, gameHistoryState, pgs, strategyParams, assignedActions);
        } else if (rc.getAttackerCount(6) > 1 && better6 != null && !strategyParams.neverHold) {
            DebugOut.println("Ranger move better6: " + rp + "->" + better6);
            updateAttackPos(better6, gameHistoryState, pgs, strategyParams, assignedActions);
        } else if(rc.getAttackerCount(6) == 0 && p61 != null) {
            DebugOut.println("Ranger push 61: " + rp + "->" + p61);
            updateAttackPos(p61, gameHistoryState, pgs, strategyParams, assignedActions);
        } else if ((rc.getAttackerCount(5) ==0 && rc.getAttackerCount(6) == 0 && rc.getAttackerCount(7) > 1)
                && (3 * enemyCount > 4 * myCount || myCountNearRange7 == 0) &&!strategyParams.neverHold) {
            if (!decidedToGetHealing(gameHistoryState, pgs, strategyParams, assignedActions)) {
                DebugOut.println("Ranger hold at 7: " + rp + " myCountTurretsRange6: " + myCountTurretsRange6);
                updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
            }
        }else if (((rc.getAttackerCount(6) >= 1)
                && (3 * enemyCount > 4 * myCount || (enemyCount > 1 && myCountNearRange6 == 0)) &&!strategyParams.neverHold)
                || (turretsRange6 > 0 && myCountTurretsRange6 < 6)
        ) {
            if (!decidedToGetHealing(gameHistoryState, pgs, strategyParams, assignedActions)) {
                DebugOut.println("Ranger hold at 6: " + rp + " myCountTurretsRange6: " + myCountTurretsRange6);
                updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
            }
        } else {
            if (isStuck(rp, gameHistoryState, pgs, strategyParams)) {
                DebugOut.println("Ranger stuck: " + rp);
                updateStuckAttack(gameHistoryState, pgs, strategyParams, assignedActions);
            } else {
                if (!decidedToGetHealing(gameHistoryState, pgs, strategyParams, assignedActions)) {
                    if(betterToPair(gameHistoryState, pgs, strategyParams, assignedActions)) {
                        updateHold(gameHistoryState, pgs, strategyParams, assignedActions);
                    } else {
                        updateAttack(gameHistoryState, pgs, strategyParams, assignedActions);
                    }
                }
            }
        }
    }

    private boolean betterToPair(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                        StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if(!strategyParams.useRangerPairing) {
            return false;
        }
        Cell rangerCell = pgs.getEntityIdToCell().get(rangerEntityId);

        if(rangerCell.getTotalNearAttackerCount() == 0) {
            return false;
        }

        int rangersPaired = Position2dUtil.upRightLeftDownDiagsFiltered(rangerCell.getPosition(),
                p-> pgs.at(p).isMyRanger()).size();

        if(rangersPaired > 0) {
            return false;
        }

        List<Position2d> rangersToWait = new ArrayList<>();

        Position2dUtil.iterAllPositionsInRangeExclusive(rangerCell.getPosition(), 4, p -> {
            if(pgs.at(p).test(c ->c.isMyRanger() && c.getPosition().lenShiftSum(attackPosition) > rangerCell.getPosition().lenShiftSum(attackPosition))) {
                rangersToWait.add(p);
            }
        });

        if(rangersToWait.size() > 0) {
            DebugOut.println("Ranger decided to wait for pair: " + rangerCell.getPosition());

            return  true;
        }

        return false;
    }

    private boolean decidedToGetHealing(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                        StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if (!strategyParams.useRangerHealing) {
            return false;
        }
        Cell rangerCell = pgs.getEntityIdToCell().get(rangerEntityId);

        if (rangerCell.getHealthLeft() >= 6) {
            return false;
        }

        NearestEntity worker = rangerCell.getMyNearestWorker();
        if(worker == null) {
            return false;
        }

        EntityAction autoAttack = new EntityAction();
        EntityType[] prioritizedAttackTypes = bestAttackTargets(
               rangerCell.getPosition(), gameHistoryState, pgs, strategyParams);

        AttackAction attackAction = new AttackAction(gameHistoryState.getAssignedAttackTargets().get(rangerEntityId),
                new AutoAttack(Position2dUtil.RANGER_RANGE,
                        prioritizedAttackTypes));

        autoAttack.setAttackAction(attackAction);
        autoAttack.setMoveAction(new MoveAction(worker.getSourceCell().getPosition().toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));

        DebugOut.println("Ranger decided to go to hospital: " + rangerCell.getPosition() + " -> " + worker.getSourceCell().getPosition());

        return true;
    }

    Position2d find61(Position2d rp, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs) {
        return Position2dUtil.upRightLeftDownFiltered(rp,
                mp -> pgs.at(mp).test(
                        c -> c.isEmpty() && c.getTurretAttackerCount(5) + c.getTurretAttackerCount(6) == 0
                                && c.getAttackerCount(5) == 0 && c.getAttackerCount(6) == 1))
                .stream().min(Comparator.comparingInt(mp -> mp.lenShiftSum(attackPosition)))
                .orElse(null);
    }

    Position2d findBetter6(Position2d rp, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs) {
        return Position2dUtil.upRightLeftDownFiltered(rp,
                mp -> pgs.at(mp).test(
                        c -> c.isEmpty() && c.getTurretAttackerCount(5) + c.getTurretAttackerCount(6) == 0
                                && c.getAttackerCount(5) == 0))
                .stream().min(Comparator.comparingInt(mp -> mp.lenShiftSum(attackPosition)))
                .orElse(null);
    }

    public EnumSet<EntityType> repairingActiveBuildings(Position2d position, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                        StrategyParams strategyParams) {

        EnumSet<EntityType> repairingBuildings = EnumSet.noneOf(EntityType.class);

        Position2dUtil.iterAllPositionsInRangeInclusive(position, Position2dUtil.RANGER_RANGE, p -> {
            if (pgs.at(p).test(
                    c -> c.isOppEntity()
                            && c.isBuilding()
                            && c.getEntity().isActive()
                            && pgs.getBuildingsByEntityId().get(c.getEntityId()).getEnemyRepairersCount() > 0
            )) {
                repairingBuildings.add(pgs.at(p).getEntityType());
            }
        });

        return repairingBuildings;
    }

    EntityType[] bestHoldTargets(Position2d position, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                 StrategyParams strategyParams) {
        return strategyParams.selectBestTargetTypesAccordingPriorities(attackRangeEnemyEntityType(position, gameHistoryState, pgs, strategyParams));


    }

    EntityType[] bestAttackTargets(Position2d position, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {


        EnumSet<EntityType> attackTypes = attackRangeEnemyEntityType(position, gameHistoryState, pgs, strategyParams);

        EnumSet<EntityType> repairing = repairingActiveBuildings(position, gameHistoryState, pgs,
                strategyParams);

        if (!repairing.isEmpty()) {
            DebugOut.println("Repairing enemy buildings: " + repairing);
            attackTypes.removeAll(repairing);
            attackTypes.add(EntityType.BUILDER_UNIT);
        }

        return strategyParams.selectBestTargetTypesAccordingPriorities(attackTypes);
    }

    EnumSet<EntityType> attackRangeEnemyEntityType(Position2d position, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                                   StrategyParams strategyParams) {
        EnumSet<EntityType> entityTypes = EnumSet.noneOf(EntityType.class);

        Position2dUtil.iterAllPositionsInRangeInclusive(position, Position2dUtil.RANGER_RANGE, p -> {
            if (pgs.at(p).isOppEntity()) {
                entityTypes.add(pgs.at(p).getEntityType());
            }
        });

        return entityTypes;
    }

    public void updateAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        decidedToAttackThisTurn = true;

        EntityAction autoAttack = new EntityAction();
        EntityType[] prioritizedAttackTypes = bestAttackTargets(
                currentParsedGameState.getEntityIdToCell().get(rangerEntityId).getPosition(), gameHistoryState, currentParsedGameState, strategyParams);

        AttackAction attackAction = new AttackAction(gameHistoryState.getAssignedAttackTargets().get(rangerEntityId),
                new AutoAttack(Position2dUtil.RANGER_RANGE,
                        prioritizedAttackTypes));

        autoAttack.setAttackAction(attackAction);

        Position2d attackShifted = Position2dUtil.crop(attackPosition.shift(xshift, yshift));

        attackShifted = findBetterMoveTo(currentParsedGameState, attackShifted, gameHistoryState, strategyParams);

        autoAttack.setMoveAction(new MoveAction(attackShifted.toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public void updateAttackPos(Position2d position, GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                                StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        decidedToAttackThisTurn = true;

        EntityAction autoAttack = new EntityAction();
        EntityType[] prioritizedAttackTypes = bestAttackTargets(
                currentParsedGameState.getEntityIdToCell().get(rangerEntityId).getPosition(), gameHistoryState, currentParsedGameState, strategyParams);

        AttackAction attackAction = new AttackAction(gameHistoryState.getAssignedAttackTargets().get(rangerEntityId),
                new AutoAttack(0,
                        prioritizedAttackTypes));

        autoAttack.setAttackAction(attackAction);
        autoAttack.setMoveAction(new MoveAction(position.toVec2dInt(), true, true));

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }


    public void updateStuckAttack(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        switchAttackPosition = attackPosition;

        attackPosition = Position2dUtil.limitWayTo(pgs.getEntityIdToCell().get(rangerEntityId).getPosition(),
                attackPosition, Position2dUtil.RANGER_RANGE - 4);
        updateAttack(gameHistoryState, pgs, strategyParams, assignedActions);
    }

    public void updateHold(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                           StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        EntityAction autoAttack = new EntityAction();

        EntityType[] prioritizedAttackTypes = bestHoldTargets(
                pgs.getEntityIdToCell().get(rangerEntityId).getPosition(), gameHistoryState, pgs, strategyParams);

        AttackAction attackAction = new AttackAction(gameHistoryState.getAssignedAttackTargets().get(rangerEntityId), new AutoAttack(0,
                prioritizedAttackTypes));
        Position2d rp = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();

        if (strategyParams.attackEnemyMineralsOnHold && pgs.getEntityIdToCell().get(rangerEntityId).getAttackerCount(5) == 0
                && attackRangeEnemyEntityType(rp, gameHistoryState, pgs, strategyParams).isEmpty()) {
            Integer mineralId = nearestMineralToOpponent(rp, gameHistoryState, pgs, strategyParams);

            if (mineralId != null) {
                attackAction = new AttackAction(mineralId, new AutoAttack(0,
                        strategyParams.rangerDefaultAndMineralsAttackTargets));

                DebugOut.println("Attack mineral on hold, rp: " + rp);
            }

        }

        autoAttack.setAttackAction(attackAction);

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    public Integer nearestMineralToOpponent(Position2d rangerPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                            StrategyParams strategyParams) {
        List<Position2d> enemyMinerals = new ArrayList<>();

        Position2dUtil.iterAllPositionsInRangeInclusive(rangerPosition, Position2dUtil.RANGER_RANGE, p -> {
            if (pgs.at(p).isEnemyTerritoryMineral()) {
                enemyMinerals.add(p);
            }
        });

        return enemyMinerals.stream()
                .min(Comparator.comparingInt(p -> p.lenShiftSum(Position2dUtil.ENEMY_CORNER)))
                .map(p -> pgs.at(p).getEntityId())
                .orElse(null);
    }

    public void updateRetreat(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(0,
                strategyParams.rangerDefaultAttackTargets));
        autoAttack.setAttackAction(attackAction);

        Position2d rp = pgs.getEntityIdToCell().get(rangerEntityId).getPosition();

        Position2d nearestEnemy = nearestEnemyUnitToPosition(rp, pgs).orElse(null);
        if(nearestEnemy == null) {
            nearestEnemy = closestTurretShortRange(rp, gameHistoryState, pgs, strategyParams).orElse(null);
        }

        if (nearestEnemy != null) {
            Position2d runTo = Position2dUtil.runAwayABit(rp, nearestEnemy);

            DebugOut.println("Ranger retreat: " + rp + " ->" + runTo);
            autoAttack.setMoveAction(new MoveAction(runTo.toVec2dInt(), true, true));
        }

        assignedActions.put(rangerEntityId, new ValuedEntityAction(0.5, rangerEntityId, autoAttack));
    }

    Optional<Position2d> nearestEnemyUnitToPosition(Position2d toPosition, ParsedGameState pgs) {
        return pgs.getEnemyArmy().keySet()
                .stream()
                .min(Comparator.comparingInt(p -> p.lenShiftSum(toPosition)));

    }
    public Optional<Position2d> closestTurretShortRange(Position2d wp,
                                                        GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        List<Position2d> turretCells = new ArrayList<>();

        Position2dUtil.iterAllPositionsInRangeExclusive(wp, 7, p -> {
            if (pgs.at(p).test(c -> c.isBuilding()
                    && c.getEntityType() == EntityType.TURRET && c.getEntity().isActive() && !c.isMyEntity())) {
                turretCells.add(p);
            }
        });

        return turretCells.stream()
                .min(Comparator.comparingInt(p -> wp.lenShiftSum(p)));
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

    public Position2d currentPosition(ParsedGameState pgs) {
        return pgs.getEntityIdToCell().get(rangerEntityId).getPosition();
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
        return "RangerAttackHoldRetreatMicroCommand{" +
                "rangerEntityId=" + rangerEntityId +
                ", attackPosition=" + attackPosition +
                ", switchAttackPosition=" + switchAttackPosition +
                ", randomMover=" + randomMover +
                ", retreatPosition=" + retreatPosition +
                ", decidedToAttackThisTurn=" + decidedToAttackThisTurn +
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

        if (lastPositions.size() < 3) {
            return false;
        }
        int curTick = pgs.curTick();

        Position2d pr = lastPositions.get(curTick - 1);

        if (pr == null) {
            return false;
        }

        int countSame = (int) lastPositions.values().stream()
                .filter(lp -> lp.equals(currentPosition))
                .count();

        return countSame > 1 && !currentPosition.equals(pr);
    }
}
