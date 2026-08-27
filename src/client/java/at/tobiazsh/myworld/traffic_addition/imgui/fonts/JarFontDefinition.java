package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;

import java.io.IOException;
import java.io.InputStream;

/**
 * A font definition that loads the font from the filesystem (JAR).
 */
public class JarFontDefinition implements FontDefinition {

    private String jarPath;
    private String displayName;

    public JarFontDefinition(String jarPath, String displayName) {
        this.jarPath = jarPath;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return jarPath;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public InputStream openStream() throws IOException {
        return JarFontDefinition.class.getResourceAsStream(jarPath);
    }
}
