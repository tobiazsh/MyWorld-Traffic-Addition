package at.tobiazsh.myworld.traffic_addition.imgui.fonts;

import dev.tobiazsh.imguib3d.client.font.ImGuiFont;
import dev.tobiazsh.imguib3d.client.natives.drawlist.ImDrawListExt;
import imgui.*;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImGuiFontRenderer {

    private static final int IMGUI_VERTEX_SIZE = 20; // Each ImDrawVert is 20 bytes (currentX, currentY, u, v, color)
    private static final int IMGUI_VERTEX_BYTE_OFFSET = 4; // Each vertex has 4 floats (currentX, currentY, u, v, color)

    /**
     * Renders the given ImGuiFont. This method should be called within an ImGui context.
     *
     * @param font The ImGuiFont to render.
     * @param x The currentX-coordinate of the text area. (Top-left corner)
     * @param y The currentY-coordinate of the text area. (Top-left corner)
     * @param width The width of the text area.
     * @param height The height of the text area.
     * @param color The color of the text in rgba format (0-1) in rgba.
     * @param rotationDegrees The rotation of the text in degrees.
     * @param pivotX The x-coordinate of the pivot point for rotation normalized (0,0 is top-left, 1,1 is bottom-right).
     *               Relative to the bounding box of the vertices. For example, (0.5, 0.5) would rotate around the center.
     * @param pivotY The y-coordinate of the pivot point for rotation normalized (0,0 is top-left, 1,1 is bottom-right).
     *               Relative to the bounding box of the vertices. For example, (0.5, 0.5) would rotate around the center.
     * @param text The text to render.
     */
    public static void renderFont(
            @NotNull ImGuiFont font,
            float x,
            float y,
            float width,
            float height,
            int color,
            float rotationDegrees,
            float pivotX,
            float pivotY,
            @NotNull String text
    ) {
        if (width == 0 || height == 0) {
            return; // Avoid division by zero or rendering with zero dimensions
        }

        if (text.isEmpty()) {
            return; // Avoid rendering empty text
        }

        ImDrawList drawList = ImGui.getWindowDrawList();

        // Get starting position in draw list to transform vertices later
        int beginVtxIdx = drawList.getVtxCurrentIdx();

        font.getImFont().renderText(
                drawList,
                font.getFontKey().fontSize(),
                x,
                y,
                ImColor.rgba(255, 255, 255, 255),
                x,
                y,
                x + width,
                y + height,
                text,
                null
        );

       int endVtxIdx = drawList.getVtxCurrentIdx();

        if (beginVtxIdx >= endVtxIdx) {
            return;
        }

        ByteBuffer vtxBuffer = ImDrawListExt.getVtxBuffer(drawList.ptr)
                .order(ByteOrder.nativeOrder()); // Java uses big endian by default, imgui uses system's endianness

        applyScaling(
                vtxBuffer,
                beginVtxIdx,
                endVtxIdx,
                width,
                height
        );

        applyRotation(
                vtxBuffer,
                beginVtxIdx,
                endVtxIdx,
                rotationDegrees,
                pivotX,
                pivotY
        );
    }

    /**
     * Applies rotation to the vertices in the given range around the specified pivot point.
     *
     * @param vertices The vertex buffer containing the vertices to rotate (writeable).
     *                 Gotten using the ImDrawListExt.getVtxBuffer(drawList.ptr) method.
     * @param beginVtxIdx The starting index of the vertex range to rotate.
     * @param endVtxIdx The ending index of the vertex range to rotate.
     * @param rotationDegrees The rotation angle in degrees (clockwise).
     * @param pivotX The x-coordinate of the pivot point for rotation normalized (0,0 is top-left, 1,1 is bottom-right).
     *               Relative to the bounding box of the vertices. For example, (0.5, 0.5) would rotate around the center.
     * @param pivotY The y-coordinate of the pivot point for rotation normalized (0,0 is top-left, 1,1 is bottom-right).
     *               Relative to the bounding box of the vertices. For example, (0.5, 0.5) would rotate around the center.
     */
    private static void applyRotation(
            @NotNull ByteBuffer vertices,
            int beginVtxIdx,
            int endVtxIdx,
            float rotationDegrees,
            float pivotX,
            float pivotY
    ) {
        ImVec4 extremes = findExtremes(vertices, beginVtxIdx, endVtxIdx);
        float centerX = extremes.x + (extremes.z - extremes.x) * pivotX;
        float centerY = extremes.y + (extremes.w - extremes.y) * pivotY;

        float radians = (float) Math.toRadians(rotationDegrees);
        float cosTheta = (float) Math.cos(radians);
        float sinTheta = (float) Math.sin(radians);

        for (int i = beginVtxIdx; i < endVtxIdx; i++) {
            int vertexOffset = i * IMGUI_VERTEX_SIZE; // Each ImDrawVert is 20 bytes
            float x = vertices.getFloat(vertexOffset);
            float y = vertices.getFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET); // 4 bytes for the currentX coordinate

            // Rotate the vertex position around the pivot point
            float dx = x - centerX;
            float dy = y - centerY;

            float rotatedX = centerX + dx * cosTheta - dy * sinTheta;
            float rotatedY = centerY + dx * sinTheta + dy * cosTheta;

            // Write back the rotated position
            vertices.putFloat(vertexOffset, rotatedX);
            vertices.putFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET, rotatedY);
        }
    }
    /**
     * Scales the vertices in the given range to fit within the specified width and height.
     * Top left corner is kept at the same position, only the width and height are scaled.
     *
     * @param vertices The vertex buffer containing the vertices to scale (writeable).
     *                 Gotten using the ImDrawListExt.getVtxBuffer(drawList.ptr) method.
     * @param beginVtxIdx The starting index of the vertex range to scale.
     * @param endVtxIdx The ending index of the vertex range to scale.
     * @param width The target width to scale the vertices to.
     * @param height The target height to scale the vertices to.
     */
    private static void applyScaling(
            @NotNull ByteBuffer vertices,
            int beginVtxIdx,
            int endVtxIdx,
            float width,
            float height
    ) {
        ImVec4 extremes = findExtremes(vertices, beginVtxIdx, endVtxIdx);
        float scaleX = width / (extremes.z - extremes.x); // width / (maxX - minX)
        float scaleY = height / (extremes.w - extremes.y); // height / (maxY - minY)

        for (int i = beginVtxIdx; i < endVtxIdx; i++) {
            int vertexOffset = i * IMGUI_VERTEX_SIZE; // Each ImDrawVert is 20 bytes
            float x = vertices.getFloat(vertexOffset);
            float y = vertices.getFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET); // 4 bytes for the currentX coordinate

            // Scale the vertex position
            float scaledX = extremes.x + (x - extremes.x) * scaleX;
            float scaledY = extremes.y + (y - extremes.y) * scaleY;

            // Write back the scaled position
            vertices.putFloat(vertexOffset, scaledX);
            vertices.putFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET, scaledY);
        }
    }

    // Maybe useful later on
    @SuppressWarnings("unused")
    private static void moveTo(
            @NotNull ByteBuffer vertices,
            int beginVtxIdx,
            int endVtxIdx,
            int x,
            int y
    ) {
        ImVec4 extremes = findExtremes(vertices, beginVtxIdx, endVtxIdx);
        float deltaX = x - extremes.x;
        float deltaY = y - extremes.y;

        for (int i = beginVtxIdx; i < endVtxIdx; i++) {
            int vertexOffset = i * IMGUI_VERTEX_SIZE; // Each ImDrawVert is 20 bytes
            float currentX = vertices.getFloat(vertexOffset);
            float currentY = vertices.getFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET); // 4 bytes for the currentX coordinate

            // Move the vertex position
            float movedX = currentX + deltaX;
            float movedY = currentY + deltaY;

            // Write back the moved position
            vertices.putFloat(vertexOffset, movedX);
            vertices.putFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET, movedY);
        }
    }

    /**
     * Finds the minimum and maximum currentX and currentY coordinates of the vertices in the given range.
     * @param vertices The ByteBuffer containing the vertex data.
     * @param beginVtxIdx The starting index of the vertex range to check.
     * @param endVtxIdx The ending index of the vertex range to check.
     * @return An ImVec4 where currentX = minX, currentY = minY, z = maxX, w = maxY
     */
    private static ImVec4 findExtremes(
            @NotNull ByteBuffer vertices,
            int beginVtxIdx,
            int endVtxIdx
    ) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (int i = beginVtxIdx; i < endVtxIdx; i++) {
            int vertexOffset = i * IMGUI_VERTEX_SIZE; // Each ImDrawVert is 20 bytes
            float x = vertices.getFloat(vertexOffset);
            float y = vertices.getFloat(vertexOffset + IMGUI_VERTEX_BYTE_OFFSET);

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        return new ImVec4(minX, minY, maxX, maxY);
    }
}
