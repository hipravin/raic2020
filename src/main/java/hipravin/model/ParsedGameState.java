package hipravin.model;

import model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParsedGameState {
    PlayerView playerView;
    Cell[][] cells;

    Population population;

    Map<Integer, Building> buildingsByEntityId;
    Map<Integer, Cell> myWorkers;

    Map<Integer, Cell> entityIdToCell; //for buildings to corner cell

    int workersAtMiningPositions;

    int mineralsAtMapCorner = 0;
    int myWorkersAtMapCorner = 0;

    int maxWorkerX = -1;
    int maxWorkerY = -1;

    public int getMyEstimatedResourceThisTick() {
        return getMyPlayer().getResource() + (workersAtMiningPositions - 1);
    }

    public int getEstimatedResourceAfterTicks(int ticks) {
        return getMyPlayer().getResource()  + (workersAtMiningPositions - 1) * ticks - 1;
    }

    public int getActiveHouseCount() {
        return (int) Arrays.stream(playerView.getEntities())
                .filter(e -> e.getEntityType() == EntityType.HOUSE
                        && e.getPlayerId() != null && e.getPlayerId() == playerView.getMyId()
                        && e.isActive())
                .count();
    }

    public int getHouseCost() {
        return playerView.getEntityProperties().get(EntityType.HOUSE).getInitialCost();
    }

    public int curTick() {
        return playerView.getCurrentTick();
    }

    public Entity getMyCc() {
        return Arrays.stream(playerView.getEntities())
                .filter(e -> e.getEntityType() == EntityType.BUILDER_BASE
                        && e.getPlayerId() != null && e.getPlayerId() == playerView.getMyId())
                .findAny().orElse(null);
    }

    public Stream<Cell> allCellsAsStream() {
        Stream<Cell> combined = Stream.of();
        for (Cell[] column : cells) {
            combined = Stream.concat(combined, Arrays.stream(column));
        }

        return combined;
    }

    public List<Building> findMyProducingBuildings() {
        return buildingsByEntityId.values().stream()
                .filter(Building::isMyBuilding)
                .filter(Building::isProducingBuilding)
                .collect(Collectors.toList());
    }

    public List<Building> findMyBuildings(EntityType entityType) {
        return buildingsByEntityId.values().stream()
                .filter(Building::isMyBuilding)
                .filter(b -> b.cornerCell.getEntity().getEntityType() == entityType)
                .collect(Collectors.toList());
    }

    public List<Building> findAllMyBuildings() {
        return buildingsByEntityId.values().stream()
                .filter(Building::isMyBuilding)
                .collect(Collectors.toList());
    }

    public Player getMyPlayer() {
        return Arrays.stream(playerView.getPlayers()).filter(p -> p.getId() == playerView.getMyId()).findAny().orElseThrow();
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public Cell at(Position2d position2d) {
        return cells[position2d.x][position2d.y];
    }

    public Cell at(Vec2Int v) {
        return at(Position2d.of(v));
    }

    public Map<Integer, Building> getBuildingsByEntityId() {
        return buildingsByEntityId;
    }

    public Map<Integer, Cell> getMyWorkers() {
        return myWorkers;
    }

    public Population getPopulation() {
        return population;
    }

    public Map<Integer, Cell> getEntityIdToCell() {
        return entityIdToCell;
    }


    public int getMaxWorkerX() {
        return maxWorkerX;
    }

    public int getMaxWorkerY() {
        return maxWorkerY;
    }

    public int getWorkersAtMiningPositions() {
        return workersAtMiningPositions;
    }

    public int getMineralsAtMapCorner() {
        return mineralsAtMapCorner;
    }

    public int getMyWorkersAtMapCorner() {
        return myWorkersAtMapCorner;
    }
}


