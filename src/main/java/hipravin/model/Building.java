package hipravin.model;

import model.Entity;
import model.EntityType;
import model.PlayerView;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class Building {
    static EnumSet<EntityType> PRODUCING_ENTITY_TYPES = EnumSet.of(
            EntityType.BUILDER_BASE, EntityType.RANGED_BASE, EntityType.MELEE_BASE);

    Cell cornerCell;

    Set<Position2d> buildingInnerEdge = new HashSet<>();
    Set<Position2d> buildingOuterEdgeWithoutCorners = new HashSet<>();
    Set<Position2d> buildingOuterEdgeWithCorners = new HashSet<>();
    Set<Position2d> buildingEmptyOuterEdgeWithoutCorners;

    int enemyRepairersCount = 0;

    int myBuildingAttackersCount = 0;

    public static Building of(Entity entity, PlayerView playerView, Cell cornerBuildingCell) {
        Building building = new Building();
        building.cornerCell = cornerBuildingCell;

        building.buildingInnerEdge = Position2dUtil.squareEdgeWithCorners(cornerBuildingCell.position,
                cornerBuildingCell.buildingSize);

        building.buildingOuterEdgeWithoutCorners = Collections.unmodifiableSet(Position2dUtil.buildingOuterEdgeWithoutCorners(
                cornerBuildingCell.position, cornerBuildingCell.buildingSize));
        building.buildingOuterEdgeWithCorners = Collections.unmodifiableSet(Position2dUtil.buildingOuterEdgeWithCorners(
                cornerBuildingCell.position, cornerBuildingCell.buildingSize));

        return building;
    }

    public boolean isProducingBuilding() {
        return PRODUCING_ENTITY_TYPES.contains(cornerCell.getEntityType());
    }

    public int getId() {
        return cornerCell.entityId;
    }

    public Cell getCornerCell() {
        return cornerCell;
    }

    public Set<Position2d> getBuildingInnerEdge() {
        return buildingInnerEdge;
    }

    public Set<Position2d> getBuildingOuterEdgeWithoutCorners() {
        return buildingOuterEdgeWithoutCorners;
    }

    public Set<Position2d> getBuildingEmptyOuterEdgeWithoutCorners() {
        return buildingEmptyOuterEdgeWithoutCorners;
    }

    public boolean isMyBuilding() {
        return cornerCell.isMyEntity;
    }

    public Set<Position2d> getBuildingOuterEdgeWithCorners() {
        return buildingOuterEdgeWithCorners;
    }

    public int getMyBuildingAttackersCount() {
        return myBuildingAttackersCount;
    }

    public void setMyBuildingAttackersCount(int myBuildingAttackersCount) {
        this.myBuildingAttackersCount = myBuildingAttackersCount;
    }

    public int getEnemyRepairersCount() {
        return enemyRepairersCount;
    }

    public void setEnemyRepairersCount(int enemyRepairersCount) {
        this.enemyRepairersCount = enemyRepairersCount;
    }
}
