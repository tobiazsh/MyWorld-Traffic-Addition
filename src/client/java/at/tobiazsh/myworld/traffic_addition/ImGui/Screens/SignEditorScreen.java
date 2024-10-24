package at.tobiazsh.myworld.traffic_addition.ImGui.Screens;


/*
 * @created 27/09/2024 (DD/MM/YYYY) - 12:36
 * @project MyWorld Traffic Addition
 * @author Tobias
 */

import at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents.ElementAddWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents.ElementEntry;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiRenderer;
import at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents.ElementPropertyWindow;
import at.tobiazsh.myworld.traffic_addition.ImGui.UIComponents.JsonPreviewPopUp;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utilities.*;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utilities.Color;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.BaseElement;
import at.tobiazsh.myworld.traffic_addition.Utils.Elements.ImageElement;
import at.tobiazsh.myworld.traffic_addition.Utils.SignStyleJson;
import at.tobiazsh.myworld.traffic_addition.Utils.Texture;
import at.tobiazsh.myworld.traffic_addition.Utils.Textures;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.components.CustomPayloads.SetCustomizableSignTexture;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utilities.FileSystem.Folder;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.*;
import io.netty.util.internal.StringUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.Utils.SignStyleJson.deconstructStyleToArray;


public class SignEditorScreen {

    private static final ImGui imgui = new ImGui();

    private static BlockPos masterBlockPos = null;
    private static World world = null;
    private static boolean signIsInit = false;
    private static boolean isIntegra = false;

    private static Texture iconTexture = new Texture();
    private static Texture noTexture = new Texture();

    private static String currentErrorPopType = "";
    private static String currentErrorPopMsg = "";
    private static String currentErrorPopIcon = "/ImGui/Icons/info.png";

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private static CustomizableSignBlockEntity customizableSignBlockEntity;

    private static int totalSignWidth;
    private static int totalSignHeight;

    private static Color isInitColors; // Colors for boolean value "Is Initialized" field in infoPop

    private static Folder availableBGStyles = new Folder(null, null);

    private static SignStyleJson signJson = new SignStyleJson();

    private static List<String> previewTextures = new ArrayList<>();

    public static List<BaseElement> baseElementDrawOrder = new ArrayList<>();

    private static String resourcePathFolderAbsolute = FileSystem.normalizePath(MyWorldTrafficAddition.class.getResource("/myworld_traffic_addition.accesswidener").getPath());

    private static void quit() {
        ImGui.closeCurrentPopup();
        ImGuiRenderer.showSignEditor = false;
    }


    private static boolean texturesLoaded = false;

    public static void loadMainTextures() {
        if (texturesLoaded) return;
        iconTexture = Textures.smartRegisterTexture("/ImGui/Icons/MWTASE.png");
        noTexture = Textures.smartRegisterTexture("/ImGui/Icons/not-found.png");
        texturesLoaded = true;
    }


    private static final int checkButtonHeight = 128;
    private static final int checkButtonWidth = 256;

    private static final int iconHeight = 512 - 128;
    private static final int iconWidth = 512 - 128;

