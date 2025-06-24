package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups;

import at.tobiazsh.myworld.traffic_addition.CustomizableSign.Elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.Utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.Components.BlockEntities.CustomizableSignBlockEntity;
import imgui.ImGui;

import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignData.getBackgroundTexturePathList;

public class BackgroundSelectorPopup {

    private static boolean shouldOpen = false;
    private static boolean styleSelected = false;
    private static FileSystem.Folder currentBackground = new FileSystem.Folder("No Background Selected", "/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/normal"); // Default to Austria's Road Style
    private final static FileSystem.Folder defaultBackground = new FileSystem.Folder("No Background Selected", "/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/normal");
    private static FileSystem.Folder oldBackground = null;
    private static FileSystem.Folder selectedCountry = new FileSystem.Folder("No Country Selected", "/");
    private static FileSystem.Folder availableBackgrounds = new FileSystem.Folder(null, null);

    public static void render(FileSystem.Folder countriesBG, CustomizableSignBlockEntity customizableSignBlockEntity) {
        ImGui.setNextWindowSize(1000, 750);
        ImGui.pushFont(ImGuiImpl.Roboto);
        if (ImGui.beginPopupModal("Choose Background")) {
            ImGui.pushFont(ImGuiImpl.RobotoBoldBig);
            ImGui.setCursorPosX((1000 - imgui.calcTextSize("Background Settings").x) / 2);
            ImGui.text("Background Settings");
            ImGui.popFont();

            ImGui.separator();

            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text("Country");
            ImGui.popFont();

            if (ImGui.beginCombo("##country", selectedCountry.name)) {
                countriesBG.forEach(country -> {
                    boolean isSelected = (Objects.equals(selectedCountry.name, country.name));

                    // If country is selected, search the country's folder for styles, which are also folders, and put them in a list
                    if (ImGui.selectable(country.name, isSelected)) {
                        selectedCountry = (FileSystem.Folder) country;

                        try {
                            availableBackgrounds = FileSystem.listFoldersRecursive(country.path, true);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (isSelected) ImGui.setItemDefaultFocus();
                });

                ImGui.endCombo();
            }

            ImGui.spacing();

            ImGui.pushFont(ImGuiImpl.RobotoBold);
            ImGui.text("Background");
            ImGui.popFont();

            if (ImGui.beginCombo("##background", currentBackground.name)) {

                availableBackgrounds.forEach(style -> {
                    boolean isSelected = (Objects.equals(currentBackground.name, style.name));
                    if (ImGui.selectable(style.name, isSelected)) {
                        oldBackground = currentBackground;
                        currentBackground = (FileSystem.Folder) style;
                        styleSelected = true;
                    }
                });

                ImGui.endCombo();
            }

            ImGui.spacing();

            if (ImGui.button("Cancel")) {
                styleSelected = false;
                currentBackground = Objects.requireNonNullElse(oldBackground, defaultBackground); // If there wasn't a background beforehand, select the default one.
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();

            boolean applyButtonDisabled = !styleSelected;

            if (applyButtonDisabled) ImGui.beginDisabled();

            if (ImGui.button("Apply")) {
                styleSelected = false;
                ImGui.closeCurrentPopup();

                ClientElementManager.getInstance().rawData.setStyle(currentBackground.path);
                ClientElementManager.getInstance().setBackgroundTextures(getBackgroundTexturePathList(ClientElementManager.getInstance().rawData, customizableSignBlockEntity));
            }

            if (applyButtonDisabled) ImGui.endDisabled();

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup("Choose Background");
            shouldOpen = false;
        }

        ImGui.popFont();
    }

    public static void open() {
        shouldOpen = true;
    }
}
