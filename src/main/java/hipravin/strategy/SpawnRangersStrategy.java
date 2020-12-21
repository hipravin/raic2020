package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.*;
import hipravin.strategy.command.*;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static hipravin.model.Position2d.of;

public class SpawnRangersStrategy implements SubStrategy {
    Position2d prevAttackPoint = null;

    boolean shouldSpawnMoreRangers(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {
        if (pgs.isRound1() && pgs.getMyWorkers().size() < strategyParams.round1WorkersFirst && pgs.curTick() < 200) {
            return false;
        }

        if (pgs.getPopulation().getMyRangerCount() >= strategyParams.maxNumberOfRangers) {
            return false; //hold
        }

        return true; //TODO: research conditions
    }

    @Override
    public boolean isApplicableAtThisTick(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Entity rang = pgs.getMyBarrack(EntityType.RANGED_BASE);
        if (rang != null && rang.isActive()) {
            boolean rangLocked = gameHistoryState.allOngoingCommandRelatedEntitiIds().anyMatch(id -> id.equals(rang.getId()));

            return !rangLocked;
        }

        return false;
    }

    List<Position2d> attackPointsOrdered = new ArrayList<>(Arrays.asList(
            of(76, 7),
            of(7, 46),
            of(7, 76),
            of(45, 45),
            of(76, 76)
    ));

    void round12redefineAttackPoints(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                     StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d mainAttackPoint = attackPointsOrdered.get(0);
        Position2d rangBasePos = of(pgs.getMyRangerBase().getPosition());

        Optional<Position2d> entityCloseToAttackPoint = nearestEnemyEntityToPosition(mainAttackPoint, pgs);
        Optional<Position2d> entityCloseToRangeBase = nearestEnemyEntityToPosition(rangBasePos, pgs);
        Optional<Position2d> armyCloseToRangeBase = nearestEnemyUnitToPosition(rangBasePos, pgs);

        if (!pgs.at(mainAttackPoint).isFog()
                && entityCloseToAttackPoint.map(p -> p.lenShiftSum(mainAttackPoint) > strategyParams.cleanBaseRangeTreshhold).orElse(true)) {
            //nothing left on base
            if (attackPointsOrdered.isEmpty() || attackPointsOrdered.size() == 1) {

                if (armyCloseToRangeBase.isPresent()) {
                    setRedefinedAttackPoint(armyCloseToRangeBase.get(), rangBasePos.halfWayTo(armyCloseToRangeBase.get()), gameHistoryState, strategyParams);
                } else if (entityCloseToRangeBase.isPresent()) {
                    setRedefinedAttackPoint(entityCloseToRangeBase.get(), rangBasePos.halfWayTo(entityCloseToRangeBase.get()), gameHistoryState, strategyParams);
                } else {
                    Position2d p = Position2dUtil.randomMapPosition();
                    setRedefinedAttackPoint(p, rangBasePos.halfWayTo(p), gameHistoryState, strategyParams);
                }
            } else {
                attackPointsOrdered.remove(0);
                setRedefinedAttackPoint(attackPointsOrdered.get(0), rangBasePos.halfWayTo(attackPointsOrdered.get(0)),
                        gameHistoryState, strategyParams);
                DebugOut.println("Attack point redefined to " + attackPointsOrdered.get(0));
            }
        }
    }


    void redefineAttackPoints(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Position2d mainAttackPoint = strategyParams.attackPoints.get(0);
        Position2d rangBasePos = of(pgs.getMyRangerBase().getPosition());

        Optional<Position2d> entityCloseToAttackPoint = nearestEnemyEntityToPosition(mainAttackPoint, pgs);
        Optional<Position2d> entityCloseToRangeBase = nearestEnemyEntityToPosition(rangBasePos, pgs);
        Optional<Position2d> armyCloseToRangeBase = nearestEnemyUnitToPosition(rangBasePos, pgs);

        if (!pgs.at(mainAttackPoint).isFog()
                && entityCloseToAttackPoint.map(p -> p.lenShiftSum(mainAttackPoint) > strategyParams.cleanBaseRangeTreshhold).orElse(true)) {
            //nothing left on base
            Position2d enemyBarrack = gameHistoryState.getEnemyBuildings().stream()
                    .filter(b -> pgs.at(b).test(c -> c.getEntityType() == EntityType.MELEE_BASE || c.getEntityType() == EntityType.RANGED_BASE))
                    .findFirst().orElse(null);

            if (enemyBarrack != null) {
                setRedefinedAttackPoint(enemyBarrack, rangBasePos.halfWayTo(armyCloseToRangeBase.get()), gameHistoryState, strategyParams);
            } else if (armyCloseToRangeBase.isPresent()) {
                setRedefinedAttackPoint(armyCloseToRangeBase.get(), rangBasePos.halfWayTo(armyCloseToRangeBase.get()), gameHistoryState, strategyParams);
            } else if (entityCloseToRangeBase.isPresent()) {
                setRedefinedAttackPoint(entityCloseToRangeBase.get(), rangBasePos.halfWayTo(entityCloseToRangeBase.get()), gameHistoryState, strategyParams);
            } else {
                Position2d p = Position2dUtil.randomMapPosition();
                setRedefinedAttackPoint(p, rangBasePos.halfWayTo(p), gameHistoryState, strategyParams);
            }
        }
    }

    void setRedefinedAttackPoint(Position2d position, Position2d retreatPosition,
                                 GameHistoryAndSharedState gameHistoryAndSharedState, StrategyParams strategyParams) {
        strategyParams.attackPoints = new ArrayList<>(strategyParams.attackPoints);
        strategyParams.attackPointRates = new ArrayList<>(strategyParams.attackPointRates);

        strategyParams.attackPoints.set(0, position);
        strategyParams.attackPointRates.set(0, 0.9);//no need/benefit for spread anymore

        gameHistoryAndSharedState.ongoingCommands.forEach(c -> {
            if (c instanceof RangerAttackHoldRetreatMicroCommand) {
                ((RangerAttackHoldRetreatMicroCommand) c).setAttackPosition(position);
                ((RangerAttackHoldRetreatMicroCommand) c).setRetreatPosition(retreatPosition);
            }
        });

    }

    public static Optional<Position2d> nearestEnemyEntityToPosition(Position2d toPosition, ParsedGameState pgs) {
        return Arrays.stream(pgs.getPlayerView().getEntities())
                .filter(e -> e.getPlayerId() != null && e.getPlayerId() != pgs.getPlayerView().getMyId())
                .map(e -> of(e.getPosition()))
                .min(Comparator.comparingInt(p -> p.lenShiftSum(toPosition)));
    }

    Optional<Position2d> nearestEnemyUnitToPosition(Position2d toPosition, ParsedGameState pgs) {
        return pgs.getEnemyArmy().keySet()
                .stream()
                .min(Comparator.comparingInt(p -> p.lenShiftSum(toPosition)));

    }

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        if (pgs.isRound2() || pgs.isRound1()) {
            round12redefineAttackPoints(gameHistoryState, pgs, strategyParams, assignedActions);
        } else {
            redefineAttackPoints(gameHistoryState, pgs, strategyParams, assignedActions);
        }

        if (defendRangBase(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }
        if (defendMyBuildings(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }
        if (defendMyWorkers(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

        if (defendMyTerritory(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

        if ((pgs.isRound1() || pgs.isRound2())
                && defendMyTerritoryRound12(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

        if (!shouldSpawnMoreRangers(gameHistoryState, pgs, strategyParams)) {
            return;
        }
        if (rangerScouting(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

//        if (!pgs.isRound1()) {
        findBestSpawnPos(gameHistoryState, pgs, strategyParams);
//        } else {
//            findBestSpawnPosRound1(gameHistoryState, pgs, strategyParams);
//        }
    }

    boolean spawnDefenceOrSwitch(Position2d defendPosition,
                                 GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                 StrategyParams strategyParams) {
        return buildRangerAttackingDefenceOrSwich(defendPosition, defenceCountRequired(defendPosition, gameHistoryState, pgs, strategyParams),
                gameHistoryState, pgs, strategyParams);
    }

    int defenceCountRequired(Position2d defendPosition, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                             StrategyParams strategyParams) {
        int enemies = countEnemyArmyNearby(defendPosition, strategyParams.defenceClosenessRange, gameHistoryState, pgs, strategyParams);

        return (int) (enemies * strategyParams.defenceOverCountRatio);
    }

    /**
     * returns true if requested to spawn a new ranger
     */
    boolean buildRangerAttackingDefenceOrSwich(Position2d defendPosition,
                                               int defenceRequiredCount,
                                               GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                               StrategyParams strategyParams) {
        if (defendPosition == null) {
            return false;
        }

        List<Map.Entry<Position2d, RangerAttackHoldRetreatMicroCommand>> rangersThatAlreadyDefendingNearby =
                rangersThatAlreadyDefendingNearby(defendPosition, pgs, gameHistoryState, strategyParams);

        if (rangersThatAlreadyDefendingNearby.size() >= defenceRequiredCount) {
            return false;
        }
        List<Map.Entry<Position2d, RangerAttackHoldRetreatMicroCommand>> rangersThatCanDefendBetterThanSpawn =
                rangersThatCanDefendBetterThanSpawn(defendPosition, pgs, gameHistoryState, strategyParams);

        switchToDefendPosition(defendPosition, rangersThatCanDefendBetterThanSpawn, defenceRequiredCount - rangersThatAlreadyDefendingNearby.size(),
                pgs, gameHistoryState, strategyParams);


        DebugOut.println("Defend pos: " + defendPosition + " already: " + rangersThatAlreadyDefendingNearby.size()
                + " switched: " + rangersThatCanDefendBetterThanSpawn);

        if (rangersThatAlreadyDefendingNearby.size() + rangersThatCanDefendBetterThanSpawn.size() < defenceRequiredCount) {
            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(defendPosition))).orElse(null);

            if (closestSpawn != null) {
                DebugOut.println("Build ranger defending: " + defendPosition);
//                buildRangerDefending(closestSpawn, gameHistoryState, pgs, strategyParams);
                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, defendPosition); //worker can move, so use micro

                gameHistoryState.setLastDefenceTick(pgs.curTick());

                return true;
            }
        }

        return false;
    }


    public void switchToDefendPosition(Position2d defendPostiion,
                                       List<Map.Entry<Position2d, RangerAttackHoldRetreatMicroCommand>> rangers, int required,
                                       ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState,
                                       StrategyParams strategyParams) {


        if (rangers.size() > required) {
            rangers.sort(Comparator.comparingInt(rce -> rce.getKey().lenShiftSum(defendPostiion)));
            rangers = rangers.subList(0, required);
        }

        for (Map.Entry<Position2d, RangerAttackHoldRetreatMicroCommand> ranger : rangers) {
            DebugOut.println("Ranger switched to defence: " + ranger.getKey() + ": " + ranger.getValue().getAttackPosition() + " -> " + defendPostiion);

            ranger.getValue().setAttackPosition(defendPostiion);
        }
    }


    public List<Map.Entry<Position2d, RangerAttackHoldRetreatMicroCommand>> rangersThatCanDefendBetterThanSpawn(Position2d defendPosition, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState,
                                                                                                                StrategyParams strategyParams) {
        if (pgs.getMyRangerBase() == null) {
            return Collections.emptyList();
        }
        Position2d rangBaseCenter = of(pgs.getMyRangerBase().getPosition()).shift(2, 2);

        return gameHistoryAndSharedState.getRangerCommands().entrySet().stream()
                .filter(rce -> rce.getKey().lenShiftSum(defendPosition) <= rangBaseCenter.lenShiftSum(defendPosition) - 2) //closer that new spawn
                .filter(rce -> rce.getValue().isAttackPosition()) //finish what is started
                .filter(rce -> rce.getValue().getLenToTarget() > rce.getValue().getAttackPosition().lenShiftSum(defendPosition)) //closer than ranger own attack target
                .collect(Collectors.toList());
    }

    public static List<Map.Entry<Position2d, RangerAttackHoldRetreatMicroCommand>> rangersThatAlreadyDefendingNearby(Position2d defendPosition, ParsedGameState pgs, GameHistoryAndSharedState gameHistoryAndSharedState,
                                                                                                                     StrategyParams strategyParams) {
        return gameHistoryAndSharedState.getRangerCommands().entrySet().stream()
                .filter(rce -> rce.getValue().getAttackPosition().lenShiftSum(defendPosition) < strategyParams.defenceClosenessRange)
                .collect(Collectors.toList());
    }

    public boolean rangerScouting(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        int numberOfScouts = (int) gameHistoryState.getOngoingCommands().stream()
                .filter(c -> c instanceof RangerNewScoutCommand).count();

        if (pgs.getMyRangers().size() < strategyParams.minCountOfRangersBeforeScouts || numberOfScouts > strategyParams.maxNumberOfScouts) {
            return false;
        }

        if (StrategyParams.ifRandom(strategyParams.scoutProb)) {
            DebugOut.println("Requesting scout");
            buildRangerScout(gameHistoryState, pgs, strategyParams);

            return true;
        }


        return false;

    }

    boolean defendMyTerritoryRound12(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                     StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d vragUvorot = pgs.getDefendingAreaEnemies()
                .stream()
                .min(Comparator.comparingInt(e -> of(15, 15).lenShiftSum(e.getPosition())))
                .map(e -> of(e.getPosition()))
                .orElse(null);

        if (vragUvorot != null) {
            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUvorot))).orElse(null);

            if (closestSpawn != null) {
                DebugOut.println("Build ranger defending terr 12: " + vragUvorot);
                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, vragUvorot);

//                buildRangerDefending(closestSpawn, gameHistoryState, pgs, strategyParams);
                return true;
            }
        }

        return false;
    }

    boolean defendMyTerritory(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Position2d rangBaseCenter = of(pgs.getMyRangerBase().getPosition()).shift(2, 2);

        int countOurRangers = pgs.getDefendingAreaMyRangers().size();
        int countOppArmy = countOppArmyValueInDefArea(gameHistoryState, pgs, strategyParams, assignedActions);

        if (countOppArmy < countOurRangers * strategyParams.defendArmyOvercomeRatio) {
            return false;
        }

        Position2d vragUvorot = pgs.getDefendingAreaEnemies()
                .stream()
                .min(Comparator.comparingInt(e -> rangBaseCenter.lenShiftSum(e.getPosition())))
                .map(e -> of(e.getPosition()))
                .orElse(null);

        if (vragUvorot != null) {
            return spawnDefenceOrSwitch(vragUvorot, gameHistoryState, pgs, strategyParams);

//            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
//            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUvorot))).orElse(null);
//
//            if (closestSpawn != null) {
//                DebugOut.println("Build ranger defending territory: " + vragUvorot);
////                buildRangerDefending(closestSpawn, gameHistoryState, pgs, strategyParams);
//                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, vragUvorot);
//
//                return true;
//            }
        }

        return false;
    }

    boolean defendMyBuildings(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Position2d rangBaseCenter = of(pgs.getMyRangerBase().getPosition()).shift(2, 2);

        List<Building> toDefend = pgs.getBuildingsByEntityId().values()
                .stream().filter(b -> b.getCornerCell().isMyBuilding())
                .filter(b -> b.getMyBuildingAttackersCount() > 0)
                .collect(Collectors.toList());

        if (toDefend.isEmpty()) {
            return false;
        }
        //selct random
        Collections.shuffle(toDefend);
        Building b = toDefend.get(0);

        Position2d vragUvorot = b.getCornerCell().getPosition();

        if (vragUvorot != null) {

            return spawnDefenceOrSwitch(vragUvorot, gameHistoryState, pgs, strategyParams);
//            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
//            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUvorot))).orElse(null);
//
//            if (closestSpawn != null) {
//                DebugOut.println("Build ranger defending building: " + vragUvorot);
////                buildRangerDefending(closestSpawn, gameHistoryState, pgs, strategyParams);
//                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, vragUvorot);
//
//                return true;
//            }
        }

        return false;
    }

    boolean defendMyWorkers(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                            StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        List<Position2d> toDefend = pgs.getMyWorkers().values()
                .stream().filter(c -> c.getTotalNearAttackerCount() > 0)
                .map(Cell::getPosition)
                .collect(Collectors.toList());

        if (toDefend.isEmpty()) {
            return false;
        }
        //selct random
        Collections.shuffle(toDefend);
        Position2d wtod = toDefend.get(0);

        Position2d defPoint = Position2dUtil.midPoint(wtod, nearestEnemyEntityToPosition(wtod, pgs).orElse(wtod));

        if (defPoint != null) {
            return spawnDefenceOrSwitch(defPoint, gameHistoryState, pgs, strategyParams);

//            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
//            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUvorot))).orElse(null);
//
//            if (closestSpawn != null) {
//                DebugOut.println("Build ranger defending worker: " + vragUvorot);
////                buildRangerDefending(closestSpawn, gameHistoryState, pgs, strategyParams);
//                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, vragUvorot); //worker can move, so use micro
//
//                return true;
//            }
        }

        return false;
    }


    public static int countEnemyArmyNearby(Position2d rangerPosition, int range,
                                           GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        AtomicInteger counter = new AtomicInteger(0);
        Position2dUtil.iterAllPositionsInRangeExclusive(rangerPosition, range, p -> {
            if (pgs.at(p).test(c -> c.isEnemySwordman() || c.isEnemyRanger())) {
                counter.incrementAndGet();
            }
        });

        return counter.get();
    }

    int countOppArmyValueInDefArea(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        return pgs.getDefendingAreaEnemies().stream()
                .filter(e -> strategyParams.armyValues.containsKey(e.getEntityType()))
                .map(e -> strategyParams.armyValues.get(e.getEntityType()))
                .mapToInt(i -> i).sum();
    }


    boolean defendRangBase(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                           StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        Position2d rangBaseCenter = of(pgs.getMyRangerBase().getPosition()).shift(2, 2);

        Position2d vragUVorot = pgs.getEnemyArmy().keySet()
                .stream()
                .filter(p -> p.lenShiftSum(rangBaseCenter) <= strategyParams.vragUVorotRange)
                .min(Comparator.comparingInt(p -> p.lenShiftSum(rangBaseCenter)))
                .orElse(null);


        if (vragUVorot == null) {
            return false;
        } else {

            DebugOut.println("Vrag u vorot detected: " + vragUVorot);
            spawnDefenceOrSwitch(vragUVorot, gameHistoryState, pgs, strategyParams);

//            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
//
//            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUVorot))).orElse(null);
//
//            if (closestSpawn != null) {
//                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, vragUVorot);
//                return true;
//            }

        }
        return false;
    }


    public void findBestSpawnPos(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams) {
        Set<Position2d> rangOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.RANGED_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

        rangOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());

        if (rangOuterEdge.isEmpty()) {
            return;
        }

        List<Position2d> edgeSorted = new ArrayList<>(rangOuterEdge);
        edgeSorted.sort(Comparator.comparing(p -> p.x + p.y, Comparator.reverseOrder()));

        createBuildRangerCommand(edgeSorted.get(0), gameHistoryState, pgs, strategyParams);
    }

    public void findBestSpawnPosRound1(
            GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
            StrategyParams strategyParams) {
        Set<Position2d> rangOuterEdge = new HashSet<>(pgs.findMyBuildings(EntityType.RANGED_BASE).get(0)
                .getBuildingOuterEdgeWithoutCorners());

        rangOuterEdge.removeIf(p -> !pgs.at(p).isEmpty());

        if (rangOuterEdge.isEmpty()) {
            return;
        }

        List<Position2d> edgeSorted = new ArrayList<>(rangOuterEdge);
        edgeSorted.sort(Comparator.comparing(p -> p.x + p.y, Comparator.reverseOrder()));

        int attackLine = 45;
        int attackWidth = 30;
        int retreatShift = -10;
        if (pgs.getEnemyArmyRight().size() < 5 && pgs.getEnemyArmyTop().size() < 5) {
            Position2d attackPoint = of(75, 75);
            Position2d retreatPoint = of(35, 35);

            createBuildRangerCommandRound1(edgeSorted.get(0), attackPoint, retreatPoint, gameHistoryState, pgs, strategyParams);
        } else {

            if (pgs.getMyArmyRight().size() < pgs.getEnemyArmyRight().size()) {
                Position2d attackPoint = of(attackLine, GameHistoryAndSharedState.random.nextInt(attackWidth) + 5);
                Position2d retreatPoint = attackPoint.shift(retreatShift, 0);

                createBuildRangerCommandRound1(edgeSorted.get(0), attackPoint, retreatPoint, gameHistoryState, pgs, strategyParams);
            } else if (pgs.getMyArmyTop().size() < pgs.getEnemyArmyTop().size()) {
                Position2d attackPoint = of(GameHistoryAndSharedState.random.nextInt(attackWidth) + 5, attackLine);
                Position2d retreatPoint = attackPoint.shift(0, retreatShift);

                createBuildRangerCommandRound1(edgeSorted.get(0), attackPoint, retreatPoint, gameHistoryState, pgs, strategyParams);

            } else if (pgs.getEnemyArmyRight().size() > pgs.getEnemyArmyTop().size()) {
                Position2d attackPoint = of(attackLine, GameHistoryAndSharedState.random.nextInt(attackWidth) + 5);
                Position2d retreatPoint = attackPoint.shift(retreatShift, 0);

                createBuildRangerCommandRound1(edgeSorted.get(0), attackPoint, retreatPoint, gameHistoryState, pgs, strategyParams);

            } else if (pgs.getEnemyArmyRight().size() < pgs.getEnemyArmyTop().size()) {
                Position2d attackPoint = of(GameHistoryAndSharedState.random.nextInt(attackWidth) + 5, attackLine);
                Position2d retreatPoint = attackPoint.shift(0, retreatShift);

                createBuildRangerCommandRound1(edgeSorted.get(0), attackPoint, retreatPoint, gameHistoryState, pgs, strategyParams);
            } else {
                Position2d attackPoint = of(75, 75);
                Position2d retreatPoint = of(35, 35);

                createBuildRangerCommandRound1(edgeSorted.get(0), attackPoint, retreatPoint, gameHistoryState, pgs, strategyParams);

            }
        }


    }


    void buildRangerDefending(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams) {

        if (pgs.isRound1() && pgs.getMyWorkers().size() < strategyParams.round1WorkersFirst) {
            return;
        }

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);

        DefenderRangerCommand defender = new DefenderRangerCommand();
        CommandUtil.chainCommands(buildRangerCommand, defender);

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }


    void buildRangerAttacking(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams, Position2d attackPosition) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);

