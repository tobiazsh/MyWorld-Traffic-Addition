package at.tobiazsh.myworld.traffic_addition.blocks;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignPoleBlock extends BlockWithEntity {

    private static final VoxelShape SHAPE = Block.createCuboidShape(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);

    public static final MapCodec<SignPoleBlock> CODEC = createCodec(SignPoleBlock::new);

    public SignPoleBlock(Settings settings)
    {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        BlockPos blockBelowPos = pos.down(1);

        if(world.getBlockEntity(blockBelowPos) instanceof SignPoleBlockEntity) {
            SignPoleBlockEntity blockEntityBelow = (SignPoleBlockEntity)world.getBlockEntity(blockBelowPos);
            SignPoleBlockEntity thisBlockEntity = (SignPoleBlockEntity)world.getBlockEntity(pos);
            thisBlockEntity.setRotationValue(blockEntityBelow.getRotationValue());
        }
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
    {
        return new SignPoleBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit)
    {
        if(player.isSneaking() && !world.isClient()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof SignPoleBlockEntity signPoleEntity) {
                MyWorldTrafficAddition.sendOpenSignPoleRotationScreenPacket((ServerPlayerEntity) player, pos);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
