package hipravin.strategy;

import model.EntityAction;

public class ValuedEntityAction implements Comparable<ValuedEntityAction> {
    double value = 0.5;
    int entityId;
    EntityAction entityAction;

    public ValuedEntityAction(double value, int entityId, EntityAction entityAction) {
        this.value = value;
        this.entityId = entityId;
        this.entityAction = entityAction;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public EntityAction getEntityAction() {
        return entityAction;
    }

    public void setEntityAction(EntityAction entityAction) {
        this.entityAction = entityAction;
    }

    @Override
    public int compareTo(ValuedEntityAction o) {
        return Double.compare(value, o.value);
    }
}
