package hipravin.model;

import model.Vec2Int;

import java.util.Objects;

public final class Position2d {
    public final int x;
    public final int y;

    public static Position2d of(int x, int y) {
        return new Position2d(x, y);
    }

    public static Position2d of(Vec2Int vec2Int) {
        return of(vec2Int.getX(), vec2Int.getY());
    }

    public Position2d shift(int xshift, int yshift) {
        return of(x + xshift, y + yshift);
    }

    public Position2d right() {
        return shift(1, 0);
    }
    public Position2d down() {
        return shift(0, 1);
    }

    public Vec2Int toVec2dInt() {
        return new Vec2Int(x, y);
    }

    private Position2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position2d that = (Position2d) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ","  + y + ")";
    }
}
