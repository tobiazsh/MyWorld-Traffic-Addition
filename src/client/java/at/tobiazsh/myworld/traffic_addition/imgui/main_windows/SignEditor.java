package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 12:36
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.CustomizableSignElementFactory;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.debug.DebugFunctions;
import at.tobiazsh.myworld.traffic_addition.error.ErrorReporter;
import at.tobiazsh.myworld.traffic_addition.gui.NativeFileDialogs;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.ElementAddWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.ElementsWindow;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.*;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.SignPreview;
import at.tobiazsh.myworld.traffic_addition.imgui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.online_image_gallery.OnlineImageGallery;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer.ElementPropertyViewer;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.SignClipboard;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.sign.elements.BaseElementInterface;
import at.tobiazsh.myworld.traffic_addition.error.Error;
import at.tobiazsh.myworld.traffic_addition.filesystem.SavesDirectory;
import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;

import at.tobiazsh.myworld.traffic_addition.utils.JsonUtil;
import com.google.gson.*;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.filesystem.SavesDirectory.createSavesDir;
import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class SignEditor {

    private CustomizableSignBlockEntity blockEntity;

    private int signWidthBlocks;
    private int signHeightBlocks;

    private boolean isVisible = false;
    private boolean isDebug = false;

    private ClientElementInterface selectedElement = null; // Maybe not necessary ??? There HAS to be another way!
    private ImVec2 signRatio;

    private BackgroundSelectorPopup backgroundSelector; // Maybe does not need to be here? Otherwise initialized when button is clicked
    private JsonInjector jsonInjector;
    private JsonPreviewPopup jsonPreviewPopup;
    private SignPreview signPreview = SignPreview.createDefault();
    private ElementPropertyViewer propertyViewer;
    private ElementAddWindow elementAddWindow;

    private final ErrorReporter errorReporter;

    private ClientElementManager clientElementManager = null;

    private final String id;

    public SignEditor(String id, ErrorReporter errorReporter) {
        this.id = id;
        this.errorReporter = errorReporter;
    }

    private void quit() {
        ImGui.closeCurrentPopup();
        isVisible = false;
        this.clientElementManager.clearAll();
    }

    public void render() {
        if (!isVisible) return;

        renderMain();
        ElementsWindow.render();

        if (elementAddWindow != null)
            elementAddWindow.render();

        if (propertyViewer != null)
            propertyViewer.render();

        ConfirmationPopup.render();
        OnlineImageGallery.render();

        if (backgroundSelector != null)
            backgroundSelector.render();
    }

    /**
     * Initializes the sign editor and sets all the necessary parameters based on the provided master block entity.
     * @param masterBlockEntity The master block entity representing the customizable sign to be edited.
     */
    public void initialize(CustomizableSignBlockEntity masterBlockEntity) {
        if (!masterBlockEntity.isInitialized()) {
            // TODO: Switch to Minecraft's built-in translations
            errorReporter.reportError(new Error(
                    tr("ImGui.Main.SignEditor.Error", "Sign not initialized!"),
                    tr("ImGui.Main.SignEditor.Error", "The sign has not been initialized yet! This is crucial, so please do not proceed without initializing the sign first!")
            ), this::quit);
        }

        this.isVisible = true;
        this.blockEntity = masterBlockEntity;

        // selectedElement = null; -> This should not be here. It needs to be exported.

        this.signHeightBlocks = masterBlockEntity.getHeight();
        this.signWidthBlocks = masterBlockEntity.getWidth();

        // backgroundTexturePath = null; -> This should not be here. Use customizable sign texture data instead.

        this.clientElementManager = new ClientElementManager(); // Drop the old manager and create a new one
        this.clientElementManager.importFromSign(masterBlockEntity); // Import the elements from the sign block entity
        this.clientElementManager.setPixelOfOneBlock(signRatio.y / signHeightBlocks);

        // TODO: Rewrite buggy undo/redo system
        // Then clear stack here

        this.signRatio = calculateRatio(
                this.signPreview.previewMaxWidth,
                this.signPreview.previewMaxHeight,
                signWidthBlocks,
                signHeightBlocks
        );

        this.jsonPreviewPopup = new JsonPreviewPopup("jsonPreview_" + id);

        this.elementAddWindow = new ElementAddWindow(
                "elementAddWindow_" + id,
                this.clientElementManager::addElementFirst
        );

        this.backgroundSelector = new BackgroundSelectorPopup(
                this.clientElementManager.textureData,
                "bg_" + id
        );

        this.propertyViewer = new ElementPropertyViewer("propertyViewer_" + id);

        this.jsonInjector = new JsonInjector(
                "jsonInjector_" + id,
                masterBlockEntity,
                errorReporter
        );
    }

    public void open() {
        this.isVisible = true;
    }

    public void renderMain(){
        ImGui.begin(tr("ImGui.Main.SignEditor", "Sign Editor"), ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoNavInputs);

        renderMenuBar();
        renderDebug();
        handleHotKeys();

        this.jsonPreviewPopup.render();

        ImGui.setCursorPos(0, 0); // Reset cursor position to the top-left corner

        // Position for the preview (in the middle)
        float zoom = this.signPreview.getZoom();
        float previewX = (ImGui.getWindowWidth() - signRatio.x * zoom) * 0.5f; // signRatio.x * getZoom() because the size of the sign changes with zoom
        float previewY = (ImGui.getWindowHeight() + ImGui.getFontSize() - signRatio.y * zoom) * 0.5f; // I just tried until it worked lmao

        // Set the cursor position once, to the top-left of the entire centered grid
        ImGui.setCursorPos(previewX, previewY);

        this.signPreview.render(
                signRatio.x,
                signRatio.y,
                this.clientElementManager.getPixelsPerBlock(),
                new ImVec2(previewX, previewY),
                this.clientElementManager.getElements(),
                this.clientElementManager.textureData.getBackground(),
                this.clientElementManager.getBorders()
        );

        // Status bar showing dimensions, zoomed dimensions, pixel/block ratio and zoom percentage
        ImGui.setCursorPosY(ImGui.getWindowHeight() - ImGui.getFontSize() - ImGui.getStyle().getWindowPaddingY()); // Position the status bar at the bottom of the window
        renderStatusBar();

        ImGui.end();
    }

    private void renderStatusBar() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.141f, 0.141f, 0.141f, 1.0f)); // Opaque gray background
        ImGui.beginChild("##Statusbar", new ImVec2(ImGui.getWindowSizeX(), ImGui.getFontSize()), false);

        // Pixels display (left-bound)
        float zoom = this.signPreview.getZoom();
        String pixelString = Math.round(signRatio.x * 100) * 0.01 + " x " + Math.round(signRatio.y * 100) * 0.01 +                       // e.g. 800 x 600
                " (" + Math.round(signRatio.x * zoom * 100) * 0.01 + " x " + Math.round(signRatio.y * zoom * 100) * 0.01 + ") "          // e.g. (1600 x 1200) (zoomed)
                + tr("Global", "At").toLowerCase() + " " + this.clientElementManager.getPixelsPerBlock() + " px/block";   // e.g. at 20 px/block

        // Example: 800 x 600 (1600 x 1200) at 20 px/block
        ImGui.text(pixelString);

        // Zoom Display (right-bound)
        String zoomString = Math.round(zoom * 100) + "%%";
        ImGui.sameLine(ImGui.getContentRegionAvailX() - ImGui.calcTextSize(zoomString).x);
        ImGui.text(zoomString);

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void renderMenuBar() {
        ImGui.beginMenuBar();
        if (ImGui.beginMenu(tr("Global", "File"))) {
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Save to Sign"), "CTRL + S"))
                this.clientElementManager.exportToSign(blockEntity.getBlockPos());

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Save to Sign and Quit"), "CTRL + W")) {
                this.clientElementManager.exportToSign(blockEntity.getBlockPos());
                quit();
            }

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Show Json"), "CTRL + F"))
                this.jsonPreviewPopup.open(this.clientElementManager.textureData);

            if (ImGui.menuItem(tr("Global", "Quit"), "CTRL + Q")) quit();

            ImGui.separator();

            if (ImGui.menuItem(tr("Global", "Import") + "...")) importSign();
            if (ImGui.menuItem(tr("Global", "Export") + "...")) exportSign();

            ImGui.separator();

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Toggle Debug Menu"))) isDebug = !isDebug;

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(tr("Global", "Edit"))) {
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Clear Canvas"))) clearCanvas();

            ImGui.separator();

            //ImGui.beginDisabled();
            //if (ImGui.menuItem(tr("Global", "Undo"), "CTRL + U")) undo();
            //if (ImGui.menuItem(tr("Global", "Redo"), "CTRL + Shift + U")) redo();
            //ImGui.endDisabled();
            ImGui.text("Undo/Redo is currently not available.");

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(tr("Global", "Background"))) {
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Choose Background") + "...", "CTRL + G"))
                backgroundSelector.open();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(tr("Global", "View"))) {
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Toggle Element Window"), "CTRL + E"))
                ElementsWindow.toggle();

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Toggle Element Properties Window")))
                this.propertyViewer.toggleVisibility();

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Toggle Element and Properties Window"))) { // Useful since normally you'd want to have both windows open
                ElementsWindow.toggle();
                this.propertyViewer.toggleVisibility();
            }

            if (ImGui.menuItem(tr("Global", "Zoom In"), "CTRL + I")) this.signPreview.zoomIn();
            if (ImGui.menuItem(tr("Global", "Zoom Out"), "CTRL + O")) this.signPreview.zoomOut();

            ImGui.endMenu();
        }

        if(ImGui.beginMenu(tr("Global", "Elements"))) {
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Add Image Element") + "...", "CTRL + SHIFT + A"))
                this.elementAddWindow.open();

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Add Text Element") + "...", "CTRL + SHIFT + T"))
                this.clientElementManager.addElementFirst(TextElementClient.createNew());

            if (ImGui.menuItem(tr("ImGui.Child.PopUps.OnlineImageGallery", "Online Image Gallery") + "..."))
                OnlineImageGallery.open();

            ImGui.separator();

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Import Element") + "...")) importElement();

            ImGui.endMenu();
        }

        if (ImGui.beginMenu(tr("Global", "Clipboard"))) {
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Copy Sign"), "CTRL + C")) copySign();
            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Paste Sign"), "CTRL + ALT + V")) pasteSign();

            ImGui.separator();

            if (ImGui.menuItem(tr("ImGui.Main.SignEditor", "Paste Element"), "CTRL + SHIFT + V")) pasteElement();

            ImGui.endMenu();
        }

        if (isDebug) if (ImGui.beginMenu("Debug")) {

            if (ImGui.menuItem("Toggle Snap to Window")) {
                ImGuiRenderer.shouldSnap = !ImGuiRenderer.shouldSnap;
            }

            if (ImGui.menuItem("Create saves folder")) {
                createSavesDir();
            }

            if (ImGui.menuItem("Test Error Popup")) {
                this.errorReporter.reportError(
                        new Error(
                                "Test Error",
                                "This is a test error message."
                        ),
                        () -> MyWorldTrafficAddition.LOGGER.info("Error popup closed.")
                );
            }

            if (ImGui.menuItem("Test TFD Popup O")) DebugFunctions.testNfd_open();
            if (ImGui.menuItem("Test TFD Popup S")) DebugFunctions.testNfd_save();

            if (ImGui.menuItem("Test Automatic Background Parsing")) DebugFunctions.testAutoBackgroundLoad();
            if (ImGui.menuItem("Test New Data Parsing")) DebugFunctions.testNewDataParse();

            if (ImGui.menuItem("Inject JSON")) jsonInjector.open();

            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }

    private void renderDebug() {
        if (!isDebug) return;
        jsonInjector.render();
    }

    private void pasteElement() {
        ClientElementInterface elementToPaste = SignClipboard.getInstance().getCopiedElement();

        if (elementToPaste == null) return; // Can't paste if empty or no ID

        elementToPaste.onPaste();
        this.clientElementManager.addElementFirst(elementToPaste);
    }

    private void copySign() {
        if (this.clientElementManager.textureData.toJson().isEmpty()) return; // Can't copy if empty

        SignClipboard.getInstance().setCopiedSign(this.clientElementManager.textureData);
    }

    private void pasteSign() {
        if (SignClipboard.getInstance().getCopiedSign() == null || SignClipboard.getInstance().getCopiedSign().toJson().isEmpty()) return; // Can't paste if empty

        this.clientElementManager.setData(SignClipboard.getInstance().getCopiedSign(), blockEntity);
    }

