package at.tobiazsh.myworld.traffic_addition.resource;

import net.minecraft.resources.Identifier;

public class Location {
    private final boolean isResource;
    private final Identifier resourceLocation;
    private final String location;

    public Location(String location) {
        this.location = location;
        this.isResource = false;
        this.resourceLocation = null;
    }

    public Location(Identifier resourceLocation) {
        this.resourceLocation = resourceLocation;
        this.isResource = true;
        this.location = null;
    }

    public boolean isResource() {
        return isResource;
    }

    public Identifier getResourceLocation() {
        return resourceLocation;
    }

    public String getLocation() {
        return location;
    }
}
