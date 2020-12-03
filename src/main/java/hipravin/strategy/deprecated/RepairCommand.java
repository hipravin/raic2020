package hipravin.strategy.deprecated;

import hipravin.model.Position2d;

import java.util.Optional;

@Deprecated
public class RepairCommand {
    int repairerEntityId;
    int repairStartTick;
    Position2d repairPosition;
    Position2d repairBuildingCornerPosition;
    Optional<Integer> moveToRepairPositionTick = Optional.empty(); //if present then need to move to repair position. Otherwise repair only if there


    public static RepairCommand repairAfterBuildSameWorker(BuildingBuildCommand c) {
        RepairCommand rc = new RepairCommand();

        rc.repairerEntityId = c.builderEntityId;
        rc.repairPosition = c.builderBuildPosition;
        rc.repairStartTick = -1;//start repair as soos as build being placed
        rc.repairBuildingCornerPosition = c.buildingCornerPosition;
        rc.moveToRepairPositionTick = Optional.empty();

        return rc;
    }

    public int getRepairerEntityId() {
        return repairerEntityId;
    }

    public int getRepairStartTick() {
        return repairStartTick;
    }

    public Position2d getRepairPosition() {
        return repairPosition;
    }

    public Position2d getRepairBuildingCornerPosition() {
        return repairBuildingCornerPosition;
    }

    public Optional<Integer> getMoveToRepairPositionTick() {
        return moveToRepairPositionTick;
    }
}
