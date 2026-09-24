package at.tobiazsh.myworld.traffic_addition.sign.elements;


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

import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import at.tobiazsh.myworld.traffic_addition.texture.Texture;
import com.google.gson.JsonObject;

import java.util.UUID;

public class ImageElement extends BaseElement {

	public String resourcePath;

	public ImageElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			float rotation,
			String resourcePath,
			ParentElementId parentId
	) {
		super(x, y, width, height, factor, rotation, parentId);
		this.resourcePath = resourcePath;
	}

	public ImageElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			float rotation,
			String resourcePath,
			ElementId id,
			ParentElementId parentId
	) {
		super(x, y, width, height, factor, rotation, id, parentId);
		this.resourcePath = resourcePath;
	}

	public ImageElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			String resourcePath,
			ParentElementId parentId
	) {
		this(x, y, width, height, factor, 0, resourcePath, parentId);
	}

	public ImageElement(float factor, String resourcePath, ParentElementId parentId) {
		this(0, 0, -1, -1, factor, resourcePath, parentId);
	}

	public String getResourcePath() {
		return resourcePath;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = super.getJson();

		object.addProperty("ElementType", ELEMENT_TYPE.IMAGE_ELEMENT.ordinal());
		object.addProperty("Texture", this.resourcePath);

		return object;
	}
}
