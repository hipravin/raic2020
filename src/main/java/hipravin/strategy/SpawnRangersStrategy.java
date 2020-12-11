package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
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

    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        if(defendRangBase(gameHistoryState, pgs, strategyParams, assignedActions)) {
            return;
        }

        if(defendMyTerritory(gameHistoryState, pgs, strategyParams, assignedActions)) {
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

        if(countOppArmy < countOurRangers * strategyParams.defendArmyOvercomeRatio ) {
            return false;
        }

        Position2d vragUvorot = pgs.getDefendingAreaEnemies()
                .stream()
                .min(Comparator.comparingInt(e -> rangBaseCenter.lenShiftSum(e.getPosition())))
                .map(e -> of(e.getPosition()))
                .orElse(null);

        if(vragUvorot != null) {
            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();
            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUvorot))).orElse(null);

            if(closestSpawn != null) {
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
            Set<Position2d> spawnPositions = pgs.getBuildingsByEntityId().get(pgs.getMyRangerBase().getId()).getBuildingEmptyOuterEdgeWithoutCorners();

            Position2d closestSpawn = spawnPositions.stream().min(Comparator.comparingInt(sp -> sp.lenShiftSum(vragUVorot))).orElse(null);

            if (closestSpawn != null) {
                buildRangerAttacking(closestSpawn, gameHistoryState, pgs, strategyParams);

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
                              StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);


        Position2d retreatPosition = Optional.ofNullable(pgs.getMyRangerBase())
                .map(b -> of(b.getPosition()).shift(6, 6)).orElse(of(40, 40));

        Position2d attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);

        RangerAttackHoldRetreatCommand rahrc = new RangerAttackHoldRetreatCommand(null, attackPosition, retreatPosition, false);
        CommandUtil.chainCommands(buildRangerCommand, rahrc);

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }

    void createBuildRangerCommand(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);


        Position2d retreatPosition = Optional.ofNullable(pgs.getMyRangerBase())
                .map(b -> of(b.getPosition()).shift(6, 6)).orElse(of(40, 40));

        Position2d attackPosition = StrategyParams.selectRandomAccordingDistribution(strategyParams.attackPoints, strategyParams.attackPointRates);

        RangerAttackHoldRetreatCommand rahrc = new RangerAttackHoldRetreatCommand(null, attackPosition, retreatPosition, false);
        CommandUtil.chainCommands(buildRangerCommand, rahrc);

        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }
}
