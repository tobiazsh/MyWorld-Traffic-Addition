package at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents;


/*
 * @created 22/10/2024 (DD/MM/YYYY) - 16:26
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;

public class ElementAddWindow {
	public static boolean shouldOpen = false;
	public static String windowId = "Element Add Window";

	public static void create() {
		if (ImGui.beginPopupModal(windowId)) {

			ImVec2 windowSize = new ImVec2(ImGui.getWindowWidth(), ImGui.getWindowHeight());
			ImGuiStyle style = ImGui.getStyle();

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Add Elements");
			ImGui.popFont();

			ImGui.separator();

			if (ImGui.beginChild("##elementsDisplay")) {

				ImGui.endChild();
			}

			ImGui.setCursorPos(style.getFramePaddingX(), (windowSize.y - (style.getFramePaddingY() * 2) - (ImGui.getFontSize() * 1.5)));
			if (ImGui.beginChild("##controls")) {
				ImGui.separator();

				if (ImGui.button("Confirm")) {

				}

				ImGui.sameLine();

				if (ImGui.button("Cancel")) {
					ImGui.closeCurrentPopup();
				}

				ImGui.endChild();
			}

			ImGui.endPopup();
		}
	}

	public static void open() {
		shouldOpen = false;
		ImGui.openPopup(windowId);
	}
}
