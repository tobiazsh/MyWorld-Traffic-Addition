package at.tobiazsh.myworld.traffic_addition.datagen.translation;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/*
* My spanish may not be great, but I tried. It was fun at least! :) (although, normally I hate working with translations)
* */

public class MyWorldTrafficAdditionSpanishLangProvider extends FabricLanguageProvider {
    protected MyWorldTrafficAdditionSpanishLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, "es_es", registryLookup);
    }

    @Override
    public void generateTranslations(
            HolderLookup.@NonNull Provider provider,
            @NonNull TranslationBuilder translationBuilder
    ) {
        var ESimGuiTranslations = new SpanishImGuiTranslations();

        ESimGuiTranslations.generate(translationBuilder);

        generateCommandTranslations(translationBuilder);
        generateBlockTranslations(translationBuilder);
    }

    private void generateCommandTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("commands.myworld_traffic_addition.main_command.root", "Esto es el comando principal de MyWorld Traffic Addition. Puedes configurar diversas cosas, ¡pero ten cuidado!");
        translationBuilder.add("commands.myworld_traffic_addition.main_command.dev.set", "Se ha dado %s para %s a %s!");
    }

    private void generateBlockTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("block.myworld_traffic_addition.border_block", "Bloque de Frontera");
        translationBuilder.add("block.myworld_traffic_addition.sign_pole_block", "Poste de Señal");
        translationBuilder.add("block.myworld_traffic_addition.triangular_sign_block", "Señal Triangular");
        translationBuilder.add("block.myworld_traffic_addition.upside_down_triangular_sign_block", "Señal Triangular Invertida");
        translationBuilder.add("block.myworld_traffic_addition.octagonal_sign_block", "Señal Octagonal");
        translationBuilder.add("block.myworld_traffic_addition.round_sign_block", "Señal Redonda");
        translationBuilder.add("block.myworld_traffic_addition.customizable_sign_block", "Señal Personalizable");

        translationBuilder.add("itemGroup.myworld_traffic_addition", "MyWorld Traffic Addition");

        translationBuilder.add("screen.myworld_traffic_addition.sign_pole_rotation_screen", "Diálogo de Rotación del Poste de Señal");

        translationBuilder.add("widget.myworld_traffic_addition.rotation_slider", "Rotar Poste de Señal");
        translationBuilder.add("widget.myworld_traffic_addition.draw_editor_button", "Mostrar Editor");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.check_button", "Poner como Maestro e Inicializar");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.init_status.false", "Sin Inicializar");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.init_status.true", "Inicializado");

        translationBuilder.add("interaction.info.myworld_traffic_addition.customizable_sign.already_being_edited", "Esta señal está siendo editada actualmente por otro jugador. Por lo tanto, no puedes accederla.");
        translationBuilder.add("interaction.info.myworld_traffic_addition.customizable_sign.too_many_elements", "¡Esta señal tiene demasiados elementos! Elimina algunos antes de poder enviarla.");
    }
}
