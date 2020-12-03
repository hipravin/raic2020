package hipravin.strategy;

import hipravin.model.Position2d;

import java.util.List;

public class BeforeFirstHouseBuildOrder {
    Position2d firstMineralToMine;
    Position2d whereToMoveAfterFirstMineralBeingMined;//0-3 options. Backward seems to be stupid
    List<BuildMine> workersWhereToBuild;
    Position2d firstHouseWhereToBuild;

    public static class BuildMine {
        public Position2d workerBuildPosition;
        public Position2d workerMinePosition;
        public Position2d mineralToMinePosition;

        public BuildMine(Position2d workerBuildPosition, Position2d workerMinePosition, Position2d mineralToMinePosition) {
            this.workerBuildPosition = workerBuildPosition;
            this.workerMinePosition = workerMinePosition;
            this.mineralToMinePosition = mineralToMinePosition;
        }

        @Override
        public String toString() {
            return "BuildMine{" +
                    "workerBuildPosition=" + workerBuildPosition +
                    ", workerMinePosition=" + workerMinePosition +
                    ", mineralToMinePosition=" + mineralToMinePosition +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BeforeFirstHouseBuildOrder{" +
                "firstMineralToMine=" + firstMineralToMine +
                ", whereToMoveAfterFirstMineralBeingMined=" + whereToMoveAfterFirstMineralBeingMined +
                ", workersWhereToBuild=" + workersWhereToBuild +
                ", firstHouseWhereToBuild=" + firstHouseWhereToBuild +
                '}';
    }
}
