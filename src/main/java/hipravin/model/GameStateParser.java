package hipravin.model;

import model.Entity;
import model.EntityType;
import model.PlayerView;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hipravin.model.Position2d.of;
import static hipravin.model.Position2dUtil.squareInclusiveCornerStream;

public abstract class GameStateParser {
    public static ParsedGameState parse(PlayerView playerView) {
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

            if(c.isMyEntity && c.getEntityType() == EntityType.BUILDER_UNIT) {
                parsedGameState.myWorkers.put(c.entityId, c);
            }
        });

        Arrays.stream(playerView.getEntities()).forEach(e -> parsedGameState.entityIdToCell.put(e.getId(), parsedGameState.at(e.getPosition())));

        if (!playerView.isFogOfWar()) {
            forEachCell(parsedGameState.cells, Cell::setVisible);
        } else {
            //TODO: set all is visible according to sight ranges
        }

        parsedGameState.population = Population.of(playerView);

        calculateNeighbourMineralsAndWorkers(parsedGameState);

        GameStateParserDjkstra.computeMyNonUniqueNearestWorkers(parsedGameState);
        GameStateParserDjkstra.computeClosestMinerals(parsedGameState);

        computeProducingBuildingEdge(parsedGameState);
        computeFreeSpaces(parsedGameState.cells);
        computeBuildingEdgeFreeCells(parsedGameState);
        return parsedGameState;
    }

    static void calculateNeighbourMineralsAndWorkers(ParsedGameState parsedGameState) {
        parsedGameState.allCellsAsStream().forEach(eachCell -> {
            eachCell.len1MineralsCount = Position2dUtil.upRightLeftDown(eachCell.getPosition()).map(p -> parsedGameState.at(p))
                    .filter(mc-> mc.isMineral)
                    .count();

            eachCell.len1MyWorkersCount = Position2dUtil.upRightLeftDown(eachCell.getPosition()).map(p -> parsedGameState.at(p))
                    .filter(wc-> wc.isMyWorker())
                    .count();
        });
    }


    static void computeFreeSpaces(Cell[][] cells) {
        //so far compute completely free and free with our units
        //can be optimised, but not sure it's reasonable, requires profiling
        forEachPosition(cells.length, corner -> {
            Cell cornerCell = getCell(cells, corner);
            //for each size 2-5
            for (int size = Cell.MIN_FP_SIZE; size <= Cell.MAX_FP_SIZE; size++) {
                Optional<Stream<Position2d>> squareStream = squareInclusiveCornerStream(corner, size);
                if(squareStream.isEmpty()) {
                    break;
                }

                boolean onlyContainOurUnits = false;
                boolean isEmpty = squareStream.get()
                        .allMatch(p -> getCell(cells, p).isEmpty());
                if(isEmpty) {
                    FreeSpace free = FreeSpace.completelyFree(size);
                    cornerCell.setFreeSpace(size, free);
                } else {
                    onlyContainOurUnits = squareInclusiveCornerStream(corner, size).get()
                            .allMatch(p -> getCell(cells, p).testOr(Cell::isEmpty, Cell::isMyUnit));
                    if(onlyContainOurUnits) {
                        FreeSpace unitFree = FreeSpace.freeButContainOutUnits(size);
                        cornerCell.setFreeSpace(size, unitFree);
                    }
                }

                if(!isEmpty && !onlyContainOurUnits) {
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

    private GameStateParser() {
    }
}
