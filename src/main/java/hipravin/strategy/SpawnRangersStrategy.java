package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.command.BuildRangerCommand;
import hipravin.strategy.command.Command;
import hipravin.strategy.command.CommandUtil;
import hipravin.strategy.command.RangerScoutCommand;
import model.Entity;
import model.EntityType;

import java.util.*;

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
        if (!shouldSpawnMoreRangers(gameHistoryState, pgs, strategyParams)) {
            return;
        }

        findBestSpawnPos(gameHistoryState, pgs, strategyParams);
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

    void createBuildRangerCommand(Position2d spawnPos, GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                  StrategyParams strategyParams) {

        Command buildRangerCommand = new BuildRangerCommand(spawnPos, pgs, 1);

        if(strategyParams.ifRandom(strategyParams.rangerScoutRateProb)) {
            RangerScoutCommand scout = RangerScoutCommand.toFogNearEnemyHalfWay(null, spawnPos, strategyParams.randomScoutPointNearEnemy());
            CommandUtil.chainCommands(buildRangerCommand, scout);
        }
        gameHistoryState.addOngoingCommand(buildRangerCommand, false);
    }
}
