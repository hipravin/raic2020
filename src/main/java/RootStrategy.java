import hipravin.model.GameStateParser;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.*;
import model.Action;
import model.DebugCommand;
import model.EntityAction;
import model.PlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RootStrategy extends MyStrategy {

    GameHistoryState gameHistoryState;
    ParsedGameState currentParsedGameState;
    StrategyParams strategyParams;

    List<SubStrategy> subStrategies = new ArrayList<>();

    public RootStrategy() {
        strategyParams = new StrategyParams();
        MicroSubStrategy microSubStrategy = new MicroSubStrategy();
        BuildOrderSubStrategy buildOrderSubStrategy = new BuildOrderSubStrategy();

        subStrategies.add(microSubStrategy);
        subStrategies.add(buildOrderSubStrategy);
    }

    void initStaticParams(PlayerView playerView) {
        if(playerView.getCurrentTick() == 0) {
            Position2dUtil.MAP_SIZE = playerView.getMapSize();
//            Position2dUtil.MY_CORNER = playerView. // TODO: set my corner just for the case
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

    void updateGameHistoryState() {


    }


    @Override
    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        initStaticParams(playerView);
        parseDataForSingleTick(playerView);

        Action decision = combineDecisions();

        updateGameHistoryState();
        return decision;
//        return new Action(new java.util.HashMap<>());
    }

    @Override
    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}
