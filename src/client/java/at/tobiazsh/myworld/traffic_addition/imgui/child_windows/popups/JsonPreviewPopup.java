package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 17:47
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import imgui.ImGui;
import net.minecraft.text.Text;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class JsonPreviewPopup {
	private static CustomizableSignData currentStyle = new CustomizableSignData();

	public static boolean shouldOpen = false;
	public static String windowId;
	private static String json;

	public static void open(CustomizableSignData style) {
		windowId = tr("ImGui.Child.PopUps.JsonViewer", "Json Viewer");
		JsonPreviewPopup.currentStyle = style;
		shouldOpen = false;
		ImGui.openPopup(windowId);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		json = gson.toJson(style.json);
	}

	public static void render() {
		if (ImGui.beginPopupModal(windowId)) {

			if (ImGui.button(tr("Global", "Close"))) {
				shouldOpen = false;
				ImGui.closeCurrentPopup();
			}

			ImGui.sameLine();

			if (ImGui.button(tr("ImGui.Child.PopUps.JsonViewer", "Copy to Clipboard"))) {
				ImGui.setClipboardText(json);
			}

			ImGui.separator();

			ImGui.beginChild("##jsonDisplayer", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY());

			if (currentStyle.json == null) ImGui.text(tr("ImGui.Child.PopUps.JsonViewer", "No data available!"));
			else ImGui.textWrapped(json);
			ImGui.endChild();

			ImGui.endPopup();
		}
	}
}
