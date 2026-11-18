package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.clipboard.ElementClipboard;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;

import java.awt.datatransfer.Clipboard;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * A window that's supposed to list all current elements on the canvas, as well as groups
 */
public class ElementList {

    private boolean isVisible = false;

    private final String id;

    private final ElementListEntryRoot rootEntry;

    public ElementList(
            String id,
            ClientElementManager manager,
            ElementSelectionCallback selectionCallback,
            ElementClipboard clipboard
    ) {
        this.id = id;
        this.rootEntry = new ElementListEntryRoot(id, manager, selectionCallback, clipboard);
        manager.elementListChangedEvent.addListener(rootEntry::refresh);
    }

    public void render() {
        if (!isVisible) return;

        if (ImGui.begin(trIC("text.mwta.element-list.title", id, "elementListWindow"))) {
            rootEntry.render(ImGui.getContentRegionAvailY());
            ImGui.end();
        }
    }

    public void toggleVisibility() {
        this.isVisible = !this.isVisible;
    }

    public void show() {
        this.isVisible = true;
    }

    public void hide() {
        this.isVisible = false;
    }
}
