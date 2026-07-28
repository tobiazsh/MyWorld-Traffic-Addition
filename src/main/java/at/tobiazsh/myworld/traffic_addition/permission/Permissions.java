package at.tobiazsh.myworld.traffic_addition.permission;

import at.tobiazsh.myworld.traffic_addition.toml.codec.Codecs;
import at.tobiazsh.myworld.traffic_addition.toml.serialization.annotation.TomlChild;
import at.tobiazsh.myworld.traffic_addition.toml.serialization.annotation.TomlRoot;

@TomlRoot
public class Permissions implements PermissionHierarchy {

    public final CustomizableSigns customizableSigns = new CustomizableSigns();

    @TomlChild("customizable_signs")
    public static class CustomizableSigns {

        public final Elements elements = new Elements();

        @TomlChild("elements")
        public static class Elements {
            public Permission<Boolean> canBypassLimit =
                    new Permission<>("can_bypass_limit", (may) -> may, Codecs.BOOLEAN);
        }
    }
}