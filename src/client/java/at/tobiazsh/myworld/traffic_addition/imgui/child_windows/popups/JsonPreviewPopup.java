package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 17:47
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.data.Background;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.utils.JsonUtil;
import imgui.ImGui;

import java.util.ArrayList;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class JsonPreviewPopup {
	private static CustomizableSignTextureData textureData = new CustomizableSignTextureData(Background.TRANSPARENT, new ArrayList<>());

	public static boolean shouldOpen = false;
	public static String windowId = null;
	private static String json;

	public static void open(CustomizableSignTextureData textureData) {
		JsonPreviewPopup.textureData = textureData;
		shouldOpen = false;
		ImGui.openPopup(windowId);

		json = JsonUtil.toPrettyJson(textureData.toJson().toString());
	}

	public static void render() {
		if (windowId == null)
			windowId = tr("ImGui.Child.PopUps.JsonViewer", "Json Viewer");

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

			if (textureData.toJson().isEmpty()) ImGui.text(tr("ImGui.Child.PopUps.JsonViewer", "No data available!"));
			else ImGui.textWrapped(json);
			ImGui.endChild();

			ImGui.endPopup();
		}
	}
}
