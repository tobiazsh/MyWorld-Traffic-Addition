package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.online_image_gallery;

import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ConfirmationPopup;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.OnlineImageDialog;
import at.tobiazsh.myworld.traffic_addition.network.CustomClientNetworking;
import at.tobiazsh.myworld.traffic_addition.texture.CommonTextures;
import at.tobiazsh.myworld.traffic_addition.metadata.CustomImageMetadata;
import at.tobiazsh.myworld.traffic_addition.cache.OnlineImageCache;
import at.tobiazsh.myworld.traffic_addition.network.OnlineImageNetworking;
import at.tobiazsh.myworld.traffic_addition.texture.Texture;
import at.tobiazsh.myworld.traffic_addition.utils.Tuple;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

public class OnlineImageGallery {

    private final String id;

    private boolean shouldRender = false;
    private boolean shouldOpen = false;
    private boolean shouldClose = false;

    private final int windowWidth = 800;
    private final int windowHeight = 610;

    private int currentPage = 0;
    private int maxPages = 0;
    private int entryCount = 0; // Keeps track of the number of uploaded images on server (without hidden ones)
    private int entriesPerPage = 0;

    private boolean isLoading = false;
    private boolean showErrorInfo = false;

    private TABS currentTab = TABS.ALL;

    private List<CustomImageMetadata> metadataList = new CopyOnWriteArrayList<>();
    private List<byte[]> currentThumbnailData = new CopyOnWriteArrayList<>();
    private final List<Texture> currentThumbnails = new CopyOnWriteArrayList<>();
    private final List<EntryCard> entryCards = new CopyOnWriteArrayList<>();

    private final List<Texture> texturesToFree = new ArrayList<>();

    private final ConfirmationPopup confirmationPopup =
            new ConfirmationPopup("OnlineImageGallery", ImGuiFontScope.create());

    private enum TABS {
        ALL,
        MINE
    }

    public OnlineImageGallery(String id) {
        this.id = id;
    }

    public void renderSubwindows() {
        OnlineImageDialog.INSTANCE.render();
    }

    public void render() {
        ImGui.pushID(id);

        if (!shouldRender) return;

        if (!texturesToFree.isEmpty()) {
            texturesToFree.forEach(Texture::delete); // Free all textures that are no longer needed
            texturesToFree.clear();
        }

        ImGui.setNextWindowSize(windowWidth, windowHeight);
        if (ImGui.beginPopupModal(tr("ImGui.Child.PopUps.OnlineImageGallery", "Online Image Gallery"))) {

            renderSubwindows();

            renderMenuBar();
            renderPageBar();

            if (isLoading) {
                displayLoading();
            }

            if (showErrorInfo) {
                displayErrorInfo();
            }

            renderEntries();
            confirmationPopup.render();

            ImGui.endPopup();
        }

        ImGui.popID();

        if (shouldOpen) {
            ImGui.openPopup(tr("ImGui.Child.PopUps.OnlineImageGallery", "Online Image Gallery"));
            shouldOpen = false;
        }

        if (shouldClose) {
            shouldClose = false;
            resetValuesDeferred();
        }
    }

    private final ImVec4 buttonActivatedColor = new ImVec4(0.26f, 0.59f, 0.98f, 1.0f);

