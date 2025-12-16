package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;

public interface ClientElementInterface extends BaseElementInterface {
    float zOffset = 0.08f;

    void renderImGui(float scale);
    void renderMinecraft(SubmitNodeCollector queue, int indexInList, int csbeHeight, PoseStack matrices, int light, Direction facing);

    /**
     * Executes an action when the element is pasted (must be called in the method associated with pasting!)
     */
    void onPaste();

    /**
     * Executes an action when the element is imported (must be called in the method associated with importing!)
     */
    void onImport();

    /**
     * Creates a copy of the element.
     * @return a new instance of the element with the same properties.
     */
    ClientElementInterface copy();

    /**
     * Renders a preview of the element in the sign editor. Used for the element list.
     * Must be used inside an ImGui window.
     */
    void renderPreview(float w, float h);

    /**
     * Disposes resources used by the element.
     */
    default void dispose() { }
}
