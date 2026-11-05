package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;
import at.tobiazsh.myworld.traffic_addition.font.ServerFont;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.DefaultFonts;
import at.tobiazsh.myworld.traffic_addition.font.FontRepository;
import dev.tobiazsh.imguib3d.client.font.ImGuiFontScope;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * Extension class for text elements supposed to be used with {@link ElementPropertyViewer}.
 */
public class PropertyViewerTextExtension implements PropertyViewerExtension {

    private final ImString textBuffer = new ImString(512);
    private TextElementClient element;

    private final ImFloat fontSize = new ImFloat();
    private final ImInt fontIndex = new ImInt();
    private List<FontDefinition> availableFonts = new ArrayList<>();
    private List<String> availableFontNames = new ArrayList<>();

    private final ImGuiFontScope fontScope = ImGuiFontScope.create();

    @Override
    public void render(String id) {
        renderTextControls(id);
        ImGui.separator();
        renderFontControls(id);
    }

    private void renderTextControls(String id) {
        fontScope.push(DefaultFonts.RobotoBold);
        ImGui.text(trIC("text.mwta.sign-editor.property-extension.text.text.header", id, "textCtrlHeader"));
        fontScope.pop();

        if (ImGui.inputText(
                trIC("text.mwta.sign-editor.property-extension.text.text.input", id, "textCtrlInput"),
                textBuffer)
        ) {
            // TODO: Make it only activate when the user presses enter or clicks away, not on every keystroke
            element.setText(textBuffer.get());
        }
    }

    private void renderFontControls(String id) {
        if (ImGui.beginChild(id + "fontControls")) {
            fontScope.push(DefaultFonts.RobotoBold);
            ImGui.text(trIC("text.mwta.sign-editor.property-extension.text.font.header", id, "fontCtrlHeader"));
            fontScope.pop();

            ImGui.inputFloat(
                    trIC("text.mwta.sign-editor.property-extension.text.font.size", id, "fontCtrlSize"),
                    fontSize
            );

            ImGui.combo(
                    trIC("text.mwta.sign-editor.property-extension.text.font.family", id, "fontCtrlFamily"),
                    fontIndex,
                    availableFontNames.toArray(new String[0]),
                    availableFonts.size()
            );

            ImGui.endChild();
        }

        if (ImGui.button(
                trIC("text.mwta.sign-editor.property-extension.text.font.apply", id, "fontApplyBtn")
        )) {
            FontDefinition selectedFont = availableFonts.get(fontIndex.get());
            element.setFont(ServerFont.of(selectedFont.getLogicalFont().fontHash(), fontSize.get()));
        }
    }

    @Override
    public void setElement(ClientElementInterface element) {
        if (!(element instanceof TextElementClient textElement))
            throw new IllegalArgumentException("PropertyViewerTextExtension can only be used with TextElementClient");

        this.element = textElement;
        this.textBuffer.set(textElement.getText());
        this.fontSize.set(textElement.getFont().fontSize());

        this.availableFonts = FontRepository.getInstance().getFonts();
        this.availableFontNames = availableFonts.stream().map(FontDefinition::getDisplayName).toList();
    }
}
