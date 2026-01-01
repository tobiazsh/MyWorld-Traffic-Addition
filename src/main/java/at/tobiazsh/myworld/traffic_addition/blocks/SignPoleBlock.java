package at.tobiazsh.myworld.traffic_addition.blocks;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SignPoleBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Block.box(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);

    public static final MapCodec<SignPoleBlock> CODEC = simpleCodec(SignPoleBlock::new);

    public SignPoleBlock(Properties settings)
    {
        super(settings);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        BlockPos blockBelowPos = pos.below(1);

        if(world.getBlockEntity(blockBelowPos) instanceof SignPoleBlockEntity blockEntityBelow) {
            SignPoleBlockEntity thisBlockEntity = (SignPoleBlockEntity)world.getBlockEntity(pos);

            if (thisBlockEntity == null) {
                MyWorldTrafficAddition.LOGGER.error("Tried to set rotationValue on invalid SignPoleBlockEntity!");
                return;
            }

            thisBlockEntity.setRotationValue(blockEntityBelow.getRotationValue());
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SignPoleBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context){
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if(player.isShiftKeyDown() && !world.isClientSide()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof SignPoleBlockEntity) {
                MyWorldTrafficAddition.sendOpenSignPoleRotationScreenPacket((ServerPlayer) player, pos);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
