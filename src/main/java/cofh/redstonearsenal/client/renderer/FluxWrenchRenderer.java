package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.entity.FluxSlashEntity;
import cofh.redstonearsenal.entity.FluxWrenchEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxWrenchRenderer extends EntityRenderer<FluxWrenchEntity> {

    protected static final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public FluxWrenchRenderer(EntityRendererManager manager) {

        super(manager);
    }

    @Override
    public void render(FluxWrenchEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        matrixStackIn.pushPose();
        matrixStackIn.translate(0, entityIn.getBbHeight() * 0.5F, 0);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(90 + entityIn.xRot));
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees((entityIn.tickCount + partialTicks) * 20));
        matrixStackIn.scale(1.25F, 1.25F, 1.25F);
        itemRenderer.renderStatic(entityIn.getItem(), ItemCameraTransforms.TransformType.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(FluxWrenchEntity entity) {

        return PlayerContainer.BLOCK_ATLAS;
    }
}
