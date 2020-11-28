package model;

import util.StreamUtil;

public class PlayerView {
    private int myId;
    public int getMyId() { return myId; }
    public void setMyId(int myId) { this.myId = myId; }
    private int mapSize;
    public int getMapSize() { return mapSize; }
    public void setMapSize(int mapSize) { this.mapSize = mapSize; }
    private boolean fogOfWar;
    public boolean isFogOfWar() { return fogOfWar; }
    public void setFogOfWar(boolean fogOfWar) { this.fogOfWar = fogOfWar; }
    private java.util.Map<EntityType, EntityProperties> entityProperties;
    public java.util.Map<EntityType, EntityProperties> getEntityProperties() { return entityProperties; }
    public void setEntityProperties(java.util.Map<EntityType, EntityProperties> entityProperties) { this.entityProperties = entityProperties; }
    private int maxTickCount;
    public int getMaxTickCount() { return maxTickCount; }
    public void setMaxTickCount(int maxTickCount) { this.maxTickCount = maxTickCount; }
    private int maxPathfindNodes;
    public int getMaxPathfindNodes() { return maxPathfindNodes; }
    public void setMaxPathfindNodes(int maxPathfindNodes) { this.maxPathfindNodes = maxPathfindNodes; }
    private int currentTick;
    public int getCurrentTick() { return currentTick; }
    public void setCurrentTick(int currentTick) { this.currentTick = currentTick; }
    private Player[] players;
    public Player[] getPlayers() { return players; }
    public void setPlayers(Player[] players) { this.players = players; }
    private Entity[] entities;
    public Entity[] getEntities() { return entities; }
    public void setEntities(Entity[] entities) { this.entities = entities; }
    public PlayerView() {}
    public PlayerView(int myId, int mapSize, boolean fogOfWar, java.util.Map<EntityType, EntityProperties> entityProperties, int maxTickCount, int maxPathfindNodes, int currentTick, Player[] players, Entity[] entities) {
        this.myId = myId;
        this.mapSize = mapSize;
        this.fogOfWar = fogOfWar;
        this.entityProperties = entityProperties;
        this.maxTickCount = maxTickCount;
        this.maxPathfindNodes = maxPathfindNodes;
        this.currentTick = currentTick;
        this.players = players;
        this.entities = entities;
    }
    public static PlayerView readFrom(java.io.InputStream stream) throws java.io.IOException {
        PlayerView result = new PlayerView();
        result.myId = StreamUtil.readInt(stream);
        result.mapSize = StreamUtil.readInt(stream);
        result.fogOfWar = StreamUtil.readBoolean(stream);
        int entityPropertiesSize = StreamUtil.readInt(stream);
        result.entityProperties = new java.util.HashMap<>(entityPropertiesSize);
        for (int i = 0; i < entityPropertiesSize; i++) {
            EntityType entityPropertiesKey;
            switch (StreamUtil.readInt(stream)) {
            case 0:
                entityPropertiesKey = EntityType.WALL;
                break;
            case 1:
                entityPropertiesKey = EntityType.HOUSE;
                break;
            case 2:
                entityPropertiesKey = EntityType.BUILDER_BASE;
                break;
            case 3:
                entityPropertiesKey = EntityType.BUILDER_UNIT;
                break;
            case 4:
                entityPropertiesKey = EntityType.MELEE_BASE;
                break;
            case 5:
                entityPropertiesKey = EntityType.MELEE_UNIT;
                break;
            case 6:
                entityPropertiesKey = EntityType.RANGED_BASE;
                break;
            case 7:
                entityPropertiesKey = EntityType.RANGED_UNIT;
                break;
            case 8:
                entityPropertiesKey = EntityType.RESOURCE;
                break;
            case 9:
                entityPropertiesKey = EntityType.TURRET;
                break;
            default:
                throw new java.io.IOException("Unexpected tag value");
            }
            EntityProperties entityPropertiesValue;
            entityPropertiesValue = EntityProperties.readFrom(stream);
            result.entityProperties.put(entityPropertiesKey, entityPropertiesValue);
        }
        result.maxTickCount = StreamUtil.readInt(stream);
        result.maxPathfindNodes = StreamUtil.readInt(stream);
        result.currentTick = StreamUtil.readInt(stream);
        result.players = new Player[StreamUtil.readInt(stream)];
        for (int i = 0; i < result.players.length; i++) {
            result.players[i] = Player.readFrom(stream);
        }
        result.entities = new Entity[StreamUtil.readInt(stream)];
        for (int i = 0; i < result.entities.length; i++) {
            result.entities[i] = Entity.readFrom(stream);
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, myId);
        StreamUtil.writeInt(stream, mapSize);
        StreamUtil.writeBoolean(stream, fogOfWar);
        StreamUtil.writeInt(stream, entityProperties.size());
        for (java.util.Map.Entry<EntityType, EntityProperties> entityPropertiesEntry : entityProperties.entrySet()) {
            EntityType entityPropertiesKey = entityPropertiesEntry.getKey();
            EntityProperties entityPropertiesValue = entityPropertiesEntry.getValue();
            StreamUtil.writeInt(stream, entityPropertiesKey.tag);
            entityPropertiesValue.writeTo(stream);
        }
        StreamUtil.writeInt(stream, maxTickCount);
        StreamUtil.writeInt(stream, maxPathfindNodes);
        StreamUtil.writeInt(stream, currentTick);
        StreamUtil.writeInt(stream, players.length);
        for (Player playersElement : players) {
            playersElement.writeTo(stream);
        }
        StreamUtil.writeInt(stream, entities.length);
        for (Entity entitiesElement : entities) {
            entitiesElement.writeTo(stream);
        }
    }
}
