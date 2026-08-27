package at.tobiazsh.myworld.traffic_addition.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class MyWorldTrafficAdditionEnglishLangProvider extends FabricLanguageProvider {
    protected MyWorldTrafficAdditionEnglishLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, registryLookup);
    }

    @Override
    public void generateTranslations(
            HolderLookup.@NonNull Provider provider,
            @NonNull TranslationBuilder translationBuilder
    ) {
        generatePropertyViewerTranslations(translationBuilder);

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

        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.header", "Text Controls");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.text.input", "Text");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.header", "Font Controls");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.size", "Font Size");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.family", "Font Family");
        translationBuilder.add("text.mwta.sign-editor.property-extension.text.font.apply", "Apply");
    }
}
