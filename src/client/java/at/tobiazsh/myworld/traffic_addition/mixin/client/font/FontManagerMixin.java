package at.tobiazsh.myworld.traffic_addition.mixin.client.font;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import at.tobiazsh.myworld.traffic_addition.font.CustomTrueTypeFontLoader;
import at.tobiazsh.myworld.traffic_addition.font.RuntimeFontRegistry;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(FontManager.class)
public abstract class FontManagerMixin {

    // FontManager.ProviderIndex = in access widener!
    @Inject(method = "loadIndex", at = @At("RETURN"), cancellable = true)
    private void onLoadIndex(ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<FontManager.ProviderIndex>> cir) {
        CompletableFuture<FontManager.ProviderIndex> originalFuture = cir.getReturnValue();                                     // ClassTweaker aka. AccessWidener!
        CompletableFuture<FontManager.ProviderIndex> modifiedFuture = originalFuture.thenApplyAsync(index -> {      // ClassTweaker aka. AccessWidener!
            Map<Identifier, List<Font.FontFilterPair>> fontSets = new HashMap<>(index.fontSets());                              // ClassTweaker aka. AccessWidener!
            List<Font> allProviders = new ArrayList<>(index.allProviders());                                                    // ClassTweaker aka. AccessWidener!

            int counter = 0;
            for (CustomTrueTypeFontLoader loader : RuntimeFontRegistry.getLoaders()) {
                try {
                    Font runtimeFont = loader.loadFont(resourceManager); // uses ResourceManager to load resources from both inside and outside resource packs
                    if (runtimeFont == null) continue;

                    Font.FontFilterPair pair = new Font.FontFilterPair(runtimeFont, FontFilterType.FilterMap.NO_FILTER);
                    Identifier runtimeId = Identifier.of(MyWorldTrafficAddition.MOD_ID, "runtime_font_" + (counter++));
                    fontSets.put(runtimeId, List.of(pair));
                    allProviders.add(runtimeFont);
                    MyWorldTrafficAddition.LOGGER.info("Injected runtime font {}", runtimeId);
                } catch (IOException ex) {
                    MyWorldTrafficAddition.LOGGER.warn("Failed to load runtime font from loader {}, skipping", loader, ex);
                } catch (Throwable t) {
                    MyWorldTrafficAddition.LOGGER.warn("Unexpected error loading runtime font: {}", loader, t);
                }
            }

            return new FontManager.ProviderIndex(fontSets, allProviders);
        }, executor);

        cir.setReturnValue(modifiedFuture);
    }
}
