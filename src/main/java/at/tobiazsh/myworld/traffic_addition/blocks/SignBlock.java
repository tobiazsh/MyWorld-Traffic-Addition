package at.tobiazsh.myworld.traffic_addition.blocks;

import at.tobiazsh.myworld.traffic_addition.block_entities.SignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.block_entities.SignPoleBlockEntity;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.OpenSignSelectionPayload;
import at.tobiazsh.myworld.traffic_addition.utils.Coordinates;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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

public abstract class SignBlock extends BlockWithEntity {

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    private final VoxelShape SHAPE_N;
    private final VoxelShape SHAPE_E;
    private final VoxelShape SHAPE_S;
    private final VoxelShape SHAPE_W;
    public final SIGN_SHAPE shape;

    public SignBlock(Settings settings, VoxelShape vn, VoxelShape ve, VoxelShape vs, VoxelShape vw, SIGN_SHAPE shape) {
        super(settings);

        SHAPE_N = vn;
        SHAPE_E = ve;
        SHAPE_S = vs;
        SHAPE_W = vw;

        this.shape = shape;
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
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        BlockPos blockBehindPos = getBehindPos(pos, state);
        if(world.getBlockEntity(blockBehindPos) instanceof SignPoleBlockEntity blockEntityBehind) {
            ((SignBlockEntity) world.getBlockEntity(pos)).setRotation(blockEntityBehind.getRotationValue());
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && !player.isSneaking())
            return ActionResult.PASS;

        ServerPlayNetworking.send(
                (ServerPlayerEntity) player,
                new OpenSignSelectionPayload(
                        pos,
                        getSignSelectionEnumInt(this.shape)
                )
        );

        return ActionResult.SUCCESS;
    }

    public static BlockPos getBehindPos(BlockPos pos, BlockState state) {
        switch(state.get(FACING)) {
            case EAST -> { return pos.west(); }
            case SOUTH -> { return pos.north(); }
            case WEST -> { return pos.east(); }
            default -> { return pos.south(); }
        }
    }

    public Coordinates getBackMovementCoordinates(BlockState state) {
        Coordinates backstepCoords;
        switch (state.get(FACING)) {
            case EAST -> backstepCoords = new Coordinates(-1.55f, 0f, 0f, Direction.EAST);
            case SOUTH -> backstepCoords = new Coordinates(0f, 0f, -1.55f, Direction.SOUTH);
            case WEST -> backstepCoords = new Coordinates(1.55f, 0f, 0f, Direction.WEST);
            default -> backstepCoords = new Coordinates(0f, 0f, .55f, Direction.NORTH);
        }

        return backstepCoords;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    public static int getSignSelectionEnumInt (SIGN_SHAPE type) {
        switch (type) {
            case TRIANGULAR_UPSIDE_UP -> { return 0; }
            case TRIANGULAR_UPSIDE_DOWN -> { return 1; }
            case OCTAGONAL -> { return 3; }
            case RECT_SMALL -> { return 4; }
            case RECT_MEDIUM -> { return 5; }
            case RECT_LARGE -> { return 6; }
            case RECT_STRETCH_SMALL -> { return 7; }
            case RECT_STRETCH_MEDIUM -> { return 8; }
            case RECT_STRETCH_LARGE -> { return 9; }
            case SQUARE_TURN_45 -> { return 10; }
            default -> { return 2; }
        }
    }

    public static SIGN_SHAPE getSignSelectionEnum (int num) {
        switch (num) {
            case 0 -> { return SIGN_SHAPE.TRIANGULAR_UPSIDE_UP; }
            case 1 -> { return SIGN_SHAPE.TRIANGULAR_UPSIDE_DOWN; }
            case 3 -> { return SIGN_SHAPE.OCTAGONAL; }
            case 4 -> { return SIGN_SHAPE.RECT_SMALL; }
            case 5 -> { return SIGN_SHAPE.RECT_MEDIUM; }
            case 6 -> { return SIGN_SHAPE.RECT_LARGE; }
            case 7 -> { return SIGN_SHAPE.RECT_STRETCH_SMALL; }
            case 8 -> { return SIGN_SHAPE.RECT_STRETCH_MEDIUM; }
            case 9 -> { return SIGN_SHAPE.RECT_STRETCH_LARGE; }
            case 10 -> { return SIGN_SHAPE.SQUARE_TURN_45; }
            default -> { return SIGN_SHAPE.ROUND; }
        }
    }

    public static SIGN_SHAPE getSignSelectionEnumFromString (String str) {
        switch (str) {
            case "triangular" -> { return SIGN_SHAPE.TRIANGULAR_UPSIDE_UP; }
            case "upside_down_triangular" -> { return SIGN_SHAPE.TRIANGULAR_UPSIDE_DOWN; }
            case "octagonal" -> { return SIGN_SHAPE.OCTAGONAL; }
            case "rect_small" -> { return SIGN_SHAPE.RECT_SMALL; }
            case "rect_medium" -> { return SIGN_SHAPE.RECT_MEDIUM; }
            case "rect_large" -> { return SIGN_SHAPE.RECT_LARGE; }
            case "rect_stretch_small" -> { return SIGN_SHAPE.RECT_STRETCH_SMALL; }
            case "rect_stretch_medium" -> { return SIGN_SHAPE.RECT_STRETCH_MEDIUM; }
            case "rect_stretch_large" -> { return SIGN_SHAPE.RECT_STRETCH_LARGE; }
            case "square_turn_45" -> { return SIGN_SHAPE.SQUARE_TURN_45; }
            default -> { return SIGN_SHAPE.ROUND; }
        }
    }

    public enum SIGN_SHAPE {
        TRIANGULAR_UPSIDE_UP,
        TRIANGULAR_UPSIDE_DOWN,
        ROUND,
        OCTAGONAL,
        RECT_SMALL,
        RECT_MEDIUM,
        RECT_LARGE,
        RECT_STRETCH_SMALL,
        RECT_STRETCH_MEDIUM,
        RECT_STRETCH_LARGE,
        SQUARE_TURN_45
    }

}
