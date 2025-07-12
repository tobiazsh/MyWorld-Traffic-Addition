package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 12:36
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementFactory;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.ElementAddWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.ElementPropertyWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.ElementsWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.*;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.SignPreview;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.Clipboard;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignData;
import at.tobiazsh.myworld.traffic_addition.utils.elements.*;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem.Folder;
import at.tobiazsh.myworld.traffic_addition.utils.Saves;
import at.tobiazsh.myworld.traffic_addition.components.block_entities.CustomizableSignBlockEntity;
import com.google.gson.*;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl.Roboto;
import static at.tobiazsh.myworld.traffic_addition.imgui.ImGuiImpl.clearFontAtlas;
import static at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignData.getPrettyJson;
import static at.tobiazsh.myworld.traffic_addition.utils.CustomizableSignData.updateToNewVersion;
import static at.tobiazsh.myworld.traffic_addition.utils.Saves.createSavesDir;

public class SignEditor {

    private static BlockPos masterBlockPos = null;
    
    private static CustomizableSignBlockEntity blockEntity;

    private static int signWidthBlocks;
    private static int signHeightBlocks;

    public static String backgroundTexturePath;
    public static ClientElementInterface selectedElement = null;
    private static Folder allBackgrounds = null; // All Countries in ImGui/SignRes/Backgrounds/
    public static ImVec2 signRatio; // Initialized when screen is opened;
    public static boolean showDebug = false;

    private static void quit() {
        ImGui.closeCurrentPopup();
        ImGuiRenderer.showSignEditor = false;
        clearFontAtlas();
        ClientElementManager.getInstance().clearAll();
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
        BackgroundSelectorPopup.render(allBackgrounds, blockEntity);
        ConfirmationPopup.render();
        FileDialogPopup.render();
        OnlineImageDialog.render();
    }

    public static void open(BlockPos masterBlockPos, @NotNull World world, boolean isInit) {

        if (!isInit) {
            ErrorPopup.open(Text.translatable("mwta.imgui.errors.sign_not_initialized").getString(), Text.translatable("mwta.imgui.errors.sign_not_initialized.description").getString(), SignEditor::quit);
        }

        ClientElementManager.getInstance().clearAll();

        SignEditor.masterBlockPos = masterBlockPos;

        disposeChildWindows();

        selectedElement = null;

        if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity csbe) blockEntity = csbe;
        else {
            MyWorldTrafficAddition.LOGGER.error("Error (Rendering Sign Editor): Couldn't determine Sign (CustomizableSignBlockEntity not found)");
        }

        getSignSize();

