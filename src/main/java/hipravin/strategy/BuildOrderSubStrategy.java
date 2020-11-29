package hipravin.strategy;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildOrderSubStrategy implements SubStrategy {
    @Override
    public void decide(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                       StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {



        stubBuildBuildBuildHouses(gameHistoryState, currentParsedGameState, strategyParams, assignedActions);
    }

    public void stubBuildBuildBuildHouses(GameHistoryState gameHistoryState, ParsedGameState currentParsedGameState,
                                          StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        List<Entity> workers = Arrays.stream(currentParsedGameState.getPlayerView().getEntities())
                .filter(e -> e.getEntityType() == EntityType.BUILDER_UNIT && e.getPlayerId() == currentParsedGameState.getPlayerView().getMyId())
                .collect(Collectors.toList());

        workers.forEach(w -> {
            EntityAction autoAttack = new EntityAction();
            AttackAction attackAction = new AttackAction(null, new AutoAttack(100, new EntityType[]{EntityType.RESOURCE}));
            autoAttack.setAttackAction(attackAction);
            assignedActions.put(w.getId(), new ValuedEntityAction(0.5, w.getId(), autoAttack));
        });

        Entity cc = Arrays.stream(currentParsedGameState.getPlayerView().getEntities())
                .filter(e -> e.getEntityType() == EntityType.BUILDER_BASE && e.getPlayerId() == currentParsedGameState.getPlayerView().getMyId() ).findAny().orElseThrow();

        EntityAction buildWorker = new EntityAction();
        BuildAction ba = new BuildAction(EntityType.BUILDER_UNIT, Position2d.of(cc.getPosition()).shift(2,5).toVec2dInt());
        buildWorker.setBuildAction(ba);

        assignedActions.put(cc.getId(), new ValuedEntityAction(0.5, cc.getId(), buildWorker));

    }


}
