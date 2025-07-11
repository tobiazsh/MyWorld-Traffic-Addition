package at.tobiazsh.myworld.traffic_addition.utils.elements;

import com.google.gson.JsonObject;

import java.util.UUID;

public class OnlineImageElement extends ImageElement {

    protected final UUID pictureReference;

//    public OnlineImageElement(float x, float y, float width, float height, float factor, float rotation, UUID pictureReference, String parentId) {
//        super(x, y, width, height, factor, rotation, (String) null, parentId);
//        this.pictureReference = pictureReference;
//    }

    public OnlineImageElement(float x, float y, float width, float height, float factor, float rotation, UUID pictureReference, UUID parentId) {
        super(x, y, width, height, factor, rotation, null, parentId);
        this.pictureReference = pictureReference;
    }

    public OnlineImageElement(float x, float y, float width, float height, float factor, float rotation, UUID pictureReference, UUID parentId, UUID id) {
        super(x, y, width, height, factor, rotation, null, parentId, id);
        this.pictureReference = pictureReference;
    }


//    public OnlineImageElement(float x, float y, float width, float height, float factor, UUID pictureReference, String parentId) {
//        super(x, y, width, height, factor, null, parentId);
//        this.pictureReference = pictureReference;
//    }

    public OnlineImageElement(float factor, UUID pictureReference, UUID parentId) {
        super(factor, null, parentId);
        this.pictureReference = pictureReference;
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
