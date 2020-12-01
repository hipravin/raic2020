package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import hipravin.model.Position2d;
import model.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * When build command is created, worker is immediately asked to move to builderPosition
 */

public class BuildingBuildCommand {
    int commandCreatedTick;
    int startBuildTick;
    EntityType buildingType;
    int size;

    int builderEntityId;
    Position2d builderBuildPosition;
    Position2d buildingCornerPosition;
    List<RepairCommand> repairCommands = new ArrayList<>();

    public static BuildingBuildCommand buildSingleWorker(
            EntityType buildingType, int builderEntityId,
            Position2d builderBuildPosition,
            Position2d buildingCornerPosition,
            int lenToStartPosition,
            ParsedGameState pgs) {
        BuildingBuildCommand command = new BuildingBuildCommand();
        command.builderEntityId = builderEntityId;
        command.commandCreatedTick = pgs.curTick();
        command.startBuildTick = pgs.curTick() + lenToStartPosition;
        command.builderBuildPosition = builderBuildPosition;
        command.buildingCornerPosition = buildingCornerPosition;
        command.buildingType = buildingType;
        command.size = pgs.getPlayerView().getEntityProperties().get(buildingType).getSize();

        command.repairCommands.add(RepairCommand.repairAfterBuildSameWorker(command));

        return command;
    }

    public Stream<Integer> buidersAndRepairers() {
        return Stream.concat(Stream.of(builderEntityId),
                repairCommands.stream().map(rc -> rc.repairerEntityId));
    }

    public int getCommandCreatedTick() {
        return commandCreatedTick;
    }

    public int getStartBuildTick() {
        return startBuildTick;
    }

    public EntityType getBuildingType() {
        return buildingType;
    }

    public int getBuilderEntityId() {
        return builderEntityId;
    }

    public Position2d getBuilderBuildPosition() {
        return builderBuildPosition;
    }

    public Position2d getBuildingCornerPosition() {
        return buildingCornerPosition;
    }

    public List<RepairCommand> getRepairCommands() {
        return repairCommands;
    }

    public int getSize() {
        return size;
    }
}
