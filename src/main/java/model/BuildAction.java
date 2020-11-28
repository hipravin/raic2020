package model;

import util.StreamUtil;

public class BuildAction {
    private EntityType entityType;
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    private Vec2Int position;
    public Vec2Int getPosition() { return position; }
    public void setPosition(Vec2Int position) { this.position = position; }
    public BuildAction() {}
    public BuildAction(EntityType entityType, Vec2Int position) {
        this.entityType = entityType;
        this.position = position;
    }
    public static BuildAction readFrom(java.io.InputStream stream) throws java.io.IOException {
        BuildAction result = new BuildAction();
        switch (StreamUtil.readInt(stream)) {
        case 0:
            result.entityType = EntityType.WALL;
            break;
        case 1:
            result.entityType = EntityType.HOUSE;
            break;
        case 2:
            result.entityType = EntityType.BUILDER_BASE;
            break;
        case 3:
            result.entityType = EntityType.BUILDER_UNIT;
            break;
        case 4:
            result.entityType = EntityType.MELEE_BASE;
            break;
        case 5:
            result.entityType = EntityType.MELEE_UNIT;
            break;
        case 6:
            result.entityType = EntityType.RANGED_BASE;
            break;
        case 7:
            result.entityType = EntityType.RANGED_UNIT;
            break;
        case 8:
            result.entityType = EntityType.RESOURCE;
            break;
        case 9:
            result.entityType = EntityType.TURRET;
            break;
        default:
            throw new java.io.IOException("Unexpected tag value");
        }
        result.position = Vec2Int.readFrom(stream);
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, entityType.tag);
        position.writeTo(stream);
    }
}
