package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.Map;
import java.util.Set;

public class MineFromExactPositionCommand extends Command {
    final Position2d spawnPosition;
    final Position2d targetPosition;

    public MineFromExactPositionCommand(int expiryTick, Position2d spawnPosition, Position2d targetPosition) {
        super(Integer.MAX_VALUE, Set.of());
        this.spawnPosition = spawnPosition;
        this.targetPosition = targetPosition;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if(pgs.at(spawnPosition).isMyWorker()) {//TODO: additionally check that worker count was increased?
            int workerId = pgs.at(spawnPosition).getEntityId();
            MoveSingleCommand moveSingleCommand = new MoveSingleCommand(pgs, workerId, targetPosition, Integer.MAX_VALUE);
            AutomineCommand autoMine = new AutomineCommand(workerId);
            moveSingleCommand.setNextCommand(autoMine);

            this.setNextCommand(moveSingleCommand);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        //do nothing, waiting for worker to appear, then proceed with next commands
    }
}
