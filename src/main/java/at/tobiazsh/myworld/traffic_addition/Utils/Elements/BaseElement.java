package at.tobiazsh.myworld.traffic_addition.Utils.Elements;


/*
 * @created 19/10/2024 (DD/MM/YYYY) - 23:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public abstract class BaseElement implements BaseElementInterface {

	protected float x, y, width, height, factor, rotation;
	protected String name;
	protected UUID id, parentId;
	protected float[] color = new float[]{1f, 1f, 1f, 1f};
	public boolean clicked = false;

	/***********************************
	 * !!!! IMPORTANT INFORMATION !!!!
	 * ID Logic moved to client side
	 * since server side has got
	 * nothing to do with it! You can
	 * find it in: ClientElementManger
	 * on the client side!
	 ***********************************/

	// !! CHANGED ID FROM STRING TO UUID !!

	public enum ELEMENT_TYPE {
		NONE,
		IMAGE_ELEMENT,
		TEXT_ELEMENT,
		GROUP_ELEMENT,
		ONLINE_IMAGE_ELEMENT,
	}

	private BaseElement(float x, float y, float width, float height, float factor) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		generateName();
	}

	public BaseElement(float x, float y, float width, float height, float factor, UUID parentId) {
		this(x, y, width, height, factor);
		this.parentId = parentId;
		generateName();
	}

	public BaseElement(float x, float y, float width, float height, float factor, UUID parentId, UUID id) {
		this(x, y, width, height, factor);
		this.parentId = parentId;
		this.id = id;
		generateName();
	}

	public BaseElement(float x, float y, float width, float height, float factor, float rotation, UUID parentId) {
		this(x, y, width, height, factor, parentId);
		this.rotation = rotation;
		generateName();
	}

	public BaseElement(float x, float y, float width, float height, float factor, float rotation, UUID parentId, UUID id) {
		this(x, y, width, height, factor, parentId, id);
		this.rotation = rotation;
		generateName();
	}

	public BaseElement(float x, float y, float width, float height, float factor, float rotation, float[] color, UUID parentId, UUID id) {
		this(x, y, width, height, factor);
		this.color = color;
		this.rotation = rotation;
		this.id = id;
		this.parentId = parentId;
		generateName();
	}

	public BaseElement(float x, float y, float width, float height, float factor, float rotation, float[] color, String name, UUID parentId, UUID id) {
		this(x, y, width, height, factor);
		this.color = color;
		this.name = name;
		this.rotation = rotation;
		this.id = id;
		this.parentId = parentId;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	private void generateName() {
		this.name = "New Element";
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}

	public UUID getParentId() {
		return parentId;
	}

	public void setParentId(UUID parentId) {
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

	public void setWidth(float width) {
		this.width = width;
	}

	public void setHeight(float height) {
		this.height = height;
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

		object.addProperty("Id", id.toString());
		object.addProperty("Name", name);
		object.addProperty("Rotation", rotation);
		object.addProperty("Factor", factor);
		object.addProperty("ParentId", parentId.toString());

		return object;
	}

	/**
	 * Converts the element to a JSON Object
	 * @return The JSON Object
	 */
	public abstract JsonObject toJson();
}
