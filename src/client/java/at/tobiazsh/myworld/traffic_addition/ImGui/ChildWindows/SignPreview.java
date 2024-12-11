package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;

import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.ImageElementClient;
import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;

import java.util.List;

public class SignPreview {
    public static void render(float signWidthPixels, float signHeightPixels, int signWidthBlocks, int signHeightBlocks, float factor, ImVec2 position, List<BaseElement> drawables, List<String> previewTextures) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);  // Remove spacing between items
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);  // Remove padding inside the frame

        //factor *= zoomScale;

        //float finalFactor = factor;
        drawables.forEach(texture -> texture.setFactor(factor));

        // Make Child that is as big as the sign in pixels
        ImGui.beginChild("##BottomToTopRenderer", signWidthPixels, signHeightPixels, false);

        // Render from bottom to top and from left to right
        if (!previewTextures.isEmpty()) {
            float currentY = ImGui.getCursorPosY() + (signHeightBlocks - 1) * factor; // Set to the position of bottom
            int previewPosition = 0;
            for (int i = signHeightBlocks - 1; i >= 0; i--) {
                ImGui.setCursorPosY(currentY);

                for (int j = 0; j < signWidthBlocks; j++) {

                    Texture currentTexture = Textures.smartRegisterTexture(previewTextures.get(previewPosition));
                    ImGui.image(currentTexture.getTextureId(), factor, factor);

                    // If the current position is smaller than the sign's height minus one, stay in row
                    if (j < signWidthBlocks - 1) {
                        ImGui.sameLine();
                    }

                    previewPosition++;
                }

                currentY -= factor; // Decrease by factor to start next line
            }
        }

        ImGui.endChild();

        if (!drawables.isEmpty()) {
            for (int i = drawables.size() - 1; i >= 0; i--) {
                BaseElement element = drawables.get(i); // Get element to render

                // Skip non-render-able elements
                if (!(element instanceof ImageElement || element instanceof TextElement)) continue;

                ImGui.setCursorPos(position.x, position.y);
                ImGui.beginChild("OVERLAY_CANVAS_" + element.getId(), signWidthPixels, signHeightPixels);

                // Scale position and dimensions
                float x = element.getX() / factor;
                float y = element.getY() / factor;
                ImGui.setCursorPos(x, y);

                // Render depending on the type of element
                if (element instanceof ImageElement) {
                    if (!((ImageElement) element).texIsLoaded)
                        ((ImageElement) element).loadTexture(); // Register textures only on client side and if texture is not loaded
                    ImageElementClient.fromImageElement((ImageElement) element).renderImGui();
                } else if (element instanceof TextElement) {
                    TextElementClient.fromTextElement((TextElement) element).renderImGui();
                }

                ImGui.endChild();
            }
        }

        ImGui.popStyleVar(2);
    }
}
