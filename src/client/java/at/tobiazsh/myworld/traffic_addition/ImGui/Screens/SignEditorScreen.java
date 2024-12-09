package at.tobiazsh.myworld.traffic_addition.ImGui.Screens;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 12:36
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.ImGui.Windows.*;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.*;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Color;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Elements.ImageElementClient;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.Elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.BasicFont;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.TextElement;
import at.tobiazsh.myworld.traffic_addition.Utils.SignStyleJson;
import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SetCustomizableSignTexture;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FileSystem.Folder;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.UpdateTextureVarsCustomizableSignBlockPayload;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.*;
import io.netty.util.internal.StringUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl.*;

public class SignEditorScreen {

    private static final ImGui imgui = new ImGui();

    private static BlockPos masterBlockPos = null;
    private static World world = null;
    private static boolean signIsInit = false;
    private static boolean isIntegra = false;

    private static Texture iconTexture = new Texture();

    private static String currentErrorPopType = "";
    private static String currentErrorPopMsg = "";
    private static String currentErrorPopIcon = "/assets/myworld_traffic_addition/textures/imgui/icons/info.png";

    private static CustomizableSignBlockEntity customizableSignBlockEntity;

    private static int totalSignWidth;
    private static int totalSignHeight;

    private static Color isInitColors; // Colors for boolean value "Is Initialized" field in infoPop

    public static SignStyleJson signJson = new SignStyleJson();
    public static List<String> previewTextures = new ArrayList<>();
    public static List<BaseElement> baseElementDrawOrder = new ArrayList<>();
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

    private static boolean texturesLoaded = false;

    public static void loadMainTextures() {
        if (texturesLoaded) return;
        iconTexture = Textures.smartRegisterTexture("/assets/myworld_traffic_addition/textures/imgui/icons/MWTASE.png");
        texturesLoaded = true;
    }

    private static final int checkButtonHeight = 128;
    private static final int checkButtonWidth = 256;

    private static final int iconHeight = 512 - 128;
    private static final int iconWidth = 512 - 128;

    private static final int INFO_POP_WIDTH = 1024;
    private static final int INFO_POP_HEIGHT = 512;

    public static void showHomepage() {
        String title = "MyWorld Traffic Addition - Sign Editor";
        ImGui.pushFont(DejaVuSans);
        if (ImGui.begin(title, ImGuiWindowFlags.NoNavInputs)) {
            float windowHeight = ImGui.getWindowHeight();
            float windowWidth = ImGui.getWindowWidth();

            renderTitle(windowWidth);
            renderCenteredImage(windowHeight, windowWidth);
            renderCheckDataButton(windowHeight, windowWidth);

            if (ImGui.beginPopupModal("infoPop")) {
                renderInfoPopup();
                ImGui.endPopup();
            }
        }
        ImGui.end();
        ImGui.popFont();
    }

    private static void renderTitle(float windowWidth) {
        String bigTitle = "MyWorld Traffic Addition - Sign Editor";
        ImGui.spacing();
        ImGui.pushFont(ImGuiImpl.DejaVuSansBoldBig);
        ImUtil.centerHorizontal(windowWidth, imgui.calcTextSize(bigTitle).x);
        ImGui.text(bigTitle);
        ImGui.popFont();
    }

    private static void renderCenteredImage(float windowHeight, float windowWidth) {
        ImUtil.center(windowHeight, windowWidth, iconHeight, iconWidth);
        ImGui.image(iconTexture.getTextureId(), iconWidth, iconHeight);
    }

    private static void renderCheckDataButton(float windowHeight, float windowWidth) {
        // Center this button between the bottom of the window and the icon
        ImGui.setCursorPos((windowWidth - checkButtonWidth) / 2, (3 * windowHeight + iconHeight - 2 * checkButtonHeight) / 4);
        if (ImGui.button("Check Data", checkButtonWidth, checkButtonHeight)) {
            ImGui.openPopup("infoPop");
        }
    }

