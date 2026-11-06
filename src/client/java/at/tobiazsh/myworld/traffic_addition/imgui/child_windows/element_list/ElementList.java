package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * A window that's supposed to list all current elements on the canvas, as well as groups
 */
public class ElementList {

    private boolean isVisible = false;

    private final ImGuiFontScope fontScope = ImGuiFontScope.create();
    private final String id;

    private final ElementListEntryRoot rootEntry;

    public ElementList(String id, ClientElementManager manager) {
        this.id = id;
        this.rootEntry = new ElementListEntryRoot(id, manager);
        manager.elementListChangedEvent.addListener(rootEntry::refresh);
    }

    public void render() {
        if (!isVisible) return;

        if (ImGui.begin(trIC("text.mwta.element-list.title", id, "elementListWindow"))) {
            rootEntry.render();

            ImGui.getStyle().getItem

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
