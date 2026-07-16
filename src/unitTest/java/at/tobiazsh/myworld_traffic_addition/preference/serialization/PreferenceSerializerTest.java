package at.tobiazsh.myworld_traffic_addition.preference.serialization;

import at.tobiazsh.myworld.traffic_addition.preference.Preference;
import at.tobiazsh.myworld.traffic_addition.preference.annotation.PreferenceChild;
import at.tobiazsh.myworld.traffic_addition.preference.annotation.PreferenceRoot;
import at.tobiazsh.myworld.traffic_addition.preference.codec.Codec;
import at.tobiazsh.myworld.traffic_addition.preference.codec.Codecs;
import at.tobiazsh.myworld.traffic_addition.preference.serialization.PreferenceNode;
import at.tobiazsh.myworld.traffic_addition.preference.serialization.PreferenceSerializer;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PreferenceSerializerTest {

    /**
     * Class for Codec Demonstration
     */
    public static class TestDog {
        private final float thirst;
        private final float hunger;
        private final String color;

        public static final Codec<TestDog> CODEC = Codec.of(
                obj -> deserialize(obj.asPrimitive().asString()),
                dog -> TomlPrimitive.of(dog.serialize())
        );

        public TestDog(float thirst, float hunger, String color) {
            this.thirst = thirst;
            this.hunger = hunger;
            this.color = color;
        }

        @SuppressWarnings("unused")
        public void woof() {
            System.out.println("Woof!");
        }

        public String serialize() {
            return "Dog{" +
                    "thirst=" + thirst +
                    ", hunger=" + hunger +
                    ", color=" + color +
                    "}";
        }

        public static TestDog deserialize(String serialized) {
            String[] parts = serialized.replace("Dog{", "").replace("}", "").split(", ");
            float thirst = Float.parseFloat(parts[0].split("=")[1]);
            float hunger = Float.parseFloat(parts[1].split("=")[1]);
            String color = parts[2].split("=")[1];

            return new TestDog(thirst, hunger, color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestDog dog)) return false;

            return  this.thirst == dog.thirst &&
                    this.hunger == dog.hunger &&
                    this.color.equals(dog.color);
        }
    }

    @PreferenceRoot
    public static class PreferenceClass {

        public General general = new General();
        public Advanced advancedField = new Advanced(); // Chose a more complex name on purpose!
        public RenderingSettings renderingSettings = new RenderingSettings();

        @PreferenceChild("general")
        public static class General {
            private General() {}

            public Preference<Boolean> enableFeatureX = new Preference<>(true, "enable_feature_x", Codecs.BOOLEAN);
            public Preference<Integer> maxConnections = new Preference<>(10, "max_connections", Codecs.INTEGER);

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof General general)) return false;

                return this.maxConnections.equals(general.maxConnections) &&
                        this.enableFeatureX.equals(general.enableFeatureX);
            }
        }

        @PreferenceChild
        public static class Advanced {
            private Advanced() {}

            public Preference<String> advancedOption = new Preference<>("default", "advanced_option", Codecs.STRING);

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Advanced)) return false;

                return this.advancedOption.equals(((Advanced) o).advancedOption);
            }
        }

        @PreferenceChild("rendering_settings")
        public static class RenderingSettings {
            private RenderingSettings() {}

            public Preference<TestDog> renderDog = new Preference<>(new TestDog(0.5f, 0.5f, "brown"), "render_dog", TestDog.CODEC);

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof RenderingSettings)) return false;

                return this.renderDog.equals(((RenderingSettings) o).renderDog);
            }
        }

        @Override
        public String toString() {
            return "PreferenceClass{" +
                    "general=" + general +
                    ", advancedField=" + advancedField +
                    ", renderingSettings=" + renderingSettings +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PreferenceClass that)) return false;

            return this.advancedField.advancedOption.equals(that.advancedField.advancedOption) &&
                    this.general.enableFeatureX.equals(that.general.enableFeatureX) &&
                    this.general.maxConnections.equals(that.general.maxConnections) &&
                    this.renderingSettings.equals(that.renderingSettings);
        }
    }

    @Test
    void scan() {
        PreferenceNode rootNode = new PreferenceNode("PreferenceClass", new PreferenceClass());

        PreferenceNode generalNode = new PreferenceNode("general", new PreferenceClass());
        PreferenceNode advancedNode = new PreferenceNode("advancedField", new PreferenceClass());
        PreferenceNode renderingSettings = new PreferenceNode("rendering_settings", new PreferenceClass());

        generalNode.preferences().put("enable_feature_x", new Preference<>(true, "enable_feature_x", Codecs.BOOLEAN));
        generalNode.preferences().put("max_connections", new Preference<>(10, "max_connections", Codecs.INTEGER));
        advancedNode.preferences().put("advanced_option", new Preference<>("default", "advanced_option", Codecs.STRING));
        renderingSettings.preferences().put("render_dog", new Preference<>(new TestDog(0.5f, 0.5f, "brown"), "render_dog", TestDog.CODEC));

        rootNode.children().put(generalNode.id(), generalNode);
        rootNode.children().put(advancedNode.id(), advancedNode);
        rootNode.children().put(renderingSettings.id(), renderingSettings);

        PreferenceNode node = PreferenceSerializer.scan(new PreferenceClass());
        Assertions.assertEquals(rootNode, node, "The scanned preference node does not match the expected structure.");
    }

    @Test
    void serialize() {
        PreferenceNode node = PreferenceSerializer.scan(new PreferenceClass());

        String toml = """
                
                [advancedField]
                advanced_option = "default"
                
                [general]
                enable_feature_x = true
                max_connections = 10
                
                [rendering_settings]
                render_dog = "Dog{thirst=0.5, hunger=0.5, color=brown}"
                """;

        String serialized = PreferenceSerializer.serializeToToml(node);
        Assertions.assertEquals(toml, serialized);
    }

    @Test
    void deserialize() {
        String toml = """
                
                [advancedField]
                advanced_option = "hehe, this is more advanced and therefore I am intellectually superior :)"
                
                [general]
                enable_feature_x = false
                max_connections = 42
                
                [rendering_settings]
                render_dog = "Dog{thirst=0.69, hunger=0.67, color=fuchsia}"
                """;

        PreferenceClass expectedClass = new PreferenceClass();
        expectedClass.advancedField.advancedOption.set("hehe, this is more advanced and therefore I am intellectually superior :)");
        expectedClass.general.enableFeatureX.set(false);
        expectedClass.general.maxConnections.set(42);
        expectedClass.renderingSettings.renderDog.set(new TestDog(0.69f, 0.67f, "fuchsia"));

        PreferenceClass clazz = PreferenceSerializer.deserializeFromToml(toml, new PreferenceClass());

        Assertions.assertEquals(expectedClass, clazz, "The deserialized preference node does not match the expected structure.");
    }
}
