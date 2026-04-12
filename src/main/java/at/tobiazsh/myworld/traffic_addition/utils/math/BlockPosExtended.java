package at.tobiazsh.myworld.traffic_addition.utils.math;


/*
 * @created 21/09/2024 (DD/MM/YYYY) - 23:15
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.utils.StringableObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class BlockPosExtended extends BlockPos implements StringableObject<BlockPosExtended> {

    public static final StreamCodec<ByteBuf, BlockPosExtended> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BlockPosExtended decode(ByteBuf object) {
            int x = object.readInt();
            int y = object.readInt();
            int z = object.readInt();
            return new BlockPosExtended(x, y, z);
        }

        @Override
        public void encode(ByteBuf object, BlockPosExtended object2) {
            object.writeInt(object2.getX());
            object.writeInt(object2.getY());
            object.writeInt(object2.getZ());
        }
    };

    public static final Codec<BlockPosExtended> CODEC = Codec.INT.listOf().comapFlatMap(
            list -> {
                if (list.size() != 3)
                    return DataResult.error(() -> "Invalid BlockPosExtended list size: " + list.size());

                int x = list.get(0);
                int y = list.get(1);
                int z = list.get(2);

                return DataResult.success(new BlockPosExtended(x, y, z));
            },
            pos -> List.of(pos.getX(), pos.getY(), pos.getZ())
    );

    public static final BlockPosExtended INSTANCE = new BlockPosExtended(0, 0, 0);

    public BlockPosExtended(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPosExtended(BlockPos pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPosExtended getOffset(BlockPos from, BlockPos to) {
        int offsetX = to.getX() - from.getX();
        int offsetY = to.getY() - from.getY();
        int offsetZ = to.getZ() - from.getZ();

        return new BlockPosExtended(offsetX, offsetY, offsetZ);
    }

    /**
     * @return The inverse of this BlockPosExtended, meaning all coordinates are negated.
     */
    public BlockPosExtended inverse() {
        return new BlockPosExtended(-this.getX(), -this.getY(), -this.getZ());
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
