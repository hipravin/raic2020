package hipravin.strategy.command;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.AttackAction;
import model.AutoAttack;
import model.EntityAction;
import model.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class MineExactMineral extends Command {
    final int minerId;
    final Position2d mineralToMine;
    final int pathRange;

    public MineExactMineral(int minerId, Position2d mineralToMine, int pathRange) {
        super(MAX_VAL, Set.of(minerId));
        this.minerId = minerId;
        this.mineralToMine = mineralToMine;
        this.pathRange = pathRange;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return Optional.ofNullable(
                pgs.getEntityIdToCell().get(minerId))
                .map(Cell::isMyWorker).orElse(false);
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        return !pgs.at(mineralToMine).isMineral();
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        int mineralId = pgs.at(mineralToMine).getEntityId();

        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(mineralId, new AutoAttack(pathRange, new EntityType[]{EntityType.RESOURCE}));
        autoAttack.setAttackAction(attackAction);
        assignedActions.put(minerId, new ValuedEntityAction(0.5, minerId, autoAttack));
    }

    @Override
    public String toString() {
        return "MineExactMineral{" +
                "minerId=" + minerId +
                ", mineralToMine=" + mineralToMine +
                ", pathRange=" + pathRange +
                '}';
    }
}
