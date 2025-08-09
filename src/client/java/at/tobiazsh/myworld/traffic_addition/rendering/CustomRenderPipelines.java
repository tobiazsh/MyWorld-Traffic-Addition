package at.tobiazsh.myworld.traffic_addition.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;

import static net.minecraft.client.gl.RenderPipelines.FOG_SNIPPET;
import static net.minecraft.client.gl.RenderPipelines.TEXT_SNIPPET;

public class CustomRenderPipelines {

    public static final RenderPipeline RENDERTYPE_CUSTOM_TEXT_INTENSITY;

    static {
        RENDERTYPE_CUSTOM_TEXT_INTENSITY = RenderPipelines.register(RenderPipeline.builder(TEXT_SNIPPET, FOG_SNIPPET)
                .withLocation("pipeline/text_intensity")
                .withVertexShader("core/rendertype_text_intensity")
                .withFragmentShader("core/rendertype_text_intensity")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withDepthBias(0f, 0f).build());
    }
}
