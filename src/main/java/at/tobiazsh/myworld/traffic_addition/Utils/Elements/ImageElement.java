package at.tobiazsh.myworld.traffic_addition.Utils.Elements;


/*
 * @created 19/10/2024 (DD/MM/YYYY) - 23:55
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

/*
 * MINIDOCS
 * 1. Create
 * 2. loadTexture()
 * 2. render()
 */

import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class ImageElement extends BaseElement{
	private Texture elementTexture = new Texture();
	private String resourcePath;
	private boolean texIsLoaded = false;
	private ImVec2 p0, p1, p2, p3;

	public ImageElement(float x, float y, float width, float height, float factor, String resourcePath) {
		super(x, y, width, height, factor);
		this.resourcePath = resourcePath;
	}

	public ImageElement(float x, float y, float factor, String resourcePath) {
		this(x, y, 0, 0, factor, resourcePath);
	}

	public ImageElement(float x, float y, String resourcePath) {
		this(x, y, 0, 0, 0, resourcePath);
	}

	public ImageElement(float factor, String resourcePath) {
		this(0, 0, 0, 0, factor, resourcePath);
	}

	@Override
	public void renderImGui() {
		ImDrawList drawList = ImGui.getWindowDrawList();

		float windowPosX = ImGui.getCursorScreenPosX();
		float windowPosY = ImGui.getCursorScreenPosY();

		p0 = new ImVec2(windowPosX + this.x, windowPosY + this.y); // Top Left Vertices
		p1 = new ImVec2(windowPosX + this.x + this.width, windowPosY + this.y); // Top Right Vertices
		p2 = new ImVec2(windowPosX + this.x + this.width, windowPosY + this.y + this.height); // Bottom Right Vertices
		p3 = new ImVec2(windowPosX + this.x, windowPosY + this.y + height); // Bottom Left Vertices

		rotateTexture(rotation, windowPosX, windowPosY);

		float uv0X = 0.0f, uv0Y = 0.0f;
		float uv1X = 1.0f, uv1Y = 0.0f;
		float uv2X = 1.0f, uv2Y = 1.0f;
		float uv3X = 0.0f, uv3Y = 1.0f;

		drawList.addImageQuad(
				this.elementTexture.getTextureId(),
				p0.x, p0.y,
				p1.x, p1.y,
				p2.x, p2.y,
				p3.x, p3.y,
				uv0X, uv0Y,
				uv1X, uv1Y,
				uv2X, uv2Y,
				uv3X, uv3Y
		);
	}

	@Override
	public void renderMinecraft() {

	}

	public void loadTexture() {
		if (resourcePath.isEmpty()) {
			System.err.println("Error (Loading texture on ImageElement): Couldn't load texture because resource path is empty!");
			return;
		}

		elementTexture = Textures.smartRegisterTexture(resourcePath);
		texIsLoaded = true;
	}

	public Texture getTexture() {
		return elementTexture;
	}

	public void setExplicitTexture(Texture texture) {
		this.elementTexture = texture;
	}

	// Always call after loadTexture() was called!
	public void sizeAuto() {
		if (elementTexture.isEmpty()) {
			System.err.println("Error (Loading ImageElement size): Couldn't determine size because texture hasn't been initialized! Initialize with ImageElement.loadTexture()!");
			return;
		}

		float w = elementTexture.getWidth();
		float h = elementTexture.getHeight();

		if (w == -1) {
			System.err.println("Error (Loading ImageElement size): Couldn't determine width because width in Texture class is -1. Possible cause: No texture ID has been associated with that resource path. Make sure that the texture has been registered!");
			return;
		}

		if (h == -1) {
			System.err.println("Error (Loading ImageElement size): Couldn't determine height because height in Texture class is -1. Possible cause: No texture ID has been associated with that resource path. Make sure that the texture has been registered!");
			return;
		}

		width = w;

		height = h;
	}

	// tl = top left; tr = top right; br = bottom right; bl = bottom left
	public void setUV(ImVec2 p0tl, ImVec2 p1tr, ImVec2 p2br, ImVec2 p3bl) {
		this.p0 = p0tl;
		this.p1 = p1tr;
		this.p2 = p2br;
		this.p3 = p3bl;
	}

	public void rotateTexture(float angle, float windowPosX, float windowPosY){
		if (!texIsLoaded) {
			System.err.println("Error (Rotating Texture): Texture isn't loaded!");
		}

		// For efficiency
		if (angle == 0) return;

		ImVec2 center = new ImVec2(windowPosX + this.x + this.width / 2, windowPosY + this.y + this.height / 2);
		float radians = (float) Math.toRadians(angle);

		p0 = rotatePivot(p0, center, radians);
		p1 = rotatePivot(p1, center, radians);
		p2 = rotatePivot(p2, center, radians);
		p3 = rotatePivot(p3, center, radians);
	}

	private ImVec2 rotatePivot(ImVec2 point, ImVec2 center, float radians) {
		float cosA = cos(radians);
		float sinA = sin(radians);

		float newX = center.x + cosA * (point.x - center.x) - sinA * (point.y - center.y);
		float newY = center.y + sinA * (point.x - center.x) + cosA * (point.y - center.y);

		return new ImVec2(newX, newY);
	}

	public String getResourcePath() {
		return resourcePath;
	}
}