    public static void showHomepage() {
        String title = "MyWorld Traffic Addition - Sign Editor";
        if (ImGui.begin(title, ImGuiWindowFlags.NoNavInputs)) {

            float windowHeight = ImGui.getWindowHeight();
            float windowWidth = ImGui.getWindowWidth();

            String bigTitle = "MyWorld Traffic Addition - Sign Editor";
            ImGui.spacing();
            ImGui.pushFont(ImGuiImpl.DejaVuSansBoldBig);
            ImUtil.centerHorizontal(windowWidth, imgui.calcTextSize(bigTitle).x);
            ImGui.text(bigTitle);
            ImGui.popFont();

            // Center Image
            ImUtil.center(windowHeight, windowWidth, iconHeight, iconWidth);

            float cursorY = ImGui.getCursorPosY() + iconHeight;

            ImGui.image(iconTexture.getTextureId(), iconWidth, iconHeight);

            // Center Button "Check Data"
            ImGui.setCursorPos((windowWidth - checkButtonWidth) / 2, ((windowHeight - cursorY) - checkButtonHeight) / 2 + cursorY - 10);
            if (ImGui.button("Check Data", checkButtonWidth, checkButtonHeight))
                ImGui.openPopup("infoPop"); // Open Check Infos popup with id "infoPop"

            int infoPopWidth = 1024;
            int infoPopHeight = 512;
            ImGui.setNextWindowSize(infoPopWidth, infoPopHeight);
            if (ImGui.beginPopupModal("infoPop")) {

                if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity) isIntegra = true;
                if (!isIntegra) {
                    ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
                    ImGui.textColored(217, 62, 62, 255, "Error (Sign Loading): Block is not a CustomizableSignBlock!");
                    ImGui.popFont();

                    if (ImGui.button("Close")) {
                        ImGuiRenderer.showSignEditor = false;
                    }
                }

                if (signIsInit) isInitColors = ImUtil.Colors.green;
                else isInitColors = ImUtil.Colors.red;

                String popUpTitle = "Please check and confirm this data!";
                ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
                ImUtil.centerHorizontal(infoPopWidth, imgui.calcTextSize(popUpTitle).x);
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

                if (!signIsInit) ImGui.beginDisabled(); // If Sign is not initialized yet, prevent continuation

                if (ImGui.button("Confirm")) {
                    ImGui.closeCurrentPopup();

                    homepageActive = false;
                    renderPreview = true;
                }

                if (!signIsInit) ImGui.endDisabled();

                ImGui.sameLine();

                if (ImGui.button("Quit")) {
                    quit();
                }

                ImGui.endPopup();
            }
        }
        ImGui.end();
    }


    public static void render() {
        if (homepageActive) showHomepage();
        if (renderPreview) renderMain();
        if (showElementsWindow) ElementsWindow();
    }


    public static boolean homepageActive = true;
    public static boolean renderPreview = false;
    public static boolean showElementsWindow = false;
    private static boolean showStylePopup = false;

    public void openSignEditorScreen(BlockPos masterBlockPos, @NotNull World world, boolean isInit) {
        this.masterBlockPos = masterBlockPos;
        this.world = world;
        this.signIsInit = isInit;

        homepageActive = true;
        renderPreview = false;
        showElementsWindow = false;

        selectedElement = null;
        signJson = new SignStyleJson();

        if (world.getBlockEntity(masterBlockPos) instanceof CustomizableSignBlockEntity csbe) customizableSignBlockEntity = csbe;
        else {
            System.err.println("Error (Rendering Sign Editor): Couldn't determine Sign (CustomizableSignBlockEntity not found)");
        }

        // Initialisation
        initializeSize();
        try {
            countriesBG = FileSystem.FromResource.listFolders("/ImGui/SignRes/Backgrounds/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ratioedSignSize = createRatio(previewHeight, previewWidth, totalSignWidth, totalSignHeight);
        baseElementDrawOrder = new ArrayList<>();

        previewTextures.clear();
        for (int i = 0; i < totalSignHeight * totalSignWidth; i++) {
            previewTextures.add("/ImGui/Icons/not-found.png");
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

    private static ImVec2 ratioedSignSize; // Initialized when screen is opened;
    private static float previewHeight = 1100;
    private static float previewWidth = 1100;

    public static void renderMain(){

        ImGui.begin("Sign Preview", ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoNavInputs);

        ImGui.beginMenuBar();
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Save to Sign")) {
                writeToSign(masterBlockPos, signJson);
            }

            if (ImGui.menuItem("Save to Sign & Quit")) {
                writeToSign(masterBlockPos, signJson);
                quit();
            }

            if (ImGui.menuItem("Show JSON")) JsonPreviewPopUp.shouldOpen = true;

            if (ImGui.menuItem("Quit")) {
                quit();
            }

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("Styling")) {
            if (ImGui.menuItem("Choose Style Type")) showStylePopup = true;

            ImGui.endMenu();
        }

        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Toggle Element Window")) showElementsWindow = !showElementsWindow;
            if (ImGui.menuItem("Toggle Element Properties Window")) ElementPropertyWindow.shouldRender = !ElementPropertyWindow.shouldRender;

            ImGui.endMenu();
        }

        if(ImGui.beginMenu("Elements")) {
            if (ImGui.menuItem("Add Element...")) ElementAddWindow.shouldOpen = true;

            ImGui.endMenu();
        }

        ImGui.endMenuBar();

        // Handle popups

        JsonPreviewPopUp.create();
        if (JsonPreviewPopUp.shouldOpen) JsonPreviewPopUp.open(signJson);

        ElementAddWindow.create();
        if (ElementAddWindow.shouldOpen) ElementAddWindow.open();

        if (showStylePopup) ImGui.openPopup("Choose Style Type");

        StylePopUp();
        ErrorPopUp();

        // Handle whole new windows

        if (showElementsWindow) ElementsWindow();
        if (ElementPropertyWindow.shouldRender) ElementPropertyWindow.render();

        // Calculations for sign preview

        float factor = ratioedSignSize.y / totalSignHeight; // Size of each "sign" tile in pixels
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
    }

    private static ImVec2 previewPos = new ImVec2();

    private static ImageElement testElem = new ImageElement(0, 0, 250, 250, 1,"/ImGui/Icons/Lucky.jpg");
    private static ImageElement testElem2 = new ImageElement(0, 0, 250, 250, 1, "/ImGui/Icons/text.png");
    private static boolean testElemIsInit = true;

    public static void SignPreview(float totalSignWidthPixels, float totalSignHeightPixels, float factor) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);  // Remove spacing between items
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);  // Remove padding inside the frame

        if (!testElemIsInit) {
            testElem.loadTexture();
            testElem2.loadTexture();
            baseElementDrawOrder.add(testElem);
            baseElementDrawOrder.add(testElem2);
            testElemIsInit = true;
        }

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
            BaseElement texture = baseElementDrawOrder.get(i);

            ImGui.setCursorPos(previewPos.x, previewPos.y);
            ImGui.beginChild("OVERLAY_CANVAS_" + texture.getId(), totalSignWidthPixels, totalSignHeightPixels);

            ImGui.setCursorPos(texture.getX(), texture.getY());
            texture.renderImGui();

            ImGui.endChild();
        }

        ImGui.popStyleVar(2);
    }


    private static BaseElement selectedElement = null;

    public static void ElementsWindow() {
        ImGui.begin("Elements", ImGuiWindowFlags.NoNavInputs);

        for (int i = 0; i < baseElementDrawOrder.size(); i++) {
            BaseElement element = baseElementDrawOrder.get(i);
            ElementEntry entry = new ElementEntry(element.name, element.getId(), element) {
                @Override
                public void moveEntryUp() {
                    baseElementDrawOrder = ArrayTools.moveElementUpBy(baseElementDrawOrder, baseElementDrawOrder.indexOf(element), 1);
                }

                @Override
                public void moveEntryDown() {
                    baseElementDrawOrder = ArrayTools.moveElementDownBy(baseElementDrawOrder, baseElementDrawOrder.indexOf(element), 1);
                }

                @Override
                public void elementSelectedAction() {
                    selectedElement = element;
                    ElementPropertyWindow.initVars(element, ratioedSignSize);
                }
            };
            entry.render(ImGui.getWindowWidth(), ImGui.getStyle().getWindowPaddingX(), element == baseElementDrawOrder.getFirst(), element == baseElementDrawOrder.getLast(), selectedElement, element); // Check if the element is first/last in the list because then there's nothing to move up/down
            if (baseElementDrawOrder.getLast() != element) ImGui.separator();
        }

        ImGui.end();
    }


    private static Folder currentCountryBG = new Folder("No Country Selected", "/");
    private static Folder oldBGStyle = null;
    private static Folder currentBGStyle = new Folder("No Style Selected", "/ImGui/SignRes/Backgrounds/Austria/Normal"); // Default to Austria's Road Style
    private static boolean styleSelected = false;
    private static boolean applyButtonDisabled = true;

    public static void StylePopUp() {
        ImGui.setNextWindowSize(1000, 750);
        if (ImGui.beginPopupModal("Choose Style Type")) {
            ImGui.pushFont(ImGuiImpl.DejaVuSansBoldBig);
            ImGui.setCursorPosX((1000 - imgui.calcTextSize("Styling Settings").x) / 2);
            ImGui.text("Styling Settings");
            ImGui.popFont();

            ImGui.separator();

            ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
            ImGui.text("Country");
            ImGui.popFont();

            if (ImGui.beginCombo("##country", currentCountryBG.name)) {
                countriesBG.forEach(country -> {
                    boolean isSelected = (Objects.equals(currentCountryBG.name, country.name));

                    // If country is selected, search the country's folder for styles, which are also folders, and put them in a list
                    if (ImGui.selectable(country.name, isSelected)) {
                        currentCountryBG = (Folder) country;
                        try {
                            availableBGStyles = FileSystem.FromResource.listFolders(country.path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (isSelected) ImGui.setItemDefaultFocus();
                });

                ImGui.endCombo();
            }

            ImGui.spacing();

            ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
            ImGui.text("Style");
            ImGui.popFont();

            if (ImGui.beginCombo("##style", currentBGStyle.name)) {

                availableBGStyles.forEach(style -> {
                    boolean isSelected = (Objects.equals(currentBGStyle.name, style.name));
                    if (ImGui.selectable(style.name, isSelected)) {
                        oldBGStyle = currentBGStyle;
                        currentBGStyle = (Folder) style;
                        styleSelected = true;
                    }
                });

                ImGui.endCombo();
            }

            ImGui.spacing();

            if (ImGui.button("Cancel")) {
                styleSelected = false;
                currentBGStyle = oldBGStyle;
                showStylePopup = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();

			applyButtonDisabled = !styleSelected;

            if (applyButtonDisabled) ImGui.beginDisabled();

            if (ImGui.button("Apply")) {
                styleSelected = false;
                showStylePopup = false;
                ImGui.closeCurrentPopup();

                previewTextures = deconstructStyleToArray(signJson.setStyle(currentBGStyle.path, customizableSignBlockEntity));
            }

            if (applyButtonDisabled) ImGui.endDisabled();

            ImGui.endPopup();
        }
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