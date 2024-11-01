package at.tobiazsh.myworld.traffic_addition.components.Blocks;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 00:26
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CustomizableSignBlock extends BlockWithEntity {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final MapCodec<CustomizableSignBlock> CODEC = createCodec(CustomizableSignBlock::new);

    // Check if there is a sign pole at any corner
    // Render another Sign pole the each time there's air underneath and render another sign pole under that if there's more air underneath
    // Rotate it around the original sign pole
    // Render textures
    // Set default textures (Selectable)
    // Make it so you can write on it with normal sign
    // Font Selection
    // Save everything in the NBT
    // Be done
    // Don't worry
    // Be happy

    private static final VoxelShape SHAPE_E = Block.createCuboidShape(0, 0, 0, 1, 16, 16);
    private static final VoxelShape SHAPE_W = Block.createCuboidShape(15, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.createCuboidShape(0, 0, 0, 16, 16, 1);
    private static final VoxelShape SHAPE_N = Block.createCuboidShape(0, 0, 15, 16, 16, 16);

    public CustomizableSignBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch(state.get(FACING)) {
            case EAST -> { return SHAPE_E; }
            case SOUTH -> { return SHAPE_S; }
            case WEST -> { return SHAPE_W; }
            default -> { return SHAPE_N; }
        }
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomizableSignBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking() && !world.isClient() && ((CustomizableSignBlockEntity)(Objects.requireNonNull(world.getBlockEntity(pos)))).isMaster()) {
            MyWorldTrafficAddition.sendOpenCustomizableSignEditScreenPacket((ServerPlayerEntity) player, pos);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
