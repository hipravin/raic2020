package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;

public class MoveOneStepTowardsCommand extends MoveSingleCommand {
    public MoveOneStepTowardsCommand(ParsedGameState pgs, int entityId, Position2d targetPosition) {
        super(pgs, entityId, targetPosition, 2);//try to prevent stucks with 2
    }
}