        // List all available backgrounds
        try {
            allBackgrounds = FileSystem.listFoldersRecursive("/assets/%s/textures/imgui/sign_res/backgrounds/".formatted(MyWorldTrafficAddition.MOD_ID), true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        signRatio = createRatio(SignPreview.previewMaxWidth, SignPreview.previewMaxHeight, signWidthBlocks, signHeightBlocks);

        backgroundTexturePath = "";

        Clipboard.getInstance().clearUndoStack();
        Clipboard.getInstance().clearRedoStack();

        // Read from sign block entity
        ClientElementManager.getInstance().importFromSign(blockEntity);

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
        ImGui.pushFont(Roboto);
        ImGui.begin(Text.translatable("mwta.imgui.window_titles.sign_editor").getString(), ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoNavInputs);

        renderMenuBar();
        handleHotKeys();

        JsonPreviewPopup.create();
        if (JsonPreviewPopup.shouldOpen) JsonPreviewPopup.open(ClientElementManager.getInstance().rawData);

        // Status bar showing dimensions, zoomed dimensions, pixel/block ratio and zoom percentage
        ImGui.setCursorPosY(ImGui.getWindowHeight() - ImGui.getFontSize() - ImGui.getStyle().getWindowPaddingY()); // Position the status bar at the bottom of the window
        renderStatusBar();

        ImGui.setCursorPos(0, 0); // Reset cursor position to the top-left corner

        // Position for the preview (in the middle)
        float previewX = (ImGui.getWindowWidth() - signRatio.x * SignPreview.getZoom()) * 0.5f; // signRatio.x * getZoom() because the size of the sign changes with zoom
        float previewY = (ImGui.getWindowHeight() + ImGui.getFontSize() - signRatio.y * SignPreview.getZoom()) * 0.5f; // I just tried until it worked lmao

        // Set the cursor position once, to the top-left of the entire centered grid
        ImGui.setCursorPos(previewX, previewY);

        SignPreview.render(
                signRatio.x,
                signRatio.y,
                signWidthBlocks,
                signHeightBlocks,
                ClientElementManager.getInstance().getPixelOfOneBlock(),
                new ImVec2(previewX, previewY),
                ClientElementManager.getInstance().getElements(),
                ClientElementManager.getInstance().backgroundTextures
        );

        ImGui.end();
        ImGui.popFont();
    }

    private static void renderStatusBar() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.141f, 0.141f, 0.141f, 1.0f)); // Opaque gray background
        ImGui.beginChild("##Statusbar", new ImVec2(ImGui.getWindowSizeX(), ImGui.getFontSize()), false);

        // Pixels display (left-bound)
        String pixelString = Math.round(signRatio.x * 100) * 0.01 + " x " + Math.round(signRatio.y * 100) * 0.01 +
                " (" + Math.round(signRatio.x * SignPreview.getZoom() * 100) * 0.01 + " x " + Math.round(signRatio.y * SignPreview.getZoom() * 100) * 0.01 + ") " + Text.translatable("mwta.imgui.sign.editor.at").getString() + " "
                + ClientElementManager.getInstance().getPixelOfOneBlock() + " px/block";

        ImGui.text(pixelString);

        // Zoom Display (right-bound)
        String zoomString = Math.round(SignPreview.getZoom() * 100) + "%%";
        ImGui.sameLine(ImGui.getContentRegionAvailX() - ImGui.calcTextSize(zoomString).x);
        ImGui.text(zoomString);

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private static void renderMenuBar() {
        ImGui.beginMenuBar();
        if (ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.menu.file").getString())) {
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.save_to_sign").getString(), "CTRL + S"))
                ClientElementManager.getInstance().exportToSign(masterBlockPos);

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.save_to_sign_quit").getString(), "CTRL + W")) {
                ClientElementManager.getInstance().exportToSign(masterBlockPos);
                quit();
            }

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.show_json").getString(), "CTRL + F")) JsonPreviewPopup.shouldOpen = true;

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.quit").getString(), "CTRL + Q")) quit();

            ImGui.separator();

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.import").getString())) importSign();
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.export").getString())) exportSign();

            ImGui.separator();

            if(ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.file.menuitem.toggle_debug").getString())) showDebug = !showDebug;

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.menu.edit").getString())) {
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.edit.menuitem.clear_canvas").getString())) clearCanvas();

            ImGui.separator();

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.edit.menuitem.undo").getString(), "CTRL + U")) undo();
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.edit.menuitem.redo").getString(), "CTRL + Shift + U")) redo();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.background").getString())) {
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.edit.menuitem.choose_background").getString(), "CTRL + G")) BackgroundSelectorPopup.open();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.menu.view").getString())) {
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.view.menuitem.toggle_element_window").getString(), "CTRL + E")) ElementsWindow.toggle();
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.view.menuitem.toggle_element_properties_window").getString(), "CTRL + P")) ElementPropertyWindow.toggle();

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.view.menuitem.toggle_properties_and_element_windows").getString())) { // Useful since normally you'd want to have both windows open
                ElementsWindow.toggle();
                ElementPropertyWindow.toggle();
            }

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.view.menuitem.zoom_in").getString(), "CTRL + I")) SignPreview.zoomIn();
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.view.menuitem.zoom_out").getString(), "CTRL + O")) SignPreview.zoomOut();

            ImGui.endMenu();
        }

        if(ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.menu.elements").getString())) {
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.elements.menuitem.add_element").getString(), "CTRL + SHIFT + A")) ElementAddWindow.open();
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.elements.menuitem.add_text_element").getString(), "CTRL + SHIFT + T")) ClientElementManager.getInstance().addElementFirst(TextElementClient.createNew());
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.elements.menuitem.upload_online_image_element").getString())) openOnlineImageDialog(); // TODO: Note to myself: FINALLY FINISH THIS MOTHERFUCKER!!

            ImGui.separator();

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.elements.menuitem.import_element").getString())) importElement();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(Text.translatable("mwta.imgui.sign.editor.menu.clipboard").getString())) {
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.clipboard.menuitem.copy_sign").getString(), "CTRL + C")) copySign();
            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.clipboard.menuitem.paste_sign").getString(), "CTRL + ALT + V")) pasteSign();

            ImGui.separator();

            if (ImGui.menuItem(Text.translatable("mwta.imgui.sign.editor.menu.clipboard.menuitem.paste_element").getString(), "CTRL + SHIFT + V")) pasteElement();

            ImGui.endMenu();
        }

        if (showDebug) if (ImGui.beginMenu("Debug")) {

            if (ImGui.menuItem("Update Json")) ClientElementManager.getInstance().updateRawData();

            if (ImGui.menuItem("Toggle Snap to Window")) {
                ImGuiRenderer.shouldSnap = !ImGuiRenderer.shouldSnap;
            }

            if (ImGui.menuItem("Create saves folder")) {
                createSavesDir();
            }

            if (ImGui.menuItem("Convert to new syntax")) {
                CustomizableSignData style = ClientElementManager.getInstance().rawData;
                updateToNewVersion(style);
            }

            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }

    public static void calcFactor() {
         ClientElementManager.getInstance().setPixelOfOneBlock(signRatio.y / signHeightBlocks);
    }

    private static void pasteElement() {
        ClientElementInterface elementToPaste = Clipboard.getInstance().getCopiedElement();

        if (elementToPaste == null) return; // Can't paste if empty or no ID

        elementToPaste.onPaste();
        ClientElementManager.getInstance().addElementFirst(elementToPaste);
    }

    private static void copySign() {
        ClientElementManager.getInstance().updateRawData();
        if (ClientElementManager.getInstance().rawData.json.isEmpty()) return; // Can't copy if empty

        Clipboard.getInstance().setCopiedSign(ClientElementManager.getInstance().rawData);
    }

    private static void pasteSign() {
        if (Clipboard.getInstance().getCopiedSign() == null || Clipboard.getInstance().getCopiedSign().json.isEmpty()) return; // Can't paste if empty

        ClientElementManager.getInstance().setData(Clipboard.getInstance().getCopiedSign(), blockEntity);
    }

    public static void addUndo() {
        Clipboard.getInstance().pushUndoStack(ClientElementManager.getInstance().rawData);
    }

    private static void undo() {
        if (Clipboard.getInstance().undoEmpty()) return; // Can't undo if empty

        Clipboard.getInstance().pushRedoStack(ClientElementManager.getInstance().rawData);
        ClientElementManager.getInstance().setData(Clipboard.getInstance().popUndoStack(), blockEntity);
    }

    private static void redo() {
        if (Clipboard.getInstance().redoEmpty()) return; // Can't redo if empty

        Clipboard.getInstance().pushUndoStack(ClientElementManager.getInstance().rawData);
        ClientElementManager.getInstance().setData(Clipboard.getInstance().popRedoStack(), blockEntity);
    }

    private static void handleHotKeys() {
        boolean ctrl = ImGui.isKeyDown(ImGuiKey.LeftCtrl) || ImGui.isKeyDown(ImGuiKey.RightCtrl);
        boolean shift = ImGui.isKeyDown(ImGuiKey.LeftShift) || ImGui.isKeyDown(ImGuiKey.RightShift);

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.I)) SignPreview.zoomIn(); // Zoom In
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.O)) SignPreview.zoomOut(); // Zoom Out
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.S)) ClientElementManager.getInstance().exportToSign(masterBlockPos); // Save

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.W)) { // Save and Quit
            ClientElementManager.getInstance().exportToSign(masterBlockPos);
            quit();
        }

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.Q)) quit(); // Quit

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.G)) BackgroundSelectorPopup.open(); // Background Selector Open
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.F)) JsonPreviewPopup.shouldOpen = true; // Open Json Preview

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.E)) ElementsWindow.toggle(); // Element Window Toggle
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.P)) ElementPropertyWindow.toggle(); // Element Properties Toggle

        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.A)) ElementAddWindow.open(); // Add Element Open
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.T)) ClientElementManager.getInstance().addElement(TextElementClient.createNew()); // Add Text Element

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.U)) undo(); // Undo
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.U)) redo(); // Redo

        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.V)) pasteElement(); // Paste Element
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.H)) pasteSign(); // Paste Sign
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.C)) copySign(); // Copy Sign
    }

    private static void clearCanvas() {
        ConfirmationPopup.show(Text.translatable("mwta.imgui.warnings.clear_canvas").getString(), Text.translatable("mwta.imgui.warnings.clear_canvas.description").getString(), (confirmed) -> {
            if (confirmed) ClientElementManager.getInstance().clearAll();
        });
    }

    private static void exportSign() {
        createSavesDir();

        // Ensure the data is up to date
        ClientElementManager.getInstance().updateRawData();
        FileDialogPopup.setData(getPrettyJson(ClientElementManager.getInstance().rawData.jsonString));

        FileDialogPopup.open(
                Saves.getSignSaveDir(),
                FileDialogPopup.FileDialogType.SAVE,
                (path) -> MyWorldTrafficAddition.LOGGER.info("Saved file successfully! Path: {}", path.toString()),
                "MWTACSIGN", "JSON"
        );
    }

    private static void importSign() {
        createSavesDir();

        FileDialogPopup.open(Saves.getSignSaveDir(), FileDialogPopup.FileDialogType.OPEN, (path) -> {
            if (path == null || path.toString().isBlank()) return;

            CustomizableSignData style = new CustomizableSignData();
            style.setJson(FileDialogPopup.getData());
            ClientElementManager.getInstance().setData(style, blockEntity);

            MyWorldTrafficAddition.LOGGER.info("Opened file successfully! Path: {}", path);
        }, "MWTACSIGN", "JSON");
    }

    private static void importElement() {
        createSavesDir();

        FileDialogPopup.open(Saves.getElementSaveDir(), FileDialogPopup.FileDialogType.OPEN, (path) -> {
            JsonObject elementObj = JsonParser.parseString(FileDialogPopup.getData()).getAsJsonObject();
            ClientElementInterface element = ClientElementFactory.toClientElement(Objects.requireNonNull(BaseElementInterface.fromJson(elementObj)));

            if (element == null) {
                MyWorldTrafficAddition.LOGGER.error("Importing element failed! Path: {}", path.toString());
                return;
            }

            element.onImport();

            ClientElementManager.getInstance().addElementFirst(element);

            MyWorldTrafficAddition.LOGGER.info("Opened file successfully! Path: {}", path.toString());
        }, "MWTACSELEMENT", "JSON");
    }

    // Used Later for the online image dialog
    private static void openOnlineImageDialog() {
        OnlineImageDialog.startDialog();
    }
}