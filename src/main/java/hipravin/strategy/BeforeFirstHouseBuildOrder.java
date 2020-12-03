package hipravin.strategy;

import hipravin.model.Position2d;

import java.util.List;
import java.util.Objects;

public class BeforeFirstHouseBuildOrder {
    Position2d firstMineralToMine;
    Position2d whereToMoveAfterFirstMineralBeingMined;//0-3 options. Backward seems to be stupid

    List<BuildMine> workersWhereToBuild;

    Position2d firstHouseWhereToBuild;
    Position2d repairer3Len2Position;

    public static class BuildMine {
        public Position2d workerBuildPosition;
        public Position2d workerMinePosition;
        public Position2d mineralToMinePosition;//I think we can use autoattack

        public BuildMine(Position2d workerBuildPosition, Position2d workerMinePosition, Position2d mineralToMinePosition) {
            this.workerBuildPosition = workerBuildPosition;
            this.workerMinePosition = workerMinePosition;
            this.mineralToMinePosition = mineralToMinePosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BuildMine buildMine = (BuildMine) o;
            return Objects.equals(workerBuildPosition, buildMine.workerBuildPosition) &&
                    Objects.equals(workerMinePosition, buildMine.workerMinePosition) &&
                    Objects.equals(mineralToMinePosition, buildMine.mineralToMinePosition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workerBuildPosition, workerMinePosition, mineralToMinePosition);
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
                ", repairer3Len2Position=" + repairer3Len2Position +
                '}';
    }
}
