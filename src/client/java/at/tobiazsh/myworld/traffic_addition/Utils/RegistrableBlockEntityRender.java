package at.tobiazsh.myworld.traffic_addition.Utils;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

import java.util.List;

public class RegistrableBlockEntityRender <T extends BlockEntity> {
    public BlockEntityType<T> blockEntityType;
    public BlockEntityRendererFactory<T> blockEntityRenderer;

    public RegistrableBlockEntityRender(BlockEntityType<T> blockEntityType, BlockEntityRendererFactory<T> blockEntityRenderer) {
        this.blockEntityType = blockEntityType;
        this.blockEntityRenderer = blockEntityRenderer;
    }

    public static <T extends BlockEntity> void RegisterBlockEntityRenderer(RegistrableBlockEntityRender<T> registrableBlockEntityRender) {
        BlockEntityRendererFactories.register(registrableBlockEntityRender.blockEntityType, registrableBlockEntityRender.blockEntityRenderer);
    }

    public static void bulkRegisterBlockEntityRenderers(List<RegistrableBlockEntityRender<? extends BlockEntity>> blockEntityRenderers) {
        blockEntityRenderers.forEach(RegistrableBlockEntityRender::RegisterBlockEntityRenderer);
    }
}
