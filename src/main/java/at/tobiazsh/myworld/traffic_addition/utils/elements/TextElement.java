package at.tobiazsh.myworld.traffic_addition.utils.elements;


/*
 * @created 20/10/2024 (DD/MM/YYYY) - 15:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.utils.BasicFont;
import com.google.gson.JsonObject;

import java.util.UUID;

public class TextElement extends BaseElement {
	protected BasicFont font;
	protected String text;

	protected boolean widthIsCalculated;

	public TextElement(float x, float y, float width, float height, float rotation, float factor, BasicFont font, String text, boolean shouldCalculateWidth, UUID parentId, UUID id) {
		super(x, y, width, height, factor, rotation, parentId, id);
		this.font = font;
		this.text = text;
		this.widthIsCalculated = !shouldCalculateWidth;
		this.setColor(new float[]{0, 0, 0, 1});
	}

	public boolean isWidthCalculated() {
		return this.widthIsCalculated;
	}

	public void setWidthCalculated(boolean widthIsCalculated) {
		this.widthIsCalculated = widthIsCalculated;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public BasicFont getFont() {
		return this.font;
	}

	public void setFont(BasicFont font) {
		this.font = font;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = super.getJson();

		object.addProperty("ElementType", ELEMENT_TYPE.TEXT_ELEMENT.ordinal());
		object.addProperty("Text", text);
		object.addProperty("FontPath", font.getFontPath());
		object.addProperty("FontSize", font.getFontSize());

		return object;
	}
}
