package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import org.jspecify.annotations.NonNull;

public class SpanishImGuiTranslations {

    public void generate(@NonNull TranslationBuilder translationBuilder) {
        generatePropertyViewerTranslations(translationBuilder);
        generateErrorPopupTranslations(translationBuilder);
        generateElementListWindowTranslations(translationBuilder);
    }

    private void generatePropertyViewerTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.sign-editor.property-viewer.name", "Nombre");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.name.apply", "Aplicar Nombre");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions", "Dimesiones");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.lock-ratio", "Bloquear Relación de Aspecto");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.width", "Ancho");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.height", "Alto");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.x", "X");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.y", "Y");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.center-x", "Mover al centro X");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.dimensions.center-y", "Mover al centro Y");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.rotation", "Rotación");
        translationBuilder.add("text.mwta.sign-editor.property-viewer.color", "Color");

        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.header", "Opciones de Texto");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.input", "Texto");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.header", "Opciones de Fuente");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.size", "Tamanño de Fuente");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.family", "Familia de Fuente");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.apply", "Confirmar");
    }

    private void generateErrorPopupTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.error-popup.title", "Error");
        translationBuilder.add("text.mwta.error-popup.message", "Mensaje");
        translationBuilder.add("text.mwta.error-popup.close", "Cerrar");
    }

    private void generateElementListWindowTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("text.mwta.element-list.title", "Lista de Elementos");
    }
}
