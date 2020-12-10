package hipravin.strategy.command;

import hipravin.model.ParsedGameState;
import model.EntityType;

import java.util.HashSet;
import java.util.Set;

public abstract class SingleEntityCommand extends Command {
    Integer entityId;
    EntityType entityType;

    boolean tryResolveEntityId(ParsedGameState pgs) {
        if (entityId == null) {
            entityId = pgs.getMyUnitSpawned(entityType).orElse(null);
            this.getRelatedEntityIds().add(entityId);
        }
        return entityId != null;
    }

    protected SingleEntityCommand(int expiryTick, int entityId) {
        super(expiryTick, Set.of(entityId));
        this.entityId = entityId;
    }

    protected SingleEntityCommand(int expiryTick, EntityType entityType) {
        super(expiryTick, new HashSet<>());
        this.entityType = entityType;
    }


}
