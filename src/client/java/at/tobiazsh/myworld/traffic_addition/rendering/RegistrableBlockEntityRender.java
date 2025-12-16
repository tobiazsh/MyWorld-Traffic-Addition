package at.tobiazsh.myworld.traffic_addition.rendering;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import java.util.List;

public class RegistrableBlockEntityRender <T extends BlockEntity, S extends BlockEntityRenderState> {
    public BlockEntityType<T> blockEntityType;
    public BlockEntityRendererProvider<T ,S> blockEntityRenderer;

    public RegistrableBlockEntityRender(BlockEntityType<T> blockEntityType, BlockEntityRendererProvider<T, S> blockEntityRenderer) {
        this.blockEntityType = blockEntityType;
        this.blockEntityRenderer = blockEntityRenderer;
    }

    public static <T extends BlockEntity, S extends BlockEntityRenderState> void RegisterBlockEntityRenderer(RegistrableBlockEntityRender<T, S> registrableBlockEntityRender) {
        BlockEntityRenderers.register(registrableBlockEntityRender.blockEntityType, registrableBlockEntityRender.blockEntityRenderer);
    }

    public static void bulkRegisterBlockEntityRenderers(List<RegistrableBlockEntityRender<? extends BlockEntity, ? extends BlockEntityRenderState>> blockEntityRenderers) {
        blockEntityRenderers.forEach(RegistrableBlockEntityRender::RegisterBlockEntityRenderer);
    }
}
