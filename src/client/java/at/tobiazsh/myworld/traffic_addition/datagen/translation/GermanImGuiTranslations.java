package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.*;
import org.jspecify.annotations.NonNull;

public class GermanImGuiTranslations {

    public void generate(@NonNull TranslationBuilder translationBuilder) {
        generatePropertyViewerTranslations(translationBuilder);
        generateErrorPopupTranslations(translationBuilder);
        generateElementListWindowTranslations(translationBuilder);
    }

    private void generatePropertyViewerTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.sign-editor.property-viewer.name", "Name");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.name.apply", "Namen anwenden");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions", "Abmessungen");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.lock-ratio", "Seitenverhältnis sperren");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.width", "Breite");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.height", "Höhe");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.x", "X");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.y", "Y");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.center-x", "X Zentrieren");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.center-y", "Y Zentrieren");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.rotation", "Rotation");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.color", "Farbe");

        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.header", "Texteinstellungen");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.input", "Text");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.header", "Schriftarteneinstellungen");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.size", "Schriftgröße");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.family", "Schriftart");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.apply", "Anwenden");
    }

    private void generateErrorPopupTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.error-popup.title", "Fehler");
        translationBuilder.add("text.mwta.error-popup.message", "Anmerkung");
        translationBuilder.add("text.mwta.error-popup.close", "Schließen");
    }

    private void generateElementListWindowTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.element-list.title", "Elementliste");
    }
}
