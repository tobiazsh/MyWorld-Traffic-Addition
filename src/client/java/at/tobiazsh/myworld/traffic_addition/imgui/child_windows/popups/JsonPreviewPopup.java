package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 17:47
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.utils.JsonUtil;
import imgui.ImGui;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class JsonPreviewPopup {

	private boolean shouldOpen = false;
	private final String id;
	private String json;

	public JsonPreviewPopup(String id) {
		this.id = id;
	}

	public void open(CustomizableSignTextureData textureData) {
		shouldOpen = false;
		ImGui.openPopup(id);
		json = JsonUtil.toPrettyJson(textureData.toJson().toString());
	}

	public void render() {
		if (ImGui.beginPopupModal(id)) {

			if (ImGui.button(tr("Global", "Close"))) {
				shouldOpen = false;
				ImGui.closeCurrentPopup();
			}

			ImGui.sameLine();

			if (ImGui.button(tr("ImGui.Child.PopUps.JsonViewer", "Copy to Clipboard")))
				ImGui.setClipboardText(json);

			ImGui.separator();

			ImGui.beginChild("##jsonDisplayer", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY());
			ImGui.textWrapped(json);
			ImGui.endChild();

			ImGui.endPopup();
		}
	}
}
