package hipravin.model;

public class FreeSpace {
    int size = -1;
//    int mineralCellsCount = 0;
//    int ourBuildingCellsCount = 0;
//    int ourUnitsCellsCount = 0;
//    int enemyUnitCellsCount = 0;
//    int enemyBuildingCellsCount = 0;
//    int fogCellsCount = 0;
    boolean isCompletelyFree = false;

    boolean isFreeButContainOurUnits = false;

    public static FreeSpace completelyFree(int size) {
        FreeSpace space = new FreeSpace();
        space.isCompletelyFree = true;
        space.size = size;

        return space;
    }
    public static FreeSpace freeButContainOutUnits(int size) {
        FreeSpace space = new FreeSpace();
        space.isCompletelyFree = false;
        space.isFreeButContainOurUnits = true;
        space.size = size;

        return space;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

//    public int getMineralCellsCount() {
//        return mineralCellsCount;
//    }
//
//    public void setMineralCellsCount(int mineralCellsCount) {
//        this.mineralCellsCount = mineralCellsCount;
//    }
//
//    public int getOurBuildingCellsCount() {
//        return ourBuildingCellsCount;
//    }
//
//    public void setOurBuildingCellsCount(int ourBuildingCellsCount) {
//        this.ourBuildingCellsCount = ourBuildingCellsCount;
//    }
//
//    public int getOurUnitsCellsCount() {
//        return ourUnitsCellsCount;
//    }
//
//    public void setOurUnitsCellsCount(int ourUnitsCellsCount) {
//        this.ourUnitsCellsCount = ourUnitsCellsCount;
//    }
//
//    public int getEnemyUnitCellsCount() {
//        return enemyUnitCellsCount;
//    }
//
//    public void setEnemyUnitCellsCount(int enemyUnitCellsCount) {
//        this.enemyUnitCellsCount = enemyUnitCellsCount;
//    }
//
//    public int getEnemyBuildingCellsCount() {
//        return enemyBuildingCellsCount;
//    }
//
//    public void setEnemyBuildingCellsCount(int enemyBuildingCellsCount) {
//        this.enemyBuildingCellsCount = enemyBuildingCellsCount;
//    }
//
//    public int getFogCellsCount() {
//        return fogCellsCount;
//    }
//
//    public void setFogCellsCount(int fogCellsCount) {
//        this.fogCellsCount = fogCellsCount;
//    }

    public boolean isCompletelyFree() {
        return isCompletelyFree;
    }

    public void setCompletelyFree(boolean completelyFree) {
        isCompletelyFree = completelyFree;
    }

    public boolean isFreeButContainOurUnits() {
        return isFreeButContainOurUnits;
    }

    public void setFreeButContainOurUnits(boolean freeButContainOurUnits) {
        isFreeButContainOurUnits = freeButContainOurUnits;
    }
}
