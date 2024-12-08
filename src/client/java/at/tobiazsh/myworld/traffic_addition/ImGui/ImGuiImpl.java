package at.tobiazsh.myworld.traffic_addition.ImGui;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 16:38
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FontManager;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiConfigFlags;

import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImInt;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FontManager.*;
import static org.lwjgl.opengl.GL11.*;

public class ImGuiImpl {
    private final static ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    public final static ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    public static ImFont DejaVuSans = null;
    public static ImFont DejaVuSansBold = null;
    public static ImFont DejaVuSansBoldBig = null;

    public static ImFontAtlas fontAtlas;

    public static void create(final long handle) {
        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(MyWorldTrafficAddition.MOD_ID + ".ini");
        io.setFontGlobalScale(1F);

        fontAtlas = io.getFonts();

        // Load fonts
        registerDefaultFonts();
        ImGui.getIO().setFontDefault(DejaVuSans);

        fontAtlas.build();

        buildFontRanges();

        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);

        imGuiImplGlfw.init(handle, true);
        imGuiImplGl3.init();
    }

    public static void registerDefaultFonts() {
        ImFontConfig fontConfig = new ImFontConfig();

        ImFontGlyphRangesBuilder glyphRangesBuilder = new ImFontGlyphRangesBuilder();
        glyphRangesBuilder.addRanges(ImGui.getIO().getFonts().getGlyphRangesDefault());

        short[] glyphRanges = glyphRangesBuilder.buildRanges();

        DejaVuSans = ImGui.getIO().getFonts().addFontFromMemoryTTF(loadFromResource("/assets/myworld_traffic_addition/font/dejavu_sans.ttf"), 20, fontConfig, glyphRanges);
        DejaVuSansBold = ImGui.getIO().getFonts().addFontFromMemoryTTF(loadFromResource("/assets/myworld_traffic_addition/font/dejavu_sans_bold.ttf"), 20, fontConfig, glyphRanges);
        DejaVuSansBoldBig = ImGui.getIO().getFonts().addFontFromMemoryTTF(loadFromResource("/assets/myworld_traffic_addition/font/dejavu_sans_bold.ttf"), 40, fontConfig, glyphRanges);
    }

    public static boolean fontsNeedRebuild = false;

    public static void uploadFontTexture() {
        // Retrieve font texture data from ImGui
        ImGuiIO io = ImGui.getIO();
        ImInt width, height, bytesPerPixel;

        width = new ImInt();
        height = new ImInt();
        bytesPerPixel = new ImInt();

        ByteBuffer pixels = io.getFonts().getTexDataAsRGBA32(width, height, bytesPerPixel);

        // Create OpenGL texture
        int textureID = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureID);

        // Set texture filtering and wrapping options
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

        // Upload texture to GPU
        GL33.glTexImage2D(
                GL33.GL_TEXTURE_2D,    // Texture target
                0,                     // Level of detail (0 is base image)
                GL33.GL_RGBA8,          // Internal format
                width.get(),           // Width
                height.get(),          // Height
                0,                     // Border (must be 0)
                GL33.GL_RGBA,          // Format of pixel data
                GL33.GL_UNSIGNED_BYTE, // Data type of pixel data
                pixels                 // Texture data
        );

        // Unbind texture
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

        // Tell ImGui the texture ID
        io.getFonts().setTexID(textureID);
    }

    public static void draw(final RenderInterface renderInterface) {
        registerPendingFonts();
        rebuildFontAtlasIfNeeded();

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();

        renderInterface.render(ImGui.getIO());

        ImGui.render();
        imGuiImplGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long pointer = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();

            GLFW.glfwMakeContextCurrent(pointer);
        }
    }

    private static void rebuildFontAtlasIfNeeded() {
        if (fontsNeedRebuild) {
            rebuildFontAtlas();
            fontsNeedRebuild = false;
        }
    }

    public static void clearFontAtlas() {
        FontManager.fontCache.clear();
        scheduleFontAtlasRebuild();
    }

    public static void scheduleFontAtlasRebuild() {
        fontsNeedRebuild = true;
    }

    public static void dispose() {
        imGuiImplGl3.shutdown();

        ImGui.destroyContext();
        ImPlot.destroyContext();
    }

    public static byte[] loadFromResource(String name) {
        try {
            return Files.readAllBytes(Paths.get(MyWorldTrafficAdditionClient.class.getResource(name).toURI()));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
