package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.algorithms.FuzzySearch;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.custom_payloads.block_modification.SignBlockTextureChangePayload;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.SignFilter;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.utils.exception.SignTextureParseException;
import at.tobiazsh.myworld.traffic_addition.utils.sign.SignTexture;
import at.tobiazsh.myworld.traffic_addition.utils.texturing.Texture;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;
import static at.tobiazsh.myworld.traffic_addition.utils.PathUtils.relativizeResourcePath;
import static at.tobiazsh.myworld.traffic_addition.utils.PathUtils.windowsToUnixPath;

/**
 * The window used to select the texture of the sign on a normal sign block (not customizable!)
 */
public class SignSelector {

    public static List<SignSelector> signSelectors = new ArrayList<>();
    private boolean shouldRender;
    private SignBlock.SIGN_SHAPE signType;
    private List<SignTexture> textureDatabase = new ArrayList<>();
    private SignFilter filter = new SignFilter(null, null, null);
    private final Texture previewTexture = new Texture();
    private BlockPos signPos;
    private RegistryKey<World> worldRegistryKey;
    private final String windowId;

    private FilterWindow filterWindow;

    public SignSelector(String windowId) {
        this.windowId = windowId;

        filterWindow = new FilterWindow(() -> {
        }, () -> {
        }, () -> {
            this.filter = filterWindow.getFilter();
            refresh();
            resultNames = this.results.stream().map(SignTexture::name).toArray(String[]::new); // Convert SignTexture object to array with names for ImGui to be able to display them
        }, true, true, windowId);
    }

    public void render() {
        if (!shouldRender)
            return;

        ImGui.begin(tr("Main.SignSelector", "Sign Selector") + "###SignSelector" + windowId);

        searchBar();
        selectionList();
        ImGui.sameLine();
        texturePreview(ImGui.getContentRegionAvailX());

        if (lastIndex != selectedIndex.get()) {
            // Update anything related to that
            lastIndex = selectedIndex.get();

            // Load texture into ImGui
            previewTexture.loadTexturePath(results.get(selectedIndex.get()).path().toString().replaceAll("\\\\", "/"));
        }

        ImGui.separator();

        if (ImGui.button("Select")) {
            System.out.println("Selected texture: " + results.get(selectedIndex.get()));
            apply();
        }

        ImGui.sameLine();

        if (ImGui.button(tr("Global", "Close")))
            close();


        filterWindow.render();

        ImGui.end();
    }

    private void refresh() {

        AtomicReference<List<SignTexture>> textures = new AtomicReference<>();

        try {
            // Get whole folder first
            FileSystem.Folder signFolder = FileSystem.listAllRecursive("/assets/myworld_traffic_addition/textures/sign/", true); // Folder with all the sign textures

            signFolder.content
                    .stream()
                    .filter(FileSystem.DirectoryElement::isFolder)
                    .forEach(textureFolder -> {
                        String filePath = ((FileSystem.Folder) textureFolder).content // Get the textures.json file or null if not present
                                .stream()
                                .filter(dirElem -> dirElem.name.matches("textures.json"))
                                .findAny()
                                .orElse(new FileSystem.DirectoryElement(null, null))
                                .path;

                        if (filePath == null) {
                            MyWorldTrafficAddition.LOGGER.warn("No textures.json found in folder: {}", textureFolder.name);
                        } else {
                            // Finally, parse the textures.json
                            try {
                                textures.set(SignTexture.parseFile(Path.of(filePath.replaceAll("\\\\", "/")), true)); // WHY HAS WINDOWS GOTTA BE SO SPECIAL WITH PATHS :((((((
                            } catch (SignTextureParseException e) {
                                MyWorldTrafficAddition.LOGGER.error("An error occurred while trying to read the sign textures from: {}", filePath, e);
                            }
                        }
                    });

        } catch (IOException | URISyntaxException e) {
            ErrorPopup.open("Error", "An error occurred while trying to read the sign textures. Stack Trace is available in the log.", this::close); // No need for translations as this should not happen in normal use
            MyWorldTrafficAddition.LOGGER.error("An error occurred while trying to read the sign textures: ", e);
            close();
        }

        List<SignTexture> parsed = textures.get();
        if (parsed == null) parsed = List.of();
        textureDatabase = filterResults(parsed, filter);
        results = textureDatabase;
    }

