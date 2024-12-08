package at.tobiazsh.myworld.traffic_addition.ImGui.Windows;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 17:47
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Utils.SignStyleJson;
import imgui.ImGui;

public class JsonPreviewPopUp {
	private static SignStyleJson currentStyle = new SignStyleJson();

	public static boolean shouldOpen = false;
	public static String windowId = "JSON Preview";

	public static void open(SignStyleJson style) {
		JsonPreviewPopUp.currentStyle = style;
		shouldOpen = false;
		ImGui.openPopup(windowId);
	}

	public static void create() {
		ImGui.setNextWindowSize(750, 500);
		if (ImGui.beginPopupModal(windowId)) {

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Current JSON Data");
			ImGui.popFont();

			ImGui.separator();

			ImGui.beginChild("JsonDisplayer", 750, 300);

			if (currentStyle.json == null) ImGui.text("Currently no JSON Data available");
			else ImGui.textWrapped(currentStyle.json.toString());
			ImGui.endChild();

			ImGui.separator();

			if (ImGui.button("Close")) {
				shouldOpen = false;
				ImGui.closeCurrentPopup();
			}

			ImGui.endPopup();
		}
	}
}
