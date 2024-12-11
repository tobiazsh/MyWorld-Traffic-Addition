package at.tobiazsh.myworld.traffic_addition.ImGui.Windows;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 12:36
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.CustomizableSign.SignOperation;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.ElementAddWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.ElementPropertyWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.ElementsWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.JsonPreviewPopUp;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.StylePopUp;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.SignPreview;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FileSystem.Folder;
import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignStyle;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl.DejaVuSans;
import static at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl.clearFontAtlas;

public class SignEditor {

    private static BlockPos masterBlockPos = null;
    
    private static CustomizableSignBlockEntity blockEntity;

    private static int signWidthBlocks;
    private static int signHeightBlocks;
    
    public static CustomizableSignStyle signJson = new CustomizableSignStyle();
    public static List<String> backgroundTextures = new ArrayList<>();
    public static List<BaseElement> elementOrder = new ArrayList<>();
    public static BaseElement selectedElement = null;

    private static void quit() {
        ImGui.closeCurrentPopup();
        ImGuiRenderer.showSignEditor = false;
        clearFontAtlas();
    }

    public static void disposeChildWindows() {
        ElementsWindow.shouldRender = false;
        ElementAddWindow.shouldRender = false;
        ElementPropertyWindow.shouldRender = false;
    }

    public static void render() {
        renderMain();
        ElementsWindow.render();
        ElementAddWindow.render();
        ElementPropertyWindow.render();
        StylePopUp.render(backgrounds, blockEntity);
    }

    public static void open(BlockPos masterBlockPos, @NotNull World world, boolean isInit) {
        
        if (!isInit) {
            ErrorPopup.open("Sign not initialized!", "The sign has not been initialized yet! This is crucial, so please do not proceed without initializing the sign first!", SignEditor::quit);
        }
        
        SignEditor.masterBlockPos = masterBlockPos;

        disposeChildWindows();

        selectedElement = null;
        signJson = new CustomizableSignStyle();

        if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity csbe) blockEntity = csbe;
        else {
            System.err.println("Error (Rendering Sign Editor): Couldn't determine Sign (CustomizableSignBlockEntity not found)");
        }
        
        getSignSize();
        
