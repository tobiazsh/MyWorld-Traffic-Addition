package at.tobiazsh.myworld.traffic_addition.datagen.translation;

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
        var ENimGuiTranslations = new EnglishImGuiTranslations();

        ENimGuiTranslations.generate(translationBuilder);

        generateCommandTranslations(translationBuilder);
        generateBlockTranslations(translationBuilder);
    }

    private void generateCommandTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("commands.myworld_traffic_addition.main_command.root", "This is the main command of MyWorld Traffic Addition. You can configure and execute different things with it, but be cautious!");
        translationBuilder.add("commands.myworld_traffic_addition.main_command.dev.set", "Set %s for %s to %s!");
    }

    private void generateBlockTranslations(@NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add("block.myworld_traffic_addition.border_block", "Border Block");
        translationBuilder.add("block.myworld_traffic_addition.special_block", "Special Block");
        translationBuilder.add("block.myworld_traffic_addition.sign_pole_block", "Sign Pole");
        translationBuilder.add("block.myworld_traffic_addition.triangular_sign_block", "Triangular Sign");
        translationBuilder.add("block.myworld_traffic_addition.upside_down_triangular_sign_block", "Upside Down Triangular Sign");
        translationBuilder.add("block.myworld_traffic_addition.octagonal_sign_block", "Octagonal Sign");
        translationBuilder.add("block.myworld_traffic_addition.round_sign_block", "Round Sign");
        translationBuilder.add("block.myworld_traffic_addition.customizable_sign_block", "Customizable Sign");

        translationBuilder.add("itemGroup.myworld_traffic_addition", "MyWorld Traffic Addition");

        translationBuilder.add("screen.myworld_traffic_addition.sign_pole_rotation_screen", "Sign Pole Rotation Screen");

        translationBuilder.add("widget.myworld_traffic_addition.rotation_slider", "Rotate Sign Pole");
        translationBuilder.add("widget.myworld_traffic_addition.draw_editor_button", "Reveal Editor");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.check_button", "Make Master and Initialize");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.init_status.false", "Uninitialized");
        translationBuilder.add("widget.myworld_traffic_addition.customizable_sign_edit_screen.init_status.true", "Initialized");

        translationBuilder.add("interaction.info.myworld_traffic_addition.customizable_sign.already_being_edited", "This sign is currently being edited by another player and you may thus not access it.");
        translationBuilder.add("interaction.info.myworld_traffic_addition.customizable_sign.too_many_elements", "This sign has too many elements! Remove a few before you can submit it.");
    }
}
