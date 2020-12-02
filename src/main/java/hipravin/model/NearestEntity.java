package hipravin.model;

public class NearestEntity {
    final Cell sourceCell;
    final Cell thisCell;
    final int pathLenEmptyCellsToThisCell;

    public NearestEntity(Cell sourceUnitCell, Cell thisCell, int pathLenEmptyCellsToThisCell) {
        this.sourceCell = sourceUnitCell;
        this.thisCell = thisCell;
        this.pathLenEmptyCellsToThisCell = pathLenEmptyCellsToThisCell;
    }

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
}
