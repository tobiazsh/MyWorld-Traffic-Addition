package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.FontHelper;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.*;

public class ElementPropertyViewer {

    private static final ElementPropertyViewer MAIN_VIEWER = new ElementPropertyViewer("mainViewer");

    private boolean isVisible = false;

    private @Nullable PropertyViewerExtension extension;
    private ClientElementInterface element;

    private final AtomicBoolean pushedBold = new AtomicBoolean(false);
    private final String id;

    private final ImString nameBuffer = new ImString(256);

    private final float[] widthBuffer  = new float[1];
    private final float[] heightBuffer = new float[1];

    private final float[] xBuffer = new float[1];
    private final float[] yBuffer = new float[1];

    private final float[] rotationBuffer = new float[1];

    private final float[] colorBuffer = new float[4];

    /** Width in pixels */
    private float canvasWidth = 1;

    /** Height in pixels */
    private float canvasHeight = 1;

    ImBoolean lockAspectRatio = new ImBoolean(false);

    public ElementPropertyViewer(String id) {
        this.id = id;
    }

    public static ElementPropertyViewer getMainViewer() {
        return MAIN_VIEWER;
    }

    public void setCanvasSize(float widthPixels, float heightPixels) {
        this.canvasWidth = widthPixels;
        this.canvasHeight = heightPixels;
    }

    public void prepare(ClientElementInterface element, float canvasWidth, float canvasHeight) {
        setCanvasSize(canvasWidth, canvasHeight);

        element.getPropertyViewerExtension().ifPresentOrElse(ext -> {
            this.extension = ext;
            ext.setElement(element);
        }, () -> this.extension = null);

        this.element = element;

        this.nameBuffer.set(element.getName());

        this.widthBuffer[0] = element.getWidth();
        this.heightBuffer[0] = element.getHeight();

        this.xBuffer[0] = element.getX();
        this.yBuffer[0] = element.getY();

        // Only copy inside to be able to make buffer final to make sure it doesn't change reference and break ImGui
        System.arraycopy(element.getColor(), 0, this.colorBuffer, 0, 4);

        this.rotationBuffer[0] = element.getRotation();
    }

    public void render() {
        if (ImGui.begin("Element Properties##" + id)) {
            // TODO: Export naming into element window to make it more intuitive for the user
            renderName();
            ImGui.separator();
            renderDimensions();
            ImGui.separator();
            renderRotation();
            ImGui.separator();
            renderColor();
            ImGui.separator();
            if (extension != null) extension.render(this.id);

            ImGui.end();
        }
    }

    private void renderName() {
        FontHelper.pushRobotoBold(pushedBold);
        ImGui.inputText(trIC("text.mwta.sign-editor.property-viewer.name", id, "nameInput"), nameBuffer, 256);
        FontHelper.popRobotoBold(pushedBold);

        if (ImGui.button(trIC("text.mwta.sign-editor.property-viewer.name.apply", id, "applyNameBtn")))
            element.setName(nameBuffer.get());
    }

    private void renderDimensions() {
        if (ImGui.beginChild("Dimensions##" + id)) {
            FontHelper.pushRobotoBold(pushedBold);
            ImGui.text(trIC("text.mwta.sign-editor.property-viewer.dimensions", id, "dimensionsTitle"));
            FontHelper.popRobotoBold(pushedBold);

            ImGui.beginGroup();
            {
                ImGui.checkbox(trIC(
                        "text.mwta.sign-editor.property-viewer.dimensions.lock-ratio",
                        id, "aspect-locker"
                ), lockAspectRatio);

                if (ImGui.dragFloat(
                        trIC("text.mwta.sign-editor.property-viewer.dimensions.width", id, "widthInput"),
                        widthBuffer)
                ) {
                    if (widthBuffer[0] > canvasWidth)
                        widthBuffer[0] = Math.clamp(widthBuffer[0], 1, canvasWidth);

                    if (lockAspectRatio.get()) {
                        float aspectRatio = element.getWidth() / element.getHeight();
                        heightBuffer[0] = (int) (widthBuffer[0] / aspectRatio);
                    }

                    applySize();
                }

                if (ImGui.dragFloat(
                        trIC("text.mwta.sign-editor.property-viewer.dimensions.height", id, "heightInput"),
                        heightBuffer)
                ) {
                    if (heightBuffer[0] > canvasHeight)
                        heightBuffer[0] = Math.clamp(heightBuffer[0], 1, canvasHeight);

                    if (lockAspectRatio.get()) {
                        float aspectRatio = element.getWidth() / element.getHeight();
                        widthBuffer[0] = (int) (heightBuffer[0] * aspectRatio);
                    }

                    applySize();
                }
            } ImGui.endGroup();

            ImGui.sameLine();

            ImGui.beginGroup();
            {
                if (ImGui.button(trIC(
                        "text.mwta.sign-editor.property-viewer.dimensions.center-x",
                        id, "centerXBtn"))
                ) {
                    xBuffer[0] = (canvasWidth - element.getWidth()) / 2;
                    applyPosition();
                }

                if (ImGui.button(trIC(
                        "text.mwta.sign-editor.property-viewer.dimensions.center-y",
                        id, "centerYBtn"))
                ) {
                    yBuffer[0] = (canvasHeight - element.getHeight()) / 2;
                    applyPosition();
                }

                if (ImGui.dragFloat(
                        trIC("text.mwta.sign-editor.property-viewer.dimensions.x", id, "xInput"),
                        xBuffer)
                ) element.setX(xBuffer[0]);

                if (ImGui.dragFloat(
                        trIC("text.mwta.sign-editor.property-viewer.dimensions.y", id, "yInput"),
                        yBuffer)
                ) element.setY(yBuffer[0]);
            } ImGui.endGroup();
        }
    }

    private void renderRotation() {
        FontHelper.pushRobotoBold(pushedBold);
        ImGui.text(trIC("text.mwta.sign-editor.property-viewer.rotation", id, "rotationTitle"));
        FontHelper.popRobotoBold(pushedBold);

        if (ImGui.dragFloat(
                trIC("text.mwta.sign-editor.property-viewer.rotation", id, "rotationInput"),
                rotationBuffer
        )) element.setRotation(rotationBuffer[0]);
    }

    private void renderColor() {
        FontHelper.pushRobotoBold(pushedBold);
        ImGui.text(trIC("text.mwta.sign-editor.property-viewer.color", id, "colorTitle"));
        FontHelper.popRobotoBold(pushedBold);

        if (ImGui.colorEdit4(
                trIC("text.mwta.sign-editor.property-viewer.color", id, "colorInput"),
                colorBuffer,
                ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf
        )) element.setColor(colorBuffer);
    }

    private void applySize() {
        this.element.setSize(this.widthBuffer[0], this.heightBuffer[0]);
    }

    private void applyPosition() {
        this.element.setPosition(this.xBuffer[0], this.yBuffer[0]);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public boolean toggleVisibility() {
        this.isVisible = !this.isVisible;
        return this.isVisible;
    }
}
