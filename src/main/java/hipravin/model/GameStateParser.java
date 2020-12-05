package hipravin.model;

import hipravin.DebugOut;
import hipravin.strategy.StrategyParams;
import model.Entity;
import model.EntityProperties;
import model.EntityType;
import model.PlayerView;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hipravin.model.Position2d.of;
import static hipravin.model.Position2dUtil.MAP_SIZE;
import static hipravin.model.Position2dUtil.squareInclusiveCornerStream;
import static hipravin.strategy.StrategyParams.MAP_CORNER_SIZE;

public abstract class GameStateParser {
    public static ParsedGameState parse(PlayerView playerView) {
        DebugOut.println("Tick: " + playerView.getCurrentTick());
        int mapSize = playerView.getMapSize();

        ParsedGameState parsedGameState = new ParsedGameState();
        parsedGameState.playerView = playerView;

        parsedGameState.cells = emptyCellsOfSize(mapSize);
        parsedGameState.buildingsByEntityId = new HashMap<>();
        parsedGameState.myWorkers = new HashMap<>();
        parsedGameState.entityIdToCell = new HashMap<>();

        for (Entity entity : playerView.getEntities()) {
            switch (entity.getEntityType()) {
                case WALL, HOUSE, BUILDER_BASE, RANGED_BASE, MELEE_BASE, TURRET -> {
                    setCells(parsedGameState.cells, Cell.ofBuilding(entity, playerView));
                    addBuilding(parsedGameState, entity);
                }
                case BUILDER_UNIT, MELEE_UNIT, RANGED_UNIT -> {
                    setCell(parsedGameState.cells,
                            Cell.ofUnit(entity, playerView));
                }
                case RESOURCE -> {
                    setCell(parsedGameState.cells,
                            Cell.ofMineral(entity, playerView));
                }
            }
        }

        forEachCell(parsedGameState.cells, c -> {
            c.setMyEntity(c.getOwnerPlayerId() == playerView.getMyId());

            if (c.isMyEntity && c.getEntityType() == EntityType.BUILDER_UNIT) {
                parsedGameState.myWorkers.put(c.entityId, c);
            }
        });

        Arrays.stream(playerView.getEntities()).forEach(e -> parsedGameState.entityIdToCell.put(e.getId(), parsedGameState.at(e.getPosition())));

        if (!playerView.isFogOfWar()) {
            forEachCell(parsedGameState.cells, Cell::setVisible);
        } else {
            //TODO: set all is visible according to sight ranges
        }

        calculateWorkerXY(parsedGameState);

        parsedGameState.population = Population.of(playerView);

        countMineralsAtMapCorner(parsedGameState);
        countWorkersAtMapCorner(parsedGameState);

        calculateNeighbourMineralsAndWorkers(parsedGameState);

        GameStateParserDjkstra.computeMyNonUniqueNearestWorkers(parsedGameState);
        GameStateParserDjkstra.computeClosestMinerals(parsedGameState);

        computeProducingBuildingEdge(parsedGameState);
        computeFreeSpaces(parsedGameState);
        computeBuildingEdgeFreeCells(parsedGameState);

        calculateWorkersAtMiningPositions(parsedGameState);
        return parsedGameState;
    }

    static void calculateWorkerXY(ParsedGameState pgs) {
        pgs.maxWorkerX = Arrays.stream(pgs.getPlayerView().getEntities())
                .filter(e -> e.getPlayerId() != null && e.getPlayerId() == pgs.getPlayerView().getMyId())
                .filter(e -> e.getEntityType() == EntityType.BUILDER_UNIT)
                .mapToInt(e -> e.getPosition().getX()).max().orElse(-1);
        pgs.maxWorkerY = Arrays.stream(pgs.getPlayerView().getEntities())
                .filter(e -> e.getPlayerId() != null && e.getPlayerId() == pgs.getPlayerView().getMyId())
                .filter(e -> e.getEntityType() == EntityType.BUILDER_UNIT)
                .mapToInt(e -> e.getPosition().getY()).max().orElse(-1);
    }

    static void countMineralsAtMapCorner(ParsedGameState pgs) {
        AtomicInteger counter = new AtomicInteger(0);

        forEachPosition(0, MAP_CORNER_SIZE, 0, MAP_CORNER_SIZE, c -> {
            if(pgs.at(c).isMineral) {
                counter.incrementAndGet();
            }
        });

        pgs.mineralsAtMapCorner = counter.get();
    }

    static void countWorkersAtMapCorner(ParsedGameState pgs) {
        AtomicInteger counter = new AtomicInteger(0);

        forEachPosition(0, MAP_CORNER_SIZE, 0, MAP_CORNER_SIZE, c -> {
            if(pgs.at(c).isMyWorker()) {
                counter.incrementAndGet();
            }
        });

        pgs.myWorkersAtMapCorner = counter.get();
    }

    static void markupFog(ParsedGameState pgs) {
        Map<EntityType, EntityProperties> entityProperties = pgs.getPlayerView().getEntityProperties();

//        List<Arrays.stream(pgs.getPlayerView().getEntities())
//                .filter(e -> e.getPlayerId() != null && e.getPlayerId() == pgs.getPlayerView().getMyId());


    }

