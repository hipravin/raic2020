package model;

import util.StreamUtil;

public class AutoAttack {
    private int pathfindRange;
    public int getPathfindRange() { return pathfindRange; }
    public void setPathfindRange(int pathfindRange) { this.pathfindRange = pathfindRange; }
    private EntityType[] validTargets;
    public EntityType[] getValidTargets() { return validTargets; }
    public void setValidTargets(EntityType[] validTargets) { this.validTargets = validTargets; }
    public AutoAttack() {}
    public AutoAttack(int pathfindRange, EntityType[] validTargets) {
        this.pathfindRange = pathfindRange;
        this.validTargets = validTargets;
    }
    public static AutoAttack readFrom(java.io.InputStream stream) throws java.io.IOException {
        AutoAttack result = new AutoAttack();
        result.pathfindRange = StreamUtil.readInt(stream);
        result.validTargets = new EntityType[StreamUtil.readInt(stream)];
        for (int i = 0; i < result.validTargets.length; i++) {
            switch (StreamUtil.readInt(stream)) {
            case 0:
                result.validTargets[i] = EntityType.WALL;
                break;
            case 1:
                result.validTargets[i] = EntityType.HOUSE;
                break;
            case 2:
                result.validTargets[i] = EntityType.BUILDER_BASE;
                break;
            case 3:
                result.validTargets[i] = EntityType.BUILDER_UNIT;
                break;
            case 4:
                result.validTargets[i] = EntityType.MELEE_BASE;
                break;
            case 5:
                result.validTargets[i] = EntityType.MELEE_UNIT;
                break;
            case 6:
                result.validTargets[i] = EntityType.RANGED_BASE;
                break;
            case 7:
                result.validTargets[i] = EntityType.RANGED_UNIT;
                break;
            case 8:
                result.validTargets[i] = EntityType.RESOURCE;
                break;
            case 9:
                result.validTargets[i] = EntityType.TURRET;
                break;
            default:
                throw new java.io.IOException("Unexpected tag value");
            }
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, pathfindRange);
        StreamUtil.writeInt(stream, validTargets.length);
        for (EntityType validTargetsElement : validTargets) {
            StreamUtil.writeInt(stream, validTargetsElement.tag);
        }
    }
}
