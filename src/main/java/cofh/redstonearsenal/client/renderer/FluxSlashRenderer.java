package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.entity.FluxSlash;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxSlashRenderer extends EntityRenderer<FluxSlash> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_slash.png");
    private static final RenderType RENDER_TYPE = FluxRenderType.flux(TEXTURE);

    public FluxSlashRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
    }

    @Override
    public void render(FluxSlash entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        matrixStackIn.pushPose();

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.yRot) - 90));
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.xRot) + 10));
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(entityIn.zRot));
        matrixStackIn.scale(0.6F, 0.6F, 1.8F);
        PoseStack.Pose matrixStackEntry = matrixStackIn.last();
        Matrix4f pose = matrixStackEntry.pose();
        Matrix3f normal = matrixStackEntry.normal();
        VertexConsumer builder = bufferIn.getBuffer(RENDER_TYPE);

        this.vertex(pose, normal, builder, 1, 0, 1, 1, 0, 0, 1, 0, packedLightIn);
        this.vertex(pose, normal, builder, 1, 0, -1, 0, 0, 0, 1, 0, packedLightIn);
        this.vertex(pose, normal, builder, -1, 0, -1, 0, 1, 0, 1, 0, packedLightIn);
        this.vertex(pose, normal, builder, -1, 0, 1, 1, 1, 0, 1, 0, packedLightIn);

        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(FluxSlash entity) {

        return TEXTURE;
    }

    public void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer builder, float x, float y, float z, float u, float v, int nx, int nz, int ny, int packedLight) {

        builder.vertex(pose, x, y, z).color(255, 255, 255, 200).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, nx, ny, nz).endVertex();
    }

    public static class FluxRenderType extends RenderType {

        public FluxRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {

            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        public static final RenderType flux(ResourceLocation texture) {

            return RenderType.create("flux_projectile",
                    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
                    RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .setShaderState(NEW_ENTITY_SHADER)
                            // alpha?
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .createCompositeState(true));
        }

    }

}
