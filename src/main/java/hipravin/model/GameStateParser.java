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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hipravin.model.Position2d.of;
import static hipravin.model.Position2dUtil.*;
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
        parsedGameState.myRangers = new HashMap<>();
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

            if (c.isMyEntity && c.getEntityType() == EntityType.RANGED_UNIT) {
                parsedGameState.myRangers.put(c.entityId, c);
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

        markupFog(parsedGameState);

        calculateForEdge(parsedGameState);
        calculateMineralEdge(parsedGameState);

        countMineralsAtMapCorner(parsedGameState);
        countWorkersAtMapCorner(parsedGameState);

        calculateNeighbourMineralsAndWorkers(parsedGameState);

//        GameStateParserDjkstra.computeMyNonUniqueNearestWorkers(parsedGameState); //only when requested
        GameStateParserDjkstra.computeClosestMinerals(parsedGameState);

        computeProducingBuildingEdge(parsedGameState);
//        computeUniqueWorkersNearby(parsedGameState);

//        computeFreeSpaces(parsedGameState);
        computeBuildingEdgeFreeCells(parsedGameState);

        calculateWorkersAtMiningPositions(parsedGameState);
        return parsedGameState;
    }

    static void calculateForEdge(ParsedGameState pgs) {
        Predicate<Position2d> notFog = (pp) -> !pgs.at(pp).fog;

        forEachCell(pgs.cells, c -> {
            Position2d p = c.getPosition();

            if (c.isFog()
                    && (withingMapBorderAndPassesFilter(p.shift(0, 1), notFog)
                    || withingMapBorderAndPassesFilter(p.shift(1, 0), notFog)
                    || withingMapBorderAndPassesFilter(p.shift(-1, 0), notFog)
                    || withingMapBorderAndPassesFilter(p.shift(0, -1), notFog))) {
                c.isFogEdge = true;
            }
        });
    }

    static void calculateMineralEdge(ParsedGameState pgs) {
        Predicate<Position2d> isVisibleAndNotMineral = (pp) -> pgs.at(pp).test(c -> !c.isFog() && !c.isMineral);

        forEachCell(pgs.cells, c -> {
            Position2d p = c.getPosition();

            if (c.isMineral
                    && (withingMapBorderAndPassesFilter(p.shift(0, 1), isVisibleAndNotMineral)
                    || withingMapBorderAndPassesFilter(p.shift(1, 0), isVisibleAndNotMineral)
                    || withingMapBorderAndPassesFilter(p.shift(-1, 0), isVisibleAndNotMineral)
                    || withingMapBorderAndPassesFilter(p.shift(0, -1), isVisibleAndNotMineral))) {
                c.isMineralEdge = true;
            }
        });
    }

    /**
     * perf tuning  - perform only when needed
     */
    public static void computeUniqueWorkersNearby(ParsedGameState pgs, int maxPath) {
        for (Cell myWorker : pgs.getMyWorkers().values()) {
            Map<Position2d, NearestEntity> nearestEntityMap =
                    GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), Set.of(myWorker.getPosition()), maxPath, true);

            nearestEntityMap.forEach((p, ne) -> {
                pgs.at(p).workersNearby.put(myWorker.position, ne);
            });
        }
    }

    public static void computeUniqueWorkersNearbyCenterMore(ParsedGameState pgs, int maxPath, int centerMaxPath) {
        for (Cell myWorker : pgs.getMyWorkers().values()) {
            Map<Position2d, NearestEntity> nearestEntityMap;
            if(myWorker.getPosition().lenShiftSum(DESIRED_BARRACK) < 30) {
                nearestEntityMap = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), Set.of(myWorker.getPosition()), centerMaxPath, true);
            } else {
                nearestEntityMap = GameStateParserDjkstra.shortWideSearch(pgs, Set.of(), Set.of(myWorker.getPosition()), maxPath, true);
            }

            nearestEntityMap.forEach((p, ne) -> {
                pgs.at(p).workersNearby.put(myWorker.position, ne);
            });
        }
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
            if (pgs.at(c).isMineral) {
                counter.incrementAndGet();
            }
        });

        pgs.mineralsAtMapCorner = counter.get();
    }

    static void countWorkersAtMapCorner(ParsedGameState pgs) {
        AtomicInteger counter = new AtomicInteger(0);

        forEachPosition(0, MAP_CORNER_SIZE, 0, MAP_CORNER_SIZE, c -> {
            if (pgs.at(c).isMyWorker()) {
                counter.incrementAndGet();
            }
        });

        pgs.myWorkersAtMapCorner = counter.get();
    }

    static void markupFog(ParsedGameState pgs) {
        Map<EntityType, EntityProperties> entityProperties = pgs.getPlayerView().getEntityProperties();

        int unitSightRange = entityProperties.get(EntityType.BUILDER_UNIT).getSightRange();

        EnumSet<EntityType> unitTypes = EnumSet.of(EntityType.BUILDER_UNIT, EntityType.RANGED_UNIT, EntityType.MELEE_UNIT);

        Set<Position2d> notFog = new HashSet<>();
        List<Position2d> units = Arrays.stream(pgs.getPlayerView().getEntities())
                .filter(e -> e.getPlayerId() != null && e.getPlayerId() == pgs.getPlayerView().getMyId())
                .filter(e -> unitTypes.contains(e.getEntityType()))
                .map(e -> of(e.getPosition()))
                .collect(Collectors.toList());

        Collections.shuffle(units);

        for (Position2d unitPosition : units) {

            if (notFog.contains(unitPosition.shift(unitSightRange, 0))
                    && notFog.contains(unitPosition.shift(-unitSightRange, 0))
                    && notFog.contains(unitPosition.shift(0, unitSightRange))
                    && notFog.contains(unitPosition.shift(0, -unitSightRange))) {
                //unnecessary to iterate

                continue; //
            } else {
                Position2dUtil.iterAllPositionsInRangeInclusive(unitPosition, unitSightRange, notFog::add);
            }
        }


        for (Building building : pgs.findAllMyBuildings()) {
            int sightRange = pgs.getPlayerView().getEntityProperties().get(building.cornerCell.getEntityType()).getSightRange();

            for (Position2d edge : building.getBuildingInnerEdge()) {
                if (notFog.contains(edge.shift(sightRange, 0))
                        && notFog.contains(edge.shift(-sightRange, 0))
                        && notFog.contains(edge.shift(0, sightRange))
                        && notFog.contains(edge.shift(0, -sightRange))) {

                } else {
                    Position2dUtil.iterAllPositionsInRangeInclusive(edge, unitSightRange, notFog::add);
                }
            }
        }

        for (Position2d notFogPosition : notFog) {
            pgs.at(notFogPosition).fog = false;
        }
    }

    static void calculateWorkersAtMiningPositions(ParsedGameState pgs) {
        pgs.workersAtMiningPositions = (int) pgs.myWorkers.values()
                .stream().filter(c -> c.len1MineralsCount > 0)
                .count();

    }


    static void calculateNeighbourMineralsAndWorkers(ParsedGameState pgs) {
        forEachCell(pgs.cells, c -> {
            Position2d p = c.getPosition();

            Position2d[] len1Positions = new Position2d[]{
                    p.shift(0, 1),
                    p.shift(1, 0),
                    p.shift(-1, 0),
                    p.shift(0, -1)};

            int isWorkerCount = 0;
            int isMineralCount = 0;

            for (Position2d len1Position : len1Positions) {
                if (Position2dUtil.isPositionWithinMapBorder(len1Position)) {
                    Cell atl1 = pgs.at(len1Position);

                    if (atl1.isMineral) {
                        isMineralCount++;
                    } else if (atl1.isMyWorker()) {
                        isWorkerCount++;
                    }
                }
            }
            c.len1MyWorkersCount = isWorkerCount;
            c.len1MineralsCount = isMineralCount;

        });
    }


    /**
     * Compute free spaces only when requested
     *
     * @param pgs
     */
    @Deprecated
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
