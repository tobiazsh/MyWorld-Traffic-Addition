package at.tobiazsh.myworld.traffic_addition.block_entities;

import at.tobiazsh.myworld.traffic_addition.utils.OptionalUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.ModBlockEntities.SIGN_POLE_BLOCK_ENTITY;

public class SignPoleBlockEntity extends BlockEntity {
    private static final String ROTATION_KEY = "RotationValue";
    private int rotation_value;
    private boolean shouldRender = true;

    // List to store all instances of that class
    public static List<SignPoleBlockEntity> instances = new ArrayList<>();

    public SignPoleBlockEntity (BlockPos pos, BlockState state) {
        super(SIGN_POLE_BLOCK_ENTITY, pos, state);
        instances.add(this);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }

    @Override
    protected void saveAdditional(ValueOutput writeView) {
        super.saveAdditional(writeView);
        writeView.putInt(ROTATION_KEY, this.rotation_value);
        writeView.putBoolean("ShouldRender", shouldRender);
    }

    @Override
    protected void loadAdditional(ValueInput readView) {
        super.loadAdditional(readView);
        this.shouldRender = readView.getBooleanOr("ShouldRender", true);
        this.rotation_value = OptionalUtils.getOrDefault(ROTATION_KEY, readView::getInt, 0, "SignPoleBlockEntity.readNbt");
    }

    public int getRotationValue() {
        return this.rotation_value;
    }

    public void setRotationValue(int value) {
        if (this.rotation_value != value) {
            this.rotation_value = value;

            setChanged();
	        assert level != null;
	        level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(null, this.getBlockState()));
            Objects.requireNonNull(this.getLevel()).sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void setShouldRender(boolean value) {
        if (this.shouldRender != value) {
            this.shouldRender = value;

            setChanged();
	        assert level != null;
	        level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(null, this.getBlockState()));
            Objects.requireNonNull(this.getLevel()).sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean isShouldRender() {
        return shouldRender;
    }
}