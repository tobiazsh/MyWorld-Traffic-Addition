package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.clipboard.ElementClipboard;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.texture.TickableImGuiTexture;
import at.tobiazsh.myworld.traffic_addition.texture.TickableImGuiTextureCacheImpl;
import at.tobiazsh.myworld.traffic_addition.utils.ThrowingSupplier;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImGui;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiPopupFlags;
import imgui.flag.ImGuiStyleVar;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * A window that's supposed to list all current elements on the canvas, as well as groups
 */
public class ElementList {

    private boolean isVisible = false;

    private final String id;

    private final ElementListEntryRoot rootEntry;
    private final ClientElementManager manager;

    @Nullable private static TickableImGuiTexture BIN_TEXTURE = null;
    private static final String BIN_TEXTURE_LOCATION = "/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/icons/trash.png";

    public ElementList(
            String id,
            ClientElementManager manager,
            ElementSelectionCallback selectionCallback,
            ElementClipboard clipboard
    ) {
        this.id = id;
        this.rootEntry = new ElementListEntryRoot(id, manager, selectionCallback, clipboard);
        this.manager = manager;
        manager.elementListChangedEvent.addListener(rootEntry::refresh);
    }

    public void render() {
        if (!isVisible) return;

        if (ImGui.begin(trIC("text.mwta.element-list.title", id, "elementListWindow"))) {
            final float itemPaddingX = ImGui.getStyle().getItemSpacingX();

            rootEntry.render(ImGui.getContentRegionAvailY() - getControlBarSize());

            ImGui.setCursorPosY(ImGui.getWindowContentRegionMaxY() - getControlBarSize() + itemPaddingX);
            renderControlBar();

            ImGui.end();
        }
    }

    private final float controlBarButtonSize = 25;
    private final String addMenuId = "##addMenu";
    private void renderControlBar() {
        renderAddMenu();

        if (ImGui.button("+", controlBarButtonSize, controlBarButtonSize)) {
            ImGui.openPopup(addMenuId);
        }

        spaceSameLine(controlBarButtonSize);
        renderUpDownButton();

        spaceSameLine(controlBarButtonSize);
        renderDeleteButton();
    }

    /**
     * Adds horizontal space on the same line and leaves the next item on the very same line too.
     * @param space The space to leave out horizontally.
     */
    private static void spaceSameLine(final float space) {
        ImGui.sameLine();
        ImGui.dummy(space, 0);
        ImGui.sameLine();
    }

    private void renderDeleteButton() {
        boolean hasEntries = !rootEntry.getSelectedIds().isEmpty();

        if (!hasEntries)
            ImGui.beginDisabled();

        if (BIN_TEXTURE != null) {
            final float imageSize = controlBarButtonSize - ImGui.getStyle().getFramePaddingX() * 2;
            BIN_TEXTURE.touch();
            if (ImGui.imageButton(
                    "##deleteButton",
                    BIN_TEXTURE.getImGuiTexture().getTextureId(),
                    imageSize,
                    imageSize
            )) {
                deleteSelectedIds();
            }
        } else {
            if (ImGui.button("X", controlBarButtonSize, controlBarButtonSize)) {
                deleteSelectedIds();
            }
        }

        if (!hasEntries)
            ImGui.endDisabled();
    }

    private void renderUpDownButton() {
        float textHeight = ImGui.getTextLineHeight();
        float arrowButtonPadding = (controlBarButtonSize - textHeight) / 2f;

        var selectedIds = rootEntry.getSelectedIds();
        boolean oneSelected = selectedIds.size() == 1;
        var child = oneSelected ? this.rootEntry.getChild(selectedIds.iterator().next()) : null;
        int selectedIndex = oneSelected && child != null ? child.getParent().indexOf(child) : -1;
        int size = child != null ? child.getParent().size() : 0;
        boolean canMoveUp = selectedIndex > 0;
        boolean canMoveDown = selectedIndex < size - 1;

        if (!canMoveUp)
            ImGui.beginDisabled();

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, arrowButtonPadding, arrowButtonPadding);
        if (ImGui.arrowButton("##moveElementUp", ImGuiDir.Up) && child != null) {
            child.getParent().moveChild(selectedIndex, selectedIndex - 1);
        }
        ImGui.popStyleVar();

        if (!canMoveUp)
            ImGui.endDisabled();

        ImGui.sameLine();

        if (!canMoveDown)
            ImGui.beginDisabled();

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, arrowButtonPadding, arrowButtonPadding);
        if (ImGui.arrowButton("##moveElementDown", ImGuiDir.Down) && child != null) {
            child.getParent().moveChild(selectedIndex, selectedIndex + 1);
        }
        ImGui.popStyleVar();

        if (!canMoveDown)
            ImGui.endDisabled();
    }

    private void renderAddMenu() {
        if (ImGui.beginPopupContextItem(addMenuId, ImGuiPopupFlags.NoOpenOverExistingPopup)) {
            if (ImGui.menuItem("New Group")) {
                var groupElement = new GroupElementClient(
                        0, 0,
                        0, 0,
                        0, ElementId.random(),
                        BaseElement.MAIN_CANVAS_ID
                );

                manager.addElementFirst(groupElement);
            }

            if (ImGui.menuItem("New Text Element")) {
                var textElement = TextElementClient.createNew();
                manager.addElementFirst(textElement);
            }

            if (ImGui.menuItem("New Image Element")) {
                System.out.println("new image!");
            }
            ImGui.endPopup();
        }
    }

    private void deleteSelectedIds() {
        rootEntry.getSelectedIds().forEach(e -> {
            var child = rootEntry.getChild(e);

            if (child != null)
                child.delete();
        });
    }

    private static void loadTextures() {
        if (ElementList.BIN_TEXTURE == null) {
            ThrowingSupplier<ImGuiTexture, Exception> supplier = () ->
                    ImGuiTextureFactory.fromStream(
                            Objects.requireNonNull(ElementList.class.getResourceAsStream(BIN_TEXTURE_LOCATION)),
                            "ElementList_BinTexture"
                    );

            try {
                ElementList.BIN_TEXTURE = TickableImGuiTexture.ofImGuiTextureCached(
                        supplier,
                        Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "bin_texture"),
                        true,
                        TickableImGuiTextureCacheImpl.getInstance()
                );
            } catch (IOException | NullPointerException e) {
                MyWorldTrafficAddition.LOGGER.error("Could not open bin texture at {}! Error: {}", BIN_TEXTURE_LOCATION, e);
            } catch (Exception e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load bin texture at {}! Error: {}", BIN_TEXTURE_LOCATION, e);
            }
        }
    }

    private float getControlBarSize() {
        return ImGui.getStyle().getItemSpacingY() * 2 + controlBarButtonSize;
    }

    public void toggleVisibility() {
        if (this.isVisible)
            hide();
        else show();
    }

    public void show() {
        this.isVisible = true;
        loadTextures();
    }

    public void hide() {
        this.isVisible = false;
    }
}
