package at.tobiazsh.myworld.traffic_addition.block_entities;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 00:30
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.CUSTOMIZABLE_SIGN_BLOCK_ENTITY;
import static at.tobiazsh.myworld.traffic_addition.utils.DirectionUtils.blockPosInDirection;

public class CustomizableSignBlockEntity extends BlockEntity {

    private boolean isMaster = true;
    private boolean isRendered = true;
    private boolean isInitialized = false;

    public AtomicBoolean hasTextureUpdateOccurred = new AtomicBoolean(false); // CLIENT-SIDE ONLY! Indicates whether a texture update has occurred and needs to be processed. Used in BER.

    private BorderProperty borders = new BorderProperty(
            true, true, true, true,
            true, true, true, true
    );

    private BlockPosExtended masterPos;
    private String signPoleDistances = "";
    private String signDistances = "";
    private CustomizableSignTextureData textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>());

    private int rotation = 0;
    private int height = 1;
    private int width = 1;

    @Nullable private UUID editedBy = null;

    // Texture variables
    // These variables are temporary and deleted after the program is closed. It is solely used to reduce the amount of operations it would take to update the textures each render. If it'd be this way, it can easily slow down the game by a lot if there are lots of these signs present.
    @Deprecated
    public List<BaseElement> elements = new ArrayList<>();

    public CustomizableSignBlockEntity(BlockPos pos, BlockState state) {
        super(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, pos, state);

        this.masterPos = new BlockPosExtended(pos);
    }



    public void updateTextureVars() {
        if (!isMaster) return;
        if (this.level == null) return;
        hasTextureUpdateOccurred.set(true);
    }

    // GETTERS / SETTERS -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    /**
     * Sets the received block data on the server. (Client -> Server)
     * @param json The block data
     * @param player The player who sent it
     */
    public static void setTransmittedTexture(String json, ServerPlayer player) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if (!jsonObject.has("texture")) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because json data does not contain the texture data! Received Data: {}", json);
            return;
        }

        String texture = jsonObject.get("texture").toString();

        if (!jsonObject.has("blockEntityPosition")) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because json data does not contain the block entity position data! Received Data: {}", json);
            return;
        }

        JsonObject blockEntityData = jsonObject.getAsJsonObject("blockEntityPosition");

        if (!blockEntityData.has("x") || !blockEntityData.has("y") || !blockEntityData.has("z")) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because json data does not contain intact block entity position data! Received Data: {}", json);
            return;
        }

        BlockPos pos = new BlockPos(blockEntityData.get("x").getAsInt(), blockEntityData.get("y").getAsInt(), blockEntityData.get("z").getAsInt());

        BlockEntity blockEntity = player.level().getBlockEntity(pos);

        if (!(blockEntity instanceof CustomizableSignBlockEntity)) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't set transmitted texture because block entity at position {} is not a CustomizableSignBlockEntity!", pos);
            return;
        }

        Objects.requireNonNull(player.level().getServer()).execute(() -> {
            ((CustomizableSignBlockEntity) blockEntity)
                    .setTextureData(
                            CustomizableSignTextureData.fromJson(
                                    JsonParser.parseString(texture).getAsJsonObject()
                            )
                    );
        });

        ((CustomizableSignBlockEntity) blockEntity).updateTextureVars();
    }


    public int getHeight() {
        return this.height;
    }
    public void setHeight(int height) {
        this.height = height;
        updateGame();
    }


    public int getWidth() {
        return this.width;
    }
    public void setWidth(int width) {
        this.width = width;
        updateGame();
    }


    public int getRotation() {
        return this.rotation;
    }
    public void setRotation(int rotation) {
        this.rotation = rotation;
        updateGame();
    }


    /**
     * Returns the raw base64-encoded string, which holds the sign distances as a byte array, which are the sign
     * distances in a {@code List<String>}. Each String in that list is a stringed {@link BlockPosExtended}.
     * ...
     * (yeah, don't ask how I came up with that idea. I myself have no idea...)
     */
    public String getSignDistancesString() {
        return signDistances;
    }
    public void setSignDistances(byte[] signDistances) {
        this.signDistances = Base64.getEncoder().encodeToString(signDistances);
        updateGame();
    }


    public String getSignPoleDistancesString() {
        return signPoleDistances;
    }
    public void setSignPoleDistances(byte[] signPolePositions) {
        this.signPoleDistances = Base64.getEncoder().encodeToString(signPolePositions);
        updateGame();
    }


    public boolean isRendering() {
        return isRendered;
    }
    public void setRendered(boolean render) {
        isRendered = render;
        updateGame();
    }


    public BorderProperty getBorderType() {
        return borders;
    }
    public void setBorderType(BorderProperty borders) {
        this.borders = borders;
        updateGame();
    }


    public boolean isMaster() {
        return isMaster;
    }
    public void setMaster(boolean value) {
        this.isMaster = value;
        updateGame();
    }


    public BlockPos getMasterPos() {
        return masterPos;
    }
    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = new BlockPosExtended(masterPos);
        updateGame();
    }


    public CustomizableSignTextureData getTextureData() {
        return this.textureData;
    }
    public void setTextureData(CustomizableSignTextureData customizableSignTextureData) {
        this.textureData = customizableSignTextureData;
        updateGame();
    }

    public @Nullable UUID getEditedBy() {
        return editedBy;
    }
    public void setEditedBy(@NotNull UUID editedBy) { // Only nullable if reset, for reset, you should use the actual reset method below
        this.editedBy = editedBy;
    }
    public void resetEditedBy() {
        this.editedBy = null;
    }


    // Other Methods -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private BlockPosExtended deconstructMasterPosString(String posStr) {
        String[] posList = posStr.split("%");
        return new BlockPosExtended(Integer.parseInt(posList[0]), Integer.parseInt(posList[1]), Integer.parseInt(posList[2]));
    }


    // NBT Methods -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void nbtWrite(ValueOutput view) {
        view.putString("Borders", borders.toObjectString());
        view.putBoolean("IsMaster", isMaster);
        view.putString("MasterPos", masterPos.toObjectString());
        view.putString("SignPoleDistances", signPoleDistances);
        view.putBoolean("RenderingState", isRendered);
        view.putString("SignDistances", signDistances);
        view.putInt("Rotation", rotation);
        view.putInt("Width", width);
        view.putInt("Height", height);
        view.putBoolean("IsInitialized", isInitialized);

        view.putString("SignTexture", textureData.toJson().toString());
    }


    @Override
    protected void saveAdditional(@NotNull ValueOutput view) {
        super.saveAdditional(view);
        nbtWrite(view);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput readView) {
        super.loadAdditional(readView);

        if (readView.getString("Borders").isEmpty() && !OptionalUtils.getOrDefault("BorderModelPath", readView::getString, "", "CustomizableSignBlockEntity.BorderModelPath").isBlank()) { // If old border string is present, convert it to new BorderProperty
            this.borders = convertOldBorderStringToBorderProperty(OptionalUtils.getOrDefault("BorderModelPath", readView::getString, "", "CustomizableSignBlockEntity.BorderModelPath"), "customizable_sign_block");
        } else {
            this.borders = BorderProperty.INSTANCE.fromString(OptionalUtils.getOrDefault("Borders", readView::getString, BorderProperty.DEFAULT, "CustomizableSignBlockEntity.Borders"));
        } // CONVERSION TO NEW VERSION

        if (readView.getString("SignPoleDistances").isEmpty() && !OptionalUtils.getOrDefault("SignPolePositions", readView::getString, "", "CustomizableSignBlockEntity.SignPolePositions").isBlank()) { // If old sign pole positions are present, convert them to new distances
            this.signPoleDistances = convertPositionsToDistances(OptionalUtils.getOrDefault("SignPolePositions", readView::getString, "", "CustomizableSignBlockEntity.SignPolePositions"), masterPos);
        } else {
            this.signPoleDistances = OptionalUtils.getOrDefault("SignPoleDistances", readView::getString, "", "CustomizableSignBlockEntity.SignPoleDistances");
        } // CONVERSION TO NEW VERSION

        if (readView.getString("SignDistances").isEmpty() && !OptionalUtils.getOrDefault("SignPositions", readView::getString, "", "CustomizableSignBlockEntity.SignDistances").isBlank()) { // If old sign positions are present, convert them to new distances
            this.signDistances = convertPositionsToDistances(OptionalUtils.getOrDefault("SignPositions", readView::getString, "", "CustomizableSignBlockEntity.SignPositions"), masterPos);
        } else {
            this.signDistances = OptionalUtils.getOrDefault("SignDistances", readView::getString, "", "CustomizableSignBlockEntity.SignDistances");
        } // CONVERSION TO NEW VERSION


        String masterPosString = OptionalUtils.getOrDefault("MasterPos", readView::getString, "", "CustomizableSignBlockEntity.MasterPos");

        if (masterStringHasOldFormat(masterPosString)) { // If old master position string is present, convert it to new format
            masterPos = deconstructMasterPosString(masterPosString);
        } else {
            masterPos = BlockPosExtended.INSTANCE.fromString(masterPosString);
        } // CONVERSION TO NEW VERSION

        isMaster = readView.getBooleanOr("IsMaster", true);
        isRendered = readView.getBooleanOr("RenderingState", true);
        isInitialized = readView.getBooleanOr("IsInitialized", false);

        textureData = CustomizableSignTextureData.fromJson(
                JsonParser.parseString(
                        OptionalUtils.getOrDefault("SignTexture", readView::getString, "{}", "CustomizableSignBlockEntity.SignTexture")
                ).getAsJsonObject()
        );

        // Convert old texture JSON to new version if necessary
        // LEFT BLANK... NEW VERSION MAY NOT NEED IT

        rotation = OptionalUtils.getOrDefault("Rotation", readView::getInt, 0, "CustomizableSignBlockEntity.Rotation");
        width = OptionalUtils.getOrDefault("Width", readView::getInt, 1, "CustomizableSignBlockEntity.Width");
        height = OptionalUtils.getOrDefault("Height", readView::getInt, 1, "CustomizableSignBlockEntity.Height");

        updateTextureVars();
    }

    @Override
    public @Nullable Packet<@NotNull ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        /* When this block entity is removed (block destroyed / replaced), tell all tracking clients
         * to clear their stale renderer cache for this position. This must be done server-side because
         * the BlockEntityRenderer (client-only) cannot be accessed from here. */
        if (this.level != null && !this.level.isClientSide()) {
            MyWorldTrafficAddition.sendClearCSBETextureRenderStatePacket(
                    (net.minecraft.server.level.ServerLevel) this.level,
                    this.getBlockPos()
            );
        }
    }

    // Everything else -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns a BorderProperty object that represents the bounding box of the CustomizableSignBlockEntity based on the surrounding blocks.
     * Does extensive neighbour-checking to determine which borders and corners should be present.
     */
    public static BorderProperty getBorderListBoundingBased(BlockPos position, Level world) {
        Direction facing = DirectionUtils.getFacing(position, world);
        Direction rightSideDirection = DirectionUtils.getRightSideDirection(facing.getOpposite());

        boolean up = false;
        boolean right = false;
        boolean down = false;
        boolean left = false;

        // Corners
        boolean upRight = false;
        boolean upLeft = false;
        boolean downRight = false;
        boolean downLeft = false;

        boolean upIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(position.above(), world, facing);
        boolean rightIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(blockPosInDirection(rightSideDirection, position, 1), world, facing);
        boolean downIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(position.below(), world, facing);
        boolean leftIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(blockPosInDirection(rightSideDirection.getOpposite(), position, 1), world, facing);

        boolean downLeftIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(blockPosInDirection(rightSideDirection.getOpposite(), position, 1).below(), world, facing);  // Check if down left is a CustomizableSignBlockEntity
        boolean downRightIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(blockPosInDirection(rightSideDirection, position, 1).below(), world, facing);               // Check if down right is a CustomizableSignBlockEntity
        boolean upLeftIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(blockPosInDirection(rightSideDirection.getOpposite(), position, 1).above(), world, facing);      // Check if up left is a CustomizableSignBlockEntity
        boolean upRightIsCustomizableBlockEntity = isUsableCustomizableSignBlockEntity(blockPosInDirection(rightSideDirection, position, 1).above(), world, facing);                   // Check if up right is a CustomizableSignBlockEntity

        if (!upIsCustomizableBlockEntity) {
            up = true;
            upRight = true;
            upLeft = true;
        }

        if (!rightIsCustomizableBlockEntity) {
            right = true;
            upRight = true;
            downRight = true;
        }

        if (!downIsCustomizableBlockEntity) {
            down = true;
            downRight = true;
            downLeft = true;
        }

        if (!leftIsCustomizableBlockEntity) {
            left = true;
            upLeft = true;
            downLeft = true;
        }


        // Special corner cases

        // Up-Left corner
        if (
                leftIsCustomizableBlockEntity && upIsCustomizableBlockEntity &&
                !upLeftIsCustomizableBlockEntity
        ) {
            upLeft = true;
        }


        // Up-Right corner
        if (
                rightIsCustomizableBlockEntity && upIsCustomizableBlockEntity &&
                !upRightIsCustomizableBlockEntity
        ) {
            upRight = true;
        }


        // Down-Left corner
        if (
                leftIsCustomizableBlockEntity && downIsCustomizableBlockEntity &&
                !downLeftIsCustomizableBlockEntity // Check if down left is not a CustomizableSignBlockEntity
        ) {
            downLeft = true;
        }


        // Down-Right corner
        if (
                rightIsCustomizableBlockEntity && downIsCustomizableBlockEntity &&
                !downRightIsCustomizableBlockEntity // Check if down right is not a CustomizableSignBlockEntity
        ) {
            downRight = true;
        }

        return new BorderProperty(
                up, right, down, left,
                upRight, upLeft, downRight, downLeft
        );
    }

    public static List<BlockPos> deconstructBlockPosListString(String blockPosListString) {
        List<String> blockPoses;
        List<BlockPos> blockPosList = new ArrayList<>();

        blockPoses = List.of(blockPosListString.split("%"));

        for (String blockPos : blockPoses) {
            List<String> blockCoordinates;

            blockCoordinates = List.of(blockPos.split("\\?"));

            BlockPos pos = new BlockPos(Integer.parseInt(blockCoordinates.get(0)), Integer.parseInt(blockCoordinates.get(1)), Integer.parseInt(blockCoordinates.get(2)));

            blockPosList.add(pos);
        }

        return blockPosList;
    }

    private void updateGame() {
        setChanged();

        if (this.getLevel() == null) return; // Cannot update if world is null

        this.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(null, this.getBlockState()));
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean initialized) {
		isInitialized = initialized;
	}

    public Direction getFacing() {
        return this.getBlockState().getValue(CustomizableSignBlock.FACING);
    }

    /**
     * Checks if block entity at given position is a usable CustomizableSignBlockEntity. Usable implies that it's not locked.
     * The check for locking and the locking itself will be implemented in the future.
     * Right now, it only checks if the block entity is an instance of CustomizableSignBlockEntity and if the block is facing the same direction.
     */
    public static boolean isUsableCustomizableSignBlockEntity(BlockPos pos, Level world, Direction shouldFace) {
        return
            world.getBlockEntity(pos) instanceof CustomizableSignBlockEntity && shouldFace == ((CustomizableSignBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getFacing();
    }







    // CONVERSION METHODS -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Converts the old border string format to a BorderProperty object.
     *
     * @param borderString The old border string format including the name prefix. For example: "customizable_sign_border_top" or "sign_border_not_right".
     * @param name The name prefix that is used in the border string. For example: "customizable_sign" or "sign".
     * @return A BorderProperty object representing the border configuration.
     */
    private static BorderProperty convertOldBorderStringToBorderProperty(String borderString, String name) {
        String withoutName = borderString.replaceFirst(name + "_border_", ""); // Counts the number of underscores in the name and removes the prefix including the underscore

        boolean left = false;
        boolean right = false;
        boolean up = false;
        boolean down = false;

        switch (withoutName) {
            case "top" -> up = true;
            case "right" -> right = true;
            case "bottom" -> down = true;
            case "left" -> left = true;

            case "not_right" -> {
                up = true;
                down = true;
                left = true;
            }

            case "not_left" -> {
                up = true;
                down = true;
                right = true;
            }

            case "not_top" -> {
                right = true;
                down = true;
                left = true;
            }

            case "not_bottom" -> {
                up = true;
                right = true;
                left = true;
            }


            case "top_bottom" -> {
                up = true;
                down = true;
            }

            case "left_right" -> {
                right = true;
                left = true;
            }

            case "bottom_left" -> {
                down = true;
                left = true;
            }

            case "bottom_right" -> {
                down = true;
                right = true;
            }

            case "top_left" -> {
                up = true;
                left = true;
            }

            case "top_right" -> {
                up = true;
                right = true;
            }

            case "all" -> {
                left = true;
                right = true;
                up = true;
                down = true;
            }

            default -> {} // No borders are present
        }

        return new BorderProperty(
                up, right, down, left,
                false, false, false, false // No information about corners. Solution: Re-initialize sign in game or live with it. I am too lazy to implement this right now since FAPI fucked up my whole codebase.
        );
    }

    private static String convertPositionsToDistances(String oldPositions, BlockPos masterPos) {
        List<BlockPos> positions = CustomizableSignBlockEntity.deconstructBlockPosListString(oldPositions);
        List<String> distances = new ArrayList<>();

        for (BlockPos pos : positions) {
            BlockPosExtended offset = BlockPosExtended.getOffset(masterPos, pos);
            distances.add(offset.toObjectString());
        }

        String distanceBlockPosStrings = "";

        try {
            distanceBlockPosStrings = Base64.getEncoder().encodeToString(ListUtils.toByteArray(distances));
        } catch (IOException e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to convert old pole positions to distances! Error: {}", e.getMessage());
        }

        return distanceBlockPosStrings;
    }

    private static boolean masterStringHasOldFormat(String masterString) {
        return masterString.contains("%");
    }
}
