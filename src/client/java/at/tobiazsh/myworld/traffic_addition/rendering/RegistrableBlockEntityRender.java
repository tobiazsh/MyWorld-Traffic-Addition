package at.tobiazsh.myworld.traffic_addition.rendering;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

import java.util.List;

public class RegistrableBlockEntityRender <T extends BlockEntity, S extends BlockEntityRenderState> {
    public BlockEntityType<T> blockEntityType;
    public BlockEntityRendererFactory<T ,S> blockEntityRenderer;

    public RegistrableBlockEntityRender(BlockEntityType<T> blockEntityType, BlockEntityRendererFactory<T, S> blockEntityRenderer) {
        this.blockEntityType = blockEntityType;
        this.blockEntityRenderer = blockEntityRenderer;
    }

    public static <T extends BlockEntity, S extends BlockEntityRenderState> void RegisterBlockEntityRenderer(RegistrableBlockEntityRender<T, S> registrableBlockEntityRender) {
        BlockEntityRendererFactories.register(registrableBlockEntityRender.blockEntityType, registrableBlockEntityRender.blockEntityRenderer);
    }

    public static void bulkRegisterBlockEntityRenderers(List<RegistrableBlockEntityRender<? extends BlockEntity, ? extends BlockEntityRenderState>> blockEntityRenderers) {
        blockEntityRenderers.forEach(RegistrableBlockEntityRender::RegisterBlockEntityRenderer);
    }
}
