package at.tobiazsh.myworld.traffic_addition.imgui.main_windows;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.algorithms.FuzzySearch;
import at.tobiazsh.myworld.traffic_addition.blocks.SignBlock;
import at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups.ErrorPopup;
import at.tobiazsh.myworld.traffic_addition.imgui.utils.SignFilter;
import at.tobiazsh.myworld.traffic_addition.utils.FileSystem;
import at.tobiazsh.myworld.traffic_addition.utils.exception.SignTextureParseException;
import at.tobiazsh.myworld.traffic_addition.utils.sign.SignTexture;
import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static at.tobiazsh.myworld.traffic_addition.language.JenguaTranslator.tr;

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

    public void render() {
        if (!shouldRender)
            return;

        ImGui.begin(tr("Main.SignSelector", "Sign Selector"));

        if (ImGui.button(tr("Global", "Close")))
            close();

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

        if (ImGui.button("Select")) {
            System.out.println("Selected texture: " + results.get(selectedIndex.get()));
        }

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

        textureDatabase = filterResults(textures.get(), filter);
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

    /**
     * Opens and initialized the sign selector window for the current sign type
     */
    public void open(SignBlock.SIGN_SHAPE signType) {
        shouldRender = true;
        this.signType = signType;
        this.filter = new SignFilter(null, null, signType); // Filter for the current sign type
        refresh();
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
}
