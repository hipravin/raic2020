package hipravin.model;

public class NearestUnit {
    final Cell sourceUnitCell;
    final Cell thisCell;
    final int pathLenEmptyCellsToThisCell;

    public NearestUnit(Cell sourceUnitCell, Cell thisCell, int pathLenEmptyCellsToThisCell) {
        this.sourceUnitCell = sourceUnitCell;
        this.thisCell = thisCell;
        this.pathLenEmptyCellsToThisCell = pathLenEmptyCellsToThisCell;
    }

    public NearestUnit pathPlus1(Cell newThisCell) {
        return new NearestUnit(sourceUnitCell, newThisCell, pathLenEmptyCellsToThisCell + 1);
    }

    public Cell getSourceUnitCell() {
        return sourceUnitCell;
    }

    public Cell getThisCell() {
        return thisCell;
    }

    public int getPathLenEmptyCellsToThisCell() {
        return pathLenEmptyCellsToThisCell;
    }
}