    private final ImString searchQuery = new ImString(128);
    private List<SignTexture> results = textureDatabase;
    private String[] resultNames = new String[0];

    private void searchBar() {
        ImGui.beginChild("##searchbar", 0, 50, false);

        ImGui.inputText("##search", searchQuery);
        ImGui.sameLine();
        if (ImGui.button(tr("Global", "Search"))) {
            if (searchQuery.get().isBlank()) {
                results = this.textureDatabase;
            } else {
                System.out.println("Searching for: " + searchQuery.get());

                FuzzySearch<SignTexture> search = new FuzzySearch<>(textureDatabase, SignTexture::name, 1);
                results = filterResults(search.search(searchQuery.get()), filter);
            }

            resultNames = this.results.stream().map(SignTexture::name).toArray(String[]::new); // Convert SignTexture object to array with names for ImGui to be able to display them
        }
        ImGui.sameLine();
        if (ImGui.button(tr("Global", "Filter") + "...")) {
            filterWindow.open(
                    filter,
                    List.of(SignTexture.CATEGORY.values()),
                    SignTexture.getCountries().stream().toList(),
                    List.of(SignBlock.SIGN_SHAPE.values())
            );
        }

        ImGui.endChild();
    }

    private List<SignTexture> filterResults(Collection<SignTexture> textures, SignFilter filter) {
        return textures.stream().filter(filter::matches).toList();
    }


    private final ImInt selectedIndex = new ImInt(0);
    private int lastIndex = 0;

    private void selectionList() {
        ImGui.text(tr("Global", "Results"));
        ImGui.listBox("##resultBox", selectedIndex, resultNames, 15);
    }

    private void texturePreview(float w) {
        ImGui.beginChild("##texturePreview", w, w, true); // Square ratio across available width
        ImVec2 windowPadding = ImGui.getStyle().getWindowPadding();

        ImGui.image(previewTexture.getTextureId(), w - (windowPadding.x * 2), w - (windowPadding.y * 2));

        ImGui.endChild();
    }

    private void apply() {
        ClientPlayNetworking.send(
                new SignBlockTextureChangePayload(
                        signPos,
                        windowsToUnixPath(relativizeResourcePath(results.get(selectedIndex.get()).path()).toString()),
                        worldRegistryKey));
    }

    /**
     * Opens and initialized the sign selector window for the current sign type
     */
    public void open(SignBlock.SIGN_SHAPE signType, BlockPos signPos, RegistryKey<World> world) {
        shouldRender = true;
        this.signType = signType;
        this.filter = new SignFilter(null, null, signType); // Filter for the current sign type
        this.signPos = signPos;
        this.worldRegistryKey = world;
        refresh();
        resultNames = this.results.stream().map(SignTexture::name).toArray(String[]::new); // Convert SignTexture object to array with names for ImGui to be able to display them
    }

    /**
     * Closes the sign selector window but doesn't reset flags!
     */
    public void close() {
        shouldRender = false;
    }

    /**
     * Whether the sign selector window should be rendered or not
     */
    public boolean isShouldRender() {
        return shouldRender;
    }

    /**
     * Get the current type of sign block that the sign selector window uses
     */
    public SignBlock.SIGN_SHAPE getSignSelectionType() {
        return this.signType;
    }





    
    /**
     * The little filtering window where you can choose which signs you'd like to see out of so many
     */
    private static class FilterWindow {

        private final Runnable onOpen, onClose, onApply;
        private final boolean closeOnApply, disableShape;
        private SignFilter currentFilter = new SignFilter(null, null, null);

        private final String windowId;

        private boolean shouldOpen = false;

        String[] availCountries, availCategories, availShapes;

        /**
         * Creates a new FilterWindow Instance.
         *
         * @param onOpen Whatever should happen when opening the window.
         *               By default, only the window will open. This functionality cannot be disabled.
         * @param onClose Whatever should happen when closing the window.
         *                By default, only the window will close. This functionality cannot be disabled.
         * @param onApply Whatever should happen when clicking the "Apply" button.
         *                By default, nothing will happen.
         * @param closeOnApply Whether the window should close when clicking the "Apply" button.
         * @param disableShape Whether the shape filter should be disabled (e.g. when selecting a sign texture for a specific sign block)
         * @param windowId The ID of the filter window. Must be unique if you have multiple filter windows.
         */
        public FilterWindow(
                @NotNull Runnable onOpen,
                @NotNull Runnable onClose,
                @NotNull Runnable onApply,
                boolean closeOnApply,
                boolean disableShape,
                @NotNull String windowId
        ) {
            this.onOpen = onOpen;
            this.onClose = onClose;
            this.onApply = onApply;
            this.closeOnApply = closeOnApply;
            this.disableShape = disableShape;
            this.windowId = windowId;
        }

