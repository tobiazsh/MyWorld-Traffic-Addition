package at.tobiazsh.myworld.traffic_addition.font;

/**
 * A record representing a font used on the server, which contains the logical font and the font size used.
 *
 * @param font
 * @param fontSize
 */
public record ServerFont(LogicalFont font, float fontSize) {

    public static ServerFont of(String fontHash, float fontSize) {
        return new ServerFont(new LogicalFont(fontHash), fontSize);
    }

}
