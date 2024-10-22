package at.tobiazsh.myworld.traffic_addition.Utils;


/*
 * @created 07/10/2024 (DD/MM/YYYY) - 19:45
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

/*
	MINI-DOCS:

	HOW TO DRAW A IMAGE ELEMENT ON SCREEN
	-------------------------------------------

	Step 1: Create new ImageElement with at least the resource path to the image (probably relative to class; probably in resources)
	Step 2: Set factor, if not done already with constructor, with setFactor(float)
	Step 3: Set size with either sizeAuto(), setSize(float, float), setWidth(float), setHeight(float) or directly in the constructor
	Step 4: Load texture with loadTexture()
	Step 5: Call render() in desired method

	Step 6 (Optional): Set elementIsClicked (boolean) variable via setClicked(boolean)
	Step 7: Don't worry, be happy
 */

/*
 * !!! DEPRECATED !!! - Replaced with BaseElement, ImageElement and TextElement as of 21st October 2024
 */

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImDrawFlags;

import java.util.HashMap;
import java.util.Map;

public class Element {
	public float x = 0;
	public float y = 0;
	private float xBlocks = 0;
	private float yBlocks = 0;

	private float width;
	private float height;
	private float widthBlocks;
	private float heightBlocks;

	private boolean elementIsClicked = false;

	private float factor;

	private Texture elementTexture = new Texture();

	private String resourcePath = "";

	private String Id;

	public String name;

	private static Map<String, Element> Ids = new HashMap<>();
	private static int nextId = 0;


	// STATIC OTHER STUFF

	private static String registerId(Element element) {
		String id = "IMAGE_ELEMENT_" + nextId;
		Ids.put(id, element);
		nextId++;

		return id;
	}

	// OTHER STUFF

	public void setClicked(boolean clicked) {
		elementIsClicked = clicked;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	public String getId() {
		return Id;
	}

	public String getResourcePath() {
		return resourcePath;
	}


	// INITIALISATION

	public Element(float x, float y, String resourcePath, float factor) {
		this.x = x;
		this.y = y;
		this.resourcePath = resourcePath;
		this.factor = factor;
		this.name = resourcePath;

		this.Id = registerId(this);
	}

	public Element(String resourcePath, float factor) {
		this(0, 0, resourcePath, factor);
	}

	public Element(String resourcePath) {
		this(0, 0, resourcePath, 1);
	}

	// POSITIONING

	public void setX(float x) {
		this.x = x;
		this.xBlocks = x / factor;
	}

	public void setY(float y) {
		this.y = y;
		this.yBlocks = y / factor;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.yBlocks = y / factor;

		this.y = y;
		this.xBlocks = x / factor;
	}

	public float getXBlocks() {
		return this.xBlocks;
	}

	public float getYBlocks() {
		return this.yBlocks;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}


	// TEXTURING

	private boolean rendererHasError = false;
	public void render() {
		if (rendererHasError) return;

		ImGui.beginChild(this.Id, this.width, this.height, false);

		if (elementIsClicked) {
			ImDrawList drawList = ImGui.getWindowDrawList();
			float borderThickness = 2.0f;
			ImVec2 imagePos = new ImVec2(ImGui.getCursorPos());

			drawList.addRect(
					imagePos.x, imagePos.y,
					imagePos.x + width, imagePos.y + height,
					ImGui.colorConvertFloat4ToU32(255, 255, 255, 255),
					0.0f,
					ImDrawFlags.None,
					borderThickness
			);
		}

		ImGui.image(elementTexture.getTextureId(), width, height);

		ImGui.endChild();
	}

	public void loadTexture() {
		if (resourcePath.isEmpty()) {
			System.err.println("Error (Loading texture on ImageElement): Couldn't load texture because resource path is empty!");
			return;
		}

		elementTexture = Textures.smartRegisterTexture(resourcePath);
	}

	public void setIndividualTexture(Texture texture){
		this.elementTexture = texture;
	}

	public Texture getTexture() {
		return elementTexture;
	}


	// SIZING

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
		widthBlocks = w / factor;

		height = h;
		heightBlocks = h / factor;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public void setWidth(float width) {
		this.width = width;
		this.widthBlocks = width / factor;
	}

	public void setHeight(float height) {
		this.height = height;
		this.heightBlocks = height / factor;
	}

	public void setSize(float height, float width) {
		this.width = width;
		this.widthBlocks = width / factor;

		this.height = height;
		this.heightBlocks = height / factor;
	}

	public void scalePercentSize(float percent) {
		this.width = this.width + (this.width * (percent / 100));
		widthBlocks = this.width / factor;
	}

	public void scaleSize(float pixels, boolean scaleByHeight) {
		float oldVal = scaleByHeight ? this.height : this.width;
		float val = oldVal;

		val += pixels;

		float factor = val / oldVal;

		float oppositeVal = !scaleByHeight ? this.height : this.width;

		float newVal = val;
		float newOppositeVal = oppositeVal * factor;

		if (scaleByHeight) {
			this.height = newVal;
			this.width = newOppositeVal;
		} else {
			this.width = newVal;
			this.height = newOppositeVal;
		}

		this.heightBlocks = this.height / factor;
		this.widthBlocks = this.width / factor;
	}

	public void scaleHeight(float pixels) {
		this.height += pixels;
		this.heightBlocks = this.height / factor;
	}

	public void scaleWidth(float pixels) {
		this.width += pixels;
		this.widthBlocks = this.width / factor;
	}

	public float getWidthBlocks() {
		return this.widthBlocks;
	}

	public float getHeightBlocks() {
		return this.heightBlocks;
	}
}