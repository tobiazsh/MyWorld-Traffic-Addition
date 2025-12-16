package at.tobiazsh.myworld.traffic_addition.blocks;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 00:26
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CustomizableSignBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<CustomizableSignBlock> CODEC = simpleCodec(CustomizableSignBlock::new);

    // Check if there is a sign pole at any corner
    // Render another Sign pole each time there's air underneath and render another sign pole under that if there's more air underneath
    // Rotate it around the original sign pole
    // Render textures
    // Set default textures (Selectable)
    // Make it so you can write on it with normal sign
    // Font Selection
    // Save everything in the NBT
    // Be done
    // Don't worry
    // Be happy

    private static final VoxelShape SHAPE_E = Block.box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape SHAPE_W = Block.box(15, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_S = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape SHAPE_N = Block.box(0, 0, 15, 16, 16, 16);

    public CustomizableSignBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch(state.getValue(FACING)) {
            case EAST -> { return SHAPE_E; }
            case SOUTH -> { return SHAPE_S; }
            case WEST -> { return SHAPE_W; }
            default -> { return SHAPE_N; }
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomizableSignBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() && !world.isClientSide()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (!(blockEntity instanceof CustomizableSignBlockEntity)) {
                return InteractionResult.FAIL;
            }

            MyWorldTrafficAddition.sendOpenCustomizableSignEditScreenPacket((ServerPlayer) player, ((CustomizableSignBlockEntity) blockEntity).getMasterPos());

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
