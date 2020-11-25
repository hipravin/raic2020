package hipravin.raic.alg;


import java.util.Arrays;

public final class VectorMath {
    private VectorMath() {
    }

    public static final Vector2d ZERO_VECTOR = new Vector2d(0, 0);

    public static Vector2d sum(Vector2d v1, Vector2d v2) {
        return new Vector2d(v1.getDx() + v2.getDx(), v1.getDy() + v2.getDy());
    }

    public static Vector2d sum(Vector2d... vectors) {
        double sumdx = Arrays.stream(vectors)
                .mapToDouble(Vector2d::getDx)
                .sum();
        double sumdy = Arrays.stream(vectors)
                .mapToDouble(Vector2d::getDy)
                .sum();

        return new Vector2d(sumdx, sumdy);
    }

    public static Vector2d negate(Vector2d v) {
        return new Vector2d(-v.getDx(), -v.getDy());
    }

    public static Vector2d mul(Vector2d v, double scale) {
        return new Vector2d(v.getDx() * scale, v.getDy() * scale);
    }

    public static Vector2d divide(Vector2d v, double scale) {
        if(Math.abs(scale) < MathConstants.ZERO_DOUBLE) {
            return v;
        } else {
            return new Vector2d(v.getDx() / scale, v.getDy() / scale);
        }
    }

    public static Point2d move(Point2d p, Vector2d v) {
        return new Point2d(p.getX() + v.getDx(), p.getY() + v.getDy());
    }

    public static double lenght(Vector2d v) {
        return Math.hypot(v.getDx(), v.getDy());
    }

    public static Vector2d normalize(Vector2d v) {
        double len = lenght(v);
        if (len < MathConstants.ZERO_DOUBLE) {
            return v;
        }

        Vector2d res = mul(v, 1 / len);
        return res;
    }

    public static Vector2d vectorBetweenPoints(Point2d p1, Point2d p2) {
        return new Vector2d(p2.getX() - p1.getX(), p2.getY() - p1.getY());
    }
}
