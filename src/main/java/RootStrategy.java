import hipravin.model.GameStateParser;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.*;
import model.*;

import java.util.*;
import java.util.stream.Collectors;

public class RootStrategy extends MyStrategy {

    GameHistoryState gameHistoryState = new GameHistoryState();
    ParsedGameState currentParsedGameState;
    StrategyParams strategyParams;

    List<SubStrategy> subStrategies = new ArrayList<>();

    public RootStrategy() {
        strategyParams = new StrategyParams();
        MiningSubStrategy miningSubStrategy = new MiningSubStrategy();
        MicroSubStrategy microSubStrategy = new MicroSubStrategy();
        BuildOrderSubStrategy buildOrderSubStrategy = new BuildOrderSubStrategy();

        subStrategies.add(miningSubStrategy);
        subStrategies.add(buildOrderSubStrategy);
        subStrategies.add(microSubStrategy);
    }

    void initStaticParams(PlayerView playerView) {
        if (playerView.getCurrentTick() == 0) {
            Position2dUtil.MAP_SIZE = playerView.getMapSize();

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
    }

    Action combineDecisions() {
        Map<Integer, ValuedEntityAction> assignedActions = new HashMap<>();

        subStrategies.forEach(
                ss -> ss.decide(gameHistoryState, currentParsedGameState, strategyParams, assignedActions));


        Map<Integer, EntityAction> entityActions = assignedActions.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getEntityAction()));

        return new Action(entityActions);
    }

    void updateGameHistoryState(Action decision) {
        gameHistoryState.setPreviousTickAction(decision);
    }


    @Override
    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        initStaticParams(playerView);
        parseDataForSingleTick(playerView);

        Action decision = combineDecisions();

        updateGameHistoryState(decision );
        return decision;
//        return new Action(new java.util.HashMap<>());
    }

    @Override
    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}