        private final ImInt shapeComboIndex = new ImInt(0);
        private final ImInt catComboIndex = new ImInt(0);
        private final ImInt countComboIndex = new ImInt(0);

        public void render() {
            if (ImGui.beginPopupModal(getWindowId())) {

                // Filtering Options

                // Shape
                if (availShapes != null) {
                    if (disableShape)
                        ImGui.beginDisabled();

                    ImGui.text(tr("Global", "Shape"));
                    ImGui.combo("##shapeCombo", shapeComboIndex, availShapes);

                    if (disableShape)
                        ImGui.endDisabled();
                }

                ImGui.spacing();

                // Category
                if (availCategories != null) {
                    ImGui.text(tr("Global", "Category"));
                    ImGui.combo("##categoryCombo", catComboIndex, availCategories);
                }

                ImGui.spacing();

                // Country
                if (availCountries != null) {
                    ImGui.text(tr("Global", "Country"));
                    ImGui.combo("##countryCombo", countComboIndex, availCountries);
                }

                // Controls (Close & Apply)

                ImGui.separator();

                if (ImGui.button(tr("Global", "Close")))
                    close();

                ImGui.sameLine();

                if (ImGui.button(tr("Global", "Apply")))
                    apply();

                ImGui.endPopup();
            }

            if (shouldOpen) {
                ImGui.openPopup(getWindowId());
                shouldOpen = false;
            }
        }

        public void open(
                SignFilter initialFilter,
                List<SignTexture.CATEGORY> availCategories,
                List<String> availCountries,
                List<SignBlock.SIGN_SHAPE> availShapes
        ) {
            shouldOpen = true;
            onOpen.run();
            this.currentFilter = initialFilter;

            // Add "any" option to the beginning of each list
            List<String> categoryNames = availCategories.stream()
                    .map(SignTexture.CATEGORY::name)
                    .toList();
            List<String> categories = new ArrayList<>(categoryNames);
            categories.add(0, "*");
            this.availCategories = categories.toArray(new String[0]);

            // Also sort countries alphabetically
            List<String> sortedCountries = new ArrayList<>(availCountries);
            sortedCountries.sort(String::compareTo);
            List<String> countries = new ArrayList<>(sortedCountries);
            countries.add(0, "*");
            this.availCountries = countries.toArray(new String[0]);

            List<String> shapeNames = availShapes.stream()
                    .map(Enum::name)
                    .toList();
            List<String> shapes = new ArrayList<>(shapeNames);
            shapes.add(0, "*");
            this.availShapes = shapes.toArray(new String[0]);

            // Combo Indices
            int shapeIdx = (initialFilter.shape() == null) ? 0 : shapes.indexOf(initialFilter.shape().name());
            this.shapeComboIndex.set(Math.max(shapeIdx, 0));

            int catIdx = (initialFilter.category() == null) ? 0 : categories.indexOf(initialFilter.category().name());
            this.catComboIndex.set(Math.max(catIdx, 0));

            int countryIdx = (initialFilter.country() == null) ? 0 : countries.indexOf(initialFilter.country());
            this.countComboIndex.set(Math.max(countryIdx, 0));
        }

        public void close() {
            onClose.run();
            ImGui.closeCurrentPopup();
        }

        public void apply() {
            currentFilter = new SignFilter(
                    availCategories[(catComboIndex.get())].equals("*") ? null : SignTexture.CATEGORY.valueOf(availCategories[catComboIndex.get()]),
                    availCountries[(countComboIndex.get())].equals("*") ? null : availCountries[countComboIndex.get()],
                    availShapes[(shapeComboIndex.get())].equals("*") ? null : SignBlock.SIGN_SHAPE.valueOf(availShapes[shapeComboIndex.get()])
            );
            onApply.run();

            if (closeOnApply)
                close();
        }

        public SignFilter getFilter() {
            return currentFilter;
        }

        private String getWindowId() {
            return tr("Main.SignSelector", "Filter") + "...###FilterWindow" + windowId;
        }
    }
}
