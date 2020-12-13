package hipravin.strategy;

import hipravin.DebugOut;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.command.*;
import model.Entity;
import model.EntityType;

import java.util.*;
import java.util.function.Function;

import static hipravin.model.Position2d.of;

public class SpawnRangersStrategy implements SubStrategy {
    boolean shouldSpawnMoreRangers(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                   StrategyParams strategyParams) {
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

    void redefineAttackPoints(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        Position2d mainAttackPoint = strategyParams.attackPoints.get(0);
        Position2d rangBasePos = of(pgs.getMyRangerBase().getPosition());

        Optional<Position2d> entityCloseToAttackPoint = nearestEnemyEntityToPosition(mainAttackPoint, pgs);
        Optional<Position2d> entityCloseToRangeBase = nearestEnemyEntityToPosition(rangBasePos, pgs);
        Optional<Position2d> armyCloseToRangeBase = nearestEnemyUnitToPosition(rangBasePos, pgs);

        if (!pgs.at(mainAttackPoint).isFog()
                && entityCloseToAttackPoint.map(p -> p.lenShiftSum(mainAttackPoint) > strategyParams.cleanBaseRangeTreshhold).orElse(true)) {
            //nothing left on base

            if(armyCloseToRangeBase.isPresent()) {
                setRedefinedAttackPoint(armyCloseToRangeBase.get(), strategyParams);
            } else if(entityCloseToRangeBase.isPresent()) {
                setRedefinedAttackPoint(entityCloseToRangeBase.get(), strategyParams);
            } else {
                setRedefinedAttackPoint(Position2dUtil.randomMapPosition(), strategyParams);
            }
        }
    }

    void setRedefinedAttackPoint(Position2d position, StrategyParams strategyParams) {
        strategyParams.attackPoints = new ArrayList<>(strategyParams.attackPoints);
        strategyParams.attackPointRates = new ArrayList<>(strategyParams.attackPointRates);

        strategyParams.attackPoints.set(0, position);
        strategyParams.attackPointRates.set(0, 0.9);//no need/benefit for spread anymore
    }

    Optional<Position2d> nearestEnemyEntityToPosition(Position2d toPosition, ParsedGameState pgs) {
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
        redefineAttackPoints(gameHistoryState, pgs, strategyParams, assignedActions);

        if (defendRangBase(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

        if (defendMyTerritory(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

        if (!shouldSpawnMoreRangers(gameHistoryState, pgs, strategyParams)) {
            return;
        }

        findBestSpawnPos(gameHistoryState, pgs, strategyParams);
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
            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUvorot))).orElse(null);

            if (closestSpawn != null) {
                DebugOut.println("Build ranger defending: " + vragUvorot);
                buildRangerDefending(closestSpawn, gameHistoryState, pgs, strategyParams);
            }
        }

        return false;
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
            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();

            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUVorot))).orElse(null);

            if (closestSpawn != null) {
                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams, vragUVorot);

            }

            return true;
        }
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

    void buildRangerDefending(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);

        DefenderRangerCommand defender = new DefenderRangerCommand();
        CommandUtil.chainCommands(buildRangerCommand, defender);

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }

    void buildRangerAttacking(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                              StrategyParams strategyParams, Position2d attackPosition) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);


        Position2d retreatPosition = Optional.ofNullable(pgs.getMyRangerBase())
                .map(b -> of(b.getPosition()).shift(2, 2)).orElse(of(40, 40));

        if (attackPosition == null) {
            attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);
        }

        if(strategyParams.useOldRangerMicro) {
            RangerAttackHoldRetreatCommand rahrc = new RangerAttackHoldRetreatCommand(null, attackPosition, retreatPosition, false);
            CommandUtil.chainCommands(buildRangerCommand, rahrc);
        } else {
            RangerAttackHoldRetreatMicroCommand rahrc = new RangerAttackHoldRetreatMicroCommand(null, attackPosition, retreatPosition, false);
            CommandUtil.chainCommands(buildRangerCommand, rahrc);
        }

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }


    void createBuildRangerCommand(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);


        Position2d retreatPosition = Optional.ofNullable(pgs.getMyRangerBase())
                .map(b -> of(b.getPosition()).shift(6, 6)).orElse(of(40, 40));

        Position2d attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);

        if(strategyParams.useOldRangerMicro) {
            RangerAttackHoldRetreatCommand rahrc = new RangerAttackHoldRetreatCommand(null, attackPosition, retreatPosition, false);
            CommandUtil.chainCommands(buildRangerCommand, rahrc);
        } else {
            RangerAttackHoldRetreatMicroCommand rahrc = new RangerAttackHoldRetreatMicroCommand(null, attackPosition, retreatPosition, false);
            CommandUtil.chainCommands(buildRangerCommand, rahrc);

        }

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }

    public void createRangerCommand() {

    }
}
