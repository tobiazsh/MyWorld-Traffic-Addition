package at.tobiazsh.myworld.traffic_addition.blocks;


/*
 * @created 30/08/2024 (DD/MM/YYYY) - 16:00
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.block_entities.OctagonalSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OctagonalSignBlock extends SignBlock {
    private final MapCodec<OctagonalSignBlock> CODEC = simpleCodec(OctagonalSignBlock::new);

    private static final VoxelShape SHAPE_N = Block.box(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_E = Block.box(14.5, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.box(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_W = Block.box(0, 0, 0, 1.5, 16, 16);

    public OctagonalSignBlock(Properties settings) {
        super(settings, SHAPE_N, SHAPE_W, SHAPE_S, SHAPE_E, SIGN_SHAPE.OCTAGONAL);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OctagonalSignBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }
}