        // List all available backgrounds
        try {
            backgrounds = FileSystem.FromResource.listFolders("/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        float previewHeight = 1100;
        float previewWidth = 1100;
        signRatio = createRatio(previewHeight, previewWidth, signWidthBlocks, signHeightBlocks);
        elementOrder = new ArrayList<>();

        backgroundTextures.clear();

        for (int i = 0; i < signHeightBlocks * signWidthBlocks; i++)
            backgroundTextures.add("/assets/myworld_traffic_addition/textures/imgui/icons/not-found.png");

        readFromSign(world);

        ImGuiRenderer.showSignEditor = true;
    }
    
    private static void readFromSign(World world) {
        SignOperation.Json.Reader reader = new SignOperation.Json.Reader();
        reader.read(masterBlockPos, world, signJson);

        elementOrder = reader.getDrawables();

        List<String> bgTex = reader.getBackgroundTextures();
        if (!bgTex.isEmpty()) backgroundTextures = bgTex;
    }

    private static void getSignSize() {
        signHeightBlocks = blockEntity.getHeight();
        signWidthBlocks = blockEntity.getWidth();
    }

    private static ImVec2 createRatio(float maxHeight, float maxWidth, float width, float height) {
        float newWidth, newHeight;

        if (width > height) {
            newHeight = maxHeight;
            newWidth = (newHeight / height) * width;

            // Ensure new width does not exceed maxWidth
            if (newWidth > maxWidth) {
                newWidth = maxWidth;

                newHeight = (newWidth / width) * height;
            }
        } else if (width == height) {
            // Handle square case by comparing maxWidth and maxHeight
            newWidth = Math.min(maxWidth, maxHeight);
            newHeight = newWidth;  // Square ratio so both should be the same
        } else {
            newWidth = maxWidth;
            newHeight = (newWidth / width) * height;

            // Ensure new height does not exceed maxHeight
            if (newHeight > maxHeight) {
                newHeight = maxHeight;

                newWidth = (newHeight / height) * width;
            }
        }

        return new ImVec2(newWidth, newHeight);
    }

    private static void handlePopUps() {
        ConfirmationPopup.render();

        // ... More to come
    }

    private static Folder backgrounds = null; // All Countries in ImGui/SignRes/Backgrounds/

    public static ImVec2 signRatio; // Initialized when screen is opened;
    private static float zoomScale = 1.0f;

    public static void renderMain(){
        ImGui.pushFont(DejaVuSans);
        ImGui.begin("Sign Preview", ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoNavInputs);

        // Factor for the size of the canvas
        float factor = signRatio.y / signHeightBlocks * zoomScale; // Size of each "sign" tile in pixels

        renderMenuBar();
        handleHotKeys();
        handlePopUps();

        JsonPreviewPopUp.create();
        if (JsonPreviewPopUp.shouldOpen) JsonPreviewPopUp.open(signJson);

        // Calculations for sign preview

        float signWidthPixels = signRatio.x;
        float signHeightPixels = signRatio.y;

        float cursorAddToHeight = (ImGui.getWindowHeight() - signHeightPixels) / 2;
        float cursorAddToWidth = (ImGui.getWindowWidth() - signWidthPixels) / 2;

        // Set the cursor position once, to the top-left of the entire centered grid
        ImGui.setCursorPos(cursorAddToWidth, cursorAddToHeight);
        ImVec2 previewPosition = ImGui.getCursorPos();

        SignPreview.render(signWidthPixels, signHeightPixels, signWidthBlocks, signHeightBlocks, factor, previewPosition, elementOrder, backgroundTextures);

        ImGui.end();
        ImGui.popFont();
    }

    private static void renderMenuBar() {
        ImGui.beginMenuBar();
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Save to Sign", "CTRL + S")) {
                SignOperation.Json.write(masterBlockPos, signJson, elementOrder, blockEntity);
            }

            if (ImGui.menuItem("Save to Sign & Quit", "CTRL + W")) {
                SignOperation.Json.write(masterBlockPos, signJson, elementOrder, blockEntity);
                quit();
            }

            if (ImGui.menuItem("Show JSON", "CTRL + F")) JsonPreviewPopUp.shouldOpen = true;

            if (ImGui.menuItem("Quit", "CTRL + Q")) {
                quit();
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Edit")) {
            if (ImGui.menuItem("Clear Canvas")) clearCanvas();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Styling")) {
            if (ImGui.menuItem("Choose Style Type", "CTRL + G")) StylePopUp.open();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Toggle Element Window", "CTRL + E")) ElementsWindow.toggle();
            if (ImGui.menuItem("Toggle Element Properties Window", "CTRL + P")) ElementPropertyWindow.toggle();

            ImGui.endMenu();
        }

        if(ImGui.beginMenu("Elements")) {
            if (ImGui.menuItem("Add Element...", "CTRL + SHIFT + A")) ElementAddWindow.open();
            if (ImGui.menuItem("Add Text Element", "CTRL + SHIFT + T")) TextElementClient.add(elementOrder);
            //if (ImGui.menuItem("Zoom Out", "CTRL + O")) zoomOut(0.05f); TODO: Implement Zooming
            //if (ImGui.menuItem("Zoom In", "CTRL + I")) zoomIn(0.05f); TODO: Implement Zooming

            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }

    private static void handleHotKeys() {
        boolean ctrl = ImGui.isKeyDown(ImGuiKey.LeftCtrl) || ImGui.isKeyDown(ImGuiKey.RightCtrl);
        boolean shift = ImGui.isKeyDown(ImGuiKey.LeftShift) || ImGui.isKeyDown(ImGuiKey.RightShift);

        //if (ctrl && ImGui.isKeyPressed(ImGuiKey.I)) zoomIn(0.05f); TODO: Implement Zooming
        //if (ctrl && ImGui.isKeyPressed(ImGuiKey.O)) zoomOut(0.05f); TODO: Implement Zooming
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.S)) SignOperation.Json.write(masterBlockPos, signJson, elementOrder, blockEntity);

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.W)) {
            SignOperation.Json.write(masterBlockPos, signJson, elementOrder, blockEntity);
            quit();
        }

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.F)) JsonPreviewPopUp.shouldOpen = true;
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.Q)) quit();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.G)) StylePopUp.open();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.E)) ElementsWindow.toggle();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.P)) ElementPropertyWindow.toggle();
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.A)) ElementAddWindow.open();
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.T)) TextElementClient.add(elementOrder);
    }

    private static void zoomOut(float amount) {
        zoomScale = Math.max(0.1f, zoomScale - amount); // Prevent zooming out too much
    }

    private static void zoomIn(float amount) {
        zoomScale = Math.min(10.0f, zoomScale + amount); // Prevent excessive zoom-in
    }

    private static void clearCanvas() {
        ConfirmationPopup.show("Are you sure you want to clear the canvas?", "All of your current elements will be removed! This action cannot be undone!", elementOrder::clear, ()->{});
    }
}