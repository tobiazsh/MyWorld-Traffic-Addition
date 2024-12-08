package at.tobiazsh.myworld.traffic_addition.ImGui.Windows;


/*
 * @created 22/10/2024 (DD/MM/YYYY) - 16:26
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Screens.SignEditorScreen.baseElementDrawOrder;

public class ElementAddWindow {
	public static boolean shouldRender = false;
	public static boolean shouldConfig = false;
	public static String windowId = "Element Add Window";
	private static FileSystem.Folder folder = null;

	/**
	 * Renders the element add window if the "shouldRender" flag is set to true.
	 * The window displays a list of elements that can be added to the sign editor.
	 */
	public static void render() {
		if (shouldConfig) config();
		if (!shouldRender) return;

		ImGui.pushFont(ImGuiImpl.DejaVuSans);
		if (ImGui.begin(windowId, ImGuiWindowFlags.MenuBar)) {

			if (ImGui.beginMenuBar()) {
				if (ImGui.menuItem("Cancel (X)")) shouldRender = false;

				ImGui.endMenuBar();
			}

			// Display the title of the window in bold font
			ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
			ImGui.text("Add Elements");
			ImGui.popFont();

			ImGui.separator();

			// Begin a child window for the elements display
			if (ImGui.beginChild("##elementsDisplay")) {
				if (folder != null) {
					folder.forEach(icon -> {
						ElementIcon elementIcon = new ElementIcon(icon.name, icon.path);
						elementIcon.render();

						if (ImGui.getCursorPosX() + elementIcon.width + 10 < ImGui.getContentRegionMaxX()) {
							ImGui.sameLine();
						}
					});
				}
			}
			ImGui.endChild();
		}

		ImGui.end();
		ImGui.popFont();
	}

	/**
	 * Configures the element add window by loading the icons from the resources folder.
	 */
	public static void config() {
		try {
			folder = FileSystem.FromResource.listFiles("/assets/myworld_traffic_addition/textures/imgui/sign_res/icons/").concentrateFileType("PNG");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		shouldConfig = false;
	}

	/**
	 * Toggles the "shouldRender" boolean to show/hide the element add window
	 */
	public static void open() {
		shouldRender = true;
		shouldConfig = true;
	}

	public static class ElementIcon {
		public String name;
		public String path;
		public ImVec2 pos = new ImVec2(0, 0);
		private float width, height, previewSize;
		private Texture texture = null;

		private static final int elementIconBackgroundColor = ImGui.getColorU32(new ImVec4(54 / 255f, 50 / 255f, 50 / 255f, 255 / 255f));

		/**
		 * Adds an element to the sign editor canvas.
		 * @param element The element to be added
		 */
		public static void addElement(ImageElement element) {
			element.loadTexture();
			element.sizeAuto();
			baseElementDrawOrder.addFirst(element);
			shouldRender = false;
		}

		/**
		 * Renders the element icon.
		 */
		public void render() {
			// Ensure texture is initialized
			if (texture == null) {
				texture = Textures.smartRegisterTexture(path);
			}

			// Begin a child window for the element icon
			if (ImGui.beginChild("##ElementIcon_" + this.path, this.width, this.height, false, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse)) {
				ImDrawList drawList = ImGui.getWindowDrawList();
				ImVec2 cursor = ImGui.getCursorScreenPos();

				// Draw a filled rectangle as the background for the icon
				drawList.addRectFilled(pos.x + cursor.x, pos.y + cursor.y, pos.x + cursor.x + this.width, pos.y + cursor.y + this.height, elementIconBackgroundColor);

				float margin = (this.width - this.previewSize) / 2;
				ImGui.setCursorPos(margin, margin);

				// Calculate the height of the overlay
				float overlayHeight = this.height - margin * 3 - ImGui.getFontSize(); // Calculated so that the button still has enough space to not overlap with the overlay

				// Begin a child window for the overlay
				if (ImGui.beginChild("##Overlay_" + this.path, this.width - margin * 2, overlayHeight, ImGuiWindowFlags.NoScrollbar)) {
					// Begin a child window for the preview
					if (ImGui.beginChild("##preview_" + this.path, previewSize, previewSize, false, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse)) {
						if (texture != null) {
							ImGui.image(texture.getTextureId(), previewSize, previewSize);
						}
					}
					ImGui.endChild();

					ImGui.spacing();

					// Display the element name in bold font
					ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
					ImGui.textWrapped(name);
					ImGui.popFont();

					ImGui.spacing();

					// Display the element path in colour and wrapped text
					ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getColorU32(92 / 255f, 93 / 255f, 94 / 255f, 1.0f));
					ImGui.textWrapped(path);
					ImGui.popStyleColor();
				}
				ImGui.endChild();

				ImGui.setCursorPos(margin, this.height - margin - ImGui.getFontSize());
				if (ImGui.button("Add")) {
					addElement(new ImageElement(1.0f, path));
				}

			}
			ImGui.endChild();
		}

		public ElementIcon (String name, String path, float width, float height) {
			this.name = name;
			this.path = path;
			this.height = height;
			this.width = width;
			this.previewSize = width / 5 * 4;
			this.setTexture(path);
		}

		public ElementIcon (String name, String path) {
			this(name, path, 230f, 325f);
		}

		public ElementIcon (String name, String path, float size) {
			this(name, path);
			this.setSize(size);
		}

		public ElementIcon setSize(float size) {
			width = 230f * size;
			height = 325f * size;
			this.previewSize = width / 5 * 4;

			return this;
		}

		/**
		 * Resizes the preview of the element icon. 1.0f = 100%.
		 * @param previewSize The new Size
		 * @return ElementIcon
		 */
		public ElementIcon setPreviewSize(float previewSize) {
			this.previewSize = previewSize;
			return this;
		}

		/**
		 * Sets the texture of the element icon.
		 * @param path The path to the texture
		 * @return The element icon
		 */
		public ElementIcon setTexture(String path) {
			this.texture = Textures.smartRegisterTexture(path);
			return this;
		}
	}
}
