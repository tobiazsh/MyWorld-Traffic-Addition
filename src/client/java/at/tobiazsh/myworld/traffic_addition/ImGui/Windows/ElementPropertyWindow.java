package at.tobiazsh.myworld.traffic_addition.ImGui.Windows;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 16:00
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FontManager;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.Utils.LinkedHashMapTool;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.*;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen.baseElementDrawOrder;

public class ElementPropertyWindow {
	private static ImString currentElementName = new ImString("", 512);
	private static float[] currentElementRotation = new float[]{};
	private static BaseElement element;
	private static final ImGui imgui = new ImGui();
	private static boolean relateSize = true;
	private static ImVec2 ratioedSignSize = new ImVec2();
	private static float factor;
	private static float[] color;

	private static ImString textElementText = new ImString("", 1024);
	private static ImFloat fontSize = new ImFloat(0);
	private static ImString fontPath = new ImString("", 512);
	private static final LinkedHashMap<String, String> availableFonts = new LinkedHashMap<>();
	private static final List<String> availableFontNames = new ArrayList<>(); // Names of available fonts
	private static final ImInt selectedFontIndex = new ImInt(0);
	private static int previousSelectedFontIndex = 0;

	static {
		FileSystem.Folder fontsFolder = FontManager.getAvailableFonts().removeFoldersCurrentDir().concentrateFileType("TTF");

		for (FileSystem.DirectoryElement element : fontsFolder) {
			if (!element.isFile()) continue;

			String name = element.name.substring(0, element.name.lastIndexOf('.'));
			availableFonts.put(element.path, name);
			availableFontNames.add(name);
		}
	}

	private static boolean isImage = true;

	public static boolean shouldRender = false;

	public static void initVars(BaseElement element, ImVec2 ratioedSignSize) {
		ElementPropertyWindow.currentElementName = new ImString(element.name, 512);
		ElementPropertyWindow.currentElementRotation = new float[]{element.getRotation()};
		relateSize = true;
		ElementPropertyWindow.element = element;
		ElementPropertyWindow.ratioedSignSize = ratioedSignSize;
		factor = element.getFactor();
		color = element.getColor();

		if (element instanceof TextElement) {
			isImage = false;
			textElementText = new ImString(((TextElement) element).getText(), 1024);
			fontSize = new ImFloat(((TextElement) element).getFont().getFontSize());
			fontPath = new ImString(((TextElement) element).getFont().getFontPath(), 512);
			selectedFontIndex.set(LinkedHashMapTool.getIndex(availableFonts, ((TextElement) element).getFont().getFontPath()));
		} else {
			isImage = true;
		}
	}

	// TODO: Clear when closing everything

