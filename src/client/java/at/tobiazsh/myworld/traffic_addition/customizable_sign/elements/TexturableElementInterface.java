package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.texture.DynamicTexture;
import at.tobiazsh.myworld.traffic_addition.texture.Texture;

public interface TexturableElementInterface {

    void markTextureStale();

    // GET / SET
    Texture getTexture();
    DynamicTexture getDynamicTexture();
    void setTexture(Texture texture);

    boolean isTextureLoaded();

    // METHODS

    void loadTexture();
}
