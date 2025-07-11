package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.online_image_gallery;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.OnlineImageElementClient;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.utils.CustomImageMetadata;
import at.tobiazsh.myworld.traffic_addition.utils.DateTimeUtility;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;

public class EntryCard {
    public static int cardHeight = 250;
    public static int cardWidth = 176;
    private static final int buttonHeight = 50;
    private static final int deleteButtonHeight = 25;
    private final int thumbnailTextureId;
    private final CustomImageMetadata imageEntry;
    private String id;
    private final boolean showDeleteButton;
    private final Runnable onDeleteAction;

    public EntryCard(CustomImageMetadata imageEntry, int thumbnailTextureId, boolean showDeleteButton, Runnable onDeleteAction) {
        this.imageEntry = imageEntry;
        this.thumbnailTextureId = thumbnailTextureId;
        this.showDeleteButton = showDeleteButton;
        this.onDeleteAction = onDeleteAction;
        constructId();
    }

    private void constructId() {
        this.id = "##entryCard" + imageEntry.getImageUUID();
    }

    public void render() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.17f, 0.18f, 0.19f, 1.0f));
        ImGui.beginChild(id, cardWidth, cardHeight, true);

        renderMoreInfoPopup();

        ImGui.image(thumbnailTextureId, ImGui.getContentRegionAvailX(), 125);
        ImGui.textWrapped(imageEntry.getImageName());

        float buttonCursorPosY = ImGui.getWindowContentRegionMaxY() - buttonHeight; // Align Bottom
        if (showDeleteButton) buttonCursorPosY -= (deleteButtonHeight + ImGui.getStyle().getItemSpacing().y); // Remove extra space if delete button is shown
        ImGui.setCursorPosY(buttonCursorPosY);

        // Render Delete Button
        if (showDeleteButton) {
            ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(1.0f, 0.2f, 0.2f, 1.0f)); // Red color for delete button
            if (ImGui.button("Delete", ImGui.getContentRegionAvailX(), deleteButtonHeight))
                this.onDeleteAction.run();
            ImGui.popStyleColor();
        }

        float spacing = ImGui.getStyle().getItemSpacing().x;
        float buttonWidth = (ImGui.getContentRegionAvailX() - spacing) * 0.5f;
        if (ImGui.button("Add", buttonWidth, buttonHeight)) addToSign();
        ImGui.sameLine();
        if (ImGui.button("More...", buttonWidth, buttonHeight)) openMoreInfoPopup();

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private String uploaderName = "";

    private boolean hasFetchedNameSuccessfully = false;
    private boolean isFetchingName = false;
    private boolean hadErrorWhileFetching = false;

    private void renderMoreInfoPopup() {
        if (ImGui.beginPopupModal("More Info##" + id)) {

            ImGui.text("Name: " + imageEntry.getImageName());
            ImGui.text("Encoded Name: " + imageEntry.getImageNameEncoded());
            ImGui.separator();
            ImGui.text("Uploader's UUID: " + imageEntry.getUploaderUUID());

            // Status display
            if (isFetchingName) {
                ImGui.text("Profile Name: FETCHING...");
            } else if (hadErrorWhileFetching) {
                ImGui.text("Profile Name: ERROR! CHECK LOGS");
            } else if (hasFetchedNameSuccessfully) {
                ImGui.text("Profile Name: " + uploaderName);
            } else {
                ImGui.text("Profile Name: Not Fetched");
            }

            // Button to start fetching the name; only shown if not fetched or error
            if (!hasFetchedNameSuccessfully && !isFetchingName) {
                ImGui.sameLine();
                if (ImGui.button("Fetch from Web (Mojang API)")) fetchUploaderName();
            }

            ImGui.separator();

            ImGui.text("Uploaded: " + DateTimeUtility.formatWithOrdinal(imageEntry.getCreationDateLocal()) + " at " + DateTimeUtility.formatMilitaryTime(imageEntry.getCreationDateLocal()));
            ImGui.separator();
            ImGui.text("Hidden: " + (imageEntry.isHidden() ? "Yes" : "No"));

            ImGui.separator();
            if (ImGui.button("Close")) ImGui.closeCurrentPopup();

            ImGui.endPopup();
        }
    }

    private void openMoreInfoPopup() {
        ImGui.openPopup("More Info##" + id);
    }

    private void addToSign() {
        ClientElementManager.getInstance().addElement(new OnlineImageElementClient(0, 0, -1, -1, 1.0f, 0f, imageEntry.getImageUUID(), ClientElementInterface.MAIN_CANVAS_ID));
    }

    private void fetchUploaderName() {
        isFetchingName = true;
        hadErrorWhileFetching = false;
        hasFetchedNameSuccessfully = false;

        imageEntry.getUploaderName().thenAccept(name -> {
            uploaderName = name;
            hasFetchedNameSuccessfully = true;
            isFetchingName = false;
        }).exceptionally(ex -> {
            MyWorldTrafficAddition.LOGGER.error("Error while fetching uploader name: {}", ex.getMessage());
            hadErrorWhileFetching = true;
            isFetchingName = false;
            return null;
        });
    }
}
