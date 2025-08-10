package at.tobiazsh.myworld.traffic_addition.utils;


/*
 * @created 21/09/2024 (DD/MM/YYYY) - 23:15
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import net.minecraft.util.math.BlockPos;

public class BlockPosExtended extends BlockPos implements StringableObject<BlockPosExtended> {

    public static final BlockPosExtended INSTANCE = new BlockPosExtended(0, 0, 0);

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

    @Override
    public String toObjectString() {
        return String.format("BlockPosExtended{x=%s, y=%s, z=%s}", this.getX(), this.getY(), this.getZ());
    }

    @Override
    public BlockPosExtended fromString(String str) {
        String trimmed = str
                .replace("BlockPosExtended{", "")
                .replace("}", "")
                .replace(" ", "");

        String[] parts = trimmed.split(",");

        if (parts.length != 3)
            throw new IllegalArgumentException("Invalid BlockPosExtended string format: " + str);

        int x = Integer.parseInt(parts[0].replace("x=", ""));
        int y = Integer.parseInt(parts[1].replace("y=", ""));
        int z = Integer.parseInt(parts[2].replace("z=", ""));

        return new BlockPosExtended(x, y, z);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.getX(), this.getY(), this.getZ());
    }

}
