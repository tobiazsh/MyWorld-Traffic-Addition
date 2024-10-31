package at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents;


/*
 * @created 09/10/2024 (DD/MM/YYYY) - 21:16
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.Objects;

public abstract class ElementEntry {
	private String name;
	private String Id;
	private BaseElement renderObject;
	private boolean isClicked = false;

	private int texId;
	private int previewSize = 50;
	private static final int textIconId = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/text.png").getTextureId();
	private static final int redXIcon = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/red_x.png").getTextureId();

	private static final ImGui imgui = new ImGui();

	private ElementEntry(String name, String Id) {
		this.name = name;
		this.Id = Id;
	}

	public void setClicked(boolean clicked) {
		isClicked = clicked;
	}

	public ElementEntry(String name, String Id, BaseElement element) {
		this(name, Id);
		renderObject = element;

		if (element instanceof ImageElement) this.texId = ((ImageElement) element).getTexture().getTextureId();
		if (element instanceof TextElement); // Create TextElement
	}

	public void changeName(String name) {
		this.name = name;
	}

	public abstract void moveEntryUp();
	public abstract void moveEntryDown();
	public abstract void elementSelectedAction();

	private final int borderCol = ImGui.colorConvertFloat4ToU32(222, 224, 226, 255);
	public float padding = 10;
	public float controlsWidth;
	private final float entryHeight = 64;
	private final float buttonSize = ImGui.getFontSize() + ImGui.getStyle().getFramePadding().y * 2;
	public boolean removeMyself = false;

	public void render(float windowWidth, float padding, boolean disableUp, boolean disableDown, BaseElement selectedOption) {
		float entryWidth = (windowWidth - (this.padding * 2));
		controlsWidth = 0;

		float framePadding = ImGui.getStyle().getFramePadding().y;

		ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 2);
		ImGui.pushStyleColor(ImGuiCol.Border, borderCol);

		ImGui.beginChild("ELEMENT_ENTRY_" + renderObject.id, entryWidth, entryHeight, isClicked, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);

		// Selection Button
		ImGui.setCursorPosX(this.padding * 2);
		ImGui.setCursorPosY((entryHeight - imgui.calcTextSize("T").y) / 2 - framePadding);
		if (ImGui.radioButton("##radioButton", Objects.equals(selectedOption, renderObject))) elementSelectedAction();

		ImGui.sameLine();

		// Preview
		ImGui.setCursorPosY((entryHeight - previewSize) / 2); // Center Preview
		ImGui.setCursorPosX(ImGui.getCursorPosX() + 2 * this.padding);
		if (renderObject instanceof ImageElement) ImGui.image(texId, previewSize, previewSize);
		else if (renderObject instanceof TextElement) ImGui.image(textIconId, previewSize, previewSize);

		ImGui.sameLine();
		ImGui.dummy(new ImVec2(10.0f, 0.0f));
		ImGui.sameLine();
		ImGui.setCursorPosY((entryHeight - imgui.calcTextSize(name).y) / 2 - framePadding); // Center Text
		ImGui.text(name);

		ImGui.sameLine();

		ImGui.setCursorPosY((entryHeight - buttonSize) / 2 - framePadding); // Center Buttons
		ImGui.setCursorPosX(entryWidth - buttonSize * 3 - padding * 3);

		if (ImGui.imageButton(redXIcon, ImGui.getFontSize(), ImGui.getFontSize())) {
			// Remove Element
			removeMyself = true;
		}

		ImGui.sameLine();

		if (disableUp) ImGui.beginDisabled(); // Disable Button

		if (ImGui.arrowButton("##up", ImGuiDir.Up)) moveEntryUp();

		// If down is also disabled, don't end
		if (disableUp) ImGui.endDisabled();

		ImGui.sameLine();

		if (disableDown) ImGui.beginDisabled();

		if (ImGui.arrowButton("##down", ImGuiDir.Down)) moveEntryDown();

		if (disableDown) ImGui.endDisabled(); // Finally end

		ImGui.endChild();

		ImGui.popStyleColor();
		ImGui.popStyleVar();
	}
}