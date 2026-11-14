package at.tobiazsh.myworld.traffic_addition.imgui.toast;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTexture;
import dev.tobiazsh.imguib3d.client.texture.ImGuiTextureFactory;

import java.io.IOException;
import java.io.InputStream;

public enum ToastType {
    INFO("/assets/" + MyWorldTrafficAddition.MOD_ID +"/textures/imgui/toast/info.png", "info"),
    WARNING("/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/toast/warning.png", "warning"),
    SUCCESS("/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/toast/success.png", "success"),
    ERROR("/assets/" + MyWorldTrafficAddition.MOD_ID + "/textures/imgui/toast/error.png", "error"),
    NONE(null, "none");


    private ImGuiTexture texture;
    private boolean mustLoad;
    private final String texturePath;
    private final String identifier;

    ToastType(String texturePath, String identifier) {
        this.texturePath = texturePath;
        this.identifier = identifier;
        this.mustLoad = texturePath != null;
    }

    /**
     * Gets the texture ID for the associated icon texture. If the texture is not loaded yet, it will be loaded and
     * cached.
     *
     * <p>For {@link #NONE}, the texture ID will always be -1, as there is no associated icon texture.
     * @return The texture ID for the associated icon texture, or -1 if the texture could not be loaded.
     */
    public long loadOrGetIcon() {
        if (this == NONE)
            return -1;

        if (mustLoad) {
            try (InputStream is = Toast.class.getResourceAsStream(texturePath)) {
                if (is == null)
                    throw new IOException("Could not find texture at path: " + texturePath);

                texture = ImGuiTextureFactory.fromStream(is, "toast_icon_" + identifier);
            } catch (IOException e) {
                MyWorldTrafficAddition.LOGGER.error("Failed to load toast icon texture for type {}", identifier, e);
            }

            mustLoad = false;
        }

        return texture.getTextureId();
    }
}