package model;

import util.StreamUtil;

public class EntityAction {
    private MoveAction moveAction;
    public MoveAction getMoveAction() { return moveAction; }
    public void setMoveAction(MoveAction moveAction) { this.moveAction = moveAction; }
    private BuildAction buildAction;
    public BuildAction getBuildAction() { return buildAction; }
    public void setBuildAction(BuildAction buildAction) { this.buildAction = buildAction; }
    private AttackAction attackAction;
    public AttackAction getAttackAction() { return attackAction; }
    public void setAttackAction(AttackAction attackAction) { this.attackAction = attackAction; }
    private RepairAction repairAction;
    public RepairAction getRepairAction() { return repairAction; }
    public void setRepairAction(RepairAction repairAction) { this.repairAction = repairAction; }
    public EntityAction() {}
    public EntityAction(MoveAction moveAction, BuildAction buildAction, AttackAction attackAction, RepairAction repairAction) {
        this.moveAction = moveAction;
        this.buildAction = buildAction;
        this.attackAction = attackAction;
        this.repairAction = repairAction;
    }
    public static EntityAction readFrom(java.io.InputStream stream) throws java.io.IOException {
        EntityAction result = new EntityAction();
        if (StreamUtil.readBoolean(stream)) {
            result.moveAction = MoveAction.readFrom(stream);
        } else {
            result.moveAction = null;
        }
        if (StreamUtil.readBoolean(stream)) {
            result.buildAction = BuildAction.readFrom(stream);
        } else {
            result.buildAction = null;
        }
        if (StreamUtil.readBoolean(stream)) {
            result.attackAction = AttackAction.readFrom(stream);
        } else {
            result.attackAction = null;
        }
        if (StreamUtil.readBoolean(stream)) {
            result.repairAction = RepairAction.readFrom(stream);
        } else {
            result.repairAction = null;
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        if (moveAction == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            moveAction.writeTo(stream);
        }
        if (buildAction == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            buildAction.writeTo(stream);
        }
        if (attackAction == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            attackAction.writeTo(stream);
        }
        if (repairAction == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            repairAction.writeTo(stream);
        }
    }
}
