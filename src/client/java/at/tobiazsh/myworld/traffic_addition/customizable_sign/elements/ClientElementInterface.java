package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer.PropertyViewerExtension;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

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

    default Optional<PropertyViewerExtension> getPropertyViewerExtension() {
        return Optional.empty();
    }

    @Nullable ImGuiTexture getThumbnailTexture();

    /**
     * Make use of a preview in the for this element. Get preview using {@link #getPreview()}.
     * @param force Whether to force-create the preview even if it already exists.
     */
    void usePreview(boolean force);

    /**
     * Destroy the current preview.
     */
    void destroyPreview();

    /**
     * Returns the preview of the element. A preview is basically a copy of the element,
     * which can be modified, without having to add an extra entry to the client element list and
     * without modifying the actual element. It will be rendered on top of the actual element, while still rendering
     * the original element itself.
     * @return Either null, if no preview as been created, or the preview itself.
     */
    @Nullable ClientElementInterface getPreview();

    /**
     * Render the preview in ImGui, as rendering the preview in Minecraft makes no sense.
     */
    void renderPreview(float scale);

    String getGenericTranslatableName();
}
