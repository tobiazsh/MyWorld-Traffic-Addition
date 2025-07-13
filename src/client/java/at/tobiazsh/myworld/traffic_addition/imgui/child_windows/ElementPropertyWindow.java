package at.tobiazsh.myworld.traffic_addition.imgui.child_windows;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 16:00
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.imgui.main_windows.SignEditor;
import at.tobiazsh.myworld.traffic_addition.utils.BasicFont;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.FontManager;
import at.tobiazsh.myworld.traffic_addition.utils.LinkedHashMapTool;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.text.Text;

import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class ElementPropertyWindow {

	private static ImString currentElementName = new ImString("", 512);
	private static float[] currentElementRotation = new float[]{};
	private static float factor;
	private static float[] color;
	private static ClientElementInterface element;
	private static List<ClientElementInterface> elementList; // The list where BaseElement element is in

	private static boolean relateSize = true;
	private static ImVec2 ratioedSignSize = new ImVec2();

	private static ImString textElementText = new ImString("", 1024);
	private static ImString previousTextElementText = new ImString("", 1024);
	private static ImFloat fontSize = new ImFloat(0);
	private static ImString fontPath = new ImString("", 512);
	private static final LinkedHashMap<String, String> availableFonts = new LinkedHashMap<>(); // Path; Name
	private static final List<String> availableFontNames = new ArrayList<>(); // Names of available fonts

	private static final ImInt selectedFontIndex = new ImInt(0);
	private static int previousSelectedFontIndex = 0;

	static {
		FileSystem.Folder fontsFolder = FontManager.getAvailableFonts().removeFoldersCurrentDir().concentrateFileType("TTF");

		for (FileSystem.DirectoryElement element : fontsFolder) {
			if (!element.isFile()) continue;

			String name = element.name.substring(0, element.name.lastIndexOf('.'));
			availableFonts.put(element.path, name);

			String[] nameParts = name.split("_");

			for (int i = 0; i < nameParts.length; i++) {
				nameParts[i] = nameParts[i].substring(0, 1).toUpperCase() + nameParts[i].substring(1);
			}

			availableFontNames.add(String.join(" ", nameParts));
		}
	}

	public static boolean shouldRender = false;

	public static void initVars(ClientElementInterface element, ImVec2 ratioedSignSize) {
		ElementPropertyWindow.currentElementName = new ImString(element.getName(), 512);
		ElementPropertyWindow.currentElementRotation = new float[]{element.getRotation()};
		relateSize = true;
		ElementPropertyWindow.element = element;
		ElementPropertyWindow.ratioedSignSize = ratioedSignSize;
		factor = element.getFactor();
		color = element.getColor();

		if (element instanceof TextElementClient) {
			textElementText = new ImString(((TextElementClient) element).getText(), 1024);
			fontSize = new ImFloat(((TextElementClient) element).getFont().getFontSize());
			fontPath = new ImString(((TextElementClient) element).getFont().getFontPath(), 512);
			selectedFontIndex.set(LinkedHashMapTool.getIndex(availableFonts, ((TextElementClient) element).getFont().getFontPath()));
		}
	}

	public static void render() {
		if (!shouldRender) return;

		if (ImGui.begin(tr("ImGui.Child.ElementPropertyWindow", "Element Properties"), ImGuiWindowFlags.NoNavInputs)) { // Translatable text for "Element Properties"

			// If no Element is selected, display message
			if (element == null) {
				ImGui.pushFont(ImGuiImpl.RobotoBold);

				String text = tr("ImGui.Child.ElementPropertyWindow", "No Element Selected");

				ImVec2 textSize = imgui.calcTextSize(text);
				ImGui.setCursorPos((ImGui.getWindowWidth() - textSize.x) / 2, (ImGui.getWindowHeight() - textSize.y) / 2);
				ImGui.text(text);

				ImGui.popFont();

				ImGui.end();
				return;
			}

			float[] elemW = {element.getWidth()};
			float[] elemH = {element.getHeight()};
			float[] elemX = {element.getX()};
			float[] elemY = {element.getY()};

			// NAMING

			ImGui.pushFont(ImGuiImpl.RobotoBold);
			ImGui.text(tr("ImGui.Child.ElementPropertyWindow", "Element Name")); // Translatable text for "Element Name"
			ImGui.popFont();

			ImGui.inputText("##nameInput", currentElementName);

			if (ImGui.button(tr("Global", "Confirm") + "##name")) { // Translatable text for "Confirm"
				element.setName(currentElementName.get());
				SignEditor.addUndo();
			}

			// SIZE

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.RobotoBold);
			ImGui.text(tr("Global", "Size")); // Translatable text for "Size"
			ImGui.popFont();

			if (ImGui.checkbox(tr("Global", "Relate"), relateSize)) relateSize = !relateSize; // Translatable text for "Relate"

			float aspectRatioW = elemH[0] / elemW[0];
			float aspectRatioH = elemW[0] / elemH[0];

			// Width Drag
			if (ImGui.dragFloat(tr("Global", "Width"), elemW, 1.0f, 0.1f, ratioedSignSize.x)) { // Translatable text for "Width"

				if (relateSize) {
					elemH[0] = elemW[0] * aspectRatioW; // Adjust height based on new width
					element.setHeight(elemH[0]);
				}

				element.setWidth(elemW[0]);
			}

			if (ImGui.isItemDeactivated()) SignEditor.addUndo();

			// Height Drag
			if (ImGui.dragFloat(tr("Global", "Height"), elemH, 1.0f, 0.1f, ratioedSignSize.y)) { // Translatable text for "Height"

				if (relateSize) {
					elemW[0] = elemH[0] * aspectRatioH; // Adjust height based on new width
					element.setWidth(elemW[0]);
				}

				element.setHeight(elemH[0]);
			}

			if (ImGui.isItemDeactivated()) SignEditor.addUndo();

			// POSITIONING

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.RobotoBold);
			ImGui.text(tr("Global", "Position")); // Translatable text for "Position"
			ImGui.popFont();

			// Drag Float for the position of the element on the X-Coordinate; Max is the sign's height minus the element's height to not exceed the bounds
			if (ImGui.dragFloat("X", elemX, 1.0f, 0.0f, ratioedSignSize.x - elemW[0])) {
				element.setX(elemX[0]);
			}

			if (ImGui.isItemDeactivated()) SignEditor.addUndo();

			// Drag Float for the position of the element on the Y-Coordinate; Max is the sign's width minus the element's width to not exceed the bounds
			if (ImGui.dragFloat("Y", elemY, 1.0f, 0.0f, ratioedSignSize.y - elemH[0])) {
				element.setY(elemY[0]);
			}

			if (ImGui.isItemDeactivated()) SignEditor.addUndo();

			// Button that centers the current selected element on the X-Coordinate
			if (ImGui.button(tr("ImGui.Child.ElementPropertyWindow", "Center X"))) { // Translatable text for "Center X"
				element.setX((ratioedSignSize.x - elemW[0]) / 2);
				SignEditor.addUndo();
			}

			// Button that centers the current selected element on the Y-Coordinate
			if (ImGui.button(tr("ImGui.Child.ElementPropertyWindow", "Center Y"))) { // Translatable text for "Center Y"
				element.setY((ratioedSignSize.y - elemH[0]) / 2);
				SignEditor.addUndo();
			}

			if (ImGui.button(tr("ImGui.Child.ElementPropertyWindow", "Center") + "...")) { // Translatable text for "Center..."
				// TODO: Create Centering logic that can adapt to different contexts and relate to different elements
			}

			// ROTATION

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.RobotoBold);
			ImGui.text(tr("Global", "Rotation")); // Translatable text for "Rotation"
			ImGui.popFont();

			if (ImGui.dragFloat("##rotationDragger", currentElementRotation, 1.0f, 0, (float)359.99)) {
				element.setRotation(currentElementRotation[0]);
			}

			if (ImGui.isItemDeactivated()) SignEditor.addUndo();

			// COLOR

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.RobotoBold);
			ImGui.text(tr("Global", "Color")); // Translatable text for "Color"
			ImGui.popFont();

			int alphaSettings = ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf; // Disable if TextElement is selected as Minecraft doesn't support alpha in text rendering

			if (ImGui.colorPicker4(tr("Global", "Color Picker"), color, alphaSettings)) { // Translatable text for "Color Picker"
				element.setColor(color);
			}

			if (ImGui.isItemDeactivated()) SignEditor.addUndo();

			if (element instanceof TextElementClient) renderTextControls();
		}

		ImGui.end();
	}

	private static void renderTextControls() {
		ImGui.separator();

		previousTextElementText = new ImString(textElementText.get());

		ImGui.pushFont(ImGuiImpl.RobotoBold);
		ImGui.text(tr("Global", "Text")); // Translatable text for "Text"
		ImGui.popFont();
		ImGui.inputText("##textElementTextEditInput", textElementText);

		if (ImGui.isItemDeactivated()) SignEditor.addUndo();

		if (!(textElementText.get().equals(previousTextElementText.get()))) {
			((TextElementClient) element).setText(textElementText.get());
		}

		ImGui.spacing();

		// Font Size
		ImGui.pushFont(ImGuiImpl.RobotoBold);
		ImGui.text(tr("Global", "Font Size")); // Translatable text for "Font Size"
		ImGui.popFont();
		ImGui.inputFloat("##fontSizeInput", fontSize);
		if (ImGui.button(tr("Global", "Confirm") + "##fontSize")) { // "Confirm" text
			((TextElementClient) element).setFont(
					new BasicFont(
							((TextElementClient) element).getFont().getFontPath(),
							fontSize.get()
					)
			);
		}

		if (ImGui.isItemDeactivated()) SignEditor.addUndo();

		ImGui.spacing();

		// Font Selection (Font Path)
		ImGui.pushFont(ImGuiImpl.RobotoBold);
		ImGui.text(tr("Global", "Font")); // Translatable text for "Font"
		ImGui.popFont();

		previousSelectedFontIndex = selectedFontIndex.get();
		ImGui.combo("##fontInput", selectedFontIndex, availableFontNames.toArray(new String[0]));

		if (ImGui.isItemDeactivated()) SignEditor.addUndo();

		// If font changes
		if (previousSelectedFontIndex != selectedFontIndex.get()) {
			((TextElementClient) element).setFont(
					new BasicFont(
						LinkedHashMapTool.getKeyAtIndex(availableFonts, selectedFontIndex.get()),
						((TextElementClient) element).getFont().getFontSize()
					)
			);
		}

		ImGui.spacing();
		ImGui.pushFont(ImGuiImpl.RobotoBold);
		ImGui.text(tr("ImGui.Child.ElementPropertyWindow", "Other Controls"));
		ImGui.popFont();
		if (ImGui.button(tr("ImGui.Child.ElementPropertyWindow", "Normalize Size"))) { // Translatable text for "Normalize Size"
			((TextElementClient) element).setWidthCalculated(false);
			SignEditor.addUndo();
		}
	}

	public static void toggle() {
		shouldRender = !shouldRender;
	}
}
