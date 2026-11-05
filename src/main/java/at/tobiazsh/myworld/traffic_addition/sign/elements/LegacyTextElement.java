package at.tobiazsh.myworld.traffic_addition.sign.elements;


/*
 * @created 20/10/2024 (DD/MM/YYYY) - 15:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.font.BasicFont;
import at.tobiazsh.myworld.traffic_addition.font.ServerFont;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import com.google.gson.JsonObject;

public class TextElement extends BaseElement {
	protected BasicFont font;
	protected String text;

	protected boolean widthIsCalculated;

	// TODO: Rework fonts

	public TextElement(
			float x,
			float y,
			float width,
			float height,
			float rotation,
			float factor,
			BasicFont font,
			String text,
			boolean shouldCalculateWidth,
			ElementId id,
			ParentElementId parentId
	) {
		super(x, y, width, height, factor, rotation, id, parentId);
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

		object.addProperty("ElementType", ELEMENT_TYPE.LEGACY_TEXT_ELEMENT.ordinal());
		object.addProperty("Text", text);
		object.addProperty("FontHash", font.font().fontHash());
		object.addProperty("FontSize", font.fontSize());

		return object;
	}
}
