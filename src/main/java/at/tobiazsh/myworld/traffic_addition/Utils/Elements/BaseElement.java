package at.tobiazsh.myworld.traffic_addition.Utils.Elements;


/*
 * @created 19/10/2024 (DD/MM/YYYY) - 23:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.BasicFont;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class BaseElement {

	protected float x, y, width, height, factor, rotation;
	protected String id, name, parentId;
	protected float[] color = new float[]{1f, 1f, 1f, 1f};
	public boolean clicked = false;
	protected boolean remove = false; // Flag indicating whether the element should be removed
	public static Map<String, BaseElement> Ids = new HashMap<>();
	private static int nextId = 0;
	public static float currentElementFactor;

	public enum ELEMENT_TYPE {
		NONE,
		IMAGE_ELEMENT,
		TEXT_ELEMENT,
		GROUP_ELEMENT
	}

	public BaseElement(float x, float y, float width, float height, float factor, String parentId) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		this.parentId = parentId;
		registerId(this);
	}

	public BaseElement(float x, float y, float width, float height, float factor, float rotation, String parentId) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		this.rotation = rotation;
		this.parentId = parentId;
		registerId(this);
	}

	public BaseElement(float x, float y, float width, float height, float rotation, float factor, float[] color, String name, String parentId) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		this.color = color;
		this.name = name;
		this.rotation = rotation;
		this.parentId = parentId;
		registerId(this);
	}

	public BaseElement(float x, float y, float width, float height, float rotation, float factor, String id, float[] color, String name, String parentId) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		this.id = id;
		this.color = color;
		this.name = name;
		this.rotation = rotation;
		this.parentId = parentId;
	}

	private String registerId(BaseElement element) {
		this.id = "ELEMENT_" + nextId;

		while (idExists(id)) {
			this.id = "ELEMENT_" + nextId;
			nextId++;
		}

		Ids.put(id, element);
		nextId++;
		this.name = id;
		return id;
	}

	public static BaseElement getElementById(String id) {
		if (Objects.equals(id, "MAIN")) return null;
		return Ids.get(id);
	}

	private static boolean idExists(String id) {
		return Ids.containsKey(id);
	}

	public String getId() {
		return id;
	}

	/**
	 * Sets the custom id of the element. NOT RECOMMENDED
	 * @param id The custom id
	 */
	public void setCustomId(int id) {
		Ids.remove(this.id);
		this.id = "ELEMENT_" + id;
		Ids.put(this.id, this);
	}

	public void removeMe() {
		remove = true;
	}

	public boolean shouldRemove() {
		return remove;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	// Separate methods for x and y to save a little bit of memory
	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public BaseElement setWidth(float width) {
		this.width = width;
		return this;
	}

	public BaseElement setHeight(float height) {
		this.height = height;
		return this;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float calcBlocks(float value) {
		return value / this.factor;
	}

	public static float calcBlocks(float value, float factor) {
		return value / factor;
	}

	public BaseElement scalePercentSize(float percent) {
		this.width = this.width + (this.width * (percent / 100));
		return this;
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
	}

	public BaseElement scaleHeight(float factor) {
		this.height *= factor;
		return this;
	}

	public BaseElement scaleWidth(float factor) {
		this.width *= factor;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}

	public float getFactor() {
		return factor;
	}

	public void setRotation(float angle) {
		this.rotation = angle;
	}

	public float getRotation() {
		return rotation;
	}

	/**
	 * Sets the color of the element (0-255)
	 * @param color The color to set
	 */
	public void setColor(float[] color) {
		this.color = color;
	}

	/**
	 * Returns the color of the element (0-1)
	 * @return The color of the element normalized
	 */
	public float[] getColor() {
		return new float[]{color[0], color[1], color[2], color[3]};
	}

	public JsonObject getJson() {
		JsonObject object = new JsonObject();

		// Arrays

		JsonArray jsonColor = new JsonArray();
		for (float v : color) jsonColor.add(v);
		object.add("Color", jsonColor);

		JsonArray jsonSize = new JsonArray();
		for (float v : new float[]{width, height}) jsonSize.add(v);
		object.add("Size", jsonSize);

		JsonArray jsonElementPosition = new JsonArray();
		for (float v : new float[]{x, y}) jsonElementPosition.add(v);
		object.add("ElementPosition", jsonElementPosition);

		// Non-Array

		object.addProperty("Id", id);
		object.addProperty("Name", name);
		object.addProperty("Rotation", rotation);
		object.addProperty("Factor", factor);
		object.addProperty("ParentId", parentId);

		return object;
	}

	/**
	 * Converts a JSON Object to an element of the type specified in the json object
	 * @param object The JSON Object to convert
	 * @return The converted Object inherited from BaseElement
	 */
	public static BaseElement fromJson(JsonObject object) {
		BaseElement element;

		// Array

		JsonArray jsonColor = object.getAsJsonArray("Color");
		float[] color = new float[]{jsonColor.get(0).getAsFloat(), jsonColor.get(1).getAsFloat(), jsonColor.get(2).getAsFloat(), jsonColor.get(3).getAsFloat()}; // R, G, B, A

		JsonArray jsonSize = object.getAsJsonArray("Size");
		float[] size = new float[]{jsonSize.get(0).getAsFloat(), jsonSize.get(1).getAsFloat()}; // Width, Height;

		JsonArray jsonElementPosition = object.getAsJsonArray("ElementPosition");
		float[] elementPosition = new float[]{jsonElementPosition.get(0).getAsFloat(), jsonElementPosition.get(1).getAsFloat()}; // X, Y

		// Non-Array

		float rotation = object.get("Rotation").getAsFloat();
		float factor = object.get("Factor").getAsFloat();
		String name = object.get("Name").getAsString();
		ELEMENT_TYPE type = ELEMENT_TYPE.values()[object.get("ElementType").getAsInt()];

		if (type == ELEMENT_TYPE.IMAGE_ELEMENT) {
			element = new ImageElement(
					elementPosition[0], elementPosition[1],
					size[0], size[1],
					factor,
					rotation,
					object.get("Texture").getAsString(),
					"MAIN"
			);
		} else if (type == ELEMENT_TYPE.TEXT_ELEMENT) {
			element = new TextElement(
					elementPosition[0], elementPosition[1],
					size[0], size[1],
					rotation,
					factor,

					new BasicFont(
							object.get("FontPath").getAsString(),
							object.get("FontSize").getAsFloat()),

					object.get("Text").getAsString(),
					false,
					"MAIN"
			);
		} else if (type == ELEMENT_TYPE.GROUP_ELEMENT) {
			element = new GroupElement(
					elementPosition[0], elementPosition[1],
					size[0], size[1],
					rotation,
					object.get("Id").getAsString(),
					name,
					"MAIN"
			);

			JsonArray elementsArray = object.getAsJsonArray("Elements");
			for (int i = 0; i < elementsArray.size(); i++) {
				JsonObject elementObject = elementsArray.get(i).getAsJsonObject();
				BaseElement element1 = fromJson(elementObject);
				((GroupElement) element).addElement(element1);
			}

			((GroupElement) element).setChildrenParentElementId();

		} else {
			MyWorldTrafficAddition.LOGGER.error("Error: Couldn't deconstruct elements to JSON! Element type is invalid.");
			return null;
		}

		// Migration from old codebase (old codebase didn't have the id stored; introduced because of undo/redo)
		if (object.has("Id")) {
			String idStr = object.get("Id").getAsString();
			int id = Integer.parseInt(idStr.substring(idStr.lastIndexOf("_") + 1));
			element.setCustomId(id);
		}

		// Migration from old codebase (old codebase didn't have the parent id stored; introduced because of ungrouping)
		if (object.has("ParentId")) {
			String parentId = object.get("ParentId").getAsString();
			element.setParentId(parentId);
		}

		element.setName(name);
		element.setColor(color);

		return element;
	}

	/**
	 * Copies the element
	 * @return The copied element
	 */
	public abstract BaseElement copy();

	/**
	 * Converts the element to a JSON Object
	 * @return The JSON Object
	 */
	public abstract JsonObject toJson();

	/**
	 * Executes an action when the element is pasted
	 */
	public void onPaste(){}
}
