package at.tobiazsh.myworld.traffic_addition.imgui.utils;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.BasicFont;
import imgui.*;

public class ImGuiFont extends BasicFont {
    public String name; // Font name
    public ImFont font; // The font object

    /**
     * Creates a new ImGuiFont object
     * @param filePath The path of the font file. Must use the format "/some/directory/some_font_name.ttf".
     * @param font The ImFont object that should be associated with the font
     */
    public ImGuiFont(String filePath, ImFont font) {
        super(filePath, font.getFontSize());
        this.font = font;
        this.name = resolveFontName(filePath.substring(filePath.lastIndexOf("/") + 1));
    }

    public ImGuiFont(String filePath, ImFont font, float fontSize) {
        super(filePath, fontSize);
        this.font = font;
        this.name = resolveFontName(filePath.substring(filePath.lastIndexOf("/") + 1));
    }

    /**
     * Renders (non-)rotated text to screen
     * @param drawList The draw list to render to
     * @param text The text to render
     * @param pos The position to render at
     * @param size The size of the text
     * @param rotation The rotation to apply in degrees (clockwise)
     * @param col The color to render the text in
     */
    public void renderText(ImDrawList drawList, String text, ImVec2 pos, ImVec2 size, float rotation, ImVec4 col) {

        // Font not found or not initialized
        if (this.font == null) {
            MyWorldTrafficAddition.LOGGER.error("Couldn't render text because the font \"{}\" was either not registered or some unknown other error occurred. (\"Font is null!\")", this.getFontPath());
            return;
        }

        char c;
        int color = ImGui.colorConvertFloat4ToU32(col);
        ImFontGlyph glyph;

        ImVec2 windowPos = new ImVec2(ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY());
        ImVec2 plainTextSize = ImGui.calcTextSize(text);

        float scaleX = size.x / plainTextSize.x;
        float scaleY = size.y / plainTextSize.y;

        ImVec2 textCenter = new ImVec2(
                windowPos.x + pos.x + plainTextSize.x * 0.5f * scaleX,
                windowPos.y + pos.y + plainTextSize.y * 0.5f * scaleY
        );

        float radians = (float) Math.toRadians(rotation);
        float cosTheta = (float) Math.cos(radians);
        float sinTheta = (float) Math.sin(radians);

        float totalAdvanceX  = 0;
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            glyph = this.font.findGlyph(c);
            if (glyph.isNotValidPtr()) {
                MyWorldTrafficAddition.LOGGER.error("Couldn't render char \"{}\" because \"{}\" does not exist in font's charset!", c, c);
                continue;
            }

            ImVec2 p0 = new ImVec2(
                    windowPos.x + totalAdvanceX + glyph.getX0() * scaleX + pos.x,
                    windowPos.y + glyph.getY0() * scaleY + pos.y
            );
            ImVec2 p1 = new ImVec2(
                    windowPos.x + totalAdvanceX + glyph.getX1() * scaleX + pos.x,
                    windowPos.y + glyph.getY0() * scaleY + pos.y
            );
            ImVec2 p2 = new ImVec2(
                    windowPos.x + totalAdvanceX + glyph.getX1() * scaleX + pos.x,
                    windowPos.y + glyph.getY1() * scaleY + pos.y
            );
            ImVec2 p3 = new ImVec2(
                    windowPos.x + totalAdvanceX + glyph.getX0() * scaleX + pos.x,
                    windowPos.y + glyph.getY1() * scaleY + pos.y
            );

            p0 = rotatePoint(p0, textCenter, cosTheta, sinTheta);
            p1 = rotatePoint(p1, textCenter, cosTheta, sinTheta);
            p2 = rotatePoint(p2, textCenter, cosTheta, sinTheta);
            p3 = rotatePoint(p3, textCenter, cosTheta, sinTheta);

            drawList.primReserve(6, 4);
            drawList.primQuadUV(
                    p0, p1, p2, p3,

                    new ImVec2(glyph.getU0(), glyph.getV0()),
                    new ImVec2(glyph.getU1(), glyph.getV0()),
                    new ImVec2(glyph.getU1(), glyph.getV1()),
                    new ImVec2(glyph.getU0(), glyph.getV1()),

                    color
            );

            totalAdvanceX += glyph.getAdvanceX() * scaleX;
        }
    }

    /**
     * Rotates a point around a center point
     * @param point The point to rotate
     * @param center The center point to rotate around
     * @param cosTheta The cosine of the angle to rotate by
     * @param sinTheta The sine of the angle to rotate by
     * @return The rotated point
     */
    private ImVec2 rotatePoint(ImVec2 point, ImVec2 center, float cosTheta, float sinTheta) {
        float dx = point.x - center.x;
        float dy = point.y - center.y;

        float rotatedX = center.x + dx * cosTheta - dy * sinTheta;
        float rotatedY = center.y + dx * sinTheta + dy * cosTheta;

        return new ImVec2(rotatedX, rotatedY);
    }
}
