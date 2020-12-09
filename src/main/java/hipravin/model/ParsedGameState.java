package hipravin.model;

import hipravin.strategy.StrategyParams;
import model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hipravin.model.GameStateParser.forEachPosition;
import static hipravin.model.Position2dUtil.isSquareWithinMapBorder;

public class ParsedGameState {
    PlayerView playerView;
    Cell[][] cells;

    Population population;

    Map<Integer, Building> buildingsByEntityId;
    Map<Integer, Cell> myWorkers;
    Map<Integer, Cell> myRangers;

    Map<Integer, Cell> entityIdToCell; //for buildings to corner cell

    List<Position2d> fogEdgePositions = new ArrayList<>();
    Set<Position2d> fogEdgePositionsSet = new HashSet<>();

    Set<Integer> newEntityIds = new HashSet<>();
    Map<Position2d, Position2d> workersMovedSinceLastTick = new HashMap<>();

    int workersAtMiningPositions;

    int mineralsAtMapCorner = 0;
    int myWorkersAtMapCorner = 0;

    int maxWorkerX = -1;
    int maxWorkerY = -1;

    public int getMyEstimatedResourceThisTick() {
        return getMyPlayer().getResource() + (workersAtMiningPositions - 1);
    }

    public int getEstimatedResourceAfterTicks(int ticks) {
        return getMyPlayer().getResource() + (workersAtMiningPositions - 1) * ticks - 1;
    }

    public Optional<FreeSpace> calculateFreeSpace(Cell cell, int size) {
        boolean containUnits = false;
        Position2d corner = cell.getPosition();

        if (!isSquareWithinMapBorder(corner, size)) {
            return Optional.empty();
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell c = at(corner.shift(i, j));

                if (c.isMyUnit()) {
                    containUnits = true;
                } else if (!c.isEmpty) {
                    return Optional.empty();
                }
            }
        }
        if (containUnits) {
            return Optional.of(FreeSpace.freeButContainOutUnits(size));
        } else {
            return Optional.of(FreeSpace.completelyFree(size));
        }
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
    public int getTurretCost() {
        return playerView.getEntityProperties().get(EntityType.TURRET).getInitialCost();
    }
    public int getBarrackCost(EntityType barrackType) {
        return playerView.getEntityProperties().get(barrackType).getInitialCost();
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

    public Entity getMyRangerBase() {
        return Arrays.stream(playerView.getEntities())
                .filter(e -> e.getEntityType() == EntityType.RANGED_BASE
                        && e.getPlayerId() != null && e.getPlayerId() == playerView.getMyId())
                .findAny().orElse(null);
    }

    public Entity getMyBarrack(EntityType barrackType) {
        return Arrays.stream(playerView.getEntities())
                .filter(e -> e.getEntityType() == barrackType
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

    public List<Cell> myWorkersSquareCellsAsStream() {
        List<Cell> cells = new ArrayList<>();

        forEachPosition(0, maxWorkerX + StrategyParams.FREE_SPACE_COMPUTE_RANGE,
                0, maxWorkerY + StrategyParams.FREE_SPACE_COMPUTE_RANGE, corner -> {
                    cells.add(at(corner));
                });

        return cells;

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

    public Map<Integer, Cell> getMyRangers() {
        return myRangers;
    }


    public List<Position2d> getFogEdgePositions() {
        return fogEdgePositions;
    }

    public Set<Position2d> getFogEdgePositionsSet() {
        return fogEdgePositionsSet;
    }

    public Optional<Integer> getMyUnitSpawned(EntityType entityType) {
        return newEntityIds.stream().filter(id -> entityIdToCell.get(id).test(c -> c.isMyEntity && c.getEntity().getEntityType() == entityType))
                .findFirst();
    }

    public Set<Integer> getNewEntityIds() {
        return newEntityIds;
    }

    public Map<Position2d, Position2d> getWorkersMovedSinceLastTick() {
        return workersMovedSinceLastTick;
    }
}


