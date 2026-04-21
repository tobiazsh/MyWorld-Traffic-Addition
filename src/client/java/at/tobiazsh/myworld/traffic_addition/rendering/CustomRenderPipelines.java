package at.tobiazsh.myworld.traffic_addition.rendering;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;

import static net.minecraft.client.renderer.RenderPipelines.*;

public class CustomRenderPipelines {

    public static final RenderPipeline RENDERTYPE_CUSTOM_TEXT_INTENSITY;
    public static final RenderPipeline RENDERTYPE_SIGN_ELEMENT_TRANSLUCENT;
    public static final RenderPipeline RENDERTYPE_SIGN_ELEMENT_CUTOUT;
    public static final RenderPipeline RENDERTYPE_SIGN_ELEMENT_SOLID;


    static {
        RENDERTYPE_CUSTOM_TEXT_INTENSITY = RenderPipelines.register(RenderPipeline.builder(TEXT_SNIPPET, FOG_SNIPPET)
                .withLocation("pipeline/text_intensity")
                .withVertexShader("core/rendertype_text_intensity")
                .withFragmentShader("core/rendertype_text_intensity")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false, 0f, 0f))
                .build()
        );

        RENDERTYPE_SIGN_ELEMENT_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/sign_element_translucent")
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler0")
                .withSampler("Sampler2")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .build()
        );

        RENDERTYPE_SIGN_ELEMENT_CUTOUT = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/sign_element_cutout")
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler0")
                .withSampler("Sampler1")
                .withSampler("Sampler2")
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .build()
        );

        RENDERTYPE_SIGN_ELEMENT_SOLID = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/sign_element_solid")
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler0")
                .withSampler("Sampler1")
                .withSampler("Sampler2")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .build()
        );
    }
}
