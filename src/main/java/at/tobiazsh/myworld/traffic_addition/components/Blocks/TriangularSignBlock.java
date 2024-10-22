package at.tobiazsh.myworld.traffic_addition.components.Blocks;

import at.tobiazsh.myworld.traffic_addition.ModVars;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.TriangularSignBlockEntity;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TriangularSignBlock extends SignBlock {

    private static final MapCodec<TriangularSignBlock> CODEC = createCodec(TriangularSignBlock::new);

    // TODO: Somehow render a holder for the sign if it's behind a sign pole
    // TODO: Fuck it and drink a bottle of Vodka

    private static final VoxelShape SHAPE_N = Block.createCuboidShape(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_W = Block.createCuboidShape(14.5, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.createCuboidShape(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_E = Block.createCuboidShape(0, 0, 0, 1.5, 16, 16);

    public TriangularSignBlock(Settings settings) {
        super(settings, SHAPE_N, SHAPE_E, SHAPE_S, SHAPE_W);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TriangularSignBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking() && !world.isClient()) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof TriangularSignBlockEntity) {
                MyWorldTrafficAddition.sendOpenSignSelectionScreenPacket((ServerPlayerEntity) player, pos, ModVars.getSignSelectionEnumInt(ModVars.SIGN_SELECTION_TYPE.TRIANGULAR_UPSIDE_UP));
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}