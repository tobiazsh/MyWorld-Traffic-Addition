package at.tobiazsh.myworld.traffic_addition.customizable_sign.elements;

import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;

public interface TexturableElementInterface {

    // GET / SET
    Texture getTexture();
    void setTexture(Texture texture);

    boolean isTextureLoaded();

    // METHODS

    void loadTexture();
}
