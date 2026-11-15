package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.*;
import org.jspecify.annotations.NonNull;

public class EnglishImGuiTranslations {

    public void generate(@NonNull TranslationBuilder translationBuilder) {
        generatePropertyViewerTranslations(translationBuilder);
        generateErrorPopupTranslations(translationBuilder);
        generateElementListWindowTranslations(translationBuilder);
        generateElementWindowTranslations(translationBuilder);
    }

    private void generatePropertyViewerTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.sign-editor.property-viewer.name", "Name");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.name.apply", "Apply Name");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions", "Dimensions");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.lock-ratio", "Lock Aspect Ratio");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.width", "Width");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.height", "Height");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.x", "X");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.y", "Y");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.center-x", "Center X");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.center-y", "Center Y");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.rotation", "Rotation");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.color", "Color");
        translationBuilder.add("text.myworld_traffic_addition.sign-editor.property-viewer.invalid-selection", "No or too many elements selected.");

        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.header", "Text");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.input", "Text");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.header", "Font");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.size", "Font Size");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.family", "Font Family");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.apply", "Apply");
    }

    private void generateElementWindowTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.myworld_traffic_addition.element-list.group.element-not-found.header", "Grouping failed");
        translationBuilder.add("text.myworld_traffic_addition.element-list.group.element-not-found.content", "The other element could not be found!");

        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.cut", "Cut");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.copy", "Copy");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.delete", "Delete");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.duplicate", "Duplicate");

        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group", "Group...");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.above", "Group with above");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.below", "Group with below");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.selected", "Group with selected");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.free", "Free from group");
    }

    private void generateErrorPopupTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.error-popup.title", "Error");
        translationBuilder.add("text.mwta.error-popup.message", "Message");
        translationBuilder.add("text.mwta.error-popup.close", "Close");
    }

    private void generateElementListWindowTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.element-list.title", "Element List");
    }
}
