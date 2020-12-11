package hipravin.strategy.command;

import hipravin.model.Cell;
import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.model.Position2dUtil;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;
import model.*;

import java.util.Map;
import java.util.Optional;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class DefenderRangerCommand extends SingleEntityCommand {


    public DefenderRangerCommand(int entityId) {
        super(MAX_VAL, entityId);
    }

    public DefenderRangerCommand() {
        super(MAX_VAL, EntityType.RANGED_UNIT);
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        tryResolveEntityId(pgs);

        Cell c = pgs.getEntityIdToCell().get(entityId);
        if (c == null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        tryResolveEntityId(pgs);

        return  pgs.getDefendingAreaEnemies().isEmpty();
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs,
                                      StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {

        Position2d myPosition = pgs.getEntityIdToCell().get(entityId).getPosition();


        Position2d nearestEnemyInDefendingArea = pgs.findClosesEnemyArmyInDefArea(myPosition).orElse(null);

        if(nearestEnemyInDefendingArea != null) {
            updateAttack(nearestEnemyInDefendingArea, gameHistoryState, pgs, strategyParams, assignedActions);
        }

    }

    public void updateAttack(Position2d p, GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState,
                             StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        EntityAction autoAttack = new EntityAction();
        AttackAction attackAction = new AttackAction(null, new AutoAttack(Position2dUtil.RANGER_RANGE,
                strategyParams.rangerDefaultAttackTargets));

        autoAttack.setAttackAction(attackAction);

        autoAttack.setMoveAction(new MoveAction(p.toVec2dInt(), true, true));

        assignedActions.put(entityId, new ValuedEntityAction(0.5, entityId, autoAttack));
    }

}
