package at.tobiazsh.myworld.traffic_addition.sign.elements;

import at.tobiazsh.myworld.traffic_addition.font.ServerFont;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import com.google.gson.JsonObject;

public class TextElement extends BaseElement {
    protected ServerFont font;
    protected String text;

    // TODO: Maybe scale it on-the-go while writing and then let the user scale manually width?
    //  You don't want squished fonts normally anyway so defaulting to a non-squished font is sensible.

    public TextElement(
            float x,
            float y,
            float width,
            float height,
            float rotation,
            float factor,
            ServerFont font,
            String text,
            ElementId id,
            ParentElementId parentId
    ) {
        super(x, y, width, height, factor, rotation, id, parentId);
        this.font = font;
        this.text = text;
        this.setColor(new float[]{0, 0, 0, 1});
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ServerFont getFont() {
        return this.font;
    }

    public void setFont(ServerFont font) {
        this.font = font;
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = super.getJson();

        object.addProperty("ElementType", ELEMENT_TYPE.TEXT_ELEMENT.ordinal());
        object.addProperty("Text", text);
        object.addProperty("FontHash", font.font().fontHash());
        object.addProperty("FontSize", font.fontSize()); // Not really necessary, but useful as a cache so the client doesn't have to calculate raster size.

        return object;
    }
}
