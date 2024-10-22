package at.tobiazsh.myworld.traffic_addition.Utils;


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

    public static BlockPos getOffset(BlockPos startPos, BlockPos endPos) {
        int offsetX = startPos.getX() - endPos.getX();
        int offsetY = startPos.getY() - endPos.getY();
        int offsetZ = startPos.getZ() - endPos.getZ();

        return new BlockPos(offsetX, offsetY, offsetZ);
    }
}
