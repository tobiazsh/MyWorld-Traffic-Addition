package at.tobiazsh.myworld.traffic_addition.ImGui.ChildWindows.Popups;

import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ExplorerUIComponents.FileView;
import at.tobiazsh.myworld.traffic_addition.ImGui.Utils.ImGuiTools;
import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.Utils.FileSystem;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A popup that allows the user to select a file or save a file.
 * When saving a file, the data to save can be set with {@link FileDialogPopup#setData(String)}. If this is not set, the file will be empty. This must be called BEFORE opening the dialog!
 * When opening a file, the data can be retrieved with {@link FileDialogPopup#getData()} or manually retrieved via the file path in {@link FileDialogPopup#open(Path, FileDialogType, Consumer, String...)} at parameter Consumer.
 * To open, call {@link FileDialogPopup#open(Path, FileDialogType, Consumer, String...)}.
 */
public class FileDialogPopup {

    public enum FileDialogType {
        SAVE,
        OPEN
    }

    // Important for functionality
    private static boolean shouldOpen = false;
    private static boolean isWaitingOnInput = false;
    private static boolean shouldRender = false;

    public static Path currentPath = Paths.get(System.getProperty("user.home"));
    private static ImString pathBarPath = new ImString("", 2048);
    private static FileSystem.Folder directoryContent = null;

    private static ImString fileName = new ImString("", 512);
    private static Path filePath = null;
    private static String fileData = "";

    private static FileDialogType type = FileDialogType.OPEN;

    private static int selectedIndex = -1;

    private static String[] extensions = new String[0];
    private static ImInt selectedExtension = new ImInt(0);
    private static final String baseExtension = "*";

    // Important for design
    private static float interactionBarHeight = 50;

    public static void render() {
        if (!shouldRender) return; // Prevent rendering when not necessary

        ImGui.setNextWindowSizeConstraints(new ImVec2(400, 800), new ImVec2(Float.MAX_VALUE, Float.MAX_VALUE));
        if (ImGui.beginPopupModal(type.name().charAt(0) + type.name().substring(1).toLowerCase() + "###FileDialog")){
            handleHotkeys();
            menuBar();

            drawPathBar();
            drawContent();
            drawInteractionBar();
            ConfirmationPopup.render();

            UserInputPopup.render();

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup("###FileDialog");
            shouldOpen = false;
        }
    }

    private static void drawContent() {
        ImGui.beginChild("##FileExplorerContent", ImGui.getContentRegionAvailX(), (ImGui.getContentRegionAvailY() - interactionBarHeight), true);

        for (int i = 0; i < directoryContent.size(); i++) {
            FileSystem.DirectoryElement element = directoryContent.at(i);
            boolean isSelected = selectedIndex == i;

            FileView.DetailedViewBar bar = new FileView.DetailedViewBar(
                    element.name,
                    LocalDate.now(), LocalDate.now(),
                    element.getSize(),
                    element.getFileExtension(),
                    "##vB" + i,
                    new ImVec4(0.168f, 0.168f, 0.168f, 1f),
                    new ImVec4(0.367f, 0.367f, 0.367f, 0.367f),
                    new ImVec4(1, 1, 1, 1),
                    30,
                    element.isFolder(),
                    isSelected
            );

            if (bar.draw()) {
                if (element.isFolder() && isSelected) {
                    updatePath(currentPath.resolve(element.name));
                    selectedIndex = -1;
                }
                else selectedIndex = i;
            }
        }

        ImGui.endChild();
    }

    private static void drawPathBar() {
        ImGui.pushItemWidth(-Float.MIN_VALUE);

        if (ImGui.inputText("##PathBar", pathBarPath, ImGuiInputTextFlags.EnterReturnsTrue)) {
            if (!updatePath(Paths.get(pathBarPath.get()))) {
                ErrorPopup.open("Invalid Path", "The path you entered is invalid.", () -> pathBarPath.set(currentPath.toString()));
            } else refresh();
        }

        ImGui.popItemWidth();
    }

    private static void menuBar() {
        ImGui.beginChild("##MenuBarRegion", ImGui.getContentRegionAvailX(), 30, false, ImGuiWindowFlags.MenuBar);
        if (ImGui.beginMenuBar()) {

            if (ImGui.menuItem("Parent Directory")) parentDir();
            if (ImGui.menuItem("Refresh")) refresh();
            if (ImGui.menuItem("New Folder...")) newFolder();

            ImGui.endMenuBar();
        }
        ImGui.endChild();
    }

    private static void newFolder() {
        UserInputPopup.open("Folder Name", UserInputPopup.InputType.STRING, (folderName) -> {
            if (folderName.isBlank()) return;

            try {
                Files.createDirectory(currentPath.resolve(folderName));
            } catch (IOException e) {
                ErrorPopup.open("Error", "A fatal error occurred while creating the folder! Please check logs!", () -> {});
                MyWorldTrafficAddition.LOGGER.error("A fatal error occurred while creating folder {}!", currentPath.resolve(folderName));
                throw new RuntimeException(e);
            }

            refresh();
        });
    }

    // Bottom-most bar on the window
    private static void drawInteractionBar() {

        boolean allowConfirm =
                ((type == FileDialogType.OPEN && selectedIndex != -1) ||
                (type == FileDialogType.SAVE && !fileName.get().isEmpty()));

        ImGuiTools.drawRect((int)ImGui.getContentRegionAvailX(), (int)interactionBarHeight, new ImVec2(ImGui.getCursorScreenPos().x, ImGui.getCursorScreenPos().y), new ImVec4(0.168f, 0.168f, 0.168f, 1f));
        ImGui.setCursorPosY(ImGui.getWindowContentRegionMaxY() - interactionBarHeight + (interactionBarHeight - ImGui.getFontSize()) / 2);

        ImGui.indent(ImGui.getStyle().getItemSpacingX());
        ImGui.columns(4, "##InteractionBarAlignment", false);

        ImGui.setColumnWidth(0, 100);
        if (!allowConfirm) ImGui.beginDisabled();
        if (ImGui.button((type == FileDialogType.SAVE) ? "SAVE" : "OPEN", 100 - ImGui.getStyle().getItemSpacingX() * 2, ImGui.getFrameHeight())) confirm();
        if (!allowConfirm) ImGui.endDisabled();
        ImGui.nextColumn();

        ImGui.setColumnWidth(1, 100);
        if (ImGui.button("Cancel", 100 - ImGui.getStyle().getItemSpacingX() * 2, ImGui.getFrameHeight())) close();
        ImGui.nextColumn();

        ImGui.setColumnWidth(2, 200);
        ImGui.pushItemWidth(-Float.MIN_VALUE);
        ImGui.combo("##FileExtensionType", selectedExtension, extensions);
        ImGui.popItemWidth();
        ImGui.nextColumn();

        ImGui.pushItemWidth(-Float.MIN_VALUE);
        ImGui.inputText("##FileNameInput", fileName);

        ImGui.popItemWidth();

        ImGui.columns(1);
    }

    private static void handleHotkeys() {
        if (ImGui.isKeyPressed(ImGuiKey.Delete)) close();
    }
    
    private static void saveFile() {
        String path = currentPath.toAbsolutePath() + "\\" + fileName.get();

        if (!Objects.equals(extensions[selectedExtension.get()], "*")) {
            path += "." + extensions[selectedExtension.get()].toLowerCase();
        }

        filePath = Paths.get(path);

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            e.printStackTrace();

            ErrorPopup.open("Error", "A fatal error occurred while creating parent directories! Please check logs!", () -> {});

            MyWorldTrafficAddition.LOGGER.error("A fatal error occurred while creating parent directories for file {}!", filePath);
            throw new RuntimeException(e);
        }

        File newFile = filePath.toFile();
        Boolean createdSuccessfully;
        try {
            createdSuccessfully = newFile.createNewFile();
        } catch (IOException e) {
            ErrorPopup.open("Error", "A fatal error occurred while creating the file! Please check logs!", () -> {});
            MyWorldTrafficAddition.LOGGER.error("A fatal error occurred while creating file {}!", filePath);
            throw new RuntimeException(e);
        }

        if (!createdSuccessfully) {
            handleFileExists();
            return;
        }

        write(fileData, filePath);

        MyWorldTrafficAddition.LOGGER.debug("Created directories and file as well as wrote to file {} successfully!", filePath);
    }

    // Writes to the file, if save is specified
    private static void write(String data, Path path) {
        try {
            Files.write(path, data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            ErrorPopup.open("Error", "A fatal error occurred while writing data to file! Please check logs!", () -> {});
            MyWorldTrafficAddition.LOGGER.error("A fatal error occurred while writing to file {}! It is likely that the data could not be encoded to UTF-8!", filePath);
            throw new RuntimeException(e);
        }

        close();
    }

    // Will be executed if file already exists in current directory
    private static void handleFileExists() {
        ConfirmationPopup.show("File already exists", "The file you are trying to save already exists. Do you want to overwrite it?", (confirmed) -> {
            if (confirmed) write(fileData, filePath);
        });
    }

    private static void openFile() {

        filePath = currentPath.resolve(directoryContent.at(selectedIndex).name);

        if (!Files.exists(filePath)) {
            ErrorPopup.open("File not found", "The file you are trying to open does not exist.", () -> {});
            return;
        }

        try {
            fileData = Files.readString(filePath);
        } catch (IOException e) {
            ErrorPopup.open("Error", "A fatal error occurred while reading data from file! Please check logs!", () -> {});
            MyWorldTrafficAddition.LOGGER.error("A fatal error occurred while reading from file {}! It is likely that the data could not be decoded from UTF-8!", filePath);
            throw new RuntimeException(e);
        }

        close();
    }

    // "OPEN" / "SAVE"
    private static void confirm() {
        if (type == FileDialogType.SAVE) saveFile();
        else openFile();
    }

    // "CANCEL"
    private static void close() {
        shouldOpen = false;
        shouldRender = false;
        isWaitingOnInput = false;
        ImGui.closeCurrentPopup();
    }

    // Sets the current dir to the parent
    private static void parentDir() {
        updatePath(currentPath.getParent());
    }

    private static void refresh() {
        try {
            directoryContent = FileSystem.listAll(currentPath.toAbsolutePath() + File.separator, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean updatePath(Path path) {
        if (path == null) return false;
        if (!Files.exists(path)) return false;

        FileDialogPopup.currentPath = path;
        FileDialogPopup.pathBarPath = new ImString(path.toString(), 2048);
        refresh();

        return true;
    }

    /**
     * Opens the file dialog popup.
     * @param path The starting path the dialog will open in.
     * @param type The type of dialog to open. ({@link FileDialogType#SAVE} or {@link FileDialogType#OPEN})
     * @param onFileSelected The consumer that will be called when a file is selected. The selected file will be passed as {@link Path} as a parameter. Commands in the lambda expression will be executed after the file has been selected.
     * @param extensions Allowed extensions for the file. If the dialog is opened for saving, the first extension will be the default extension.
     */
    public static void open(Path path, FileDialogType type, Consumer<Path> onFileSelected, String... extensions) {
        FileDialogPopup.shouldRender = true;
        FileDialogPopup.shouldOpen = true;
        FileDialogPopup.isWaitingOnInput = true;
        FileDialogPopup.type = type;
        selectedIndex = -1;

        List<String> ext = new ArrayList<>(Arrays.stream(extensions).toList());
        ext.add(baseExtension);
        FileDialogPopup.extensions = ext.toArray(new String[0]);

        interactionBarHeight = ImGui.getStyle().getItemSpacingX() * 2 + ImGui.getFrameHeight();

        updatePath(path);

        new Thread(() -> {
            while (FileDialogPopup.isWaitingOnInput) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {} // Wait for user
            }
            if (selectedIndex != -1 || !fileName.get().isBlank()) {
                onFileSelected.accept(filePath);
            }
        }).start();
    }

    public static void setData(String data) {
        fileData = data;
    }

    public static String getData() {
        return fileData;
    }
}