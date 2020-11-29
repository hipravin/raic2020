package hipravin.model;

import model.PlayerView;

import java.util.Arrays;
import java.util.stream.Stream;

public class ParsedGameState {
    PlayerView playerView;
    Cell[][] cells;

    public Stream<Cell> allCellsAsStream() {
        Stream<Cell> combined = Stream.of();
        for (Cell[] column : cells) {
            combined = Stream.concat(combined, Arrays.stream(column));
        }

        return combined;
    }
}
