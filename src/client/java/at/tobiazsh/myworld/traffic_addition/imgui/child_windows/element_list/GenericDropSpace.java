package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.element_list;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

import java.util.function.BiConsumer;

/**
 * Renders a generic drop space for any data type.
 *
 * @param dropSpaceHeight The height of the drop space.
 * @param dropSpaceColor The color of the drop space.
 * @param dropDataType The data type of the drop space.
 * @param clasz A class instance of the drag'n'dropped payload.
 * @param onDrop What happens when an element is successfully dropped? Null-check is done by this class.
 * @param <P> The type of payload that gets drag'n'dropped.
 */
record GenericDropSpace<P>(
        float dropSpaceHeight,
        int dropSpaceColor,
        String dropDataType,
        Class<P> clasz,
        BiConsumer<P, Integer> onDrop
) {
    public void renderNew(int index) {
        ImGui.pushID("DropSpace" + index);
        try {
            float maxWidth = ImGui.getContentRegionAvailX();
            ImVec2 cursorPos = ImGui.getCursorScreenPos();
            ImDrawList drawList = ImGui.getWindowDrawList();

            ImVec2 rectMin = new ImVec2(cursorPos.x, cursorPos.y);
            ImVec2 rectMax = new ImVec2(cursorPos.x + maxWidth, cursorPos.y + dropSpaceHeight);

            drawList.addRectFilled(rectMin, rectMax, dropSpaceColor);
            ImGui.dummy(new ImVec2(maxWidth, dropSpaceHeight));

            if (ImGui.beginDragDropTarget()) {
                try {
                    var payload = ImGui.acceptDragDropPayload(dropDataType, clasz);

                    if (payload == null)
                        return;

                    onDrop.accept(payload, index);
                } finally {
                    ImGui.endDragDropTarget();
                }
            }
        } finally {
            ImGui.popID();
        }
    }
}
