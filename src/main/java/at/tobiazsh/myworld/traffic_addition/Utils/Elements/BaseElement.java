package at.tobiazsh.myworld.traffic_addition.Utils.Elements;


/*
 * @created 19/10/2024 (DD/MM/YYYY) - 23:31
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import java.util.HashMap;
import java.util.Map;

public abstract class BaseElement {
	protected float x, y, width, height, factor, rotation;
	public boolean clicked = false;
	public String id, name;
	private float[] color = new float[]{1f, 1f, 1f, 1f};
	public static Map<String, BaseElement> Ids = new HashMap<>();
	private static int nextId = 0;

	public BaseElement(float x, float y, float width, float height, float factor) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		registerId(this);
	}

	public BaseElement(float x, float y, float width, float height, float rotation, float factor, String id, float[] color, String name) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.factor = factor;
		this.id = id;
		this.color = color;
		this.name = name;
		this.rotation = rotation;
	}

	private String registerId(BaseElement element) {
		this.id = "ELEMENT_" + nextId;
		Ids.put(id, element);
		nextId++;
		this.name = id;
		return id;
	}

	public String getId() {
		return id;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
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
}
