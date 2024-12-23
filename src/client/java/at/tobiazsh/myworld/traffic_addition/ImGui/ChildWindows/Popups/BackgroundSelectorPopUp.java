package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups;

import at.tobiazsh.myworld.traffic_addition.ImGui.ImGuiImpl;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.components.BlockEntities.CustomizableSignBlockEntity;
import imgui.ImGui;

import java.util.Objects;

import static at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.SignEditor.backgroundTextures;
import static at.tobiazsh.myworld.traffic_addition.ImGui.MainWindows.SignEditor.signJson;
import static at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient.imgui;
import static at.tobiazsh.myworld.traffic_addition.Utils.CustomizableSignStyle.deconstructStyleToArray;

public class BackgroundSelectorPopUp {

    private static boolean shouldOpen = false;
    private static boolean applyButtonDisabled = true;
    private static boolean styleSelected = false;
    private static FileSystem.Folder currentBGStyle = new FileSystem.Folder("No Background Selected", "/assets/myworld_traffic_addition/textures/imgui/sign_res/backgrounds/austria/normal"); // Default to Austria's Road Style
    private static FileSystem.Folder oldBGStyle = null;
    private static FileSystem.Folder currentCountryBG = new FileSystem.Folder("No Country Selected", "/");
    private static FileSystem.Folder availableBGStyles = new FileSystem.Folder(null, null);

    public static void render(FileSystem.Folder countriesBG, CustomizableSignBlockEntity customizableSignBlockEntity) {
        ImGui.setNextWindowSize(1000, 750);
        ImGui.pushFont(ImGuiImpl.DejaVuSans);
        if (ImGui.beginPopupModal("Choose Background")) {
            ImGui.pushFont(ImGuiImpl.DejaVuSansBoldBig);
            ImGui.setCursorPosX((1000 - imgui.calcTextSize("Background Settings").x) / 2);
            ImGui.text("Background Settings");
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
                        currentCountryBG = (FileSystem.Folder) country;

                        try {
                            availableBGStyles = FileSystem.FromResource.listFolders(country.path);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (isSelected) ImGui.setItemDefaultFocus();
                });

                ImGui.endCombo();
            }

            ImGui.spacing();

            ImGui.pushFont(ImGuiImpl.DejaVuSansBold);
            ImGui.text("Background");
            ImGui.popFont();

            if (ImGui.beginCombo("##background", currentBGStyle.name)) {

                availableBGStyles.forEach(style -> {
                    boolean isSelected = (Objects.equals(currentBGStyle.name, style.name));
                    if (ImGui.selectable(style.name, isSelected)) {
                        oldBGStyle = currentBGStyle;
                        currentBGStyle = (FileSystem.Folder) style;
                        styleSelected = true;
                    }
                });

                ImGui.endCombo();
            }

            ImGui.spacing();

            if (ImGui.button("Cancel")) {
                styleSelected = false;
                currentBGStyle = oldBGStyle;
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();

            applyButtonDisabled = !styleSelected;

            if (applyButtonDisabled) ImGui.beginDisabled();

            if (ImGui.button("Apply")) {
                styleSelected = false;
                ImGui.closeCurrentPopup();

                backgroundTextures = deconstructStyleToArray(signJson.setStyle(currentBGStyle.path, customizableSignBlockEntity));
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