    private void renderMenuBar() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.129f, 0.129f, 0.129f, 1.0f));
        ImGui.beginChild("##tabControls", ImGui.getWindowSizeX() - 2 * ImGui.getStyle().getWindowPaddingX(), ImGui.getFrameHeight(), false);

        if (ImGui.button(tr("Global", "Close"))) close();

        ImGui.sameLine();

        boolean pushedAll = currentTab == TABS.ALL;
        if (pushedAll) ImGui.pushStyleColor(ImGuiCol.Button, buttonActivatedColor);
        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageGallery", "All Images")) && currentTab != TABS.ALL)  {
            currentTab = TABS.ALL;
            refresh();
        }
        if (pushedAll) ImGui.popStyleColor();

        ImGui.sameLine();

        boolean pushedMine = currentTab == TABS.MINE;
        if (pushedMine) ImGui.pushStyleColor(ImGuiCol.Button, buttonActivatedColor);
        if (ImGui.button(tr("ImGui.Child.PopUps.OnlineImageGallery", "My Images")) && currentTab != TABS.MINE) {
            currentTab = TABS.MINE;
            refresh();
        }
        if (pushedMine) ImGui.popStyleColor();

        ImGui.sameLine();

        if (ImGui.button(tr("Global", "Refresh"))) {
            refresh();
        }

        ImGui.sameLine();

        if (ImGui.button(tr("ImGui.Main.SignEditor", "Upload Image") + "..."))
            OnlineImageDialog.INSTANCE.startDialog(); // Open online image dialog and after upload, re-open gallery

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private  void renderPageBar() {
        // Bar at the top
        ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.129f, 0.129f, 0.129f, 1.0f));
        ImGui.beginChild("##pageControls", ImGui.getWindowSizeX() - 2 * ImGui.getStyle().getWindowPaddingX(), ImGui.getFrameHeight(), false);

        boolean disablePrev = currentPage == 0;
        if (disablePrev) ImGui.beginDisabled();
        if (ImGui.button("< %s".formatted(tr("ImGui.Child.PopUps.OnlineImageGallery", "Previous Page")))) previousPage(); // < Previous Page
        if (disablePrev) ImGui.endDisabled();

        ImGui.sameLine();

        String pageText = "%s %s %s %s".formatted(tr("Global", "Page"), currentPage + 1, tr("Global", "Of").toLowerCase(), maxPages + 1); // Page ... of ...
        ImGui.setCursorPosX((ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(pageText)) / 2);
        ImGui.text(pageText);

        ImGui.sameLine();

        boolean disableNext = currentPage == maxPages;
        ImGui.setCursorPosX(ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX("%s >".formatted(tr("ImGui.Child.PopUps.OnlineImageGallery", "Next Page"))) - ImGui.getStyle().getWindowPaddingX());
        if (disableNext) ImGui.beginDisabled();
        if (ImGui.button("%s >".formatted(tr("ImGui.Child.PopUps.OnlineImageGallery", "Next Page")))) nextPage(); // Next Page >
        if (disableNext) ImGui.endDisabled();

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            downloadPageMetadata(currentPage, currentTab == TABS.MINE);
        }
    }

    private void nextPage() {
        if (currentPage < maxPages) {
            currentPage++;
            downloadPageMetadata(currentPage, currentTab == TABS.MINE);
        }
    }

    /**
     * Renders the entries in the gallery.
     */
    private void renderEntries() {
        if (isLoading) return;

        if (metadataList.isEmpty()) {
            String noImagesFound = tr("ImGui.Child.PopUps.OnlineImageGallery", "No images found");
            ImGui.setCursorPos(
                    new ImVec2(
                            (ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(noImagesFound)) / 2,
                            (ImGui.getWindowSizeY() - MyWorldTrafficAdditionClient.imgui.calcTextSizeY(noImagesFound)) / 2
                    )
            );
            ImGui.text(noImagesFound);
            return;
        }

        // Only build on first render
        if (entryCards.isEmpty()) {
            for (int i = 0; i < currentThumbnails.size(); i++) {
                int finalI = i;
                entryCards.add(new EntryCard(metadataList.get(i), currentThumbnails.get(i).getTextureId(), currentTab == TABS.MINE, () -> {
                    this.confirmationPopup.show(
                            "%s %s?".formatted(
                                    tr("ImGui.Child.PopUps.OnlineImageGallery", "Are you sure you want to delete"),
                                    metadataList.get(finalI).getImageName()
                            ),
                            tr("ImGui.Global.Warn", "This action cannot be undone!"),
                            callback -> {
                                if (callback) {
                                    requestDeletion(metadataList.get(finalI).getImageUUID());
                                    refresh();
                                }
                            }
                    );
                }, this::close));
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

    private void displayLoading() {
        String loading = tr("ImGui.Child.PopUps.OnlineImageGallery", "Loading...");
        ImGui.setCursorPos(new ImVec2(
                (ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(loading)) / 2,
                (ImGui.getWindowSizeY() - MyWorldTrafficAdditionClient.imgui.calcTextSizeY(loading)) / 2
        ));
        ImGui.text(loading);
    }

    private void displayErrorInfo() {
        String message = tr("ImGui.Child.PopUps.OnlineImageGallery.Error", "An Error occurred while loading images! More info in the log.");
        ImGui.setCursorPos(new ImVec2(
                (ImGui.getWindowSizeX() - MyWorldTrafficAdditionClient.imgui.calcTextSizeX(message)) / 2,
                (ImGui.getWindowSizeY() - MyWorldTrafficAdditionClient.imgui.calcTextSizeY(message)) / 2
        ));
        ImGui.textWrapped(message);
    }

    public void open() {
        shouldOpen = true;
        shouldRender = true;
        freeTextureIds();
        initializeGallery();
    }

    private void refresh() {
        currentPage = 0;
        freeTextureIds();
        initializeGallery();
    }

    private void initializeGallery() {
        isLoading = true;

        if (currentTab == TABS.MINE) {
            OnlineImageNetworking.fetchPrivateEntryCount()
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
            OnlineImageNetworking.fetchEntryCount()
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

    private void exceptionallyInitializeGallery(Throwable ex) {
        MyWorldTrafficAddition.LOGGER.error("Failed to fetch image count: {}", ex.getMessage());
        showErrorInfo = true;
        resetValuesDeferred();
    }

    private void downloadPageMetadata(int page, boolean privateEntriesOnly) {
        isLoading = true;
        int startIndex = page * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, entryCount);
        entryCards.clear();
        currentThumbnails.clear();

        OnlineImageNetworking.fetchImageMetadata(startIndex, endIndex, privateEntriesOnly)
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

    @NullMarked
    private void loadThumbnailsForCurrentPage(List<CustomImageMetadata> metadataList) {
        isLoading = true;

        List<UUID> uuids = metadataList.stream().map(CustomImageMetadata::getImageUUID).toList(); // Get UUIDs of all images
        Tuple<List<UUID>, List<Tuple<Integer, UUID>>> listPair = splitCached(uuids);
        List<Tuple<Integer, UUID>> cachedUuids = listPair.b(); // Get UUIDs of cached images
        uuids = listPair.a(); // Get UUIDs of non-cached images

        // Get Cached thumbnails
        List<Tuple<Integer, byte[]>> cachedThumbnails = getFromCachedImages(cachedUuids);

        // All thumbnails are cached; skip fetch
        if (uuids.isEmpty()) {
            List<byte[]> thumbnails = new ArrayList<>();
            cachedThumbnails.forEach(pair -> thumbnails.add(pair.a(), pair.b()));
            Minecraft.getInstance().execute(() -> loadThumbnails(thumbnails));
            return;
        }

        List<UUID> finalUuids = uuids;
        OnlineImageNetworking.fetchThumbnails(uuids)
                .thenAccept(thumbnails -> {
                    currentThumbnailData = thumbnails;
                    cachedThumbnails.forEach(pair -> thumbnails.add(pair.a(), pair.b())); // Add cached thumbnails to the list

                    for (int i = 0; i < finalUuids.size(); i++) {
                        UUID uuid = finalUuids.get(i);
                        byte[] data = thumbnails.get(i);
                        cacheThumbnail(new Tuple<>(uuid, data));
                    }

                    Minecraft.getInstance().execute(() -> loadThumbnails(thumbnails));
                })
                .exceptionally(ex -> {
                    MyWorldTrafficAddition.LOGGER.error("Failed to fetch thumbnails: {}", ex.getMessage());
                    showErrorInfo = true;
                    resetValuesDeferred();
                    return null;
                });
    }

    private void loadThumbnails(List<byte[]> thumbnails) {
        freeTextureIds();

        for (byte[] bytes : thumbnails) {
            if (bytes == null) {
                MyWorldTrafficAddition.LOGGER.error("Error (Loading Thumbnails): Texture not found on server!");
                currentThumbnails.add(CommonTextures.NOT_FOUND_PLACEHOLDER);
                continue;
            }

            ByteBuffer thumbnailData = null;
            try {
                thumbnailData = MemoryUtil.memAlloc(bytes.length);
                thumbnailData.put(bytes);
                thumbnailData.flip();

                Texture tex = new Texture();
                tex.loadTextureData(thumbnailData);
                currentThumbnails.add(tex);
            } catch (Throwable t) {
                MyWorldTrafficAddition.LOGGER.error("Failed to create texture from thumbnail data: {}", t.getMessage());
                currentThumbnails.add(CommonTextures.NOT_FOUND_PLACEHOLDER);
            }
            // DO NOT free thumbnailData here, Texture.loadTextureData already does that
        }

        isLoading = false;
    }

    @NullMarked
    private void cacheThumbnail(Tuple<UUID, byte[]> uncachedThumbnail) {
        String imageName = uncachedThumbnail.a() + "_thumbnail.png";

        try {
            OnlineImageCache.cacheImage(uncachedThumbnail.b(), imageName);
        } catch (Exception e) {
            MyWorldTrafficAddition.LOGGER.error("Failed to cache thumbnail for image {}: {}", uncachedThumbnail.a(), e.getMessage());
        }
    }

    /**
     * Splits the list of UUIDs into two lists: one containing the UUIDs that are not cached and one containing the cached UUIDs.
     * @param uuids The list of UUIDs to split
     * @return Left list: UUIDs that are not cached, right list: List of Pair, where left is the UUID that is cached and right is its index in the original list
     */
    @NullMarked
    private Tuple<List<UUID>, List<Tuple<Integer, UUID>>> splitCached(List<UUID> uuids) {
        List<UUID> cached = OnlineImageCache.getCachedUUIDs("_thumbnail.png");
        List<UUID> finalCached = cached;
        List<UUID> notCached = uuids.stream().filter(uuid -> !finalCached.contains(uuid)).toList();
        cached = uuids.stream().filter(cached::contains).toList(); // Filter out UUIDs that aren't even in the list

        List<Tuple<Integer, UUID>> cachedUuids = cached.stream().map(uuid -> {
            int index = uuids.indexOf(uuid);
            return new Tuple<>(index, uuid);
        }).toList();

        return new Tuple<>(
                notCached,
                cachedUuids
        );
    }

    @NullMarked
    private List<Tuple<Integer, byte[]>> getFromCachedImages(List<Tuple<Integer, UUID>> images) {
        List<Tuple<Integer, byte[]>> cachedImages = new ArrayList<>();

        for (Tuple<Integer, UUID> pair : images) {
            byte[] image = OnlineImageCache.loadImage(pair.b() + "_thumbnail.png");
            if (image != null) {
                cachedImages.add(new Tuple<>(pair.a(), image));
            }
        }

        return cachedImages;
    }

    private void calcEntriesPerPage() {
        float padding = ImGui.getStyle().getWindowPaddingX();
        float entries = (windowWidth - 2 * padding) / EntryCard.cardWidth;

        if (entries < 1) {
            entriesPerPage = 0;
            return;
        }

        entriesPerPage = (int) Math.floor(entries) * 2; // 2 rows
    }

    private void calcMaxPages() {
        if (entryCount == 0 || entriesPerPage == 0) {
            maxPages = 0;
            return;
        }

        maxPages = (int) Math.ceil((float) entryCount / entriesPerPage) - 1; // -1 because page starts at 0
    }

    private void freeTextureIds() {
        for (Texture texture : currentThumbnails) {
            if (texture != null && !texture.isEmpty()) {
                texturesToFree.add(texture);
            }
        }
        currentThumbnails.clear();
    }

    private void calculatePage() {
        calcEntriesPerPage();
        calcMaxPages();
    }

    private void resetValuesDeferred() {
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

    private void requestDeletion(UUID imageUUID) {
        CustomClientNetworking.getInstance().sendBytesToServer( // Send request to server
                Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "request_image_deletion"),
                imageUUID.toString().getBytes(),
                -1, -1
        );
    }

    private void close() {
        shouldClose = true;
        ImGui.closeCurrentPopup();
    }
}
