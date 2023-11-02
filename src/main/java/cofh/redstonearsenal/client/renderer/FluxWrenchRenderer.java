package cofh.redstonearsenal.client.renderer;

import cofh.core.util.helpers.RenderHelper;
import cofh.redstonearsenal.entity.ThrownFluxWrench;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public class FluxWrenchRenderer extends EntityRenderer<ThrownFluxWrench> {

    protected static final ItemRenderer itemRenderer = RenderHelper.renderItem();

    public FluxWrenchRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
    }

    @Override
    public void render(ThrownFluxWrench entityIn, float entityYaw, float partialTicks, PoseStack poseStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        poseStackIn.pushPose();
        poseStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.yRot) + 180));
        poseStackIn.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.xRot) + 90));
        poseStackIn.mulPose(Axis.ZP.rotationDegrees((entityIn.tickCount + partialTicks) * 20));
        poseStackIn.scale(1.25F, 1.25F, 1.25F);
        itemRenderer.renderStatic(entityIn.getItem(), ItemDisplayContext.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, poseStackIn, bufferIn, entityIn.level, 0);
        poseStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, poseStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownFluxWrench entity) {

        return InventoryMenu.BLOCK_ATLAS;
    }

}
