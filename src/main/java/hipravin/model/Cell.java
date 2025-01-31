package hipravin.model;

import model.Entity;
import model.EntityProperties;
import model.EntityType;
import model.PlayerView;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static hipravin.strategy.StrategyParams.MAX_VAL;

public class Cell implements Cloneable {
    public static final int MIN_FP_SIZE = 2;
    public static final int MAX_FP_SIZE = 5;

    Position2d position;
    boolean isEmpty = true;
    boolean wasEmptyWhileVisible = false;
    boolean isProducingMyBuildingOuterEdge = false;
    boolean fog = true;

    int ownerPlayerId = -1;
    EntityType entityType = null;
    int healthLeft = -1;
    int maxHealth = -1;

    Position2d rangerSwitchedAttackPositionTo = null;

    boolean isMineral = false;
    boolean isEnemyTerritoryMineral = false;
    boolean isBuilding = false;
    int buildingSize = -1;

    boolean isUnit = false;
    boolean isMyEntity = false;
    Entity entity;
    EntityProperties entityProps;
    int sightRange = -1;
    int entityId = -1;
    NearestEntity myNearestWorker = null;
    NearestEntity nearestMineralField = null;

    //worker position -> ne
    Map<Position2d, NearestEntity> workersNearby = new HashMap<>();

    long len1MineralsCount = -1;
    long len1MyWorkersCount = -1;

//    Map<Position2d, Integer> nearbyWorkersPathToThisCell;//from position to this, value is path length

    private FreeSpace[] freeSpaces = new FreeSpace[MAX_FP_SIZE - MIN_FP_SIZE + 1];//size - 2 -> index ()

    boolean isMineralEdge = false;
    boolean isFogEdge = false;
    boolean isMapEdge = false;

    int range5attackersCount = 0;//5 or less
    int range6attackersCount = 0;
    int range7attackersCount = 0;
    int range8attackersCount = 0;
    int range9attackersCount = 0;
    int range10attackersCount = 0;

    int range5turretCount = 0;//5 or less
    int range6turretCount = 0;
    int range7turretCount = 0;
    int range8turretCount = 0;

    int totalNearAttackerCount = 0;
    int range5enemyWorkers = 0;

    public void setAttackerCount(int range, int count) {
        switch (range) {
            case 5:
                range5attackersCount = count;
                break;
            case 6:
                range6attackersCount = count;
                break;
            case 7:
                range7attackersCount = count;
                break;
            case 8:
                range8attackersCount = count;
                break;
            case 9:
                range9attackersCount = count;
                break;
            case 10:
                range10attackersCount = count;
                break;
        }
    }

    public int getAttackerCount(int range) {

        switch(range) {
            case 5:
                return range5attackersCount;
            case 6:
                return range6attackersCount;
            case 7:
                return range7attackersCount;
            case 8:
                return range8attackersCount;
            case 9:
                return range9attackersCount;
            case 10:
                return range10attackersCount;
        }

        return -1;
    }

    public void setTurretAttackerCount(int range, int count) {
        switch (range) {
            case 5:
                range5turretCount = count;
                break;
            case 6:
                range6turretCount = count;
                break;
            case 7:
                range7turretCount = count;
                break;
            case 8:
                range8turretCount = count;
                break;
        }
    }

    public int getTurretAttackerCount(int range) {

        switch(range) {
            case 5:
                return range5turretCount;
            case 6:
                return range6turretCount;
            case 7:
                return range7turretCount;
            case 8:
                return range8turretCount;
        }

        return -1;
    }


//    private int mineralsNearby5;

    public boolean test(Predicate<? super Cell> condition) {
        return condition.test(this);
    }

    public boolean testOr(Predicate<? super Cell>... conditions) {
        return Arrays.stream(conditions).anyMatch(predicate -> predicate.test(this));
    }
    public boolean testAnd(Predicate<? super Cell>... conditions) {
        return Arrays.stream(conditions).allMatch(predicate -> predicate.test(this));
    }

    public boolean isMyWorker() {
        return isMyUnit() && entityType == EntityType.BUILDER_UNIT;
    }
    public boolean isMyRanger() {
        return isMyUnit() && entityType == EntityType.RANGED_UNIT;
    }
    public boolean isEnemyRanger() {
        return isUnit && !isMyUnit() && entityType == EntityType.RANGED_UNIT;
    }

    public boolean isEnemySwordman() {
        return isUnit && !isMyUnit() && entityType == EntityType.MELEE_UNIT;
    }

    public boolean isEnemyTurret() {
        return isBuilding && !isMyEntity && entityType == EntityType.TURRET;
    }

    public static Cell empty(Position2d position) {
        Cell cell = new Cell();
        cell.isEmpty = true;
        cell.position = position;
        cell.fog = true;

        return cell;
    }

    public static Cell ofMineral(Entity entity, PlayerView playerView) {
        Cell cell = new Cell();
        cell.position = Position2d.of(entity.getPosition());;
        cell.isMineral = true;
        cell.healthLeft = entity.getHealth();
        cell.entityId = entity.getId();
        cell.maxHealth = playerView.getEntityProperties().get(EntityType.RESOURCE).getMaxHealth();
        cell.isEmpty = false;
        cell.entity = entity;

        return cell;
    }

    public static Cell ofUnit(Entity entity, PlayerView playerView) {
        Cell cell = new Cell();

        withPlayerEntity(cell, entity, playerView);

        cell.position = Position2d.of(entity.getPosition());
        cell.isUnit = true;

        return cell;
    }

