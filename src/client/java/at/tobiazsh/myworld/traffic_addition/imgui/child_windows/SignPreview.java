package at.tobiazsh.myworld.traffic_addition.imgui.child_windows;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.rendering.renderers.BackgroundRenderer;
import at.tobiazsh.myworld.traffic_addition.texture.Texture;
import at.tobiazsh.myworld.traffic_addition.texture.Textures;
import at.tobiazsh.myworld.traffic_addition.utils.BorderProperty;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.List;

public class SignPreview {

    private float zoomFactor;

    private final float zoomSpeed;
    private final float zoomMin;
    private final float zoomMax;

    public final float previewMaxWidth; // Tweak if necessary in the future
    public final float previewMaxHeight; // Tweak if necessary in the future

    public SignPreview(
            float zoomFactor,
            float zoomSpeed,
            float zoomMin,
            float zoomMax,
            float previewMaxWidth,
            float previewMaxHeight
    ) {
        this.zoomFactor = zoomFactor;
        this.zoomSpeed = zoomSpeed;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
        this.previewMaxWidth = previewMaxWidth;
        this.previewMaxHeight = previewMaxHeight;
    }

    public static SignPreview createDefault() {
        return new SignPreview(1.0f, 0.05f, 0.5f, 3.0f, 950.0f, 950.0f);
    }

    public void render(
            float signWidthPixels, float signHeightPixels,
            float pxOfBlock, // Pixel of one block
            ImVec2 position,
            List<ClientElementInterface> drawables,
            Background background,
            BorderProperty[][] borders
    ) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);  // Remove spacing between items
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);  // Remove padding inside the frame

        pxOfBlock *= zoomFactor;

        // Make Child that is as big as the sign in pixels
        ImGui.beginChild("##BottomToTopRenderer", signWidthPixels * zoomFactor, signHeightPixels  * zoomFactor, false, ImGuiWindowFlags.NoScrollbar);

        // Render Background Textures
        // Render from bottom to top and from left to right
        BackgroundRenderer.ImGuiRenderer.render(
                background,
                borders,
                pxOfBlock
        );

        ImGui.endChild();

        if (!drawables.isEmpty()) {
            for (int i = drawables.size() - 1; i >= 0; i--) {
                ClientElementInterface element = drawables.get(i); // Get element to render

                // Skip non-render-able elements
                if (element == null) continue;

                ImGui.setCursorPos(position.x, position.y);
                ImGui.beginChild("OVERLAY_CANVAS_" + element.getId(), signWidthPixels * zoomFactor, signHeightPixels  * zoomFactor, false, ImGuiWindowFlags.NoScrollbar);

                // Render depending on the type of element
                renderElement(element, zoomFactor);

                ImGui.endChild();
            }
        }

        ImGui.popStyleVar(2);
    }

    private void renderElement(ClientElementInterface element, float scale) {
        element.renderImGui(scale);
    }

    /**
     * Returns the current zoom pxOfBlock
     */
    public float getZoom() {
        return zoomFactor;
    }

    /**
     * Zooms the canvas in
     */
    public void zoomIn() {
        zoomFactor = Math.min(zoomFactor + zoomSpeed, zoomMax);
    }

    /**
     * Zooms the canvas out
     */
    public void zoomOut() {
        zoomFactor = Math.max(zoomFactor - zoomSpeed, zoomMin);
    }
}
