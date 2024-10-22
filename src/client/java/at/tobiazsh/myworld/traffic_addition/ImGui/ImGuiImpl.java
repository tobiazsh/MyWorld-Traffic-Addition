package at.tobiazsh.myworld.traffic_addition.ImGui;


/*
 * @created 26/09/2024 (DD/MM/YYYY) - 16:38
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import imgui.*;
import imgui.extension.implot.ImPlot;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImGuiImpl {
    private final static ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final static ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    public static ImFont DejaVuSans = null;
    public static ImFont DejaVuSansBold = null;
    public static ImFont DejaVuSansBoldBig = null;

    public static void create(final long handle) {
        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(MyWorldTrafficAddition.MOD_ID + ".ini");
        io.setFontGlobalScale(1F);

        // Load fonts
        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());

        final ImFontConfig fontConfig = new ImFontConfig();

        final short[] glyphRanges = rangesBuilder.buildRanges();
        DejaVuSans = io.getFonts().addFontFromMemoryTTF(loadFromResource("/ImGui/Fonts/DejaVuSans.ttf"), 20, fontConfig, glyphRanges);
        DejaVuSansBold = io.getFonts().addFontFromMemoryTTF(loadFromResource("/ImGui/Fonts/DejaVuSans-Bold.ttf"), 20, fontConfig, glyphRanges);
        DejaVuSansBoldBig = io.getFonts().addFontFromMemoryTTF(loadFromResource("/ImGui/Fonts/DejaVuSans-Bold.ttf"), 30, fontConfig, glyphRanges);

        io.getFonts().build();
        fontConfig.destroy();

        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);

        imGuiImplGlfw.init(handle, true);
        imGuiImplGl3.init();
    }

    public static void draw(final RenderInterface runnable) {
        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame(); // Handle keyboard and mouse interactions
        ImGui.newFrame();

        runnable.render(ImGui.getIO());

        ImGui.render();
        imGuiImplGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long pointer = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();

            GLFW.glfwMakeContextCurrent(pointer);
        }
    }

    public static void dispose() {
        imGuiImplGl3.shutdown();

        ImGui.destroyContext();
        ImPlot.destroyContext();
    }

    private static byte[] loadFromResource(String name) {
        try {
            return Files.readAllBytes(Paths.get(MyWorldTrafficAdditionClient.class.getResource(name).toURI()));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
