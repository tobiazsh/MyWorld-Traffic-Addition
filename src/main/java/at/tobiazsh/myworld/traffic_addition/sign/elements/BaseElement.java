package at.tobiazsh.myworld.traffic_addition.sign.elements;


/*
 * @created 19/10/2024 (DD/MM/YYYY) - 23:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public abstract class BaseElement implements BaseElementInterface {

	protected float x, y, width, height, factor, rotation;
	protected String name = "New Element";
	protected final ElementId id;
	protected ParentElementId parentId;
	protected float[] color = new float[]{1f, 1f, 1f, 1f};

	/***********************************
	 * !!!! IMPORTANT INFORMATION !!!!
	 * ID Logic moved back to the
	 * BaseElement class since it just
	 * makes more sense to have it here.
	 * Furthermore, the ID is now final,
	 * since it should not be changed after
	 * A reason for moving it back here
	 * is that the JSON also needs to
	 * store the ID. Everything already
	 * must match at the JSON level
	 * (parent id, id mismatch)
	 * and since this class is serialized
	 * it makes so much more sense to
	 * have the ID here.
	 ***********************************/

	public enum ELEMENT_TYPE {
		NONE,
		IMAGE_ELEMENT,
		LEGACY_TEXT_ELEMENT, // before font rework
		GROUP_ELEMENT,
		ONLINE_IMAGE_ELEMENT,
		TEXT_ELEMENT
	}

	public BaseElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			float rotation,
			ElementId id,
			ParentElementId parentId
	) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		this.rotation = rotation;
		this.id = id;
		this.parentId = parentId;
	}

	public BaseElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			ParentElementId parentId
	) {
		this(x, y, width, height, factor, 0, ElementId.random(), parentId);
	}

	public BaseElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			float rotation,
			ParentElementId parentId
	) {
		this (x, y, width, height, factor, rotation, ElementId.random(), parentId);
	}

	public BaseElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			float rotation,
			float[] color,
			ElementId id,
			ParentElementId parentId
	) {
		this(x, y, width, height, factor, rotation, id, parentId);
		this.color = color;
	}

	public BaseElement(
			float x,
			float y,
			float width,
			float height,
			float factor,
			float rotation,
			float[] color,
			String name,
			ElementId id,
			ParentElementId parentId
	) {
		this(x, y, width, height, factor, rotation, color, id, parentId);
		this.name = name;
	}

	@Override
	public ElementId getId() {
		return id;
	}

	@Override
	public ParentElementId getParentId() {
		return parentId;
	}

	@Override
	public void setParentId(ParentElementId parentId) {
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
	 * Sets the color of the element (0-1)
	 * @param color The color to set in rgba format (0-1)
	 */
	public void setColor(float[] color) {
		this.color = color;
	}

	/**
	 * Returns the color of the element (0-1)
	 * @return The color of the element normalized in rgba format (0-1)
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
