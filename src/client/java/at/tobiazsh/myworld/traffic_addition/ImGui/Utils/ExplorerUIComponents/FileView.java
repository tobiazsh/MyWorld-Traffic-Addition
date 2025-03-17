package at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ExplorerUIComponents;

import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ImGuiTools;
import at.tobiazsh.myworld.traffic_addition.Utils.ByteSize;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSelectableFlags;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FileView {

    public static class DetailedViewBar {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private boolean selected = false; // Track selection state
        private float regionWidth;
        private float barHeight;
        private String filename;
        private LocalDate creationDate;
        private LocalDate lastModified;
        private long size;
        private String type;
        private ImVec4 barColor;
        private ImVec4 selectedColor;
        private ImVec4 textColor;
        private boolean autoWidth = false;
        private final String barId;
        public final boolean isFolder;

        public DetailedViewBar(
                String filename,
                LocalDate creationDate, LocalDate lastModified,
                long size,
                String type,
                String barId,
                ImVec4 barColor, ImVec4 selectedColor, ImVec4 textColor,
                float barHeight,
                boolean isFolder, boolean selected
        ) {
            this(
                    filename,
                    creationDate, lastModified,
                    size,
                    type,
                    barId,
                    barColor, selectedColor, textColor,
                    barHeight, -1,
                    isFolder, selected
            );
        }

        public DetailedViewBar(
                String filename,
                LocalDate creationDate, LocalDate lastModified,
                long size,
                String type,
                String barId,
                ImVec4 barColor, ImVec4 selectedColor, ImVec4 textColor,
                float barHeight, float regionWidth,
                boolean isFolder, boolean selected
        ) {
            this.barHeight = barHeight;
            this.regionWidth = regionWidth;
            this.filename = filename;
            this.creationDate = creationDate;
            this.lastModified = lastModified;
            this.size = size;
            this.type = type;
            this.barColor = barColor;
            this.textColor = textColor;
            this.selectedColor = selectedColor;
            this.barId = barId;
            this.isFolder = isFolder;
            this.selected = selected;

            if (regionWidth == -1)
                this.autoWidth = true;
        }

        /**
         * Draws the detailed view bar
         * @return true if the bar was clicked last frame
         */
        public boolean draw() {
            boolean wasClicked = false;

            regionWidth = autoWidth ? ImGui.getContentRegionAvailX() : regionWidth;

            // Background
            ImGuiTools.drawRect((int)regionWidth, (int)barHeight, new ImVec2(ImGui.getCursorScreenPos().x, ImGui.getCursorScreenPos().y), selected ? selectedColor : barColor);

            if (ImGui.selectable(barId, selected, ImGuiSelectableFlags.SpanAllColumns | ImGuiSelectableFlags.DontClosePopups, new ImVec2(regionWidth,  barHeight))) {
                wasClicked = true;
            }

            ImGui.sameLine();

            // Styling
            ImGui.pushStyleColor(ImGuiCol.Text, textColor);

            float diff = (barHeight - ImGui.getFontSize()) / 2;
            ImGui.setCursorPosY(ImGui.getCursorPosY() + diff);

            ImGui.columns(5, "##DetailedViewBarColumns", false);

            // Filename
            ImGui.setColumnWidth(0, ImGui.getWindowContentRegionMaxX() - 250 - 150 - 150 - 150);
            ImGui.dummy(diff, 0);
            ImGui.sameLine();
            ImGui.text(filename);
            ImGui.nextColumn();

            if (isFolder) {

                for (int i = 1; i < 4; i++) {
                    ImGui.setColumnWidth(i, 150);
                    ImGui.nextColumn(); // Jump to last column
                }

                // Type
                ImGui.setColumnWidth(4, 250);
                ImGui.text("FOLDER");

                ImGui.columns(1);
                ImGui.popStyleColor();

                return wasClicked;
            }

            // CONTINUE IF FILE ...

            // Creation Date
            ImGui.setColumnWidth(1, 150);
            ImGui.text(formatter.format(creationDate));
            ImGui.nextColumn();

            // Last Modified
            ImGui.setColumnWidth(2, 150);
            ImGui.text(formatter.format(lastModified));
            ImGui.nextColumn();

            // Size
            ImGui.setColumnWidth(3, 150);
            ImGui.text(ByteSize.convertReal(size, ByteSize.ByteUnitsReal.B, ByteSize.ByteUnitsReal.MiB) + " MiB");
            ImGui.nextColumn();

            // Type
            ImGui.setColumnWidth(4, 250);
            ImGui.text(type);
            ImGui.nextColumn();

            ImGui.columns(1);
            ImGui.popStyleColor();

            return wasClicked;
        }
    }
}
