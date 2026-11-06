package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.GroupElementClient;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.tr;

public class ElementListEntry {

    protected final ClientElementInterface element;
    protected final ElementId id;
    protected final String idStr;
    protected final ImString elementName = new ImString(512);
    protected final String dragDropDataType;
    protected final ListEntryParent parent; // Null if this is a top-level element, otherwise points to the parent group entry

    protected final Consumer<ElementId> onSelect;

    protected final Function<ElementId, @Nullable ElementListEntry> listEntryLookup;

    protected final Consumer<ElementListEntry> onNewEntry;
    protected final Consumer<ElementListEntry> onEntryRemoved;

    protected @Nullable ImGuiTexture thumbnailTexture; // Not final because GroupListEntry overrides it with a different texture

    protected int previewSize = 50;

    protected static @Nullable ImGuiTexture genericThumbnailTexture;

    public ElementListEntry(
            ClientElementInterface element,
            ElementId id,
            String dragDropDataType,
            ListEntryParent parent,
            Consumer<ElementId> onSelect,
            Consumer<ElementListEntry> onNewEntry,
            Consumer<ElementListEntry> onEntryRemoved,
            Function<ElementId, @Nullable ElementListEntry> listEntryLookup
    ) {
        this.element = element;
        this.id = id;
        this.idStr = id.toString();
        this.dragDropDataType = dragDropDataType;
        this.thumbnailTexture = element.getThumbnailTexture();
        this.elementName.set(element.getName());
        this.parent = parent;

        this.onSelect = onSelect;

        this.listEntryLookup = listEntryLookup;

        this.onNewEntry = onNewEntry;
        this.onEntryRemoved = onEntryRemoved;
    }

    public ElementId getId() {
        return id;
    }

    public ClientElementInterface getElement() {
        return element;
    }

    public void render(
        Set<ElementId> selectedIds
    ) {
        ImGui.pushID(idStr);
        try {
            boolean isSelected = selectedIds.contains(id);

            ImVec2 startPos = ImGui.getCursorPos();
            float spacingY = ImGui.getStyle().getItemSpacingY();

            renderContent(selectedIds);

            ImVec2 endPos = ImGui.getCursorPos();
            float contentHeight = endPos.y - startPos.y - spacingY;

            ImGui.setCursorPos(startPos);

            ImGui.selectable(
                    "##" + id,
                    isSelected,
                    ImGuiSelectableFlags.AllowOverlap | ImGuiSelectableFlags.SpanAllColumns,
                    new ImVec2(0, contentHeight)
            );

            renderContextMenu();

            ImGui.setCursorPosY(endPos.y);

            if (ImGui.isItemHovered()) {
                if (ImGui.isMouseClicked(ImGuiMouseButton.Left))
                    onSelect.accept(id);
                else if (ImGui.isMouseClicked(ImGuiMouseButton.Right))
                    ImGui.openPopup("###context_menu");

                if (ImGui.beginDragDropSource()) {
                    sendDragDropPayload();
                    ImGui.endDragDropSource();
                }
            }

            if (ImGui.beginDragDropTarget()) {
                acceptDragDropPayload();
                ImGui.endDragDropTarget();
            }
        } finally {
            ImGui.popID();
        }
    }

    protected void renderContextMenu() {
        if (ImGui.beginPopupContextItem("###context_menu")) {
            if (ImGui.menuItem("Delete")) {
                parent.deleteChild(this);
            }
            ImGui.endPopup();
        }
    }

    protected void renderContent(Set<ElementId> selectedElementIds) {
        float startY = ImGui.getCursorPosY();

        renderPreview();

        float previewHeight = ImGui.getCursorPosY() - startY;
        float framePaddingY = ImGui.getStyle().getItemSpacingY();
        float textHeight = ImGui.getFrameHeight() + framePaddingY;

        ImGui.sameLine();

        ImGui.setCursorPosY(startY + (previewHeight - textHeight) / 2);
        renderName(ImGui.getCursorPosX(), ImGui.getCursorPosY());
        ImGui.setCursorPosY(startY + previewHeight);
    }

