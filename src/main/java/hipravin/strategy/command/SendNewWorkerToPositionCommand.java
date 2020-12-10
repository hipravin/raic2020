package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class SendNewWorkerToPositionCommand extends Command {
    final Position2d spawnPosition;
    final Position2d targetPosition;
    final CommandPredicate moveCancelPredicate;
    final int distanceCancelTreshhold;
    BiConsumer<Integer, Integer> oncompleteIdTickConsumer;
    BiConsumer<Integer, Integer> onStartIdTickConsumer;


    public SendNewWorkerToPositionCommand(Position2d spawnPosition, Position2d targetPosition,
                                          CommandPredicate mineCancelPredicate, int distanceCancelTreshhold,
                                          BiConsumer<Integer, Integer> oncompleteIdTickConsumer,
                                                  BiConsumer<Integer, Integer> onStartIdTickConsumer

    ) {
        super(MAX_VAL, Set.of());
        this.spawnPosition = spawnPosition;
        this.targetPosition = targetPosition;
        this.moveCancelPredicate = mineCancelPredicate;
        this.distanceCancelTreshhold = distanceCancelTreshhold;
        this.oncompleteIdTickConsumer = oncompleteIdTickConsumer;
        this.onStartIdTickConsumer = onStartIdTickConsumer;
    }
    public SendNewWorkerToPositionCommand(Position2d spawnPosition, Position2d targetPosition,
                                          CommandPredicate mineCancelPredicate, int distanceCancelTreshhold
                                          ) {
        super(MAX_VAL, Set.of());
        this.spawnPosition = spawnPosition;
        this.targetPosition = targetPosition;
        this.moveCancelPredicate = mineCancelPredicate;
        this.distanceCancelTreshhold = distanceCancelTreshhold;
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
                MoveTowardsCommand moveTowardsCommand = new MoveTowardsCommand(pgs, workerId, targetPosition, MAX_VAL, distanceCancelTreshhold);
                if(moveCancelPredicate != null) {
                    moveTowardsCommand.setConditionalReplacer(new CancelCommand(), moveCancelPredicate);
                }
                moveTowardsCommand.setOncompleteIdTickConsumer(oncompleteIdTickConsumer);
                moveTowardsCommand.setOnStartIdTickConsumer(onStartIdTickConsumer);


                CommandUtil.chainCommands(this, moveTowardsCommand);
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
