package at.tobiazsh.myworld.traffic_addition.blocks;


/*
 * @created 30/08/2024 (DD/MM/YYYY) - 16:00
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.block_entities.OctagonalSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class OctagonalSignBlock extends SignBlock {
    private final MapCodec<OctagonalSignBlock> CODEC = createCodec(OctagonalSignBlock::new);

    private static final VoxelShape SHAPE_N = Block.createCuboidShape(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_E = Block.createCuboidShape(14.5, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.createCuboidShape(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_W = Block.createCuboidShape(0, 0, 0, 1.5, 16, 16);

    public OctagonalSignBlock(Settings settings) {
        super(settings, SHAPE_N, SHAPE_W, SHAPE_S, SHAPE_E, SIGN_SHAPE.OCTAGONAL);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OctagonalSignBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
    }
}
