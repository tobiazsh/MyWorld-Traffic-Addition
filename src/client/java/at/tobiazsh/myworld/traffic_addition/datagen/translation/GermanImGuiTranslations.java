package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.*;
import org.jspecify.annotations.NonNull;

public class GermanImGuiTranslations {

    public void generate(@NonNull TranslationBuilder translationBuilder) {
        generatePropertyViewerTranslations(translationBuilder);
        generateErrorPopupTranslations(translationBuilder);
        generateElementListWindowTranslations(translationBuilder);
        generateElementWindowTranslations(translationBuilder);
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
        translationBuilder.add("text.myworld_traffic_addition.sign-editor.property-viewer.invalid-selection", "Keines oder zu viele Elemente ausgewählt.");

        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.header", "Texteinstellungen");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.input", "Text");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.header", "Schriftarteneinstellungen");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.size", "Schriftgröße");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.family", "Schriftart");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.apply", "Anwenden");
    }

    private void generateElementWindowTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.myworld_traffic_addition.element-list.group.below.none-below.header", "Gruppierung fehlgeschlagen!");
        translationBuilder.add("text.myworld_traffic_addition.element-list.group.element-not-found.content", "Das andere Element konnte nicht gefunden werden.");

        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.cut", "Ausschneiden");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.copy", "Kopieren");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.paste-in-place", "Hier einfügen");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.paste", "Einfügen");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.delete", "Löschen");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.duplicate", "Duplizieren");

        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group", "Gruppieren...");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.above", "Mit oben gruppieren");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.below", "Mit unten gruppieren");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.selected", "Mit ausgewählten gruppieren");
        translationBuilder.add("text.myworld_traffic_addition.element-list.context-menu.group.free", "Von Gruppe befreien");
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
