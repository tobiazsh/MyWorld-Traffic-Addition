package at.tobiazsh.myworld.traffic_addition.Utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockPosFloat {
    public float x, y, z;

    public BlockPosFloat(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosFloat(BlockPosFloat pos) {
        this(pos.x, pos.y, pos.z);
    }

    public BlockPosFloat(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Converts a BlockPosFloat to a BlockPos
     * @param bpf The BlockPosFloat to convert
     * @return The converted BlockPos
     */
    public static BlockPos toBlockPos(BlockPosFloat bpf) {
        return new BlockPos((int) bpf.x, (int) bpf.y, (int) bpf.z);
    }

    /**
     * Converts a BlockPos to a BlockPosFloat
     * @param pos The BlockPos to convert
     * @return The converted BlockPosFloat
     */
    public static BlockPosFloat fromBlockPos(BlockPos pos) {
        return new BlockPosFloat(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Offsets the BlockPosFloat to a specific amount in the specified direction
     * @param direction The direction to offset
     * @param amount The amount to offset
     * @return BlockPosFloat
     */
    public BlockPosFloat offset(Direction direction, float amount) {
        return amount == 0 ? this : new BlockPosFloat(x + direction.getOffsetX() * amount, y + direction.getOffsetY() * amount, z + direction.getOffsetZ() * amount);
    }

    /**
     * Offsets the BlockPosFloat to a specific amount to north
     * @param amount The amount to offset to north
     * @return BlockPosFloat
     */
    public BlockPosFloat north(float amount) {
        return this.offset(Direction.NORTH, amount);
    }

    /**
     * Offsets the BlockPosFloat to north by 1
     * @return BlockPosFloat
     */
    public BlockPosFloat north() {
        return this.offset(Direction.NORTH, 1);
    }

    /**
     * Offsets the BlockPosFloat to a specific amount to south
     * @param amount The amount to offset to south
     * @return BlockPosFloat
     */
    public BlockPosFloat south(float amount) {
        return this.offset(Direction.SOUTH, amount);
    }

    /**
     * Offsets the BlockPosFloat to south by 1
     * @return BlockPosFloat
     */
    public BlockPosFloat south() {
        return this.offset(Direction.SOUTH, 1);
    }

    /**
     * Offsets the BlockPosFloat to a specific amount to east
     * @param amount The amount to offset to east
     * @return BlockPosFloat
     */
    public BlockPosFloat east(float amount) {
        return this.offset(Direction.EAST, amount);
    }

    /**
     * Offsets the BlockPosFloat to east by 1
     * @return BlockPosFloat
     */
    public BlockPosFloat east() {
        return this.offset(Direction.EAST, 1);
    }

    /**
     * Offsets the BlockPosFloat to a specific amount to west
     * @param amount The amount to offset to west
     * @return BlockPosFloat
     */
    public BlockPosFloat west(float amount) {
        return this.offset(Direction.WEST, amount);
    }

    /**
     * Offsets the BlockPosFloat to west by 1
     * @return BlockPosFloat
     */
    public BlockPosFloat west() {
        return this.offset(Direction.WEST, 1);
    }

    /**
     * Offsets the BlockPosFloat to a specific amount up
     * @param amount The amount to offset to up
     * @return BlockPosFloat
     */
    public BlockPosFloat up(float amount) {
        return this.offset(Direction.UP, amount);
    }

    /**
     * Offsets the BlockPosFloat up by 1
     * @return BlockPosFloat
     */
    public BlockPosFloat up() {
        return this.offset(Direction.UP, 1);
    }

    /**
     * Offsets the BlockPosFloat to a specific amount down
     * @param amount The amount to offset to down
     * @return BlockPosFloat
     */
    public BlockPosFloat down(float amount) {
        return this.offset(Direction.DOWN, amount);
    }

    /**
     * Offsets the BlockPosFloat down by 1
     * @return BlockPosFloat
     */
    public BlockPosFloat down() {
        return this.offset(Direction.DOWN, 1);
    }

    /**
     * Calculates the distance between to BlockPosFloats'
     * @param pos The other BlockPosFloat
     * @return BlockPosFloat
     */
    public BlockPosFloat calcDistance(BlockPosFloat pos) {
        return new BlockPosFloat(pos.x - x, pos.y - y, pos.z - z);
    }

    /**
     * Calculates the distance between to BlockPosFloats'
     * @param pos1 The first BlockPosFloat
     * @param pos2 The second BlockPosFloat
     * @return BlockPosFloat
     */
    public static BlockPosFloat calcDistance(BlockPosFloat pos1, BlockPosFloat pos2) {
        return pos1.calcDistance(pos2);
    }
}
