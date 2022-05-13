package cofh.redstonearsenal.client.renderer;

import cofh.core.util.helpers.RenderHelper;
import cofh.redstonearsenal.entity.ThrownFluxWrench;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class FluxWrenchRenderer extends EntityRenderer<ThrownFluxWrench> {

    protected static final ItemRenderer itemRenderer = RenderHelper.renderItem();

    public FluxWrenchRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
    }

    @Override
    public void render(ThrownFluxWrench entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.yRot) + 180));
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.xRot) + 90));
        matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees((entityIn.tickCount + partialTicks) * 20));
        matrixStackIn.scale(1.25F, 1.25F, 1.25F);
        itemRenderer.renderStatic(entityIn.getItem(), ItemTransforms.TransformType.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, 0);
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownFluxWrench entity) {

        return InventoryMenu.BLOCK_ATLAS;
    }

}
