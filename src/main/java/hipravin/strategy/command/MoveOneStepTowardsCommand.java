package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;

public class MoveOneStepTowardsCommand extends MoveSingleCommand {
    public MoveOneStepTowardsCommand(ParsedGameState pgs, int entityId, Position2d targetPosition) {
        super(pgs, entityId, targetPosition, 1);//try to prevent stucks with 2
    }

    @Override
    public String toString() {
        return "MoveOneStepTowardsCommand{" +
                "entityId=" + entityId +
                ", targetPosition=" + targetPosition +
                '}';
    }
}
