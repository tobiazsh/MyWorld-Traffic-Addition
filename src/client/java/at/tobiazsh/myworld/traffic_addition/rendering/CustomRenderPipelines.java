package at.tobiazsh.myworld.traffic_addition.rendering;

import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static net.minecraft.client.renderer.RenderPipelines.*;

public class CustomRenderPipelines {

    //public static final RenderPipeline RENDERTYPE_CUSTOM_TEXT_INTENSITY;
    public static final RenderPipeline RENDERTYPE_SIGN_ELEMENT_TRANSLUCENT;
    public static final RenderPipeline RENDERTYPE_SIGN_ELEMENT_CUTOUT;
    public static final RenderPipeline RENDERTYPE_SIGN_ELEMENT_SOLID;


    static {
//        RENDERTYPE_CUSTOM_TEXT_INTENSITY = RenderPipelines.register(RenderPipeline.builder()
//                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)
//                .withPrimitiveTopology(PrimitiveTopology.QUADS)
//                .withLocation("pipeline/text_intensity")
//                .withBindGroupLayout(BindGroupLayout.builder()
//                        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
//                        .withUniform("Projection", UniformType.UNIFORM_BUFFER)
//                        .build()
//                )
//                .withVertexShader(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "core/rendertype_text_intensity"))
//                .withFragmentShader(Identifier.fromNamespaceAndPath(MyWorldTrafficAddition.MOD_ID, "core/rendertype_text_intensity"))
//                .withBindGroupLayout(BindGroupLayout.builder()
//                        .withSampler("Sampler0")
//                        .withSampler("Sampler2")
//                        .build()
//                )
//                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false, -1, -10))
//                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
//                .build()
//        );

        RENDERTYPE_SIGN_ELEMENT_TRANSLUCENT = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/sign_element_translucent")
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler1").build())
                .build()
        );

        RENDERTYPE_SIGN_ELEMENT_CUTOUT = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/sign_element_cutout")
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .build()
        );

        RENDERTYPE_SIGN_ELEMENT_SOLID = RenderPipelines.register(RenderPipeline.builder(ENTITY_SNIPPET)
                .withLocation("pipeline/sign_element_solid")
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .build()
        );
    }
}