//    public static void addUndo() {
//        SignClipboard.getInstance().pushUndoStack(ClientElementManager.getInstance().textureData);
//    }

//    private static void undo() {
//        if (SignClipboard.getInstance().undoEmpty()) return; // Can't undo if empty
//
//        SignClipboard.getInstance().pushRedoStack(ClientElementManager.getInstance().textureData);
//        ClientElementManager.getInstance().setData(SignClipboard.getInstance().popUndoStack(), blockEntity);
//    }
//
//    private static void redo() {
//        if (SignClipboard.getInstance().redoEmpty()) return; // Can't redo if empty
//
//        SignClipboard.getInstance().pushUndoStack(ClientElementManager.getInstance().textureData);
//        ClientElementManager.getInstance().setData(SignClipboard.getInstance().popRedoStack(), blockEntity);
//    }

    private void handleHotKeys() {
        boolean ctrl = ImGui.isKeyDown(ImGuiKey.LeftCtrl) || ImGui.isKeyDown(ImGuiKey.RightCtrl);
        boolean shift = ImGui.isKeyDown(ImGuiKey.LeftShift) || ImGui.isKeyDown(ImGuiKey.RightShift);

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.I)) this.signPreview.zoomIn();  // Zoom In
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.O)) this.signPreview.zoomOut(); // Zoom Out
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.S)) this.clientElementManager.exportToSign(blockEntity.getBlockPos()); // Save

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.W)) { // Save and Quit
            this.clientElementManager.exportToSign(blockEntity.getBlockPos());
            quit();
        }

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.Q)) quit(); // Quit

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.G)) backgroundSelector.open();

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.E)) ElementsWindow.toggle(); // Element Window Toggle

        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.A)) this.elementAddWindow.open(); // Add Element Open

