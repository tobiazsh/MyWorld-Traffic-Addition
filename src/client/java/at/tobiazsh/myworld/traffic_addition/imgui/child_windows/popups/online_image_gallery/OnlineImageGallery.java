package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.online_image_gallery;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.networking.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.utils.CommonImages;
import at.tobiazsh.myworld.traffic_addition.utils.CustomImageMetadata;
import at.tobiazsh.myworld.traffic_addition.utils.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.utils.OnlineImageLogic;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class OnlineImageGallery {

    private static boolean shouldRender = false;
    private static boolean shouldOpen = false;
    private static boolean shouldClose = false;
    private static final int windowWidth = 800;
    private static final int windowHeight = 610;
    private static int currentPage = 0;
    private static int maxPages = 0;
    private static int entryCount = 0; // Keeps track of the number of uploaded images on server (without hidden ones)
    private static int entriesPerPage = 0;
    private static boolean isLoading = false;
    private static boolean showErrorInfo = false;
    private static TABS currentTab = TABS.ALL;

    private static List<CustomImageMetadata> metadataList = new CopyOnWriteArrayList<>();
    private static List<byte[]> currentThumbnailData = new CopyOnWriteArrayList<>();
    private static final List<Texture> currentThumbnails = new CopyOnWriteArrayList<>();
    private static final List<EntryCard> entryCards = new CopyOnWriteArrayList<>();

    private static final List<Integer> texturesToFree = new ArrayList<>();

    private enum TABS {
        ALL,
        MINE
    }

    // TODO: Implement "Add" button

    public static void render() {
        if (!shouldRender) return;

        if (!texturesToFree.isEmpty()) {
            texturesToFree.forEach(GL11::glDeleteTextures); // Free all textures that are no longer needed
            texturesToFree.clear();
        }

        ImGui.setNextWindowSize(windowWidth, windowHeight);
        if (ImGui.beginPopupModal("Online Image Gallery")) {

            renderMenuBar();
            renderPageBar();

            if (isLoading) {
                displayLoading();
            }

            if (showErrorInfo) {
                displayErrorInfo();
            }


            renderEntries();

            ConfirmationPopup.render();

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup("Online Image Gallery");
            shouldOpen = false;
        }

        if (shouldClose) {
            shouldClose = false;
            resetValuesDeferred();
        }
    }

    private static final ImVec4 buttonActivatedColor = new ImVec4(0.26f, 0.59f, 0.98f, 1.0f);

    private static void renderMenuBar() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.129f, 0.129f, 0.129f, 1.0f));
        ImGui.beginChild("##tabControls", ImGui.getWindowSizeX() - 2 * ImGui.getStyle().getWindowPaddingX(), ImGui.getFrameHeight(), false);

        if (ImGui.button("Cancel (X)")) {
            shouldClose = true;
            ImGui.closeCurrentPopup();
        }

        ImGui.sameLine();

        boolean pushedAll = currentTab == TABS.ALL;
        if (pushedAll) ImGui.pushStyleColor(ImGuiCol.Button, buttonActivatedColor);
        if (ImGui.button("All Images") && currentTab != TABS.ALL)  {
            currentTab = TABS.ALL;
            refresh();
        }
        if (pushedAll) ImGui.popStyleColor();

        ImGui.sameLine();

        boolean pushedMine = currentTab == TABS.MINE;
        if (pushedMine) ImGui.pushStyleColor(ImGuiCol.Button, buttonActivatedColor);
        if (ImGui.button("My Images") && currentTab != TABS.MINE) {
            currentTab = TABS.MINE;
            refresh();
        }
        if (pushedMine) ImGui.popStyleColor();

        ImGui.sameLine();

        if (ImGui.button("Refresh")) {
            refresh();
        }

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private static void renderPageBar() {
        // Bar at the top
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.129f, 0.129f, 0.129f, 1.0f));
        ImGui.beginChild("##pageControls", ImGui.getWindowSizeX() - 2 * ImGui.getStyle().getWindowPaddingX(), ImGui.getFrameHeight(), false);

        boolean disablePrev = currentPage == 0;
        if (disablePrev) ImGui.beginDisabled();
        if (ImGui.button("< Previous Page")) previousPage();
        if (disablePrev) ImGui.endDisabled();

        ImGui.sameLine();

        String pageText = "Page " + (currentPage + 1) + " of " + (maxPages + 1);
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(pageText)) / 2);
        ImGui.text(pageText);

        ImGui.sameLine();

        boolean disableNext = currentPage == maxPages;
        ImGui.setCursorPosX(ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX("Next Page >") - ImGui.getStyle().getWindowPaddingX());
        if (disableNext) ImGui.beginDisabled();
        if (ImGui.button("Next Page >")) nextPage();
        if (disableNext) ImGui.endDisabled();

        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX("Mine") - MyWorldTrafficAdditionClient.imgui.calcTextSizeX("All")));

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private static void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            downloadPageMetadata(currentPage, currentTab == TABS.MINE);
        }
    }

    private static void nextPage() {
        if (currentPage < maxPages) {
            currentPage++;
            downloadPageMetadata(currentPage, currentTab == TABS.MINE);
        }
    }

    /**
     * Renders the entries in the gallery.
     */
    private static void renderEntries() {
        if (isLoading) return;

        if (metadataList.isEmpty()) {
            ImGui.setCursorPos(
                    new ImVec2(
                            (ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX("No images found.")) / 2,
                            (ImGui.getWindowSizeY() - MyWorldTrafficAdditionClient.imgui.calcTextSizeY("No images found.")) / 2
                    )
            );
            ImGui.text("No images found.");
            return;
        }

        // Only build on first render
        if (entryCards.isEmpty()) {
            for (int i = 0; i < currentThumbnails.size(); i++) {
                int finalI = i;
                entryCards.add(new EntryCard(metadataList.get(i), currentThumbnails.get(i).getTextureId(), currentTab == TABS.MINE, () -> {
                    ConfirmationPopup.show("Are you sure you want to delete " + metadataList.get(finalI).getImageName() + "?", "This action cannot be undone!", callback -> {
                        if (callback) {
                            requestDeletion(metadataList.get(finalI).getImageUUID());
                            refresh();
                        }
                    });
                }));
            }
        }

        float availableWidth = ImGui.getContentRegionAvailX();
        float usedWidth = 0.0f; // Start from 0, not padded
        float spacing = ImGui.getStyle().getItemSpacingX();

        for (int i = 0; i < entryCards.size(); i++) {
            // If card won't fit, wrap to next line
            if (i > 0 && (usedWidth + EntryCard.cardWidth > availableWidth)) {
                usedWidth = 0.0f;
            } else if (i > 0) {
                ImGui.sameLine();
            }

            entryCards.get(i).render();
            usedWidth += EntryCard.cardWidth + spacing;
        }
    }

    private static void displayLoading() {
        ImGui.setCursorPos(new ImVec2(
                (ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX("LOADING...")) / 2,
                (ImGui.getWindowSizeY() - MyWorldTrafficAdditionClient.imgui.calcTextSizeY("LOADING...")) / 2
        ));
        ImGui.text("LOADING...");
    }

    private static void displayErrorInfo() {
        String message = "An Error occurred while loading images! More info in the log.";
        ImGui.setCursorPos(new ImVec2(
                (ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(message)) / 2,
                (ImGui.getWindowSizeY() - MyWorldTrafficAdditionClient.imgui.calcTextSizeY(message)) / 2
        ));
        ImGui.textWrapped(message);
    }

    public static void open() {
        shouldOpen = true;
        shouldRender = true;
        freeTextureIds();
        initializeGallery();
    }

    private static void refresh() {
        currentPage = 0;
        freeTextureIds();
        initializeGallery();
    }

    private static void initializeGallery() {
        isLoading = true;

        if (currentTab == TABS.MINE) {
            OnlineImageLogic.fetchPrivateEntryCount()
                    .thenAccept(count -> {
                        entryCount = count;
                        calculatePage();
                        downloadPageMetadata(currentPage, true);
                    })
                    .exceptionally(ex -> {
                        exceptionallyInitializeGallery(ex);
                        return null;
                    });
        } else if (currentTab == TABS.ALL) {
            OnlineImageLogic.fetchEntryCount()
                    .thenAccept(count -> {
                        entryCount = count;
                        calculatePage();
                        downloadPageMetadata(currentPage, false);
                    })
                    .exceptionally(ex -> {
                        exceptionallyInitializeGallery(ex);
                        return null;
                    });
        }

    }

    private static void exceptionallyInitializeGallery(Throwable ex) {
        MyWorldTrafficAddition.LOGGER.error("Failed to fetch image count: {}", ex.getMessage());
        showErrorInfo = true;
        resetValuesDeferred();
    }

    private static void downloadPageMetadata(int page, boolean privateEntriesOnly) {
        isLoading = true;
        int startIndex = page * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, entryCount);
        entryCards.clear();
        currentThumbnails.clear();

        OnlineImageLogic.fetchImageMetadata(startIndex, endIndex, privateEntriesOnly)
                .thenAccept(list -> {
                    metadataList = new CopyOnWriteArrayList<>(list);
                    loadThumbnailsForCurrentPage(list);
                })
                .exceptionally(ex -> {
                    MyWorldTrafficAddition.LOGGER.error("Failed to fetch image metadata: {}", ex.getMessage());
                    showErrorInfo = true;
                    resetValuesDeferred();
                    return null;
                });
    }

    // What the fuck...
    private static void loadThumbnailsForCurrentPage(List<CustomImageMetadata> metadataList) {
        isLoading = true;

        List<UUID> uuids = metadataList.stream().map(CustomImageMetadata::getImageUUID).toList(); // Get UUIDs of all images
        List<Pair<Integer, UUID>> cachedUuids = splitCached(uuids).getRight(); // Get UUIDs of cached images
        uuids = splitCached(uuids).getLeft(); // Get UUIDs of non-cached images

        // Get Cached thumbnails
        List<Pair<Integer, byte[]>> cachedThumbnails = getFromCachedImages(cachedUuids);

        // All thumbnails are cached; skip fetch
        if (uuids.isEmpty()) {
            List<byte[]> thumbnails = new ArrayList<>();
            cachedThumbnails.forEach(pair -> thumbnails.add(pair.getLeft(), pair.getRight()));
            loadThumbnails(thumbnails);
            return;
        }

        List<UUID> finalUuids = uuids;
        OnlineImageLogic.fetchThumbnails(uuids)
                .thenAccept(thumbnails -> {
                    currentThumbnailData = thumbnails;
                    cachedThumbnails.forEach(pair -> thumbnails.add(pair.getLeft(), pair.getRight())); // Add cached thumbnails to the list

                    for (int i = 0; i < finalUuids.size(); i++) {
                        UUID uuid = finalUuids.get(i);
                        byte[] data = thumbnails.get(i);
                        cacheThumbnail(new Pair<>(uuid, data));
                    }

                    loadThumbnails(thumbnails);
                })
                .exceptionally(ex -> {
                    MyWorldTrafficAddition.LOGGER.error("Failed to fetch thumbnails: {}", ex.getMessage());
                    showErrorInfo = true;
                    resetValuesDeferred();
                    return null;
                });
    }

    private static void loadThumbnails(List<byte[]> thumbnails) {
        freeTextureIds();

        for (byte[] bytes : thumbnails) {
            if (bytes == null) {
                MyWorldTrafficAddition.LOGGER.error("Error (Loading Thumbnails): Texture not found on server!");
                currentThumbnails.add(CommonImages.NOT_FOUND_PLACEHOLDER);
                continue;
            }

            ByteBuffer thumbnailData = ByteBuffer.allocateDirect(bytes.length);
            thumbnailData.put(bytes);
            thumbnailData.flip();

            Texture tex = new Texture();
            tex.loadTextureData(thumbnailData);
            currentThumbnails.add(tex);
        }

        isLoading = false;
    }

    private static void cacheThumbnail(Pair<UUID, byte[]> uncachedThumbnail) {
        String imageName = uncachedThumbnail.getLeft() + "_thumbnail.png";
        OnlineImageCache.cacheImage(uncachedThumbnail.getRight(), imageName);
    }

    /**
     * Splits the list of UUIDs into two lists: one containing the UUIDs that are not cached and one containing the cached UUIDs.
     * @param uuids The list of UUIDs to split
     * @return Left list: UUIDs that are not cached, right list: List of Pair, where left is the UUID that is cached and right is its index in the original list
     */
    private static Pair<List<UUID>, List<Pair<Integer, UUID>>> splitCached(List<UUID> uuids) {
        List<UUID> cached = OnlineImageCache.getCachedUUIDs("_thumbnail.png");
        List<UUID> finalCached = cached;
        List<UUID> notCached = uuids.stream().filter(uuid -> !finalCached.contains(uuid)).toList();
        cached = uuids.stream().filter(cached::contains).toList(); // Filter out UUIDs that aren't even in the list

        List<Pair<Integer, UUID>> cachedUuids = cached.stream().map(uuid -> {
            int index = uuids.indexOf(uuid);
            return new Pair<>(index, uuid);
        }).toList();

        return new Pair<>(
                notCached,
                cachedUuids
        );
    }

    private static List<Pair<Integer, byte[]>> getFromCachedImages(List<Pair<Integer, UUID>> images) {
        List<Pair<Integer, byte[]>> cachedImages = new ArrayList<>();

        for (Pair<Integer, UUID> pair : images) {
            byte[] image = OnlineImageCache.loadImage(pair.getRight().toString() + "_thumbnail.png");
            if (image != null) {
                cachedImages.add(new Pair<>(pair.getLeft(), image));
            }
        }

        return cachedImages;
    }

    private static void calcEntriesPerPage() {
        float padding = ImGui.getStyle().getWindowPaddingX();
        float entries = (windowWidth - 2 * padding) / EntryCard.cardWidth;

        if (entries < 1) {
            entriesPerPage = 0;
            return;
        }

        entriesPerPage = (int) Math.floor(entries) * 2; // 2 rows
    }

    private static void calcMaxPages() {
        if (entryCount == 0 || entriesPerPage == 0) {
            maxPages = 0;
            return;
        }

        maxPages = (int) Math.ceil((float) entryCount / entriesPerPage) - 1; // -1 because page starts at 0
    }

    private static void freeTextureIds() {
        currentThumbnails.forEach(texture -> texturesToFree.add(texture.getTextureId()));

        currentThumbnails.clear();
        entryCards.clear();
    }

    private static void calculatePage() {
        calcEntriesPerPage();
        calcMaxPages();
    }

    private static void resetValuesDeferred() {
        shouldRender = false;
        shouldOpen = false;
        currentPage = 0;
        maxPages = 0;
        entryCount = 0;
        entriesPerPage = 0;
        isLoading = false;
        showErrorInfo = false;
        currentTab = TABS.ALL;

        freeTextureIds();
        metadataList.clear();
        currentThumbnailData.clear();
    }

    private static void requestDeletion(UUID imageUUID) {
        CustomClientNetworking.getInstance().sendBytesToServer( // Send request to server
                Identifier.of(MyWorldTrafficAddition.MOD_ID, "request_image_deletion"),
                imageUUID.toString().getBytes(),
                -1, -1
        );
    }
}
