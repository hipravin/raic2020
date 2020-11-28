package hipravin.model;

import model.*;

import java.util.List;
import java.util.stream.Collectors;

public class Cell implements Cloneable {
    private Position2d position;
    private boolean isEmpty = true;
    private boolean fog = true;
    private int ownerPlayerId = -1;
    private EntityType entityType = null;
    private boolean isMineral = false;
    private int healthLeft = -1;
    private int maxHealth = -1;
    private boolean isBuilding = false;
    private boolean isUnit = false;
    private boolean isMyEntity = false;
    private Entity entity;
    private EntityProperties entityProps;
    private int sightRange = -1;
    private int entityId = -1;

    public static Cell empty(Position2d position) {
        Cell cell = new Cell();
        cell.isEmpty = true;
        cell.position = position;

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
        List<Position2d> positions = Position2dUtil.squareInclusiveCorner(Position2d.of(entity.getPosition()), size);

        Cell cell = new Cell();
        cell.entityType = entity.getEntityType();
        cell.isBuilding = true;
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

    public void setMyEntity(boolean myEntity) {
        isMyEntity = myEntity;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isUnit() {
        return isUnit;
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
}