    private static void renderInfoPopup() {
        ImGui.setNextWindowSize(INFO_POP_WIDTH, INFO_POP_HEIGHT);
        if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity) {
            isIntegra = true;
        }
        if (!isIntegra) {
            renderErrorPopup();
        } else {
            renderInfoPopupContent();
        }
    }

    private static void renderErrorPopup() {
        ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
        ImGui.textColored(217, 62, 62, 255, "Error (Sign Loading): Block is not a CustomizableSignBlock!");
        ImGui.popFont();
        if (ImGui.button("Close")) {
            ImGuiRenderer.showSignEditor = false;
        }
    }

    private static void renderInfoPopupContent() {
        if (signIsInit) {
            isInitColors = ImUtil.Colors.green;
        } else {
            isInitColors = ImUtil.Colors.red;
        }

        String popUpTitle = "Please check and confirm this data!";
        ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
        ImUtil.centerHorizontal(INFO_POP_WIDTH, imgui.calcTextSize(popUpTitle).x);
        ImGui.text(popUpTitle);
        ImGui.popFont();
        ImGui.separator();

        ImGui.text("Block Master Position: X(" + masterBlockPos.getX() + "), Y(" + masterBlockPos.getY() + "), Z(" + masterBlockPos.getZ() + ")");
        ImGui.text("Height: " + ((CustomizableSignBlockEntity) world.getBlockEntity(masterBlockPos)).getHeight());
        ImGui.text("Width: " + ((CustomizableSignBlockEntity) world.getBlockEntity(masterBlockPos)).getWidth());
        ImGui.text("Is Initialized: ");
        ImGui.sameLine();
        ImGui.textColored(isInitColors.red, isInitColors.green, isInitColors.blue, isInitColors.alpha, signIsInit ? "True" : "False");
        ImGui.separator();

        if (!signIsInit) {
            ImGui.beginDisabled();
        }

        if (ImGui.button("Confirm")) {
            ImGui.closeCurrentPopup();
            homepageActive = false;
            renderPreview = true;
        }

        if (!signIsInit) {
            ImGui.endDisabled();
        }

        ImGui.sameLine();
        if (ImGui.button("Quit")) {
            quit();
        }
    }

    public static void render() {
        if (homepageActive) showHomepage();
        if (renderPreview) renderMain();
        ElementsWindow.render();
        ElementAddWindow.render();
        ElementPropertyWindow.render();
        StylePopUp.render(countriesBG, customizableSignBlockEntity);
    }

    public static boolean homepageActive = true;
    public static boolean renderPreview = false;
    private static boolean showStylePopup = false;

    public void openSignEditorScreen(BlockPos masterBlockPos, @NotNull World world, boolean isInit) {
        this.masterBlockPos = masterBlockPos;
        this.world = world;
        this.signIsInit = isInit;

        homepageActive = true;
        renderPreview = false;
        disposeChildWindows();

        selectedElement = null;
        signJson = new SignStyleJson();

        if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity csbe) customizableSignBlockEntity = csbe;
        else {
            System.err.println("Error (Rendering Sign Editor): Couldn't determine Sign (CustomizableSignBlockEntity not found)");
        }

        // Initialisation
        initializeSize();
        try {
            countriesBG = FileSystem.FromResource.listFolders("/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ratioedSignSize = createRatio(previewHeight, previewWidth, totalSignWidth, totalSignHeight);
        baseElementDrawOrder = new ArrayList<>();

        previewTextures.clear();
        for (int i = 0; i < totalSignHeight * totalSignWidth; i++) {
            previewTextures.add("/assets/myworld_traffic_addition/textures/imgui/icons/not-found.png");
        }

        readFromSign(this.masterBlockPos);

        ImGuiRenderer.showSignEditor = true;
    }

    private static void initializeSize() {
        totalSignHeight = customizableSignBlockEntity.getHeight();
        totalSignWidth = customizableSignBlockEntity.getWidth();
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


    private static Folder countriesBG = null; // All Countries in ImGui/SignRes/Backgrounds/

    private static Texture currentTexture;

    public static ImVec2 ratioedSignSize; // Initialized when screen is opened;
    private static float previewHeight = 1100;
    private static float previewWidth = 1100;
    private static float factor; // Factor for the size of the canvas
    private static float zoomScale = 1.0f;

    public static void renderMain(){
        ImGui.pushFont(DejaVuSans);
        ImGui.begin("Sign Preview", ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoNavInputs);

        factor = ratioedSignSize.y / totalSignHeight * zoomScale; // Size of each "sign" tile in pixels

        renderMenuBar();
        handleHotKeys();

        // Handle popups

        JsonPreviewPopUp.create();
        if (JsonPreviewPopUp.shouldOpen) JsonPreviewPopUp.open(signJson);

        if (showStylePopup) ImGui.openPopup("Choose Style Type");

        ErrorPopUp();

        // Calculations for sign preview

        float totalSignWidthPixels = ratioedSignSize.x;
        float totalSignHeightPixels = ratioedSignSize.y;

        float cursorAddToHeight = (ImGui.getWindowHeight() - totalSignHeightPixels) / 2;
        float cursorAddToWidth = (ImGui.getWindowWidth() - totalSignWidthPixels) / 2;

        // Set the cursor position once, to the top-left of the entire centered grid
        ImGui.setCursorPos(cursorAddToWidth, cursorAddToHeight);
        previewPos = ImGui.getCursorPos();

        // Render Sign Preview
        SignPreview(totalSignWidthPixels, totalSignHeightPixels, factor);

        ImGui.end();
        ImGui.popFont();
    }

    private static void renderMenuBar() {
        ImGui.beginMenuBar();
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Save to Sign", "CTRL + S")) {
                writeToSign(masterBlockPos, signJson);
            }

            if (ImGui.menuItem("Save to Sign & Quit", "CTRL + W")) {
                writeToSign(masterBlockPos, signJson);
                quit();
            }

            if (ImGui.menuItem("Show JSON", "CTRL + F")) JsonPreviewPopUp.shouldOpen = true;

            if (ImGui.menuItem("Quit", "CTRL + Q")) {
                quit();
            }

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
            if (ImGui.menuItem("Add Text Element", "CTRL + SHIFT + T")) addTextElement();
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
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.S)) writeToSign(masterBlockPos, signJson);

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.W)) {
            writeToSign(masterBlockPos, signJson);
            quit();
        }

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.F)) JsonPreviewPopUp.shouldOpen = true;
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.Q)) quit();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.G)) StylePopUp.open();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.E)) ElementsWindow.toggle();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.P)) ElementPropertyWindow.toggle();
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.A)) ElementAddWindow.open();
        if (ctrl && shift && ImGui.isKeyPressed(ImGuiKey.T)) addTextElement();
    }

    private static void zoomOut(float amount) {
        zoomScale = Math.max(0.1f, zoomScale - amount); // Prevent zooming out too much
    }

    private static void zoomIn(float amount) {
        zoomScale = Math.min(10.0f, zoomScale + amount); // Prevent excessive zoom-in
    }

    private static ImVec2 previewPos = new ImVec2();

    public static void SignPreview(float totalSignWidthPixels, float totalSignHeightPixels, float factor) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);  // Remove spacing between items
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);  // Remove padding inside the frame

        //factor *= zoomScale;

        //float finalFactor = factor;
        baseElementDrawOrder.forEach(texture -> texture.setFactor(factor));

        // Make Child that is as big as the sign in pixels
        ImGui.beginChild("##BottomToTopRenderer", totalSignWidthPixels, totalSignHeightPixels, false);

        // Render from bottom to top and from left to right
        float currentY = ImGui.getCursorPosY() + (totalSignHeight - 1) * factor; // Set to the position of bottom
        int position = 0;
        for (int i = totalSignHeight - 1; i >= 0; i--) {
            ImGui.setCursorPosY(currentY);

            for (int j = 0; j < totalSignWidth; j++) {

                currentTexture = Textures.smartRegisterTexture(previewTextures.get(position));
                ImGui.image(currentTexture.getTextureId(), factor, factor);

                // If the current position is smaller than the sign's height minus one, stay in row
                if (j < totalSignWidth - 1) {
                    ImGui.sameLine();
                }

                position++;
            }

            currentY -= factor; // Decrease by factor to start next line
        }

        ImGui.endChild();

        for (int i = baseElementDrawOrder.size() - 1; i >= 0; i--) {
            BaseElement element = baseElementDrawOrder.get(i); // Get element to render

            // Skip non-render-able elements
            if (!(element instanceof ImageElement || element instanceof TextElement)) continue;

            ImGui.setCursorPos(previewPos.x, previewPos.y);
            ImGui.beginChild("OVERLAY_CANVAS_" + element.getId(), totalSignWidthPixels, totalSignHeightPixels);

            // Scale position and dimensions
            ImGui.setCursorPos(element.getX() / factor, element.getY() / factor);

            // Render depending on the type of element
            if (element instanceof ImageElement) {
                Textures.smartRegisterTexture(((ImageElement) element).getResourcePath()); // Register textures only on client side
                ImageElementClient.fromImageElement((ImageElement) element).renderImGui();
            } else if (element instanceof TextElement) {
                TextElementClient.fromTextElement((TextElement) element).renderImGui();
            }

            ImGui.endChild();
        }

        ImGui.popStyleVar(2);
    }

    private static String defaultFontPath = "/assets/" + MyWorldTrafficAddition.MOD_ID + "/font/dejavu_sans.ttf";
    private static int defaultFontSize = 24;
    private static String defaultText = "Lorem Ipsum";
    private static void addTextElement() {
        baseElementDrawOrder.addFirst(new TextElement(0, 0,0,0, 0, 1, new BasicFont(defaultFontPath, defaultFontSize), defaultText, true));
    }

    public static void ErrorPopUp() {
        if (ImGui.beginPopupModal(currentErrorPopType)) {

            ImGui.pushFont(ImGuiImpl.DejaVuSansBold);

            ImGui.image(Textures.smartRegisterTexture(currentErrorPopIcon).getTextureId(), 20, 20);

            ImGui.sameLine();
            ImGui.spacing();
            ImGui.sameLine();

            ImGui.text(currentErrorPopType);
            ImGui.separator();

            ImGui.text("Message:");

            ImGui.popFont();

            ImGui.textWrapped(currentErrorPopMsg);

            ImGui.separator();

            if (ImGui.button("Close")) {
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private static void openErrorPopup(String cept, String cepm) {
        currentErrorPopType = cept;
        currentErrorPopMsg = cepm;

        ImGui.openPopup(currentErrorPopType);
    }
    // NOT recommended using; Icon should always stay the same.

    private static void changeErrorPopIcon(String resourcePath) {
        SignEditorScreen.currentErrorPopIcon = resourcePath;
    }

    private static void writeToSign(BlockPos pos, SignStyleJson signJson) {
        signJson = signJson.setElements(baseElementDrawOrder, customizableSignBlockEntity);

        if (StringUtil.isNullOrEmpty(signJson.jsonString)) {
            openErrorPopup("Error", "Couldn't write to Sign: Current JSON is Empty! It seems like nothing has been edited!");
            return;
        }

        ClientPlayNetworking.send(new SetCustomizableSignTexture(pos, signJson.jsonString));
        ClientPlayNetworking.send(new UpdateTextureVarsCustomizableSignBlockPayload(pos));
    }

    private static void readFromSign(BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof CustomizableSignBlockEntity)) return; // Return nothing; No BlockEntity found

        String jsonString = ((CustomizableSignBlockEntity) blockEntity).getSignTextureJson();
        if (StringUtil.isNullOrEmpty(jsonString)) return;

        signJson = signJson.convertStringToJson(jsonString);

        if (signJson.json.has("Style")) previewTextures = SignStyleJson.deconstructStyleToArray(signJson);
        if (signJson.json.has("Elements")) baseElementDrawOrder = SignStyleJson.deconstructElementsToArray(signJson);
    }
}