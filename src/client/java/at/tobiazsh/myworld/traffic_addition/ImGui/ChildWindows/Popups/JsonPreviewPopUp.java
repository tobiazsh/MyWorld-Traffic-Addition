package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 17:47
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import imgui.ImGui;

public class JsonPreviewPopUp {
	private static CustomizableSignStyle currentStyle = new CustomizableSignStyle();

	public static boolean shouldOpen = false;
	public static String windowId = "JSON Preview";
	private static String json;

	public static void open(CustomizableSignStyle style) {
		JsonPreviewPopUp.currentStyle = style;
		shouldOpen = false;
		ImGui.openPopup(windowId);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		json = gson.toJson(style.json);
	}

	public static void create() {
		if (ImGui.beginPopupModal(windowId)) {

			if (ImGui.button("Close")) {
				shouldOpen = false;
				ImGui.closeCurrentPopup();
			}

			ImGui.sameLine();

			if (ImGui.button("Copy to Clipboard")) {
				ImGui.setClipboardText(json);
			}

			ImGui.separator();

			ImGui.beginChild("##jsonDisplayer", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY());

			if (currentStyle.json == null) ImGui.text("Currently no JSON Data available");
			else ImGui.textWrapped(json);
			ImGui.endChild();

			ImGui.endPopup();
		}
	}
}
