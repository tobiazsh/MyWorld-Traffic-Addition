package at.tobiazsh.myworld.traffic_addition.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Contract;

public class DirectionUtils {

    /**
     * Calculates the rotation angle based on the facing direction.
     */
    @Contract(pure = true)
    public static int getFacingRotation(Direction facingDirection) {
        switch (facingDirection) {
            case SOUTH -> { return 180; }
            case WEST -> { return 90; }
            case EAST -> { return 270; }
            default -> { return 0; }
        }
    }

    /**
     * Returns the direction to the right side of the given direction.
     */
    @Contract(pure = true)
    public static Direction getRightSideDirection(Direction dir) {
        switch (dir) {
            case EAST -> { return Direction.SOUTH; }
            case SOUTH -> { return Direction.WEST; }
            case WEST -> { return Direction.NORTH; }
            default -> { return Direction.EAST; }
        }
    }

    /**
     * Returns the position at the given direction and offset from the given position in given direction.
     */
    public static BlockPos blockPosInDirection(Direction dir, BlockPos pos, int offset) {
        if (offset == 0) return pos;

        switch (dir) {
            case EAST -> { return pos.east(offset); }
            case SOUTH -> { return pos.south(offset); }
            case WEST -> { return pos.west(offset); }
            default -> { return pos.north(offset); }
        }
    }
}