	public static void render() {
		if (!shouldRender) return;

		if (ImGui.begin("Element Properties", ImGuiWindowFlags.NoNavInputs)) {

			// If no Element is selected, display message
			if (element == null) {
				ImGui.pushFont(ImGuiImpl.DejaVuSansBold);

				String text = "No Element selected!";

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

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Element Name");
			ImGui.popFont();

			ImGui.inputText("##nameInput", currentElementName);

			if (ImGui.button("Confirm##name")) {
				int index = baseElementDrawOrder.indexOf(element);
				element.name = currentElementName.get();
				baseElementDrawOrder.set(index, element);
			}

			// SIZE

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Size");
			ImGui.popFont();

			if (ImGui.checkbox("Relate", relateSize)) relateSize = !relateSize;

			float aspectRatioW = elemH[0] / elemW[0];
			float aspectRatioH = elemW[0] / elemH[0];

			if (ImGui.dragFloat("Width", elemW, 1.0f, 0.1f, ratioedSignSize.x)) {
				int index = baseElementDrawOrder.indexOf(element);

				if (relateSize) {
					elemH[0] = elemW[0] * aspectRatioW; // Adjust height based on new width
					element.setHeight(elemH[0]);
				}

				element.setWidth(elemW[0]);
				baseElementDrawOrder.set(index, element);
			}

			if (ImGui.dragFloat("Height", elemH, 1.0f, 0.1f, ratioedSignSize.y)) {
				int index = baseElementDrawOrder.indexOf(element);

				if (relateSize) {
					elemW[0] = elemH[0] * aspectRatioH; // Adjust height based on new width
					element.setWidth(elemW[0]);
				}

				element.setHeight(elemH[0]);
				baseElementDrawOrder.set(index, element);
			}


			// POSITIONING

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Position");
			ImGui.popFont();

			// Drag Float for the position of the element on the X-Coordinate; Max is the sign's height minus the element's height to not exceed the bounds
			if (ImGui.dragFloat("X", elemX, 1.0f, 0.0f, ratioedSignSize.x - elemW[0])) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setX(elemX[0]);
				baseElementDrawOrder.set(index, element);
			}

			// Drag Float for the position of the element on the Y-Coordinate; Max is the sign's width minus the element's width to not exceed the bounds
			if (ImGui.dragFloat("Y", elemY, 1.0f, 0.0f, ratioedSignSize.y - elemH[0])) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setY(elemY[0]);
				baseElementDrawOrder.set(index, element);
			}

			// Button that centers the current selected element on the X-Coordinate
			if (ImGui.button("Center X")) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setX((ratioedSignSize.x - elemW[0]) / 2);
				baseElementDrawOrder.set(index, element);
			}

			// Button that centers the current selected element on the Y-Coordinate
			if (ImGui.button("Center Y")) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setY((ratioedSignSize.y - elemH[0]) / 2);
				baseElementDrawOrder.set(index, element);
			}

			if (ImGui.button("Center...")) {
				// TODO: Create Centering logic that can adapt to different contexts and relate to different elements
			}

			// ROTATION

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Rotation");
			ImGui.popFont();

			if (ImGui.dragFloat("##rotationDragger", currentElementRotation, 1.0f, 0, (float)359.99)) {
				element.setRotation(currentElementRotation[0]);
			}

			// COLOR

			ImGui.separator();
			ImGui.spacing();

			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Color");
			ImGui.popFont();

			int alphaSettings = (element instanceof TextElement) ? ImGuiColorEditFlags.NoAlpha : ImGuiColorEditFlags.AlphaBar | ImGuiColorEditFlags.AlphaPreviewHalf;

			if (ImGui.colorPicker4("Color Picker", color, alphaSettings)) {
				element.setColor(color);
			}

			if (!isImage) renderTextControls();
		}

		ImGui.end();
	}

	private static void renderTextControls() {
		ImGui.separator();

		ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
		ImGui.text("Text");
		ImGui.popFont();
		ImGui.inputText("##textElementTextEditInput", textElementText);
		if (ImGui.button("Confirm##textElementText"))
			((TextElement) element).setText(textElementText.get());

		ImGui.spacing();

		ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
		ImGui.text("Font Size");
		ImGui.popFont();
		ImGui.inputFloat("##fontSizeInput", fontSize);
		if (ImGui.button("Confirm##fontSize"))
			((TextElement) element).getFont().setFontSize(fontSize.get());

		ImGui.spacing();

		ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
		ImGui.text("Font");
		ImGui.popFont();

		previousSelectedFontIndex = selectedFontIndex.get();
		ImGui.combo("##fontInput", selectedFontIndex, availableFontNames.toArray(new String[0]));

		if (previousSelectedFontIndex != selectedFontIndex.get())
			((TextElement) element).getFont().setFontPath(LinkedHashMapTool.getKeyAtIndex(availableFonts, selectedFontIndex.get()));

		ImGui.spacing();
		ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
		ImGui.text("Size Extended");
		ImGui.popFont();
		if (ImGui.button("Normalize Size"))
			((TextElement) element).setWidthCalculated(false);
	}

	public static void toggle() {
		shouldRender = !shouldRender;
	}
}
