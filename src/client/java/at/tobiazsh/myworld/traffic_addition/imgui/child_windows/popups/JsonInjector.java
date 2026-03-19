package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.popups;

import at.tobiazsh.myworld.traffic_addition.block_entities.CustomizableSignBlockEntity;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementManager;
import at.tobiazsh.myworld.traffic_addition.data.CustomizableSignTextureData;
import at.tobiazsh.myworld.traffic_addition.error.Error;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

import java.util.Optional;

public class JsonInjector {

    public JsonInjector(String id, CustomizableSignBlockEntity blockEntity) {
        this.id = id;
        this.blockEntity = blockEntity;
    }

    protected boolean shouldOpen = false;
    protected ImString text = new ImString(10000);

    protected final String id;
    protected final CustomizableSignBlockEntity blockEntity;

    public void open() {
        shouldOpen = true;
    }

    public void close() {
        ImGui.closeCurrentPopup();
    }

    public void render() {
        if (ImGui.beginPopupModal("Inject JSON##" + id)) {

            ImGui.inputTextMultiline("##jsonInput_" + id, text, -Float.MIN_VALUE, ImGui.getTextLineHeight() * 24, ImGuiInputTextFlags.AlwaysOverwrite | ImGuiInputTextFlags.AllowTabInput);

            if (ImGui.button("Paste from Clipboard", -Float.MIN_VALUE, ImGui.getTextLineHeightWithSpacing())) {
                text = new ImString(ImGui.getClipboardText());
            }

            ImGui.separator();

            if (ImGui.button("Inject"))
                createTexture(text.get()).ifPresent(this::applyTexture);

            ImGui.sameLine();

            if (ImGui.button("Close"))
                close();

            ImGui.endPopup();
        }

        if (shouldOpen) {
            ImGui.openPopup("Inject JSON##" + id);
            shouldOpen = false;
        }
    }

    private void applyTexture(CustomizableSignTextureData textureData) {
        ClientElementManager.getInstance().setData(textureData, blockEntity);
    }

    /**
     * Creates texture data from provided JSON.
     */
    private Optional<CustomizableSignTextureData> createTexture(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        Optional<CustomizableSignTextureData> textureData = Optional.empty();

        try {
            textureData = Optional.of(CustomizableSignTextureData.fromJson(obj));

            if (textureData.get().getBackground().texture == null && textureData.get().getBackground().color == null)
                throw new IllegalArgumentException("Background must have either a texture or a color");

        } catch (IllegalArgumentException e) {
            throwError(new Error(
                    "Unable to inject JSON",
                    "JSON does not contain valid texture data:\n" + e
            ));
        }

        return textureData;
    }

    private void throwError(Error error) {
        ErrorPopup.open(error, this::close);
    }
}
