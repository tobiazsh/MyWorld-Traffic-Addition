package at.tobiazsh.myworld.traffic_addition.blocks;

import at.tobiazsh.myworld.traffic_addition.block_entities.TriangularSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TriangularSignBlock extends SignBlock {

    private static final MapCodec<TriangularSignBlock> CODEC = simpleCodec(TriangularSignBlock::new);

    private static final VoxelShape SHAPE_N = Block.box(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_W = Block.box(14.5, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.box(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_E = Block.box(0, 0, 0, 1.5, 16, 16);

    public TriangularSignBlock(Properties settings) {
        super(settings, SHAPE_N, SHAPE_E, SHAPE_S, SHAPE_W, SIGN_SHAPE.TRIANGULAR);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TriangularSignBlockEntity(pos, state);
    }
}