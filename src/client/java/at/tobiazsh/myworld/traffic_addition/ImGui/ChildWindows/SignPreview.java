package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;

import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.GroupElementClient;
import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.ImageElementClient;
import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.GroupElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.List;

public class SignPreview {

    private static float zoomFactor = 1.0f;
    private static float zoomSpeed = 0.05f;
    private static float zoomMin = 0.5f;
    private static float zoomMax = 3.0f;

    public static final float previewMaxWidth = 950.0f; // Tweak if necessary in the future
    public static final float previewMaxHeight = 950.0f; // Tweak if necessary in the future

    public static void render(float signWidthPixels, float signHeightPixels, int signWidthBlocks, int signHeightBlocks, float factor, ImVec2 position, List<BaseElement> drawables, List<String> backgroundTextures) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);  // Remove spacing between items
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);  // Remove padding inside the frame

        float rawFactor = factor;
        drawables.forEach(texture -> texture.setFactor(rawFactor));

        factor *= zoomFactor; // Add zoom factor to the factor

        // Make Child that is as big as the sign in pixels
        ImGui.beginChild("##BottomToTopRenderer", signWidthPixels * zoomFactor, signHeightPixels  * zoomFactor, false, ImGuiWindowFlags.NoScrollbar);

        // Render from bottom to top and from left to right
        float currentY = ImGui.getCursorPosY() + (signHeightBlocks - 1) * factor; // Set to the position of bottom
        int previewPosition = 0;
        for (int i = signHeightBlocks - 1; i >= 0; i--) {
            ImGui.setCursorPosY(currentY);

            for (int j = 0; j < signWidthBlocks; j++) {

                String texturePath = backgroundTextures.isEmpty() ? "/assets/myworld_traffic_addition/textures/imgui/icons/not-found.png" : backgroundTextures.get(previewPosition);

                Texture currentTexture = Textures.smartRegisterTexture(texturePath);
                ImGui.image(currentTexture.getTextureId(), factor, factor);

                // If the current position is smaller than the sign's height minus one, stay in row
                if (j < signWidthBlocks - 1) {
                    ImGui.sameLine();
                }

                previewPosition++;
            }

            currentY -= factor; // Decrease by factor to start next line
        }


        ImGui.endChild();

        if (!drawables.isEmpty()) {
            for (int i = drawables.size() - 1; i >= 0; i--) {
                BaseElement element = drawables.get(i); // Get element to render

                // Skip non-render-able elements
                if (!(element instanceof ImageElement || element instanceof TextElement || element instanceof GroupElement)) continue;

                ImGui.setCursorPos(position.x, position.y);
                ImGui.beginChild("OVERLAY_CANVAS_" + element.getId(), signWidthPixels * zoomFactor, signHeightPixels  * zoomFactor, false, ImGuiWindowFlags.NoScrollbar);

                // Render depending on the type of element
                renderElement(element);

                ImGui.endChild();
            }
        }

        ImGui.popStyleVar(2);
    }

    public static void renderElement(BaseElement element) {
        if (element instanceof ImageElement) {
            if (!((ImageElement) element).texIsLoaded)
                ((ImageElement) element).loadTexture(); // Register textures only on client side and if texture is not loaded

            ImageElementClient imgElem = ImageElementClient.fromImageElement((ImageElement) element);
            imgElem.setSize(imgElem.getWidth() * zoomFactor, imgElem.getHeight() * zoomFactor);
            imgElem.setPosition(imgElem.getX() * zoomFactor, imgElem.getY() * zoomFactor);
            imgElem.renderImGui();
        } else if (element instanceof TextElement) {
            TextElementClient txtElem = TextElementClient.fromTextElement((TextElement) element);
            txtElem.setSize(txtElem.getWidth() * zoomFactor, txtElem.getHeight() * zoomFactor);
            txtElem.setPosition(txtElem.getX() * zoomFactor, txtElem.getY() * zoomFactor);
            txtElem.renderImGui();
        } else if (element instanceof GroupElement) {
            GroupElementClient grpElem = GroupElementClient.fromGroupElement((GroupElement) element);
            grpElem.setSize(grpElem.getWidth() * zoomFactor, grpElem.getHeight() * zoomFactor);
            grpElem.setPosition(grpElem.getX() * zoomFactor, grpElem.getY() * zoomFactor);
            grpElem.renderImGui();
        }
    }

    /**
     * Returns the current zoom factor
     */
    public static float getZoom() {
        return zoomFactor;
    }

    /**
     * Zooms the canvas in
     */
    public static void zoomIn() {
        zoomFactor += zoomSpeed;
    }

    /**
     * Zooms the canvas out
     */
    public static void zoomOut() {
        zoomFactor -= zoomSpeed;
    }

    /**
     * Sets the zoom of the canvas
     * @param zoom Zoom in percent
     */
    public static void setZoom(float zoom) {
        zoomFactor = zoom;
    }

    /**
     * Sets the zoom speed
     * @param speed speed of the zoom (1.0f = 100%)
     */
    public static void setZoomSpeed(float speed) {
        zoomSpeed = speed;
    }

    /**
     * Sets the minimum zoom
     * @param min Maximum zoom in percent
     */
    public static void setZoomMin(float min) {
        zoomMin = min;
    }

    /**
     * Sets the maximum zoom
     * @param max Maximum zoom in percent
     */
    public static void setZoomMax(float max) {
        zoomMax = max;
    }
}
