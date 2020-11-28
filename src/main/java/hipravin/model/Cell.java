package hipravin.model;

import model.EntityProperties;
import model.EntityType;
import model.PlayerView;
import model.Vec2Int;

public class Cell {
    private Vec2Int location;
    private boolean isEmpty = true;
    private boolean fog = true;
    private int ownerPlayerId = -1;
    private Vec2Int entityCorner = null;
    private EntityType entityType = null;
    private boolean isMineral = false;
    private int healthLeft = 0;
    private int maxHealth = 0;

    public static Cell ofMineral(Vec2Int location, int healthLeft, PlayerView playerView) {
        Cell cell = new Cell();
        cell.location = location;
        cell.isMineral = true;
        cell.healthLeft = healthLeft;
        cell.maxHealth = playerView.getEntityProperties().get(EntityType.RESOURCE).getMaxHealth();

        return cell;
    }

    public static Cell ofUnit(Vec2Int location, EntityType entityType, int ownerPlayerId, int healthLeft) {
        Cell cell = new Cell();

        return null;
    }

    public Cell visible() {
        fog = false;
        return this;
    }
}
