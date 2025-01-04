package at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public interface ClientElementRenderInterface {
    float zOffset = 0.075f;

    void renderImGui();
    void renderMinecraft(int indexInList, int csbeHeight, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing);
}
