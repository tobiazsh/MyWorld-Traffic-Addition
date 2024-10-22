package at.tobiazsh.myworld.traffic_addition.components.BlockEntities;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import net.minecraft.block.Block;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

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
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        nbt.putInt(ROTATION_KEY, this.rotation_value);
        nbt.putBoolean("ShouldRender", shouldRender);
        return nbt;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt(ROTATION_KEY, this.rotation_value);
        nbt.putBoolean("ShouldRender", shouldRender);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.rotation_value = nbt.getInt(ROTATION_KEY);
        this.shouldRender = nbt.getBoolean("ShouldRender");
    }

    public int getRotationValue() {
        return this.rotation_value;
    }

    public void setRotationValue(int value) {
        if (this.rotation_value != value) {
            this.rotation_value = value;

            markDirty();
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(null, this.getCachedState()));
            this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public void setShouldRender(boolean value) {
        if (this.shouldRender != value) {
            this.shouldRender = value;

            markDirty();
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(null, this.getCachedState()));
            this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public boolean isShouldRender() {
        return shouldRender;
    }
}