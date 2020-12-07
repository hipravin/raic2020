package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import hipravin.strategy.GameHistoryAndSharedState;
import hipravin.strategy.StrategyParams;
import hipravin.strategy.ValuedEntityAction;

import java.util.Map;
import java.util.Set;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class MineFromExactPositionCommand extends Command {
    final Position2d spawnPosition;
    final Position2d targetPosition;
    final Position2d mineralToMinePosition;
    final CommandPredicate mineCancelPredicate;

    public MineFromExactPositionCommand(Position2d spawnPosition, Position2d targetPosition, Position2d mineralToMinePosition,
                                        CommandPredicate mineCancelPredicate) {
        super(MAX_VAL, Set.of());
        this.spawnPosition = spawnPosition;
        this.targetPosition = targetPosition;
        this.mineralToMinePosition = mineralToMinePosition;
        this.mineCancelPredicate = mineCancelPredicate;

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
                MineExactMineral mineExact = new MineExactMineral(workerId, mineralToMinePosition, 1);
                CommandUtil.chainCommands(this, moveSingleCommand, mineExact); //again, automine should be automated in order not to lock workers with commands
            } else {
                MineExactMineral mineExact = new MineExactMineral(workerId, mineralToMinePosition, MAX_VAL);

                if(mineCancelPredicate != null) {//used in spawn worker strategy to cancel action near mine location and switch to automine
                    mineExact.setConditionalReplacer(new CancelCommand(), mineCancelPredicate);
                }

                CommandUtil.chainCommands(this, mineExact); //again, automine should be automated in order not to lock workers with commands
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
        return "MineFromExactPositionCommand{" +
                "spawnPosition=" + spawnPosition +
                ", targetPosition=" + targetPosition +
                ", mineralToMinePosition=" + mineralToMinePosition +
                '}';
    }
}
