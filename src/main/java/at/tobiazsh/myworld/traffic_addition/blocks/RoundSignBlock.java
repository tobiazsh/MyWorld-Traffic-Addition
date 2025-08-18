package at.tobiazsh.myworld.traffic_addition.blocks;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 00:11
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.block_entities.RoundSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RoundSignBlock extends SignBlock {

    private static final MapCodec<RoundSignBlock> CODEC = createCodec(RoundSignBlock::new);

    private static final VoxelShape SHAPE_N = Block.createCuboidShape(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_W = Block.createCuboidShape(14.5, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.createCuboidShape(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_E = Block.createCuboidShape(0, 0, 0, 1.5, 16, 16);

    public RoundSignBlock(Settings settings) {
        super(settings, SHAPE_N, SHAPE_E, SHAPE_S, SHAPE_W);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RoundSignBlockEntity(pos, state);
    }
}