        if (attackPosition == null) {
            if (prevAttackPoint != null && StrategyParams.ifRandom(strategyParams.preserveAttackPointProb)) {
                attackPosition = prevAttackPoint;
            } else {
                attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);
            }

            prevAttackPoint = attackPosition;
        }
        Position2d retreatPosition = Optional.ofNullable(pgs.getMyRangerBase())
                .map(b -> of(b.getPosition()).shift(2, 2)).orElse(of(40, 40))
                .halfWayTo(attackPosition);


        RangerAttackHoldRetreatMicroCommand rahrc = new RangerAttackHoldRetreatMicroCommand(null, attackPosition, retreatPosition, false);
        CommandUtil.chainCommands(buildRangerCommand, rahrc);

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }

    void buildRangerScout(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                          StrategyParams strategyParams) {

        Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
        Position2d spawnPosition = spawnPositions.stream().max(Comparator.comparingInt(sp -> sp.lenShiftSum(strategyParams.attackPoints.get(0))))
                .orElse(null);//try to go from behind

        if (spawnPosition != null) {

            Command buildRangerCommand = new BuildRangerCommand(spawnPosition, pgs, 1);
            RangerNewScoutCommand scout = new RangerNewScoutCommand(EntityType.RANGED_UNIT);
            CommandUtil.chainCommands(buildRangerCommand, scout);

            gameHistoryState.addOngoingCommand(buildRangerCommand, false);
        }
    }


    void createBuildRangerCommand(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);


        Position2d retreatPosition = Optional.ofNullable(pgs.getMyRangerBase())
                .map(b -> of(b.getPosition()).shift(6, 6)).orElse(of(40, 40));

        Position2d attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);


        RangerAttackHoldRetreatMicroCommand rahrc = new RangerAttackHoldRetreatMicroCommand(null, attackPosition, retreatPosition, false);
        CommandUtil.chainCommands(buildRangerCommand, rahrc);


        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }

    void createBuildRangerCommandRound1(Position2d spawnPos, Position2d attackPosition, Position2d retreatPosition,
                                        GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                        StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);

        RangerAttackHoldRetreatMicroCommand rahrc = new RangerAttackHoldRetreatMicroCommand(null, attackPosition, retreatPosition, false);
        CommandUtil.chainCommands(buildRangerCommand, rahrc);

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }


}