    static void calculateWorkersAtMiningPositions(ParsedGameState pgs) {
        pgs.workersAtMiningPositions = (int) pgs.myWorkers.values()
                .stream().filter(c -> c.len1MineralsCount > 0)
                .count();

    }


    static void calculateNeighbourMineralsAndWorkers(ParsedGameState parsedGameState) {
        parsedGameState.allCellsAsStream().forEach(eachCell -> {
            eachCell.len1MineralsCount = Position2dUtil.upRightLeftDown(eachCell.getPosition()).map(p -> parsedGameState.at(p))
                    .filter(mc -> mc.isMineral)
                    .count();

            eachCell.len1MyWorkersCount = Position2dUtil.upRightLeftDown(eachCell.getPosition()).map(p -> parsedGameState.at(p))
                    .filter(wc -> wc.isMyWorker())
                    .count();
        });
    }


    static void computeFreeSpaces(ParsedGameState pgs) {
        Cell[][] cells = pgs.cells;

        //so far compute completely free and free with our units
        //can be optimised, but not sure it's reasonable, requires profiling
        forEachPosition(0, pgs.maxWorkerX + StrategyParams.FREE_SPACE_COMPUTE_RANGE,
                0, pgs.maxWorkerY + StrategyParams.FREE_SPACE_COMPUTE_RANGE, corner -> {
            Cell cornerCell = getCell(cells, corner);
            //for each size 2-5
            for (int size = Cell.MIN_FP_SIZE; size <= Cell.MAX_FP_SIZE; size++) {
                Optional<Stream<Position2d>> squareStream = squareInclusiveCornerStream(corner, size);
                if (squareStream.isEmpty()) {
                    break;
                }

                boolean onlyContainOurUnits = false;
                boolean isEmpty = squareStream.get()
                        .allMatch(p -> getCell(cells, p).isEmpty());
                if (isEmpty) {
                    FreeSpace free = FreeSpace.completelyFree(size);
                    cornerCell.setFreeSpace(size, free);
                } else {
                    onlyContainOurUnits = squareInclusiveCornerStream(corner, size).get()
                            .allMatch(p -> getCell(cells, p).testOr(Cell::isEmpty, Cell::isMyUnit));
                    if (onlyContainOurUnits) {
                        FreeSpace unitFree = FreeSpace.freeButContainOutUnits(size);
                        cornerCell.setFreeSpace(size, unitFree);
                    }
                }

                if (!isEmpty && !onlyContainOurUnits) {
                    break;//save unnecessary iterations
                }
            }
        });
    }

    static void computeBuildingEdgeFreeCells(ParsedGameState pgs) {
        pgs.buildingsByEntityId.values().forEach(b -> {
            b.buildingEmptyOuterEdgeWithoutCorners = new HashSet<>(b.buildingOuterEdgeWithoutCorners);
            b.buildingEmptyOuterEdgeWithoutCorners.removeIf(p -> !pgs.at(p).isEmpty());
        });
    }

    static void computeProducingBuildingEdge(ParsedGameState pgs) {
        pgs.getBuildingsByEntityId().values().stream()
                .filter(Building::isMyBuilding)
                .filter(Building::isProducingBuilding)
                .forEach(b -> {
                    b.getBuildingOuterEdgeWithCorners().forEach(p -> {
                        pgs.at(p).isProducingMyBuildingOuterEdge = true;
                    });
                });
    }

    static Cell getCell(Cell[][] cells, Position2d position2d) {
        return cells[position2d.x][position2d.y];
    }

    static void setCells(Cell[][] cells, List<Cell> cellsToSet) {
        cellsToSet.forEach(c -> setCell(cells, c));
    }

    static void addBuilding(ParsedGameState pgs, Entity entity) {
        Building building = Building.of(entity, pgs.playerView, pgs.at(entity.getPosition()));
        pgs.buildingsByEntityId.put(entity.getId(), building);
    }

    static void setCell(Cell[][] cells, Cell cell) {
        Position2d p = cell.getPosition();

        cells[p.x][p.y] = cell;
    }

    static Cell[][] emptyCellsOfSize(int size) {
        Cell[][] cells = new Cell[size][];
        for (int x = 0; x < size; x++) {
            cells[x] = new Cell[size];
        }

        forEachPosition(size,
                p -> cells[p.x][p.y] = Cell.empty(p));

        return cells;
    }


    static void forEachCell(Cell[][] cells, Consumer<Cell> consumer) {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                consumer.accept(cell);
            }
        }
    }

    public static void forEachPosition(int size, Consumer<Position2d> positionConsumer) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                positionConsumer.accept(of(x, y));
            }
        }
    }
    public static void forEachPosition(int minX, int maxX, int minY, int maxY, Consumer<Position2d> positionConsumer) {
        int maxXlimited = Math.min(maxX, MAP_SIZE);
        int maxYlimited = Math.min(maxY, MAP_SIZE);

        for (int x = Math.max(0, minX); x < maxXlimited; x++) {
            for (int y = Math.max(0, minY); y < maxYlimited; y++) {
                positionConsumer.accept(of(x, y));
            }
        }
    }

    private GameStateParser() {
    }
}
