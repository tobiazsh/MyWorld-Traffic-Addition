package at.tobiazsh.myworld.traffic_addition.utils.graphics;

import at.tobiazsh.myworld.traffic_addition.utils.Error;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.function.Consumer;

public class NativeFileDialogs {

    public static String open(FilterItem[] filterItems, String defaultPath, Consumer<String> onAbort, Consumer<Error> onError) {
        NativeFileDialog.NFD_Init();

        int numFilters = filterItems.length;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer outPath = stack.mallocPointer(1);
            NFDFilterItem.Buffer filterList = NFDFilterItem.malloc(numFilters, stack);

            for (int i = 0; i < numFilters; i++) {
                FilterItem item = filterItems[i];
                filterList.get(i)
                        .name(stack.UTF8(item.getName()))
                        .spec(stack.UTF8(String.join(",", item.getExt())));
            }

            int result = NativeFileDialog.NFD_OpenDialog(outPath, filterList, defaultPath);

            if (result == NativeFileDialog.NFD_OKAY) {
                String path = outPath.getStringUTF8(0);
                NativeFileDialog.NFD_FreePath(outPath.get(0));
                return path;
            } else if (result == NativeFileDialog.NFD_CANCEL) {
                onAbort.accept("User canceled the open dialog.");
                return null; // User canceled the dialog
            } else {
                String error = NativeFileDialog.NFD_GetError();
                onError.accept(new Error("Error on open", error));
                throw new RuntimeException("Error (Open): " + error);
            }
        }
    }

    public static String save(FilterItem[] filterItems, String defaultPath, String defaultFileName, Consumer<String> onAbort, Consumer<Error> onError) {
        NativeFileDialog.NFD_Init();

        int numFilters = filterItems.length;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer outPath = stack.mallocPointer(1);
            NFDFilterItem.Buffer filterList = NFDFilterItem.malloc(numFilters, stack);

            for (int i = 0; i < numFilters; i++) {
                FilterItem item = filterItems[i];
                filterList.get(i)
                        .name(stack.UTF8(item.getName()))
                        .spec(stack.UTF8(String.join(",", item.getExt())));
            }

            int result = NativeFileDialog.NFD_SaveDialog(outPath, filterList, defaultPath, defaultFileName);

            if (result == NativeFileDialog.NFD_OKAY) {
                String path = outPath.getStringUTF8(0);
                NativeFileDialog.NFD_FreePath(outPath.get(0));
                return path;
            } else if (result == NativeFileDialog.NFD_CANCEL) {
                onAbort.accept("User canceled the save dialog.");
                return null; // User canceled the dialog
            } else {
                String error = NativeFileDialog.NFD_GetError();
                onError.accept(new Error("Error on save", error));
                throw new RuntimeException("Error (Save): " + error);
            }
        }
    }

    public record FilterItem(String name, String[] ext) {
        public String[] getExt() {
            return ext;
        }

        public String getName() {
            return name;
        }
    }
}