    protected void sendDragDropPayload() {
        ImGui.setDragDropPayload(dragDropDataType, new ElementListEntryDragDropPayload(id));
        renderPreview();
        ImGui.sameLine();
        ImGui.text(element.getName());
    }

    protected void acceptDragDropPayload() {
        var payload = ImGui.acceptDragDropPayload(dragDropDataType, ElementListEntryDragDropPayload.class);

        if (payload == null)
            return;

        if (payload.elementEntryId().equals(id))
            return;

        onElementDropped(payload);
    }

    /**
     * Called when an element is dropped onto this entry. Override this method to implement custom behavior, such as
     * creating a group or reordering elements.
     * @param payload The received drag-and-drop payload.
     */
    protected void onElementDropped(@NonNull ElementListEntryDragDropPayload payload) {
        // Create group here
        var droppedElementEntry = listEntryLookup.apply(payload.elementEntryId());

        if (droppedElementEntry == null)
            return;

        droppedElementEntry.parent.removeChild(droppedElementEntry);
        parent.removeChild(this);

        float minX = Math.min(droppedElementEntry.getElement().getX(), this.getElement().getX());
        float minY = Math.min(droppedElementEntry.getElement().getY(), this.getElement().getY());
        float maxX = Math.max(droppedElementEntry.getElement().getX(), this.getElement().getX());
        float maxY = Math.max(droppedElementEntry.getElement().getY(), this.getElement().getY());

        var groupElement = new GroupElementClient(
                minX,
                minY,
                maxX - minX,
                maxY- minY,
                0,
                ElementId.random(),
                parent.getId().toParent()
        );

        var groupEntry = new GroupListEntry(
                groupElement,
                ElementId.random(), // Generate a new ID for the group
                dragDropDataType,
                parent,
                onSelect,
                onNewEntry,
                onEntryRemoved,
                listEntryLookup
        );

        parent.addChild(groupEntry);
        onNewEntry.accept(groupEntry);
    }

    protected void renderPreview() {
        if (genericThumbnailTexture == null)
            createTextures();

        // If specific thumbnail available, render this; otherwise render generic thumbnail
        // If all else fails, render a placeholder text
        if (thumbnailTexture != null && thumbnailTexture.isUsable()) {
            ImGui.image(thumbnailTexture.getTextureId(), previewSize, previewSize);
        } else if (genericThumbnailTexture.isUsable()) {
            ImGui.image(genericThumbnailTexture.getTextureId(), previewSize, previewSize);
        } else {
            renderNoThumbnailPlaceholder();
        }

        ImGui.setItemTooltip(tr(element.getGenericTranslatableName()));
    }

    protected void renderName(float posX, float posY) {
        ImGui.setCursorPos(posX, posY);

        float width = ImGui.calcTextSizeX(elementName.get()) + 20; // Add some padding for the input box
        ImGui.setNextItemWidth(width);

        if (ImGui.inputText("##nameInput", elementName, ImGuiInputTextFlags.EnterReturnsTrue))
            element.setName(elementName.get());
    }

    protected void renderNoThumbnailPlaceholder() {
        if (ImGui.beginChild("##no_thumbnail", previewSize, previewSize)) {
            ImGui.textWrapped("No thumbnail available");
            ImGui.endChild();
        }
    }

    protected static void createTextures() {
        try {
            genericThumbnailTexture = ImGuiTextureFactory.fromStream(
                    Objects.requireNonNull(ElementListEntry.class.getResourceAsStream(
                            "/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/icons/element_icon.png"
                    )),
                    "generic_element_thumbnail"
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load generic element thumbnail texture", e);
        }
    }

    protected void dispose() {
        if (thumbnailTexture != null)
            thumbnailTexture.dispose();
    }
}
