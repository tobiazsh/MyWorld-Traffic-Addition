package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 17:47
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.utils.JsonUtil;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import org.jspecify.annotations.NonNull;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class JsonPreviewPopup {

	private boolean shouldOpen = false;
	private final String id;
	private String json;
	private final @NonNull ImGuiFontScope fontScope;

	public JsonPreviewPopup(String id, @NonNull ImGuiFontScope fontScope) {
		this.id = id;
		this.fontScope = fontScope;
	}

	public void open(CustomizableSignTextureData textureData) {
		shouldOpen = true;
		ImGui.openPopup(id);
		json = JsonUtil.toPrettyJson(textureData.toJson().toString());
	}

	public void render() {
		if (ImGui.beginPopupModal(id, ImGuiWindowFlags.AlwaysAutoResize)) {

			if (ImGui.button(tr("Global", "Close"))) {
				shouldOpen = false;
				ImGui.closeCurrentPopup();
			}

			ImGui.sameLine();

			if (ImGui.button(tr("ImGui.Child.PopUps.JsonViewer", "Copy to Clipboard"))) {
				ImGui.setClipboardText(json);
			}

			ImGui.separator();

			this.fontScope.push(DefaultFonts.JetbrainsMonoRegular, DefaultFonts.NORMAL_SIZE);
			ImGui.pushStyleColor(ImGuiCol.ChildBg, 50, 50, 50, 255);
			ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, new ImVec2(10, 10));

			ImGui.beginChild("##jsonDisplayer", ImGuiChildFlags.AlwaysUseWindowPadding, 0);
			ImGui.text(json);
			ImGui.endChild();

			ImGui.popStyleVar();
			ImGui.popStyleColor();
			this.fontScope.pop();

			ImGui.endPopup();
		}

		if (shouldOpen) {
			ImGui.openPopup(id);
			shouldOpen = false;
		}
	}
}
