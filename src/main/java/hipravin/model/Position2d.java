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

    public Position2d up() {
        return shift(0, 1);
    }
    public Position2d right() {
        return shift(1, 0);
    }

    public Position2d down() {
        return shift(0, -1);
    }
    public Position2d left() {
        return shift(-1, 0);
    }

    public Position2d halfWayTo(Position2d other) {
        return of((x + other.x) /2 , (y + other.y) /2 );
    }


    public Position2d diag(int n) {
        switch (n % 4 ) {
            case 1 : return diag1();
            case 2 : return diag2();
            case 3 : return diag3();
            case 0 : return diag4();
            default: throw new IllegalStateException("Unexpected value: " + n % 4);
        }
    }

    public Position2d diag1() {
        return shift(1, -1);
    }
    public Position2d diag2() {
        return shift(-1, -1);
    }
    public Position2d diag3() {
        return shift(-1, 1);
    }
    public Position2d diag4() {
        return shift(1, 1);
    }

    public long lenSquare(Position2d other) {
        return square(x - other.x) + square(y - other.y);
    }

    public int lenShiftSum(Position2d other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    public int lenShiftSum(Vec2Int other) {
        return Math.abs(x - other.getX()) + Math.abs(y - other.getY());
    }

    private static long square(long val) {
        return val * val;
    }


    public Vec2Int toVec2dInt() {
        return new Vec2Int(x, y);
    }

    private Position2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
        return "(" + x + "," + y + ")";
    }
}
