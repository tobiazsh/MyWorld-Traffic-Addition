package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;

import java.util.ArrayList;
import java.util.List;

public class FontRepository {

    private static final FontRepository INSTANCE = new FontRepository();

    private final List<FontDefinition> fonts = new ArrayList<>();

    public static FontRepository getInstance() {
        return INSTANCE;
    }

    public void discover() {

    }

    public List<FontDefinition> getFonts() {
        return List.copyOf(fonts);
    }
}
