package at.tobiazsh.myworld.traffic_addition.Utils;

import at.tobiazsh.myworld.traffic_addition.Rendering.Renderers.CustomizableSignBlockEntityRenderer;
import at.tobiazsh.myworld.traffic_addition.Rendering.Renderers.SignBlockEntityRenderer;

import java.util.Objects;

public class PreferenceControl {

    public static final Preference gameplayPreference = new Preference("myworld_traffic_addition/gameplay_config.json");

    public static void loadGameplayPreferences() {
        // SIGNS
        SignBlockEntityRenderer.zOffsetRenderLayer = Objects.requireNonNullElse(
                gameplayPreference.getFloat("viewDistanceSigns"),
                SignBlockEntityRenderer.zOffsetRenderLayerDefault
        );

        // CUSTOMIZABLE SIGNS
        CustomizableSignBlockEntityRenderer.zOffsetRenderLayer = Objects.requireNonNullElse(
                gameplayPreference.getFloat("viewDistanceCustomizableSigns"),
                CustomizableSignBlockEntityRenderer.zOffsetRenderLayerDefault
        );

        CustomizableSignBlockEntityRenderer.elementDistancingRenderLayer = Objects.requireNonNullElse(
                gameplayPreference.getFloat("elementDistancingCustomizableSigns"),
                CustomizableSignBlockEntityRenderer.elementDistancingRenderLayerDefault
        );
    }

}
