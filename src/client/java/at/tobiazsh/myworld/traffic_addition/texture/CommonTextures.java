package at.tobiazsh.myworld.traffic_addition.texture;

public class CommonTextures {
    public static Texture NOT_FOUND_PLACEHOLDER = null;
    public static Texture LOADING_PLACEHOLDER = null;

    public static void loadTextures() {
        NOT_FOUND_PLACEHOLDER = Textures.smartRegisterTexture("assets/myworld_traffic_addition/textures/imgui/icons/not_found_placeholder.png");
        LOADING_PLACEHOLDER = Textures.smartRegisterTexture("assets/myworld_traffic_addition/textures/imgui/icons/loading_placeholder.png");
    }
}