//        if (ctrl && ImGui.isKeyPressed(ImGuiKey.U)) undo(); // Undo
//        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.U)) redo(); // Redo

        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.V)) pasteElement(); // Paste Element
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.H)) pasteSign(); // Paste Sign
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.C)) copySign(); // Copy Sign
    }

    private void clearCanvas() {
        ConfirmationPopup.show(tr("ImGui.Main.SignEditor", "Are you sure you want to clear the canvas?"), tr("ImGui.Global.Warn", "This action cannot be undone!"), (confirmed) -> {
            if (confirmed)
                this.clientElementManager = new ClientElementManager(); // Drop the old manager and create a new one
        });
    }

    private void exportSign() {
        createSavesDir();

        String data = JsonUtil.toPrettyJson(this.clientElementManager.textureData.toJson().toString());

        try {
            NativeFileDialogs.writeFileWithDialog(
                    "Export Customizable Sign...",
                    new NativeFileDialogs.FilterItem(
                            "MyWorld Traffic Addition Customizable Sign Data",
                            new String[]{"*.MWTACSIGN", "*.mwtacsign", "*.JSON", "*.json"}
                    ),
                    SavesDirectory.getSignSaveDir(),
                    "New Customizable Sign",
                    data.getBytes(StandardCharsets.UTF_8),
                    (_) -> {}
            );
        } catch (IOException e) {
            this.errorReporter.reportError(
                    new Error(
                            tr("ImGui.Main.Export", "Export failed!"),
                            "An error occurred while exporting the sign data. Please check logs."
                    ),
                    () -> {}
            );

            MyWorldTrafficAddition.LOGGER.error("Error while exporting sign data!", e);
        }
    }

    private void importSign() {
        createSavesDir();

        try {
            byte[] readFile = NativeFileDialogs.readFileWithDialog(
                    "Import Sign...",
                    new NativeFileDialogs.FilterItem(
                           "MyWorld Traffic Addition Customizable Signs",
                            new String[]{"*.MWTACSIGN", "*.mwtacsign", "*.JSON", "*. json"}
                    ),
                    SavesDirectory.getSignSaveDir(),
                    (_) -> {}
            );

            if (readFile.length == 0) // Abort
                return;

            String readData = new String(readFile, StandardCharsets.UTF_8);

            var parsedTexture = CustomizableSignTextureData.fromJson(
                    (JsonObject) JsonParser.parseString(readData)
            );

            this.clientElementManager.setData(parsedTexture, blockEntity);
        } catch (IOException e) {
            errorReporter.reportError(
                    new Error(
                            tr("ImGui.Main.Import", "Import failed!"),
                            "An error occurred while reading the file. Please check logs"
                    ),
                    () -> {}
            );

            MyWorldTrafficAddition.LOGGER.error("Failed to import sign!", e);
        } catch (IllegalArgumentException e) {
            errorReporter.reportError(
                    new Error(
                            tr("ImGui.Main.Import", "Import failed!"),
                            tr("ImGui.Main.Import", "The file you provided does not appear to have valid sign data!")
                    ),
                    () -> {}
            );
        }
    }

    private void importElement() {
        try {
            byte[] readFile = NativeFileDialogs.readFileWithDialog(
                    "Import Element...",
                    new NativeFileDialogs.FilterItem(
                            "MyWorld Traffic Addition Customizable Sign Elements",
                            new String[]{"*.MWTACSELEMENT", "*.mwtacselement", "*.JSON", "*. json"}
                    ),
                    SavesDirectory.getElementSaveDir(),
                    (_) -> {}
            );

            if (readFile.length == 0) // Abort
                return;

            String readData = new String(readFile, StandardCharsets.UTF_8);
            JsonObject elementObj = JsonParser.parseString(readData).getAsJsonObject();
            var element = CustomizableSignElementFactory.toClientElement(
                    Objects.requireNonNull(BaseElementInterface.fromJson(elementObj))
            );

            if (element == null)
                throw new IllegalStateException("Customizable Sign Element does not appear to be valid!");

            element.onImport();
            this.clientElementManager.addElementFirst(element);
        } catch (IOException e) {
            errorReporter.reportError(
                    new Error(
                            tr("ImGui.Main.Import", "Import failed!"),
                            "An error occurred while reading the file. Please check logs"
                    ),
                    () -> {}
            );

            MyWorldTrafficAddition.LOGGER.error("Failed to import sign!", e);
        } catch (IllegalArgumentException e) {
            errorReporter.reportError(
                    new Error(
                            tr("ImGui.Main.Import", "Import failed!"),
                            tr("ImGui.Main.Import", "The file you provided does not appear to have valid element data!")
                    ),
                    () -> {}
            );
        }
    }

    /**
     * Calculates the ratio of the sign's width and height to fit within the maximum width and height while maintaining
     * the aspect ratio.
     * @param maxWidth The maximum width available for the sign preview.
     * @param maxHeight The maximum height available for the sign preview.
     * @param width The actual width of the sign in blocks.
     * @param height The actual height of the sign in blocks.
     * @return An {@link ImVec2} containing the scaled width and height that fit within the specified maximum dimensions.
     */
    @SuppressWarnings("SameParameterValue")
    private ImVec2 calculateRatio(
            float maxWidth,
            float maxHeight,
            float width,
            float height
    ) {
        if (width == 0 || height == 0) return new ImVec2(1, 1); // Avoid division by zero

        float scale = Math.min(maxWidth / width, maxHeight / height);

        return new ImVec2(width * scale, height * scale);
    }
}