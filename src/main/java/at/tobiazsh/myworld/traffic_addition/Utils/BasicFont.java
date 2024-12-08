package at.tobiazsh.myworld.traffic_addition.Utils;

public class BasicFont {
    private float fontSize;
    private String fontPath;

    public BasicFont(String path, float size) {
        this.fontPath = path;
        this.fontSize = size;
    }

    enum SPECIAL_FONT_SIZE {
        MINECRAFT(1.0f);

        private final float size;

        SPECIAL_FONT_SIZE(float size) {
            this.size = size;
        }

        public float getSize() {
            return size;
        }
    }

    /**
     * Extracts the font name from the font path
     * @param fontPath The path to the font file
     * @return The name of the font
     */
    public static String resolveFontName(String fontPath) {
        // Extract the file name without extension
        String name = fontPath.substring(fontPath.lastIndexOf("/") + 1);

        // Check if there is a period in the name, indicating an extension
        int lastDot = name.lastIndexOf(".");
        if (lastDot != -1) {
            name = name.substring(0, lastDot);
        }

        // Split by underscores and capitalize words if necessary
        String[] nameWords = name.split("_");

        for (int i = 0; i < nameWords.length; i++) {
            // Capitalize each word
            nameWords[i] = nameWords[i].substring(0, 1).toUpperCase() + nameWords[i].substring(1).toLowerCase();
        }

        return String.join(" ", nameWords);
    }

    public float getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(float size) {
        this.fontSize = size;
    }

    public String getFontPath() {
        return this.fontPath;
    }

    public void setFontPath(String path) {
        this.fontPath = path;
    }
}
