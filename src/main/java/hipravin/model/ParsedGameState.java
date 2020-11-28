package hipravin.model;

import model.Entity;
import model.PlayerView;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hipravin.model.Position2d.of;

public class ParsedGameState {
    private PlayerView playerView;
    private int currentTick;

    private Cell[][] cells;

    public static ParsedGameState parse(PlayerView playerView) {
        int mapSize = playerView.getMapSize();

        ParsedGameState parsedGameState = new ParsedGameState();
        parsedGameState.playerView = playerView;

        parsedGameState.cells = emptyCellsOfSize(mapSize);

        for (Entity entity : playerView.getEntities()) {
            switch (entity.getEntityType()) {
                case WALL, HOUSE, BUILDER_BASE, RANGED_BASE, MELEE_BASE, TURRET -> {
                    setCells(parsedGameState.cells, Cell.ofBuilding(entity, playerView));
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
        });

        if(!playerView.isFogOfWar()) {
            forEachCell(parsedGameState.cells, Cell::setVisible);
        } else {
            //TODO: set all is visible according to sight ranges
        }

        return parsedGameState;
    }

    public static void setCells(Cell[][] cells, List<Cell> cellsToSet) {
        cellsToSet.forEach(c -> setCell(cells, c));
    }

    public static void setCell(Cell[][] cells, Cell cell) {
        Position2d p = cell.getPosition();

        cells[p.x][p.y] = cell;
    }

    public static Cell[][] emptyCellsOfSize(int size) {
        Cell[][] cells = new Cell[size][];
        for (int x = 0; x < size; x++) {
            cells[x] = new Cell[size];
        }

        forEachPosition(size,
                p -> cells[p.x][p.y] = Cell.empty(p));

        return cells;
    }

    public Stream<Cell> allCellsAsStream() {
        Stream<Cell> combined = Stream.of();
        for (Cell[] row : cells) {
            combined = Stream.concat(combined, Arrays.stream(row));
        }

        return combined;
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
}
