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
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.codecs.ListCodec;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.CUSTOMIZABLE_SIGN_BLOCK_ENTITY;

public class CustomizableSignBlockEntity extends BlockEntity {

    private boolean isMaster = true;
    private boolean isRendered = true;
    private boolean isInitialized = false;

    private boolean doNotReinitialize = false;

    public AtomicBoolean hasTextureUpdateOccurred = new AtomicBoolean(false); // CLIENT-SIDE ONLY! Indicates whether a texture update has occurred and needs to be processed. Used in BER.

    private BorderProperty borders = new BorderProperty(
            true, true, true, true,
            true, true, true, true
    );

    private BlockPos masterPos;
    private CustomizableSignTextureData textureData = new CustomizableSignTextureData(Background.WHITE, new ArrayList<>());

    private List<BlockPosExtended> signPositionsRelative     = new ArrayList<>();
    private List<BlockPosExtended> signPolePositionsRelative = new ArrayList<>();

    private int rotation = 0;
    private int height = 1;
    private int width = 1;
    private int version = 1; // Version 0 = pre-1.8.0

    @Nullable private UUID editedBy = null;

    // Texture variables
    // These variables are temporary and deleted after the program is closed. It is solely used to reduce the amount of operations it would take to update the textures each render. If it'd be this way, it can easily slow down the game by a lot if there are lots of these signs present.
    @Deprecated
    public List<BaseElement> elements = new ArrayList<>();

    public CustomizableSignBlockEntity(BlockPos pos, BlockState state) {
        super(CUSTOMIZABLE_SIGN_BLOCK_ENTITY, pos, state);

        this.masterPos = new BlockPos(pos);
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

    public boolean isDoNotReinitialize() {
        return doNotReinitialize;
    }
    public void setDoNotReinitialize(boolean doNotReinitialize) {
        this.doNotReinitialize = doNotReinitialize;
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

    // NBT Methods -----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void saveAdditional(@NotNull ValueOutput view) {
        super.saveAdditional(view);
        view.store("sign_positions_relative", new ListCodec<>(BlockPosExtended.CODEC, 0, 10000), signPositionsRelative);
        view.store("sign_pole_positions_relative", new ListCodec<>(BlockPosExtended.CODEC, 0, 10000), signPolePositionsRelative);
        view.store("borders", BorderProperty.CODEC, this.getBorderType());
        view.store("master_pos", BlockPos.CODEC, masterPos);
        view.putBoolean("is_master", isMaster);
        view.putBoolean("is_rendered", isRendered);
        view.putBoolean("is_initialized", isInitialized);
        view.putInt("rotation", rotation);
        view.putInt("width", width);
        view.putInt("height", height);
        view.putInt("version", version);

        view.putString("sign_texture", textureData.toJson().toString());
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput readView) {
        super.loadAdditional(readView);

        // Determined by initialization

        signPositionsRelative = readView.read(
                "sign_positions_relative",
                new ListCodec<>(BlockPosExtended.CODEC, 0, 10000)
        ).orElse(new ArrayList<>());

        signPolePositionsRelative = readView.read(
                "sign_pole_positions_relative",
                new ListCodec<>(BlockPosExtended.CODEC, 0, 10000)
        ).orElse(new ArrayList<>());

        borders = readView.read("borders", BorderProperty.CODEC).orElse(BorderProperty.INSTANCE);
        masterPos = readView.read("master_pos", BlockPos.CODEC).orElse(this.getBlockPos());

        isMaster = readView.getBooleanOr("is_master", true);
        isRendered = readView.getBooleanOr("is_rendered", true);
        isInitialized = readView.getBooleanOr("is_initialized", false);

        width = readView.getIntOr("width", 1);
        height = readView.getIntOr("height", 1);
        version = readView.getIntOr("version", 1);

        // User-customizable

        if (readView.contains("Rotation")) // Old version
            rotation = readView.getIntOr("Rotation", 0);
        else // New version
            rotation = readView.getIntOr("rotation", 0);

        if (readView.contains("SignTexture")) { // Old version
            textureData = CustomizableSignTextureData.fromJson(
                    JsonParser.parseString(
                            readView.getStringOr("SignTexture", "{}")
                    ).getAsJsonObject()
            );
        } else { // New version
            textureData = CustomizableSignTextureData.fromJson(
                    JsonParser.parseString(
                            readView.getStringOr("sign_texture", "{}")
                    ).getAsJsonObject()
            );
        }

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
            BlockPosExtended offset = BlockPosExtended.getOffset(masterPos, pos); // Maybe inverse not necessary
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