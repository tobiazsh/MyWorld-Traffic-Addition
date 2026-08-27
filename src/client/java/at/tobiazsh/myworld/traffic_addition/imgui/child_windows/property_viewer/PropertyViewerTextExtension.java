package at.tobiazsh.myworld.traffic_addition.imgui.child_windows.property_viewer;

import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.ClientElementInterface;
import at.tobiazsh.myworld.traffic_addition.customizable_sign.elements.TextElementClient;
import at.tobiazsh.myworld.traffic_addition.font.BasicFont;
import at.tobiazsh.myworld.traffic_addition.font.FontDefinition;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.FontHelper;
import at.tobiazsh.myworld.traffic_addition.imgui.fonts.FontRepository;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static at.tobiazsh.myworld.traffic_addition.language.MinecraftTranslationHelper.trIC;

/**
 * Extension class for text elements supposed to be used with {@link ElementPropertyViewer}.
 */
public class PropertyViewerTextExtension implements PropertyViewerExtension {

    private final AtomicBoolean pushedBold = new AtomicBoolean(false);
    private final ImString textBuffer = new ImString(512);
    private TextElementClient element;

    private final ImFloat fontSize = new ImFloat();
    private final ImInt fontIndex = new ImInt();
    private List<FontDefinition> availableFonts = new ArrayList<>();
    private List<String> availableFontNames = new ArrayList<>();

    @Override
    public void render(String id) {
        renderTextControls(id);
        ImGui.separator();
        renderFontControls(id);
    }

    private void renderTextControls(String id) {
        FontHelper.pushRobotoBold(pushedBold);
        ImGui.text(trIC("text.mwta.sign-editor.property-extension.text.text.header", id, "textCtrlHeader"));
        FontHelper.popRobotoBold(pushedBold);

        if (ImGui.inputText(
                trIC("text.mwta.sign-editor.property-extension.text.text.input", id, "textCtrlInput"),
                textBuffer)
        ) {
            // Make it only activate when the user presses enter or clicks away, not on every keystroke
            System.out.println("Text changed to: " + textBuffer.get());
            element.setText(textBuffer.get());
        }
    }

    private void renderFontControls(String id) {
        if (ImGui.beginChild(id + "fontControls")) {
            FontHelper.pushRobotoBold(pushedBold);
            ImGui.text(trIC("text.mwta.sign-editor.property-extension.text.font.header", id, "fontCtrlHeader"));
            FontHelper.popRobotoBold(pushedBold);

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
            element.setFont(new BasicFont(
                    selectedFont.getId(), // TODO: Switch up everything font-related to use FontDefinition instead of BasicFont
                    fontSize.get()
            ));
        }
    }

    @Override
    public void setElement(ClientElementInterface element) {
        if (!(element instanceof TextElementClient textElement))
            throw new IllegalArgumentException("PropertyViewerTextExtension can only be used with TextElementClient");

        this.element = textElement;
        this.textBuffer.set(textElement.getText());
        this.fontSize.set(textElement.getFont().getFontSize());

        this.availableFonts = FontRepository.getInstance().getFonts();
        this.availableFontNames = availableFonts.stream().map(FontDefinition::getDisplayName).toList();
    }
}
