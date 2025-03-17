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
import com.google.gson.JsonObject;

public class ImageElement extends BaseElement {
	public Texture elementTexture = new Texture();
	public String resourcePath;
	public boolean texIsLoaded = false;

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, Texture texture, String parentId, String id) {
		this(x, y, width, height, factor, rotation, texture, parentId);
		this.id	= id;
	}

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, Texture texture, String parentId) {
		super(x, y, width, height, factor, parentId);
		this.elementTexture = texture;
		this.rotation = rotation;
	}

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, String resourcePath, String parentId) {
		super(x, y, width, height, factor, parentId);
		this.resourcePath = resourcePath;
		this.rotation = rotation;
	}

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, String resourcePath, String parentId, String id) {
		super(x, y, width, height, factor, parentId, id);
		this.resourcePath = resourcePath;
		this.rotation = rotation;
	}

	public ImageElement(float x, float y, float width, float height, float factor, Texture texture, String parentId) {
		this(x, y, width, height, factor, 0, texture, parentId);
	}

	public ImageElement(float x, float y, float width, float height, float factor, String resourcePath, String parentId) {
		this(x, y, width, height, factor, 0, resourcePath, parentId);
	}

	public ImageElement(float x, float y, float factor, String resourcePath, String parentId) {
		this(x, y, 0, 0, factor, resourcePath, parentId);
	}

	public ImageElement(float x, float y, String resourcePath, String parentId) {
		this(x, y, 0, 0, 0, resourcePath, parentId);
	}

	public ImageElement(float factor, String resourcePath, String parentId) {
		this(0, 0, 0, 0, factor, resourcePath, parentId);
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

	public void setCustomTexture(Texture texture) {
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

	public String getResourcePath() {
		return resourcePath;
	}

	public ImageElement setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}

	public ImageElement copy() {
		ImageElement imageElement = new ImageElement(x, y, width, height, factor, rotation, elementTexture, parentId);
		imageElement.setName(this.getName());
		imageElement.setResourcePath(this.resourcePath);
		imageElement.setColor(this.getColor());
		return imageElement;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = super.getJson();

		object.addProperty("ElementType", ELEMENT_TYPE.IMAGE_ELEMENT.ordinal());
		object.addProperty("Texture", this.resourcePath);

		return object;
	}

	@Override
	public void onImport() {
		this.regenerateId();
	}

	@Override
	public void onPaste() {
		this.regenerateId();
	}
}
