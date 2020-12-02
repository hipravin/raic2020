import hipravin.model.*;
import hipravin.strategy.*;
import hipravin.strategy.command.BuildingBuildCommand;
import model.*;

import java.util.*;
import java.util.stream.Collectors;

public class RootStrategy extends MyStrategy {

    GameHistoryAndSharedState gameHistoryState = new GameHistoryAndSharedState();
    ParsedGameState currentParsedGameState;
    StrategyParams strategyParams;

    List<SubStrategy> subStrategies = new ArrayList<>();

    public RootStrategy() {
        strategyParams = new StrategyParams();

        BuildFirstTwoHousesStrategy bfths = new BuildFirstTwoHousesStrategy();

        MiningSubStrategy miningSubStrategy = new MiningSubStrategy();
        MicroSubStrategy microSubStrategy = new MicroSubStrategy();
        BuildOrderSubStrategy buildOrderSubStrategy = new BuildOrderSubStrategy();

        PerformBuildCommandsSubStrategy performBuildCommandsSubStrategy = new PerformBuildCommandsSubStrategy();

        subStrategies.add(bfths);
        subStrategies.add(miningSubStrategy);
        subStrategies.add(buildOrderSubStrategy);
        subStrategies.add(microSubStrategy);

        subStrategies.add(performBuildCommandsSubStrategy);

    }

    void initStaticParams(PlayerView playerView) {
        if (playerView.getCurrentTick() == 0) {
            Position2dUtil.MAP_SIZE = playerView.getMapSize();
            Optional<Entity> myCC = Arrays.stream(playerView.getEntities())
                    .filter(e -> e.getEntityType() == EntityType.BUILDER_BASE && e.getPlayerId().equals(playerView.getMyId()))
                    .findAny();

            myCC.ifPresent(entity -> {
                Position2dUtil.MY_CC = Position2d.of(entity.getPosition());
            });

            setMyCorner(playerView);
            setBuildingSizes(playerView);
        }
    }

    void setBuildingSizes(PlayerView playerView) {
        Map<EntityType, EntityProperties> properties = playerView.getEntityProperties();

        Position2dUtil.CC_SIZE = properties.get(EntityType.BUILDER_BASE).getSize();
        Position2dUtil.WALL_SIZE = properties.get(EntityType.WALL).getSize();
        Position2dUtil.TURRET_SIZE = properties.get(EntityType.TURRET).getSize();
        Position2dUtil.HOUSE_SIZE = properties.get(EntityType.HOUSE).getSize();
        Position2dUtil.RANGED_BASE_SIZE = properties.get(EntityType.RANGED_BASE).getSize();
        Position2dUtil.MELEE_BASE_SIZE = properties.get(EntityType.MELEE_BASE).getSize();
    }

    void setMyCorner(PlayerView playerView) {
        Entity myEntity = Arrays.stream(playerView.getEntities())
                .filter(e -> e.getPlayerId() != null && e.getPlayerId() == playerView.getMyId()).findAny().orElse(null);
        int myCornerX = 0;
        int myCornerY = 0;

        if (myEntity != null) {
            if (myEntity.getPosition().getX() > Position2dUtil.MAP_SIZE / 2) {
                myCornerX = Position2dUtil.MAP_SIZE - 1;
            }
            if (myEntity.getPosition().getY() > Position2dUtil.MAP_SIZE / 2) {
                myCornerY = Position2dUtil.MAP_SIZE - 1;
            }

            Position2dUtil.MY_CORNER = Position2d.of(myCornerX, myCornerY);

        }
    }

    void parseDataForSingleTick(PlayerView playerView) {
        currentParsedGameState = GameStateParser.parse(playerView);
        removeCompletedOrStaleBuildActions();

    }

    void removeCompletedOrStaleBuildActions() {
        gameHistoryState.getOngoingBuildCommands()
                .removeIf(this::isBuildCommandCompletedOrStale);
    }

    boolean isBuildCommandCompletedOrStale(BuildingBuildCommand bc) {
        //completed
        if (currentParsedGameState.at(bc.getBuildingCornerPosition())
                .test(c -> c.isBuilding() && c.getEntity().isActive())) {
            return true;
        }
        //not empty, but expected to start building already - need new position
        if (bc.getStartBuildTick() <= currentParsedGameState.curTick()
                && currentParsedGameState.at(bc.getBuildingCornerPosition())
                .test(c -> !c.isMyBuilding()
                        && c.getFreeSpace(bc.getSize()).map(fs -> !fs.isCompletelyFree()).orElse(true))) {
            return true;
        }
        //not started to build
        if (currentParsedGameState.at(bc.getBuildingCornerPosition())
                .test(c -> !c.isBuilding()) //empty or contains something else
                && bc.getStartBuildTick() + strategyParams.buildCommandMaxWaitTicks
                < currentParsedGameState.getPlayerView().getCurrentTick()) {
            return true;
        }
        //builder is dead
        if (!currentParsedGameState.getMyWorkers().containsKey(bc.getBuilderEntityId())) {
            return true;
        }
        bc.getRepairCommands().removeIf(rc -> !currentParsedGameState.getMyWorkers().containsKey(rc.getRepairerEntityId()));

        return false;
    }

    Action combineDecisions() {
        Map<Integer, ValuedEntityAction> assignedActions = new HashMap<>();

        //exclusive strategy, e.g. for game start
        List<SubStrategy> thisTickStrategies = subStrategies.stream()
                .filter(s -> s.isExclusivelyApplicableAtThisTick(gameHistoryState, currentParsedGameState, strategyParams, assignedActions))
                .limit(1).collect(Collectors.toList());

        if (thisTickStrategies.isEmpty()) {
            thisTickStrategies = subStrategies.stream()
                    .filter(s -> s.isApplicableAtThisTick(gameHistoryState, currentParsedGameState, strategyParams, assignedActions))
                    .collect(Collectors.toList());
        }

        thisTickStrategies.forEach(
                ss -> ss.decide(gameHistoryState, currentParsedGameState, strategyParams, assignedActions));


        Map<Integer, EntityAction> entityActions = assignedActions.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getEntityAction()));

        return new Action(entityActions);
    }

    void updateGameHistoryState(Action decision) {
        gameHistoryState.setPreviousTickAction(decision);
        gameHistoryState.setPreviousParsedGameState(currentParsedGameState);
        gameHistoryState.getPlayerInfo().put(currentParsedGameState.curTick(), currentParsedGameState.getPlayerView().getPlayers());
    }


    @Override
    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        initStaticParams(playerView);
        parseDataForSingleTick(playerView);

        Action decision = combineDecisions();
        updateGameHistoryState(decision);
        return decision;
    }

    @Override
    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }

    public GameHistoryAndSharedState getGameHistoryState() {
        return gameHistoryState;
    }

    public ParsedGameState getCurrentParsedGameState() {
        return currentParsedGameState;
    }

    public StrategyParams getStrategyParams() {
        return strategyParams;
    }

    public List<SubStrategy> getSubStrategies() {
        return subStrategies;
    }
}
