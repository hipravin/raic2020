package hipravin.strategy;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2dUtil;
import model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MiningSubStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        stubJustMine(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void stubJustMine(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {


        List<Entity> myWorkers = currentParsedGameState.getMyWorkers()
                .values().stream().map(Cell::getEntity).collect(Collectors.toList());


        myWorkers.forEach(w -> {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(2 * Position2dUtil.MAP_SIZE, new EntityType[]{EntityType.RESOURCE}));
            autoAttack.setAttackAction(attackAction);
            assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
        });
    }
}
