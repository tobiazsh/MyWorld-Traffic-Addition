package at.tobiazsh.myworld.traffic_addition.preference;

import at.tobiazsh.myworld.traffic_addition.preference.annotation.PreferenceChild;
import at.tobiazsh.myworld.traffic_addition.preference.annotation.PreferenceRoot;
import at.tobiazsh.myworld.traffic_addition.preference.codec.Codecs;

@PreferenceRoot
public class ClientPreferences implements PreferenceHierarchy {

    public final CustomizableSigns customizableSigns = new CustomizableSigns();
    public final Signs signs = new Signs();
    public final Rendering rendering = new Rendering();
    public final General general = new General();

    @PreferenceChild("customizable_signs")
    public static class CustomizableSigns {
        private CustomizableSigns() {}

        public final Preference<Float> elementDistancing = new Preference<>(0.75f, "view_distance", Codecs.FLOAT);
        public final Preference<Float> viewDistance = new Preference<>(3f, "element_distancing", Codecs.FLOAT);
    }

    @PreferenceChild("signs")
    public static class Signs {
        private Signs() {}

        public final Preference<Float> viewDistance = new Preference<>(3f, "view_distance", Codecs.FLOAT);
    }

    @PreferenceChild("rendering")
    public static class Rendering {
        private Rendering() {}

        public final Preference<Integer> imageRenderLayerCacheSize = new Preference<>(200, "image_render_layer_cache_size", Codecs.INTEGER);
        public final Preference<Integer> textRenderLayerCacheSize = new Preference<>(100, "text_render_layer_cache_size", Codecs.INTEGER);
    }

    @PreferenceChild("general")
    public static class General {
        private General() {}

        public final Preference<String> language = new Preference<>("auto", "language", Codecs.STRING);
    }
}
