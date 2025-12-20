package at.tobiazsh.myworld.traffic_addition.utils.math;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class Coordinates {
    public float x, y, z;
    public Direction direction;

    public Coordinates(float xs, float ys, float zs, @Nullable Direction direction) {
        this.x = xs;
        this.y = ys;
        this.z = zs;
        this.direction = direction;
    }

    /**
     * Returns normalized coordinates in the given direction at the given distance (normalized = starting from 0,0,0)
     * @param distance the distance to add in the given direction
     * @param facing the direction to add the distance to
     * @return the new Coordinates
     */
    public static Coordinates getNormalInDirection(float distance, Direction facing) {
        return switch (facing) {
            case NORTH -> new Coordinates(0, 0, -distance, Direction.NORTH);
            case EAST -> new Coordinates(distance, 0, 0, Direction.EAST);
            case SOUTH -> new Coordinates(0, 0, distance, Direction.SOUTH);
            case WEST -> new Coordinates(-distance, 0, 0, Direction.WEST);
            case UP -> new Coordinates(0, distance, 0, Direction.UP);
            case DOWN -> new Coordinates(0, -distance, 0, Direction.DOWN);
        };
    }

    /**
     * Returns normalized coordinates in the given direction at a distance of 1.0f
     * @param facing the direction to add the distance to
     * @return the new Coordinates
     */
    public static Coordinates getNormalInDirection(Direction facing) {
        return getNormalInDirection(1.0f, facing);
    }
}
