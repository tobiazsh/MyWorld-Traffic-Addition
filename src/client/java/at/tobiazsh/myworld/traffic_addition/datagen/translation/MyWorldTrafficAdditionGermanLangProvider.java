package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class MyWorldTrafficAdditionGermanLangProvider extends FabricLanguageProvider {
    protected MyWorldTrafficAdditionGermanLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, "de_de", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder translationBuilder) {
        var DEimGuiTranslations = new GermanImGuiTranslations();

        DEimGuiTranslations.generate(translationBuilder);

        generateBlockTranslations(translationBuilder);
        generateCommandTranslations(translationBuilder);
    }

    private void generateCommandTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("commands.myworld_traffic_addition.main_command.root", "Das ist der Hauptbefehl von MyWorld Traffic Addition. Du kannst damit verschiedenste Dinge konfigurieren, aber sei vorsichtig dabei!");
        translationBuilder.add("commands.myworld_traffic_addition.main_command.dev.set", "%s für %s auf %s gesetzt!");
    }

    private void generateBlockTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("block.myworld_traffic_addition.border_block", "Grenzblock");
        translationBuilder.add("block.myworld_traffic_addition.sign_pole_block", "Schildpfosten");
        translationBuilder.add("block.myworld_traffic_addition.triangular_sign_block", "Dreiecksschild");
        translationBuilder.add("block.myworld_traffic_addition.upside_down_triangular_sign_block", "Umgedrehtes Dreiecksschild");
        translationBuilder.add("block.myworld_traffic_addition.octagonal_sign_block", "Achtecksschild");
        translationBuilder.add("block.myworld_traffic_addition.round_sign_block", "Rundschild");
        translationBuilder.add("block.myworld_traffic_addition.customizable_sign_block", "Anpassbares Schild");

        translationBuilder.add("itemGroup.myworld_traffic_addition", "MyWorld Traffic Addition");

        translationBuilder.add("screen.myworld_traffic_addition.sign_pole_rotation_screen", "Schildpfosten Drehdialog");

        translationBuilder.add("widget.myworld_traffic_addition.rotation_slider", "Schildpfosten drehen");
        translationBuilder.add("widget.myworld_traffic_addition.draw_editor_button", "Manipulator anzeigen");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.check_button", "Master ernennen und initialisieren");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.init_status.false", "Nicht initialisiert");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.init_status.true", "Initialisiert");

        translationBuilder.add("interaction.info.myworld_traffic_addition.customizable_sign.already_being_edited", "Dieses schild wird gerade von jemand anderem bearbeitet. Du kannst daher darauf nicht zugreifen.");
        translationBuilder.add("interaction.info.myworld_traffic_addition.customizable_sign.too_many_elements", "Dieses Schild hat derzeit zu viele Elemente! Entferne ein paar, bevor du es absenden kannst.");
    }
}
