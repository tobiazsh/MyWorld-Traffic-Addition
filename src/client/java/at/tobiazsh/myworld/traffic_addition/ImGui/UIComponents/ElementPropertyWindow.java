package at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents;


/*
 * @created 21/10/2024 (DD/MM/YYYY) - 16:00
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen.baseElementDrawOrder;

public class ElementPropertyWindow {
	private static ImString currentElementName = new ImString("", 512);
	private static float[] currentElementRotation = new float[]{};
	private static BaseElement element;
	private static final ImGui imgui = new ImGui();
	private static boolean relateSize = true;
	private static ImVec2 ratioedSignSize = new ImVec2();
	private static float factor;

	public static boolean shouldRender = false;

	public static void initVars(BaseElement element, ImVec2 ratioedSignSize) {
		ElementPropertyWindow.currentElementName = new ImString(element.name, 512);
		ElementPropertyWindow.currentElementRotation = new float[]{element.getRotation()};
		relateSize = true;
		ElementPropertyWindow.element = element;
		ElementPropertyWindow.ratioedSignSize = ratioedSignSize;
		factor = element.getFactor();
	}

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

			if (ImGui.button("Confirm")) {
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

			if (ImGui.dragFloat("Horizontal", elemX, 1.0f, 0.0f, ratioedSignSize.x)) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setX(elemX[0]);
				baseElementDrawOrder.set(index, element);
			}

			if (ImGui.dragFloat("Vertical", elemY, 1.0f, 0.0f, ratioedSignSize.y)) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setY(elemY[0]);
				baseElementDrawOrder.set(index, element);
			}

			if (ImGui.button("Center Horizontally")) {
				int index = baseElementDrawOrder.indexOf(element);
				element.setX((ratioedSignSize.x - elemW[0]) / 2);
				baseElementDrawOrder.set(index, element);
			}

			if (ImGui.button("Center Vertically")) {
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

			// TODO: Make Picture rotatable
		}

		ImGui.end();
	}

	public static void toggle() {
		shouldRender = !shouldRender;
	}
}
