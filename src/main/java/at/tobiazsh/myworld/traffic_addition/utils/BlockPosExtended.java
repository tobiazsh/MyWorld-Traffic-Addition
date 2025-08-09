package at.tobiazsh.myworld.traffic_addition.utils;


/*
 * @created 21/09/2024 (DD/MM/YYYY) - 23:15
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import net.minecraft.util.math.BlockPos;

public class BlockPosExtended extends BlockPos {
    public BlockPosExtended(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPosExtended(BlockPos pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPosExtended getOffset(BlockPos from, BlockPos to) {
        int offsetX = from.getX() - to.getX();
        int offsetY = from.getY() - to.getY();
        int offsetZ = from.getZ() - to.getZ();

        return new BlockPosExtended(offsetX, offsetY, offsetZ);
    }

    public BlockPosExtended addOffset(BlockPosExtended offset) {
        return new BlockPosExtended(
            this.getX() + offset.getX(),
            this.getY() + offset.getY(),
            this.getZ() + offset.getZ()
        );
    }

    public static BlockPosExtended addOffset(BlockPosExtended pos, BlockPosExtended offset) {
        return pos.addOffset(offset);
    }
}
