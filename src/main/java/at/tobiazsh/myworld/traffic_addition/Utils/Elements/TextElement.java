package at.tobiazsh.myworld.traffic_addition.Utils.Elements;


/*
 * @created 20/10/2024 (DD/MM/YYYY) - 15:13
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.Utils.BasicFont;

public class TextElement extends BaseElement {
	private BasicFont font;
	private String text;

	private boolean widthIsCalculated = false;

	public TextElement(float x, float y, float width, float height, float rotation, float factor, BasicFont font, String text, boolean shouldCalculateWidth) {
		super(x, y, width, height, factor);
		this.font = font;
		this.text = text;
		this.rotation = rotation;
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

	public TextElement copy() {
		TextElement textElement = new TextElement(x, y, width, height, rotation, factor, font, text, false);
		textElement.setName(this.getName());
		textElement.setColor(this.getColor());
		return textElement;
	}
}
