package at.tobiazsh.myworld.traffic_addition.block_entities;


/*
 * @created 07/09/2024 (DD/MM/YYYY) - 00:30
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.payload.custom_network.SetCustomizableSignTexturePayload;
import at.tobiazsh.myworld.traffic_addition.utils.*;
import at.tobiazsh.myworld.traffic_addition.blocks.CustomizableSignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.math.BlockPosExtended;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.codecs.ListCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
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

    // Client only
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
     * @param payload The payload containing the data to set
     * @param player The player who sent it
     */
    @Environment(EnvType.SERVER)
    public static void setTransmittedTexture(SetCustomizableSignTexturePayload payload, ServerPlayer player) {
        BlockPos pos = new BlockPos(payload.x(), payload.y(), payload.z());
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        var textureData = payload.textureData();

        int maxElements = MyWorldTrafficAddition.getServerPreferences().customizableSigns.general.maxElements.getOrDefault();

        if (textureData.getElementContainer().getTotalElementCount() > maxElements) {
            MyWorldTrafficAddition.LOGGER.warn(
                    "Player {} with UUID {} tried to update the sign's" +
                            "texture but the maximum amount has already been reached.",
                    player.getName(), player.getUUID()
            );

            player.sendSystemMessage(Component.translatable("interaction.info.myworld_traffic_addition.customizable_sign.too_many_elements"));

            return;
        }

        Objects.requireNonNull(player.level().getServer()).execute(() ->
            ((CustomizableSignBlockEntity) blockEntity).setTextureData(textureData)
        );

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
    public List<BlockPosExtended> getSignPositionsRelative() {
        return signPositionsRelative;
    }
    public void setSignPositionsRelative(List<BlockPosExtended> signPositionsRelative) {
        this.signPositionsRelative = signPositionsRelative;
        updateGame();
    }


    public List<BlockPosExtended> getSignPolePositionsRelative() {
        return signPolePositionsRelative;
    }
    public void setSignPolePositionsRelative(List<BlockPosExtended> signPolePositionsRelative) {
        this.signPolePositionsRelative = signPolePositionsRelative;
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
        this.masterPos = masterPos;
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

        isMaster = readView.getBooleanOr("is_master", false);
        isRendered = readView.getBooleanOr("is_rendered", true);
        isInitialized = readView.getBooleanOr("is_initialized", false);

        width = readView.getIntOr("width", 1);
        height = readView.getIntOr("height", 1);
        version = readView.getIntOr("version", 0); // If not available, sign is pre-1.8.0. 0 marks it that way.

        // User-customizable

        String rotationKey = readView.contains("Rotation") ? "Rotation" : "rotation";
        rotation = readView.getIntOr(rotationKey, 0);

        String textureKey = readView.contains("SignTexture") ? "SignTexture" : "sign_texture";
        try {
            textureData = CustomizableSignTextureData.fromJson(
                    JsonParser.parseString(
                            readView.getStringOr(textureKey, "{}")
                    ).getAsJsonObject()
            );
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to parse texture data for CustomizableSignBlockEntity at position {}! Defaulting to empty texture. Error: {}", this.getBlockPos(), e.getMessage());
            textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>());
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
     * Initializes the sign according to the current master state (master or not)
     * @param data The data from the initialization process.
     * @param border The border for specifically this sign.
     */
    public void initialize(
            CustomizableSignInitializer.CustomizableSignInitializationResult data,
            BorderProperty border
    ) {
        if (!this.getBlockPos().equals(data.realMaster())) {
            this.setMasterPos(data.realMaster());
            this.setRendered(false);
            this.setMaster(false);
            this.setBorderType(border);
            return;
        }

        this.setMaster(true);
        this.setRendered(true);
        this.setMasterPos(data.realMaster());
        this.setBorderType(border);
        this.setInitialized(true);
        this.setWidth(data.signWidth());
        this.setHeight(data.signHeight());

        this.setSignPositionsRelative(data.signRelative());
        this.setSignPolePositionsRelative(data.poleRelative());
    }
}