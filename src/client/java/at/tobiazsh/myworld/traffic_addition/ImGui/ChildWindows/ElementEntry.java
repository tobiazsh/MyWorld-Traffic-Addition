package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows;


/*
 * @created 09/10/2024 (DD/MM/YYYY) - 21:16
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.*;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.FileDialogPopup;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.ArrayTools;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Clipboard;
import at.tobiazsh.myworld.traffic_addition.Utils.Saves;
import at.tobiazsh.myworld.traffic_addition.Utils.Texturing.Textures;
import com.google.gson.JsonObject;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.SignEditor.*;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData.getPrettyJson;

public abstract class ElementEntry {
	private final String name;
	private ClientElementInterface renderObject;
	private UUID parentId;

    private int texId;
	private final int previewSize = 50;
	private static final int textIconId = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/text.png").getTextureId();
	private static final int groupIconId = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/group.png").getTextureId();
	private static final int redXIcon = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/red_x.png").getTextureId();
	private static final int otherIcon = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/other.png").getTextureId();

	private ElementEntry(String name) {
		this.name = name;
	}

	public ElementEntry(ClientElementInterface element, UUID parentId) {
		this(element.getName());
		renderObject = element;
		this.parentId = parentId;

		if (element instanceof ImageElementClient) this.texId = ((ImageElementClient) element).getTexture().getTextureId();
    }

	public abstract void moveEntryUp();
	public abstract void moveEntryDown();
	public abstract void elementSelectedAction();

	// List actions
	public abstract ClientElementInterface getElement(int i);
	public abstract void addElementFirst(ClientElementInterface element);
	public abstract int indexOfElement(ClientElementInterface element);
	public abstract int sizeOfList();
	public abstract void addElement(ClientElementInterface element);
	public abstract void addElement(int index, ClientElementInterface element);
	public abstract void addAllElements(List<ClientElementInterface> elements);
	public abstract void addAllElements(int index, List<ClientElementInterface> elements);
	public abstract void removeElement(ClientElementInterface element);
	public abstract void removeElement(int index);
	public abstract void deleteElement(ClientElementInterface element);

	private final int borderCol = ImGui.colorConvertFloat4ToU32(222, 224, 226, 255);
	public float padding = 10;
	public float controlsWidth;
	private final float entryHeight = 64;
	private final float buttonSize = ImGui.getFontSize() + ImGui.getStyle().getFramePadding().y * 2;

	public void render(float windowWidth, float padding, boolean disableUp, boolean disableDown, ClientElementInterface selectedOption) {
		float entryWidth = (windowWidth - (this.padding * 2));
		controlsWidth = 0;
		float framePadding = ImGui.getStyle().getFramePadding().y;

		ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 2);
		ImGui.pushStyleColor(ImGuiCol.Border, borderCol);

		// Main entry container
        boolean isClicked = false;
        ImGui.beginChild("ELEMENT_ENTRY_" + renderObject.getId(), entryWidth, entryHeight + (renderObject instanceof GroupElementClient && ((GroupElementClient) renderObject).isExpanded() ? getGroupContentHeight((GroupElementClient) renderObject) : 0), isClicked, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);

		// Base element content rendering
		renderBaseElementContent(framePadding, selectedOption);

		// Expand Button for GroupElements
		if (renderObject instanceof GroupElementClient) {
			ImGui.sameLine();
			ImGui.dummy(new ImVec2(10.0f, 0.0f));
			ImGui.sameLine();
			ImGui.setCursorPosY((entryHeight - buttonSize) / 2 - ImGui.getStyle().getFramePadding().y); // Center Button Y
			if (((GroupElementClient) renderObject).isExpanded()) {
				if (ImGui.arrowButton("##expand", ImGuiDir.Up)) {
					((GroupElementClient) renderObject).setExpanded(false);
				}
			} else {
				if (ImGui.arrowButton("##expand", ImGuiDir.Down)) {
					((GroupElementClient) renderObject).setExpanded(true);
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
		if (renderObject instanceof GroupElementClient && ((GroupElementClient) renderObject).isExpanded()) {
			// Add spacing for the tree content
			ImGui.dummy(0, 10);

			// Create indented region for group contents
			ImGui.indent(previewSize + padding * 4);

			// Render each child element
			for (ClientElementInterface element : ((GroupElementClient) renderObject).getClientElements()) {
				ElementEntry entry = createChildElementEntry(element, (GroupElementClient) renderObject);
				entry.render(windowWidth - (previewSize + padding * 4), padding,
						element == ((GroupElementClient) renderObject).getClientElements().getFirst(),
						element == ((GroupElementClient) renderObject).getClientElements().getLast(),
						selectedOption
				);
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
			deleteElement(renderObject);
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

			if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.copy").getString())) { // "Copy" button
				Clipboard.getInstance().setCopiedElement(renderObject.copy());
				ImGui.closeCurrentPopup();
			}

			if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.cut").getString())) { // "Cut" button
				Clipboard.getInstance().setCopiedElement(renderObject.copy());
				deleteElement(renderObject);
				ImGui.closeCurrentPopup();
			}

			if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.duplicate").getString())) { // "Duplicate" button
				ClientElementInterface copiedElement = renderObject.copy();
				addElementFirst(copiedElement);
			}

			if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.export").getString() + "...")) exportElement(); // "Export..." button

			int indexInList = indexOfElement(renderObject);

			renderGroupControls(indexInList);

			ImGui.endPopup();
		}
	}

	private void renderGroupControls(int indexInList) {

		if (ClientElementManager.getInstance().getElementById(renderObject.getParentId()) instanceof GroupElementClient parentElement) { // Only show if element is inside a Group / has a parent
			if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.remove_from_group").getString())) { // "Remove from group" button
				if (ClientElementManager.getInstance().getElementById(parentElement.getParentId()) instanceof GroupElementClient parentParentElement) { // If parent has another parent, execute this instead
					parentParentElement.addClientElement(renderObject.copy()); // Add the element to be removed to the parent of the parent
					parentElement.removeClientElement(renderObject);
				} else { // Only happens when null is returned and that means the parent is the main element and it isn't enclosed by anything else
					renderObject.setParentId(ClientElementInterface.MAIN_CANVAS_ID); // Set to main since parent does not exist and element is now in root folder
					ClientElementManager.getInstance().addElement(ClientElementManager.getInstance().indexOfElement(parentElement), renderObject.copy());
					parentElement.removeClientElement(renderObject);
				}
			}
		}

		if (sizeOfList() > indexInList + 1) {
			if (getElement(indexInList + 1) instanceof GroupElementClient) {
				if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.add_to_group_below").getString())) { // "Add to group below" button
					((GroupElementClient) getElement(indexInList + 1)).addClientElement(renderObject.copy());
					((GroupElementClient) getElement(indexInList + 1)).addClientElementFirst(renderObject.copy());
					deleteElement(renderObject);
				}
			}

			if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.group_with_below").getString())) { // "Group with below" button
				GroupElementClient groupElement = new GroupElementClient(0, 0, 0, 0, 0, null, parentId);
				addElement(indexInList, groupElement); // First add the new GroupElement at the current index so it gets a new ID and is registered in the element list. Done, so client elements can have a parent id that's not null
				groupElement.addClientElement(renderObject.copy()); // Now add the elements
				groupElement.addClientElement(getElement(indexInList + 2).copy());
				deleteElement(getElement(indexInList + 2));  // + 2 because: (Index 1 = New GroupElement) <-- We're here (at indexInList)! (Index 2 = This element) (Index 3 = Element to group with and remove)
				deleteElement(renderObject);
			}
		}

		if (!(renderObject instanceof GroupElementClient)) return; // Render the following only when the renderObject is a GroupElement

		// "Ungroup" button. Only active on GroupElements
		if (ImGui.button(Text.translatable("mwta.imgui.sign.editor.ungroup").getString())) { // "Ungroup" button
			// If the group element isn't enclosed by any other group, give the children "MAIN" as parent id, otherwise give the id of the enclosing element, that encloses the group element
			UUID newParentId = Objects.equals(renderObject.getParentId(), ClientElementInterface.MAIN_CANVAS_ID) ? ClientElementInterface.MAIN_CANVAS_ID : renderObject.getParentId();

			List<ClientElementInterface> elements = ((GroupElementClient) renderObject).getClientElements();
			elements.forEach(element -> element.setParentId(newParentId)); // Give children new parent id

			addAllElements(indexOfElement(renderObject) + 1, elements); // Index +1 because renderObject is the Group and GroupIndex + 1 is the first child index
			removeElement(renderObject); // Remove Group Element from element list
		}
	}

	private float getGroupContentHeight(GroupElementClient group) {
		if (!group.isExpanded()) return 0;

		float height = 10; // Initial spacing
		for (ClientElementInterface element : group.getClientElements()) {
			height += entryHeight + padding;
			if (element instanceof GroupElementClient && ((GroupElementClient)element).isExpanded()) {
				height += getGroupContentHeight((GroupElementClient)element);
			}
		}
		return height;
	}

	private void renderBaseElementContent(float framePadding, ClientElementInterface selectedOption) {
		// Selection Button
		ImGui.setCursorPosX(this.padding * 2);
		ImGui.setCursorPosY((entryHeight - imgui.calcTextSize("T").y) / 2 - framePadding);
		if (ImGui.radioButton("##radioButton", Objects.equals(selectedOption, renderObject))) elementSelectedAction();

		ImGui.sameLine();

		// Preview
		ImGui.setCursorPosY((entryHeight - previewSize) / 2);
		ImGui.setCursorPosX(ImGui.getCursorPosX() + 2 * this.padding);
		if (renderObject instanceof ImageElementClient) ImGui.image(texId, previewSize, previewSize);
		else if (renderObject instanceof TextElementClient) ImGui.image(textIconId, previewSize, previewSize);
		else if (renderObject instanceof GroupElementClient) ImGui.image(groupIconId, previewSize, previewSize);
	}

	private ElementEntry createChildElementEntry(ClientElementInterface element, GroupElementClient grpElement) {
		return new ElementEntry(element, grpElement.getId()) {
			@Override
			public void moveEntryUp() {
				grpElement.setClientElements(ArrayTools.moveElementUpBy(
                        grpElement.getClientElements(),
                        grpElement.getClientElements().indexOf(element),
                        1
                ));
			}

			@Override
			public void moveEntryDown() {
				grpElement.setClientElements(ArrayTools.moveElementDownBy(
                        grpElement.getClientElements(),
                        grpElement.getClientElements().indexOf(element),
                        1
                ));
			}

			@Override
			public void elementSelectedAction() {
				selectedElement = element;
				ElementPropertyWindow.initVars(element, signRatio);
			}

			@Override
			public ClientElementInterface getElement(int i) {
				return grpElement.getClientElements().get(i);
			}

			@Override
			public void addElementFirst(ClientElementInterface element) {
				grpElement.addClientElementFirst(element);
			}

			@Override
			public int indexOfElement(ClientElementInterface element) {
				return grpElement.getClientElements().indexOf(element);
			}

			@Override
			public int sizeOfList() {
				return grpElement.getClientElements().size();
			}

			@Override
			public void addElement(ClientElementInterface element) {
				grpElement.addClientElement(element);
			}

			@Override
			public void addElement(int index, ClientElementInterface element) {
				grpElement.addClientElement(index, element);
			}

			@Override
			public void addAllElements(List<ClientElementInterface> elements) {
				grpElement.addAllElements(elements);
			}

			@Override
			public void addAllElements(int index, List<ClientElementInterface> elements) {
				grpElement.addAllElements(index, elements);
			}

			@Override
			public void removeElement(ClientElementInterface element) {
				grpElement.removeClientElement(element);
			}

			@Override
			public void removeElement(int index) {
				grpElement.removeClientElement(index);
			}

			@Override
			public void deleteElement(ClientElementInterface element) {
				grpElement.removeClientElement(element);
				ClientElementManager.getInstance().unregisterElement(element);
			}
		};
	}

	private void exportElement() {
		Saves.createSavesDir();

		JsonObject modifiedJson = renderObject.toJson();
		modifiedJson.addProperty("Id", "null");
		modifiedJson.addProperty("ParentId", "null");
		FileDialogPopup.setData(getPrettyJson(modifiedJson.toString()));

		FileDialogPopup.open(
				Saves.getElementSaveDir(),
				FileDialogPopup.FileDialogType.SAVE,
				(path) -> MyWorldTrafficAddition.LOGGER.info("Saved file successfully! Path: {}", path.toString()),
				"MWTACSELEMENT", "JSON"
		);
	}
}