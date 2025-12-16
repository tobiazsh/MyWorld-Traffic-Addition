package at.tobiazsh.myworld.traffic_addition.blocks;


/*
 * @created 04/09/2024 (DD/MM/YYYY) - 00:11
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.block_entities.RoundSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RoundSignBlock extends SignBlock {

    private static final MapCodec<RoundSignBlock> CODEC = simpleCodec(RoundSignBlock::new);

    private static final VoxelShape SHAPE_N = Block.box(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_W = Block.box(14.5, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.box(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_E = Block.box(0, 0, 0, 1.5, 16, 16);

    public RoundSignBlock(Properties settings) {
        super(settings, SHAPE_N, SHAPE_E, SHAPE_S, SHAPE_W, SIGN_SHAPE.ROUND);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RoundSignBlockEntity(pos, state);
    }
}