    public static List<Cell> ofBuilding(Entity entity, PlayerView playerView) {
        int size = playerView.getEntityProperties().get(entity.getEntityType()).getSize();
        List<Position2d> positions = Position2dUtil.squareInclusiveCorner(Position2d.of(entity.getPosition()), size).get();

        Cell cell = new Cell();
        cell.entityType = entity.getEntityType();
        cell.isBuilding = true;
        cell.buildingSize = size;
        cell.ownerPlayerId = entity.getPlayerId();

        withPlayerEntity(cell, entity, playerView);

        List<Cell> cells = positions.stream().map(cell::cloneTo)
                .collect(Collectors.toList());

        return cells;
    }

    public static void withPlayerEntity(Cell cell, Entity entity, PlayerView playerView) {
        cell.entity = entity;
        cell.entityType = entity.getEntityType();
        cell.entityProps = props(entity, playerView);
        cell.sightRange = cell.entityProps.getSightRange();
        cell.ownerPlayerId = entity.getPlayerId();
        cell.isEmpty = false;
        cell.healthLeft = entity.getHealth();
        cell.maxHealth = cell.entityProps.getMaxHealth();
        cell.entityId = entity.getId();
    }

    public static EntityProperties props(Entity entity, PlayerView playerView) {
        return playerView.getEntityProperties().get(entity.getEntityType());
    }

    public Cell cloneTo(Position2d newPosition) {
        try {
            Cell cloned = (Cell) this.clone();
            cloned.position = newPosition;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public Position2d getPosition() {
        return position;
    }

    public void setPosition(Position2d position) {
        this.position = position;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public void setVisible() {
        fog = false;
    }

    public boolean isFog() {
        return fog;
    }

    public void setFog(boolean fog) {
        this.fog = fog;
    }

    public int getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public void setOwnerPlayerId(int ownerPlayerId) {
        this.ownerPlayerId = ownerPlayerId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public boolean isMineral() {
        return isMineral;
    }

    public void setMineral(boolean mineral) {
        isMineral = mineral;
    }

    public int getHealthLeft() {
        return healthLeft;
    }

    public void setHealthLeft(int healthLeft) {
        this.healthLeft = healthLeft;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isBuilding() {
        return isBuilding;
    }

    public void setBuilding(boolean building) {
        isBuilding = building;
    }

    public boolean isMyEntity() {
        return isMyEntity;
    }
    public boolean isOppEntity() {
        return (isBuilding || isUnit) && !isMyEntity;
    }

    public void setMyEntity(boolean myEntity) {
        isMyEntity = myEntity;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isUnit() {
        return isUnit;
    }

    public boolean isMyUnit() {
        return isUnit && isMyEntity;
    }

    public boolean isMyBuilding() {
        return isBuilding && isMyEntity;
    }

    public void setUnit(boolean unit) {
        isUnit = unit;
    }

    public EntityProperties getEntityProps() {
        return entityProps;
    }

    public void setEntityProps(EntityProperties entityProps) {
        this.entityProps = entityProps;
    }

    public int getSightRange() {
        return sightRange;
    }

    public void setSightRange(int sightRange) {
        this.sightRange = sightRange;
    }

    public void setFreeSpace(int size, FreeSpace freeSpace) {
        freeSpaces[size - MIN_FP_SIZE] = freeSpace;
    }

//    public Optional<FreeSpace> getFreeSpace(int size) {
//        if(size - MIN_FP_SIZE >= freeSpaces.length) {
//            //in case building size will be increased unexpectedly
//            return Optional.empty();
//        }
//        return Optional.ofNullable(freeSpaces[size - MIN_FP_SIZE]);
//    }

    public int getBuildingSize() {
        return buildingSize;
    }

    public void setBuildingSize(int buildingSize) {
        this.buildingSize = buildingSize;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public NearestEntity getMyNearestWorker() {
        return myNearestWorker;
    }

    public long getLen1MineralsCount() {
        return len1MineralsCount;
    }

    public long getLen1MyWorkersCount() {
        return len1MyWorkersCount;
    }

    public boolean isProducingMyBuildingOuterEdge() {
        return isProducingMyBuildingOuterEdge;
    }

    public int getPathLenToNearestMineralOrInf() {
        return Optional.ofNullable(nearestMineralField).map(NearestEntity::getPathLenEmptyCellsToThisCell)
                .orElse(MAX_VAL);
    }

    public NearestEntity getNearestMineralField() {
        return nearestMineralField;
    }

    public boolean isWasEmptyWhileVisible() {
        return wasEmptyWhileVisible;
    }

    public Map<Position2d, NearestEntity> getWorkersNearby() {
        return workersNearby;
    }

    public boolean isMineralEdge() {
        return isMineralEdge;
    }

    public boolean isFogEdge() {
        return isFogEdge;
    }

    public boolean isMapEdge() {
        return isMapEdge;
    }

    public Position2d getRangerSwitchedAttackPositionTo() {
        return rangerSwitchedAttackPositionTo;
    }

    public int getTotalNearAttackerCount() {
        return totalNearAttackerCount;
    }

    public void setTotalNearAttackerCount(int totalNearAttackerCount) {
        this.totalNearAttackerCount = totalNearAttackerCount;
    }

    public boolean isEnemyTerritoryMineral() {
        return isEnemyTerritoryMineral;
    }

    public void setEnemyTerritoryMineral(boolean enemyTerritoryMineral) {
        isEnemyTerritoryMineral = enemyTerritoryMineral;
    }

    public int getRange5enemyWorkers() {
        return range5enemyWorkers;
    }

    public void setRange5enemyWorkers(int range5enemyWorkers) {
        this.range5enemyWorkers = range5enemyWorkers;
    }
}

