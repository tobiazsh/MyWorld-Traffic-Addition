package at.tobiazsh.myworld.traffic_addition.sign.elements;

import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ElementId;
import at.tobiazsh.myworld.traffic_addition.sign.elements.id.ParentElementId;
import com.google.gson.JsonObject;

import java.util.UUID;

public class OnlineImageElement extends BaseElement {

    protected final UUID pictureReference;

    public OnlineImageElement(
            float x,
            float y,
            float width,
            float height,
            float factor,
            float rotation,
            UUID pictureReference,
            ElementId id,
            ParentElementId parentId
    ) {
        super(x, y, width, height, factor, rotation, id, parentId);
        this.pictureReference = pictureReference;
    }

    public OnlineImageElement(
            float x,
            float y,
            float width,
            float height,
            float factor,
            float rotation,
            UUID pictureReference,
            ParentElementId parentId
    ) {
        this(x, y, width, height, factor, rotation, pictureReference, ElementId.random(), parentId);
    }

    public UUID getPictureReference() {
        return pictureReference;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = super.getJson();

        jsonObject.addProperty("ElementType", ELEMENT_TYPE.ONLINE_IMAGE_ELEMENT.ordinal());
        jsonObject.addProperty("PictureReference", pictureReference.toString());

        return jsonObject;
    }
}
