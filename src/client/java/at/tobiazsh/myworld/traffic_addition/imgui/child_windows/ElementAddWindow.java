package at.tobiazsh.myworld.traffic_addition.imgui.child_windows;


/*
 * @created 22/10/2024 (DD/MM/YYYY) - 16:26
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.CustomizableSignElementFactory;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ImageElementClient;
import at.tobiazsh.myworld.traffic_addition.filesystem.FileSystem;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.sign.elements.ImageElement;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;
import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class ElementAddWindow {
	private boolean isVisible = false;
	private final String id;
	private @Nullable List<ElementIcon> icons = null;

	private final Consumer<ImageElementClient> onAdd;
	private final ImGuiFontScope fontScope = ImGuiFontScope.create();

	private static final float ICON_MARGIN = 10f; // Margin between icons

	public ElementAddWindow(@NonNull String id, Consumer<ImageElementClient> onAdd) {
		this.id = id;
		this.onAdd = onAdd;
	}

	/**
	 * Renders the element add window if the "shouldRender" flag is set to true.
	 * The window displays a list of elements that can be added to the sign editor.
	 */
	public void render() {
		if (!isVisible) return;

		if (ImGui.begin(id, ImGuiWindowFlags.MenuBar)) {

			if (ImGui.beginMenuBar()) {
				if (ImGui.menuItem(tr("Global", "Cancel"))) isVisible = false; // "Cancel" button

				ImGui.endMenuBar();
			}

			// Display the title of the window in bold font
			fontScope.push(DefaultFonts.RobotoBold);
			ImGui.text(tr("ImGui.Child.ElementAddWindow", "Add New Element")); // "Add New Element" title
			fontScope.pop();

			ImGui.separator();

			// Begin a child window for the elements display
			if (ImGui.beginChild("##elementsDisplay")) {
				if (icons != null) {
					float usedSpaceX = 0;
					float windowWidth = ImGui.getContentRegionAvailX();
					boolean firstEntry = true;

					for (ElementIcon icon : icons) {
						if (!firstEntry && (usedSpaceX + icon.getWidth() < windowWidth)) {
							ImGui.sameLine();
						} else {
							usedSpaceX = 0;
						}

						firstEntry = false;
						icon.render();
						usedSpaceX += icon.getWidth() + ICON_MARGIN;
					}
				}
			}
			ImGui.endChild();
		}

		ImGui.end();
	}

	/**
	 * Loads the previews of the elements from the icons folder and online image server.
	 */
	public void loadPreviews() {
		try {
			var folder = Objects.requireNonNull(
					FileSystem.listFilesRecursive(
							"/assets/%s/textures/imgui/sign_res/icons/".formatted(MyWorldTrafficAddition.MOD_ID),
							true
					)
			).concentrateFileType("PNG");

			if (folder == null || folder.size() <= 0)
				return;

			icons = folder.content.stream()
					.map(icon -> new ElementIcon(icon.name, icon.path, this.id, this.onAdd, this.fontScope))
					.toList();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Toggles the "shouldRender" boolean to show/hide the element add window
	 */
	public void open() {
		if (icons == null)
			loadPreviews();

		isVisible = true;
	}

	/**
	 * Disposes of the textures used by the element icons and hides the element add window.
	 */
	public void close() {
		isVisible = false;
		disposeTextures();
		icons = null;
	}

	/**
	 * Disposes of the textures used by the element icons to free up memory.
	 */
	private void disposeTextures() {
		if (icons == null) return;

		for (ElementIcon icon : icons)
			icon.disposeTexture();
	}

	private static class ElementIcon {
		private final String name;
		private final String parentId;
        private final String path;
		private final float width;
		private final float height;
		private final float previewSize;
		private @Nullable ImGuiTexture previewTexture = null;

		private final Consumer<ImageElementClient> onAdd;
		private final ImGuiFontScope fontScope;

		private static final float DEFAULT_WIDTH = 230f;
		private static final float DEFAULT_HEIGHT = 325f;

		private static final int elementIconBackgroundColor = ImGui.getColorU32(new ImVec4(54 / 255f, 50 / 255f, 50 / 255f, 255 / 255f));

		public ElementIcon(
				String name,
				String path,
				String parentId,
				float width,
				float height,
				Consumer<ImageElementClient> onAdd,
				ImGuiFontScope fontScope
		) {
			this.name = name;
			this.path = path;
			this.parentId = parentId;
			this.height = height;
			this.width = width;
			this.previewSize = width / 5 * 4;
			this.onAdd = onAdd;
			this.fontScope = fontScope;

			this.setTexture(path);
		}

		public ElementIcon(
				String name,
				String path,
				String parentId,
				Consumer<ImageElementClient> onAdd,
				ImGuiFontScope fontScope
		) {
			this(name, path, parentId, DEFAULT_WIDTH, DEFAULT_HEIGHT, onAdd, fontScope);
		}

		/**
		 * Registers and assigns the texture of the element icon.
		 * @param path The path to the texture
		 */
		private void setTexture(String path) {
			try {
				this.previewTexture = ImGuiTextureFactory.fromStream(
                        Objects.requireNonNull(ElementAddWindow.class.getResourceAsStream(path)),
						"ElementIcon_" + path
				);
			} catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load texture for element icon: {}", path, e);
			}
		}

		/**
		 * Constructs a new ImageElementClient and passes it to the onAdd consumer when the "Add" button is clicked.
		 */
		public void addElement() {
			var element = (ImageElementClient) CustomizableSignElementFactory.toClientElement(
					new ImageElement(1.0f, path, ClientElementInterface.MAIN_CANVAS_ID)
			);

			onAdd.accept(element);
		}

		/**
		 * Renders the element icon.
		 */
		public void render() {
			// Begin a child window for the element icon
			if (ImGui.beginChild(
					"##ElementIcon_" + this.path + "_" + parentId,
					this.width,
					this.height,
					false,
					ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse
			)) {
				ImDrawList drawList = ImGui.getWindowDrawList();
				ImVec2 cursor = ImGui.getCursorScreenPos();

				// Draw a filled rectangle as the background for the icon
				drawList.addRectFilled(
						cursor.x, cursor.y,
						cursor.x + this.width, cursor.y + this.height,
						elementIconBackgroundColor
				);

				float margin = (this.width - this.previewSize) / 2;
				ImGui.setCursorPos(margin, margin);

				// Calculate the height of the overlay
				float overlayHeight = this.height - margin * 3 - ImGui.getFontSize(); // Calculated so that the button still has enough space to not overlap with the overlay

				// Begin a child window for the overlay
				if (ImGui.beginChild(
						"##Overlay_" + this.path + "_" + parentId,
						this.width - margin * 2,
						overlayHeight,
						false,
						ImGuiWindowFlags.NoScrollbar
				)) {
					// Begin a child window for the preview
					if (ImGui.beginChild(
							"##Preview_" + this.path + "_" + parentId,
							previewSize,
							previewSize,
							false,
							ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse
					)) {
						if (previewTexture != null && previewTexture.isUsable())
							ImGui.image(previewTexture.getTextureId(), previewSize, previewSize);
					}
					ImGui.endChild();

					ImGui.spacing();

					// Display the element name in bold font
					fontScope.push(DefaultFonts.RobotoBold);
					ImGui.textWrapped(name);
					fontScope.pop();

					ImGui.spacing();

					// Display the element path in color and wrapped text
					ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getColorU32(92 / 255f, 93 / 255f, 94 / 255f, 1.0f));
					ImGui.textWrapped(path);
					ImGui.popStyleColor();
				}
				ImGui.endChild();

				ImGui.setCursorPos(margin, this.height - margin - ImGui.getFontSize());

				if (ImGui.button(tr("Global", "Add"))) // "Add" button
					addElement();
			}
			ImGui.endChild();
		}

		public float getWidth() {
			return width;
		}

		public float getHeight() {
			return height;
		}

		public void disposeTexture() {
			if (this.previewTexture != null && !this.previewTexture.isDisposed())
				this.previewTexture.dispose();

			this.previewTexture = null;
		}
	}
}
