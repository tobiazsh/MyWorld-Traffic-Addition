package at.tobiazsh.myworld.traffic_addition.resource;

import java.io.InputStream;

public class ResourceLoader {

    public static InputStream getResourceAsStream(Location location) {
        if (location.isResource()) {
            return ResourceLoader.class.getClassLoader().getResourceAsStream(location.getResourceLocation().toString().substring(9));
        } else {
            try {
                return new java.io.FileInputStream(location.getLocation());
            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
