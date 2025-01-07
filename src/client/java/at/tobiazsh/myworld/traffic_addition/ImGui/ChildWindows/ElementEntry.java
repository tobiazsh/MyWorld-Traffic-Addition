package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;


/*
 * @created 09/10/2024 (DD/MM/YYYY) - 21:16
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ArrayTools;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Clipboard;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.GroupElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

import java.util.List;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.SignEditor.*;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;

public abstract class ElementEntry {
	private String name;
	private String Id;
	private BaseElement renderObject;
	private List<BaseElement> elementList;
	private String parentId;
	private boolean isClicked = false;

	private int texId;
	private int previewSize = 50;
	private static final int textIconId = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/text.png").getTextureId();
	private static final int groupIconId = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/group.png").getTextureId();
	private static final int redXIcon = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/red_x.png").getTextureId();
	private static final int otherIcon = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/other.png").getTextureId();

	private ElementEntry(String name, String Id) {
		this.name = name;
		this.Id = Id;
	}

	public ElementEntry(String name, String Id, BaseElement element, List<BaseElement> elementList, String parentId) {
		this(name, Id);
		renderObject = element;
		this.elementList = elementList;
		this.parentId = parentId;

		if (element instanceof ImageElement) this.texId = ((ImageElement) element).getTexture().getTextureId();
		if (element instanceof TextElement); // Create TextElement
	}

	public abstract void moveEntryUp();
	public abstract void moveEntryDown();
	public abstract void elementSelectedAction();

	private final int borderCol = ImGui.colorConvertFloat4ToU32(222, 224, 226, 255);
	public float padding = 10;
	public float controlsWidth;
	private final float entryHeight = 64;
	private final float buttonSize = ImGui.getFontSize() + ImGui.getStyle().getFramePadding().y * 2;

	public void render(float windowWidth, float padding, boolean disableUp, boolean disableDown, BaseElement selectedOption) {
		float entryWidth = (windowWidth - (this.padding * 2));
		controlsWidth = 0;
		float framePadding = ImGui.getStyle().getFramePadding().y;

		ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 2);
		ImGui.pushStyleColor(ImGuiCol.Border, borderCol);

		// Main entry container
		ImGui.beginChild("ELEMENT_ENTRY_" + renderObject.getId(), entryWidth, entryHeight + (renderObject instanceof GroupElement && ((GroupElement) renderObject).isExpanded() ? getGroupContentHeight((GroupElement)renderObject) : 0), isClicked, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);

		// Base element content rendering
		renderBaseElementContent(framePadding, selectedOption);


		// Expand Button for GroupElements
		if (renderObject instanceof GroupElement) {
			ImGui.sameLine();
			ImGui.dummy(new ImVec2(10.0f, 0.0f));
			ImGui.sameLine();
			ImGui.setCursorPosY((entryHeight - buttonSize) / 2 - ImGui.getStyle().getFramePadding().y); // Center Button Y
			if (((GroupElement) renderObject).isExpanded()) {
				if (ImGui.arrowButton("##expand", ImGuiDir.Up)) {
					((GroupElement) renderObject).setExpanded(false);
				}
			} else {
				if (ImGui.arrowButton("##expand", ImGuiDir.Down)) {
					((GroupElement) renderObject).setExpanded(true);
				}
			}
		}

		// Name
		ImGui.sameLine();
		ImGui.dummy(new ImVec2(10.0f, 0.0f));
		ImGui.sameLine();
		ImGui.setCursorPosY((entryHeight - imgui.calcTextSize(name).y) / 2 - framePadding - 2); // Center Text; Offset by 2 to fit in with the icons
		ImGui.text(name);

		// Control buttons (right-aligned)
		renderControlButtons(entryWidth, buttonSize, padding, disableUp, disableDown);

		// Group Tree (or Group Content, idc how you call it)
		if (renderObject instanceof GroupElement && ((GroupElement) renderObject).isExpanded()) {
			// Add spacing for the tree content
			ImGui.dummy(0, 10);

			// Create indented region for group contents
			ImGui.indent(previewSize + padding * 4);

			// Render each child element
			for (BaseElement element : ((GroupElement) renderObject).getElements()) {
				ElementEntry entry = createChildElementEntry(element, (GroupElement) renderObject);
				entry.render(windowWidth - (previewSize + padding * 4), padding,
						element == ((GroupElement) renderObject).getElements().getFirst(),
						element == ((GroupElement) renderObject).getElements().getLast(),
						selectedOption);
			}

			ImGui.unindent(previewSize + padding * 4);
		}

		ImGui.endChild();
		ImGui.popStyleColor();
		ImGui.popStyleVar();
	}

	/**
	 * Renders the control buttons on the right side of the entry
	 */
	private void renderControlButtons(float entryWidth, float buttonSize, float padding, boolean disableUp, boolean disableDown) {
		ImGui.setCursorPosY((entryHeight - buttonSize) / 2 - ImGui.getStyle().getFramePadding().y); // Center Buttons Y
		ImGui.setCursorPosX(entryWidth - buttonSize * 4 - padding * 4); // Center Buttons X

		// Context menu button
		if (ImGui.imageButton(otherIcon, ImGui.getFontSize(), ImGui.getFontSize())) {
			ImGui.openPopup("ElementEntryContextMenu##" + renderObject.getId());
		}
		contextualMenu();

		ImGui.sameLine();

		// Delete button
		if (ImGui.imageButton(redXIcon, ImGui.getFontSize(), ImGui.getFontSize())) {
			renderObject.removeMe();
		}

		ImGui.sameLine();

		// Up button
		if (disableUp) ImGui.beginDisabled();
		if (ImGui.arrowButton("##up", ImGuiDir.Up)) moveEntryUp();
		if (disableUp) ImGui.endDisabled();

		ImGui.sameLine();

		// Down button
		if (disableDown) ImGui.beginDisabled();
		if (ImGui.arrowButton("##down", ImGuiDir.Down)) moveEntryDown();
		if (disableDown) ImGui.endDisabled();
	}

	/**
	 * Renders the button with the three dots (...) on the right (it's called contextual menu because I couldn't come up with something better, and it's a context menu and I must admit that I quite like it :D )
	 */
	private void contextualMenu() {
		if (ImGui.beginPopupContextItem("ElementEntryContextMenu##" + renderObject.getId())) {

			if (ImGui.button("Copy")) {
				Clipboard.setCopiedElement(renderObject);
				ImGui.closeCurrentPopup();
			}

			if (ImGui.button("Cut")) {
				Clipboard.setCopiedElement(renderObject);
				renderObject.removeMe();
				ImGui.closeCurrentPopup();
			}

			if (ImGui.button("Duplicate")) {
				BaseElement copiedElement = renderObject.copy();
				copiedElement.setName(renderObject.getName());
				copiedElement.setColor(renderObject.getColor());
				elementList.addFirst(copiedElement);
			}

			int indexInList = elementList.indexOf(renderObject);

			renderGroupControls(indexInList);

			ImGui.endPopup();
		}
	}

	private void renderGroupControls(int indexInList) {
		if (BaseElement.getElementById(renderObject.getParentId()) instanceof GroupElement parentElement) {
			if (ImGui.button("Remove from Group")) {
				if (BaseElement.getElementById(parentElement.getParentId()) instanceof GroupElement parentParentElement) {
					parentParentElement.addElement(renderObject);
					parentElement.removeElement(renderObject);
				} else { // Only happens with "null" is returned and that means the parent is the main element list
					renderObject.setParentId("MAIN");
					elementOrder.add(parentElement.getElements().indexOf(renderObject), renderObject);
					parentElement.removeElement(renderObject);
				}
			}
		}

		if (elementList.size() > indexInList + 1) {
			if (elementList.get(indexInList + 1) instanceof GroupElement) {
				if (ImGui.button("Add to group below")) {
					((GroupElement) elementList.get(indexInList + 1)).addElement(renderObject.copy());
					renderObject.removeMe();
				}
			} else {
				if (ImGui.button("Group with below")) {
					GroupElement groupElement = new GroupElement(0, 0, 0, 0, 0, parentId);
					groupElement.addElement(renderObject.copy());
					groupElement.addElement(elementList.get(indexInList + 1));
					elementList.add(indexInList, groupElement);
					elementList.remove(indexInList + 2); // + 2 because: (Index 1 = New GroupElement) <-- We're here (at indexInList)! (Index 2 = This element) (Index 3 = Element to group with and remove)
					renderObject.removeMe();
				}
			}
		}

		if (!(renderObject instanceof GroupElement)) return;

		// "Ungroup" button. Only active on GroupElements
		if (ImGui.button("Ungroup")) {
			elementList.addAll(elementList.indexOf(renderObject) + 1, ((GroupElement) renderObject).getElements());
			elementList.remove(renderObject);
		}
	}

	private float getGroupContentHeight(GroupElement group) {
		if (!group.isExpanded()) return 0;

		float height = 10; // Initial spacing
		for (BaseElement element : group.getElements()) {
			height += entryHeight + padding;
			if (element instanceof GroupElement && ((GroupElement)element).isExpanded()) {
				height += getGroupContentHeight((GroupElement)element);
			}
		}
		return height;
	}

	private void renderBaseElementContent(float framePadding, BaseElement selectedOption) {
		// Selection Button
		ImGui.setCursorPosX(this.padding * 2);
		ImGui.setCursorPosY((entryHeight - imgui.calcTextSize("T").y) / 2 - framePadding);
		if (ImGui.radioButton("##radioButton", Objects.equals(selectedOption, renderObject))) elementSelectedAction();

		ImGui.sameLine();

		// Preview
		ImGui.setCursorPosY((entryHeight - previewSize) / 2);
		ImGui.setCursorPosX(ImGui.getCursorPosX() + 2 * this.padding);
		if (renderObject instanceof ImageElement) ImGui.image(texId, previewSize, previewSize);
		else if (renderObject instanceof TextElement) ImGui.image(textIconId, previewSize, previewSize);
		else if (renderObject instanceof GroupElement) ImGui.image(groupIconId, previewSize, previewSize);
	}

	private ElementEntry createChildElementEntry(BaseElement element, GroupElement grpElement) {
		return new ElementEntry(element.getName(), element.getId(), element, grpElement.getElements(), grpElement.getId()) {
			@Override
			public void moveEntryUp() {
				grpElement.setElements(ArrayTools.moveElementUpBy(
                        grpElement.getElements(),
                        grpElement.getElements().indexOf(element),
                        1
                ));
			}

			@Override
			public void moveEntryDown() {
				grpElement.setElements(ArrayTools.moveElementDownBy(
                        grpElement.getElements(),
                        grpElement.getElements().indexOf(element),
                        1
                ));
			}

			@Override
			public void elementSelectedAction() {
				selectedElement = element;
				ElementPropertyWindow.initVars(element, grpElement.getElements(), signRatio);
			}
		};
	}
}