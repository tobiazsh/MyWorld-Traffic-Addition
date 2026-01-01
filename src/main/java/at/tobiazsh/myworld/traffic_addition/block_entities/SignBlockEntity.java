package at.tobiazsh.myworld.traffic_addition.block_entities;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.utils.OptionalUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SignBlockEntity extends BlockEntity {

    private int rotation = 0;
    private int shapeType;
    private String texturePath;

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;

        setChanged();

        if (this.level == null) {
            MyWorldTrafficAddition.LOGGER.error("Tried to update SignBlockEntity texture, but level is null!");
            return;
        }

        level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(null, this.getBlockState()));
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;

        setChanged();
    }

    public int getRotation() {
        return this.rotation;
    }

    public SignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, SignBlock.SIGN_SHAPE shapeType, String texturePath) {
        super(type, pos, state);
        this.shapeType = SignBlock.getSignSelectionEnumInt(shapeType);
        this.texturePath = texturePath;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput writeView) {
        super.saveAdditional(writeView);
        writeView.putInt("Rotation", this.rotation);
        writeView.putInt("ShapeType", this.shapeType);
        writeView.putString("Texture", this.texturePath);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput readView) {
        super.loadAdditional(readView);
        this.rotation = OptionalUtils.getOrDefault("Rotation", readView::getInt, 0, "SignBlockEntity.readNbt");
        this.shapeType = OptionalUtils.getOrDefault("ShapeType", readView::getInt, 2, "SignBlockEntity.readNbt"); // Default to 2 (Round Sign)
        this.texturePath = OptionalUtils.getOrDefault("Texture", readView::getString, "", "SignBlockEntity.readNbt");
    }

    @Override
    public @Nullable Packet<@NotNull ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }
}
