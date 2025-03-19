package at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows;


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
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups.*;
import at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.SignPreview;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Clipboard;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.Utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.Utils.FileSystem.Folder;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.SavesLogic.Saves;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.CustomizableSignBlockEntity;
import com.google.gson.*;
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
import static at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData.getPrettyJson;
import static at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData.updateToNewVersion;
import static at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement.currentElementFactor;
import static at.tobiazsh.myworld.traffic_addition.Utils.SavesLogic.Saves.createSavesDirIfNonExistent;

public class SignEditor {

    private static BlockPos masterBlockPos = null;
    
    private static CustomizableSignBlockEntity blockEntity;

    private static int signWidthBlocks;
    private static int signHeightBlocks;
    
    public static CustomizableSignData signJson = new CustomizableSignData();
    public static List<String> backgroundTextures = new ArrayList<>();
    public static String backgroundTexturePath;
    public static List<BaseElement> elementOrder = new ArrayList<>();
    public static BaseElement selectedElement = null;
    private static Folder backgrounds = null; // All Countries in ImGui/SignRes/Backgrounds/
    public static ImVec2 signRatio; // Initialized when screen is opened;
    public static boolean showDebug = false;

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
        BackgroundSelectorPopup.render(backgrounds, blockEntity);
        ConfirmationPopup.render();
        FileDialogPopup.render();
    }

    public static void setSignJson(CustomizableSignData json) {
        signJson = json;
    }

    public static CustomizableSignData getSignJson() {
        return signJson;
    }

    private static void readFromSign(World world) {
        SignOperation.Json.Reader reader = new SignOperation.Json.Reader();
        reader.readFromBlock(masterBlockPos, world);

        elementOrder = reader.getDrawables();

        List<String> bgTex = reader.getBackgroundTextures();
        if (!bgTex.isEmpty()) backgroundTextures = bgTex;

        updateToJson();
    }

    private static void updateFromJson() {
        elementOrder = CustomizableSignData.deconstructElementsToArray(getSignJson());
        backgroundTextures = CustomizableSignData.getBackgroundTexturePathList(getSignJson(), blockEntity);
    }

    private static void updateToJson() {
        CustomizableSignData json = new CustomizableSignData();

        if (backgroundTextures.isEmpty()) backgroundTexturePath = null;
        else backgroundTexturePath = backgroundTextures.getFirst().substring(0, backgroundTextures.getFirst().lastIndexOf("/") + 1);

        json.setStyle(backgroundTexturePath);
        json.setElements(elementOrder);

        setSignJson(json);
        BaseElement.setCurrentSignData(signJson, elementOrder);
    }

    public static void open(BlockPos masterBlockPos, @NotNull World world, boolean isInit) {

        if (!isInit) {
            ErrorPopup.open("Sign not initialized!", "The sign has not been initialized yet! This is crucial, so please do not proceed without initializing the sign first!", SignEditor::quit);
        }

        SignEditor.masterBlockPos = masterBlockPos;

        disposeChildWindows();

        selectedElement = null;
        setSignJson(new CustomizableSignData());

        if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity csbe) blockEntity = csbe;
        else {
            System.err.println("Error (Rendering Sign Editor): Couldn't determine Sign (CustomizableSignBlockEntity not found)");
        }

        getSignSize();

        // List all available backgrounds
        try {
            backgrounds = FileSystem.listFoldersRecursive("/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        signRatio = createRatio(SignPreview.previewMaxWidth, SignPreview.previewMaxHeight, signWidthBlocks, signHeightBlocks);
        elementOrder = new ArrayList<>();

        backgroundTextures.clear();
        backgroundTexturePath = "";

        Clipboard.clearUndoStack();
        Clipboard.clearRedoStack();

        readFromSign(world);
        calcFactor(); // Calculate the factor for the sign (the value to be multiplied to get MC blocks)

        ImGuiRenderer.showSignEditor = true;
    }

    private static void getSignSize() {
        signHeightBlocks = blockEntity.getHeight();
        signWidthBlocks = blockEntity.getWidth();
    }

    private static ImVec2 createRatio(float maxWidth, float maxHeight, float width, float height) {
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

    public static void renderMain(){
        ImGui.pushFont(DejaVuSans);
        ImGui.begin("Sign Preview", ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoNavInputs);

        renderMenuBar();
        handleHotKeys();

        JsonPreviewPopup.create();
        if (JsonPreviewPopup.shouldOpen) JsonPreviewPopup.open(getSignJson());

        // Position for the preview (in the middle)
        float previewX = (ImGui.getWindowWidth() - signRatio.x * SignPreview.getZoom()) * 0.5f; // signRatio.x * getZoom() because the size of the sign changes with zoom
        float previewY = (ImGui.getWindowHeight() - signRatio.y * SignPreview.getZoom()) * 0.5f; // -------//-------

        // Set the cursor position once, to the top-left of the entire centered grid
        ImGui.setCursorPos(previewX, previewY);

        SignPreview.render(signRatio.x, signRatio.y, signWidthBlocks, signHeightBlocks, currentElementFactor, new ImVec2(previewX, previewY), elementOrder, backgroundTextures);

        ImGui.end();
        ImGui.popFont();
    }

    private static void renderMenuBar() {
        ImGui.beginMenuBar();
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Save to Sign", "CTRL + S"))
                SignOperation.Json.write(masterBlockPos, getSignJson(), elementOrder);

            if (ImGui.menuItem("Save to Sign & Quit", "CTRL + W")) {
                SignOperation.Json.write(masterBlockPos, getSignJson(), elementOrder);
                quit();
            }

            if (ImGui.menuItem("Show JSON", "CTRL + F")) JsonPreviewPopup.shouldOpen = true;

            if (ImGui.menuItem("Quit", "CTRL + Q")) quit();

            ImGui.separator();

            if (ImGui.menuItem("Import...")) importSign();
            if (ImGui.menuItem("Export...")) exportSign();

            ImGui.separator();

            if(ImGui.menuItem("Toggle Debug")) showDebug = !showDebug;

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Edit")) {
            if (ImGui.menuItem("Clear Canvas")) clearCanvas();

            ImGui.separator();

            if (ImGui.menuItem("Undo", "CTRL + U")) undo();
            if (ImGui.menuItem("Redo", "CTRL + Shift + U")) redo();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Background")) {
            if (ImGui.menuItem("Choose Background", "CTRL + G")) BackgroundSelectorPopup.open();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Toggle Element Window", "CTRL + E")) ElementsWindow.toggle();
            if (ImGui.menuItem("Toggle Element Properties Window", "CTRL + P")) ElementPropertyWindow.toggle();

            if (ImGui.menuItem("Toggle Element and Properties Windows")) { // Useful since normally you'd want to have both windows open
                ElementsWindow.toggle();
                ElementPropertyWindow.toggle();
            }

            if (ImGui.menuItem("Zoom In", "CTRL + I")) SignPreview.zoomIn();
            if (ImGui.menuItem("Zoom Out", "CTRL + O")) SignPreview.zoomOut();

            ImGui.endMenu();
        }

        if(ImGui.beginMenu("Elements")) {
            if (ImGui.menuItem("Add Element...", "CTRL + SHIFT + A")) ElementAddWindow.open();
            if (ImGui.menuItem("Add Text Element", "CTRL + SHIFT + T")) TextElementClient.createNew(elementOrder);

            ImGui.separator();

            if (ImGui.menuItem("Import Element...")) importElement();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Clipboard")) {
            if (ImGui.menuItem("Copy Sign", "CTRL + C")) copySign();
            if (ImGui.menuItem("Paste Sign", "CTRL + ALT + V")) pasteSign();

            ImGui.separator();

            if (ImGui.menuItem("Paste Element", "CTRL + SHIFT + V")) pasteElement();

            ImGui.endMenu();
        }

        if (showDebug) if (ImGui.beginMenu("Debug")) {

            if (ImGui.menuItem("Update Json")) updateToJson();

            if (ImGui.menuItem("Toggle Snap to Window")) {
                ImGuiRenderer.shouldSnap = !ImGuiRenderer.shouldSnap;
            }

            if (ImGui.menuItem("Create saves folder")) {
                createSavesDirIfNonExistent();
            }

            if (ImGui.menuItem("Convert to new syntax")) {
                CustomizableSignData style = signJson;
                updateToNewVersion(style);
            }

            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }

    public static void calcFactor() {
        currentElementFactor = signRatio.y / signHeightBlocks;
    }

    private static void pasteElement() {
        BaseElement elementToPaste = Clipboard.getCopiedElement();

        if (elementToPaste == null) return; // Can't paste if empty

        elementToPaste.onPaste();
        elementOrder.addFirst(elementToPaste);
        updateToJson();
    }

    private static void copySign() {
        if (getSignJson().json.isEmpty()) return; // Can't copy if empty

        Clipboard.setCopiedSign(getSignJson());
    }

    private static void pasteSign() {
        if (Clipboard.getCopiedSign() == null || Clipboard.getCopiedSign().json.isEmpty()) return; // Can't paste if empty

        setSignJson(Clipboard.getCopiedSign());
        updateFromJson();
        updateToJson();
    }

    public static void addUndo() {
        Clipboard.pushUndoStack(getSignJson());
        updateToJson();
    }

    private static void undo() {
        if (Clipboard.undoEmpty()) return; // Can't undo if empty

        Clipboard.pushRedoStack(getSignJson());
        setSignJson(Clipboard.popUndoStack());
        updateFromJson();
    }

    private static void redo() {
        if (Clipboard.redoEmpty()) return; // Can't redo if empty

        Clipboard.pushUndoStack(getSignJson());
        setSignJson(Clipboard.popRedoStack());
        updateFromJson();
    }

    private static void handleHotKeys() {
        boolean ctrl = ImGui.isKeyDown(ImGuiKey.LeftCtrl) || ImGui.isKeyDown(ImGuiKey.RightCtrl);
        boolean shift = ImGui.isKeyDown(ImGuiKey.LeftShift) || ImGui.isKeyDown(ImGuiKey.RightShift);

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.I)) SignPreview.zoomIn(); // Zoom In
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.O)) SignPreview.zoomOut(); // Zoom Out
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.S)) SignOperation.Json.write(masterBlockPos, getSignJson(), elementOrder); // Save

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.W)) { // Save and Quit
            SignOperation.Json.write(masterBlockPos, getSignJson(), elementOrder);
            quit();
        }

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.Q)) quit(); // Quit

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.G)) BackgroundSelectorPopup.open(); // Background Selector Open
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.F)) JsonPreviewPopup.shouldOpen = true; // Open Json Preview

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.E)) ElementsWindow.toggle(); // Element Window Toggle
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.P)) ElementPropertyWindow.toggle(); // Element Properties Toggle

        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.A)) ElementAddWindow.open(); // Add Element Open
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.T)) TextElementClient.createNew(elementOrder); // Add Text Element

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.U)) undo(); // Undo
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.U)) redo(); // Redo

        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.V)) pasteElement(); // Paste Element
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.H)) pasteSign(); // Paste Sign
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.C)) copySign(); // Copy Sign
    }

    private static void clearCanvas() {
        ConfirmationPopup.show("Are you sure you want to clear the canvas?", "All of your current elements will be removed! This action cannot be undone!", (confirmed) -> {
            if (confirmed) elementOrder.clear();
        });
    }

    private static void exportSign() {
        createSavesDirIfNonExistent();

        FileDialogPopup.setData(getPrettyJson(signJson.jsonString));

        FileDialogPopup.open(
                Saves.getSignSaveDir(),
                FileDialogPopup.FileDialogType.SAVE,
                (path) -> MyWorldTrafficAddition.LOGGER.info("Saved file successfully! Path: {}", path.toString()),
                "MWTACSIGN", "JSON"
        );
    }

    private static void importSign() {
        createSavesDirIfNonExistent();

        FileDialogPopup.open(Saves.getSignSaveDir(), FileDialogPopup.FileDialogType.OPEN, (path) -> {
            if (path == null || path.toString().isBlank()) return;

            CustomizableSignData style = new CustomizableSignData();
            style.setJson(FileDialogPopup.getData());
            setSignJson(style);
            updateFromJson();
            updateToJson();

            elementOrder.forEach(element -> element.setFactor(currentElementFactor)); // For proportions

            MyWorldTrafficAddition.LOGGER.info("Opened file successfully! Path: {}", path.toString());
        }, "MWTACSIGN", "JSON");
    }

    private static void importElement() {
        createSavesDirIfNonExistent();

        FileDialogPopup.open(Saves.getElementSaveDir(), FileDialogPopup.FileDialogType.OPEN, (path) -> {
            JsonObject elementObj = JsonParser.parseString(FileDialogPopup.getData()).getAsJsonObject();
            BaseElement element = BaseElement.fromJson(elementObj);

            if (element == null) {
                MyWorldTrafficAddition.LOGGER.error("Importing element failed! Path: {}", path.toString());
            }

            element.onImport();
            element.setFactor(currentElementFactor);

            elementOrder.addFirst(element);
            updateToJson();

            MyWorldTrafficAddition.LOGGER.info("Opened file successfully! Path: {}", path.toString());
        });

    }
}