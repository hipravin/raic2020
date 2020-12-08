package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.Map;
import java.util.Set;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class SendNewWorkerToPositionCommand extends Command {
    final Position2d spawnPosition;
    final Position2d targetPosition;
    final CommandPredicate moveCancelPredicate;

    public SendNewWorkerToPositionCommand(Position2d spawnPosition, Position2d targetPosition,
                                          CommandPredicate mineCancelPredicate) {
        super(MAX_VAL, Set.of());
        this.spawnPosition = spawnPosition;
        this.targetPosition = targetPosition;
        this.moveCancelPredicate = mineCancelPredicate;
    }

    @Override
    public boolean isValid(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams) {
        return true;
    }

    @Override
    public boolean isCompleted(GameHistoryAndSharedState gameHistoryState, ParsedGameState pgs, StrategyParams strategyParams) {
        if(pgs.at(spawnPosition).isMyWorker()) {//TODO: additionally check that worker count was increased?
            int workerId = pgs.at(spawnPosition).getEntityId();
            if(targetPosition != null) {
                MoveSingleCommand moveSingleCommand = new MoveSingleCommand(pgs, workerId, targetPosition, MAX_VAL);
                if(moveCancelPredicate != null) {
                    moveSingleCommand.setConditionalReplacer(new CancelCommand(), moveCancelPredicate);
                }
                CommandUtil.chainCommands(this, moveSingleCommand);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateAssignedActions(GameHistoryAndSharedState gameHistoryState, ParsedGameState currentParsedGameState, StrategyParams strategyParams, Map<Integer, ValuedEntityAction> assignedActions) {
        //do nothing, waiting for worker to appear, then proceed with next commands
    }

    @Override
    public String toString() {
        return "SendNewWorkerToPositionCommand{" +
                "spawnPosition=" + spawnPosition +
                ", targetPosition=" + targetPosition +
                ", moveCancelPredicate=" + moveCancelPredicate +
                '}';
    }
}