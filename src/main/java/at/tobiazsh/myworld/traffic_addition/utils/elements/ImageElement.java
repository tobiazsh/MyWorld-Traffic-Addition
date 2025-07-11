package at.tobiazsh.myworld.traffic_addition.utils.elements;


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

import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;
import com.google.gson.JsonObject;

import java.util.UUID;

public class ImageElement extends BaseElement {

	public String resourcePath;
	public Texture elementTexture = new Texture();

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, String resourcePath, UUID parentId) {
		super(x, y, width, height, factor, parentId);
		this.resourcePath = resourcePath;
		this.rotation = rotation;
	}

	public ImageElement(float x, float y, float width, float height, float factor, float rotation, String resourcePath, UUID parentId, UUID id) {
		super(x, y, width, height, factor, parentId);
		this.resourcePath = resourcePath;
		this.rotation = rotation;
		this.id = id;
	}

	public ImageElement(float x, float y, float width, float height, float factor, String resourcePath, UUID parentId) {
		this(x, y, width, height, factor, 0, resourcePath, parentId);
	}

	public ImageElement(float factor, String resourcePath, UUID parentId) {
		this(0, 0, -1, -1, factor, resourcePath, parentId);
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public ImageElement setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = super.getJson();

		object.addProperty("ElementType", ELEMENT_TYPE.IMAGE_ELEMENT.ordinal());
		object.addProperty("Texture", this.resourcePath);

		return object;
	}
}
