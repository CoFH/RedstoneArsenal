package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.entity.FluxSlashEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxSlashRenderer extends EntityRenderer<FluxSlashEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_slash.png");
    private static final RenderType RENDER_TYPE = FluxRenderType.flux(TEXTURE);

    public FluxSlashRenderer(EntityRendererManager manager) {

        super(manager);
    }

    @Override
    public void render(FluxSlashEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, entityIn.getBbHeight() * 0.5F, 0);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.yRotO, entityIn.yRot) - 90));
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.xRotO, entityIn.xRot) + 10));
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(entityIn.zRot));
        matrixStackIn.scale(0.6F, 0.6F, 1.8F);
        MatrixStack.Entry matrixStackEntry = matrixStackIn.last();
        Matrix4f pose = matrixStackEntry.pose();
        Matrix3f normal = matrixStackEntry.normal();
        IVertexBuilder builder = bufferIn.getBuffer(RENDER_TYPE);

        this.vertex(pose, normal, builder, 1, 0, 1, 1, 0, 0, 1, 0, packedLightIn);
        this.vertex(pose, normal, builder, 1, 0, -1, 0, 0, 0, 1, 0, packedLightIn);
        this.vertex(pose, normal, builder, -1, 0, -1, 0, 1, 0, 1, 0, packedLightIn);
        this.vertex(pose, normal, builder, -1, 0, 1, 1, 1, 0, 1, 0, packedLightIn);

        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(FluxSlashEntity entity) {

        return TEXTURE;
    }

    public void vertex(Matrix4f pose, Matrix3f normal, IVertexBuilder builder, float x, float y, float z, float u, float v, int nx, int nz, int ny, int packedLight) {

        builder.vertex(pose, x, y, z).color(255, 255, 255, 200).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, nx, ny, nz).endVertex();
    }

    public static class FluxRenderType extends RenderType {

        public FluxRenderType(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {

            super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
        }

        public static final RenderType flux(ResourceLocation texture) {

            return RenderType.create("flux_projectile",
                    DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true,
                    RenderType.State.builder().setTextureState(new RenderState.TextureState(texture, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .setAlphaState(DEFAULT_ALPHA)
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .createCompositeState(true));
        }
    }
}
