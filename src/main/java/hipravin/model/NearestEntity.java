package hipravin.model;

import java.util.Comparator;

public class NearestEntity {
    final Cell sourceCell;
    final Cell thisCell;
    final int pathLenEmptyCellsToThisCell;

    public NearestEntity(Cell sourceUnitCell, Cell thisCell, int pathLenEmptyCellsToThisCell) {
        this.sourceCell = sourceUnitCell;
        this.thisCell = thisCell;
        this.pathLenEmptyCellsToThisCell = pathLenEmptyCellsToThisCell;
    }

    public static Comparator<? super NearestEntity> comparedByPathLen =
            Comparator.comparingInt(NearestEntity::getPathLenEmptyCellsToThisCell);
    public static Comparator<? super NearestEntity> comparedByPathLenReversed
            = Comparator.comparing(NearestEntity::getPathLenEmptyCellsToThisCell, Comparator.reverseOrder());

    public NearestEntity pathPlus1(Cell newThisCell) {
        return new NearestEntity(sourceCell, newThisCell, pathLenEmptyCellsToThisCell + 1);
    }

    public Cell getSourceCell() {
        return sourceCell;
    }

    public Cell getThisCell() {
        return thisCell;
    }

    public int getPathLenEmptyCellsToThisCell() {
        return pathLenEmptyCellsToThisCell;
    }

    @Override
    public String toString() {
        return "NearestEntity{" +
                "sourceCell=" + sourceCell.getPosition() +
                ", thisCell=" + thisCell.getPosition() +
                ", pathLenEmptyCellsToThisCell=" + pathLenEmptyCellsToThisCell +
                '}';
    }
}
