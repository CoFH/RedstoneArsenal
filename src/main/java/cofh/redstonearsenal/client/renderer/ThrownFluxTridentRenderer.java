package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.entity.ThrownFluxTrident;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;

public class ThrownFluxTridentRenderer extends EntityRenderer<ThrownFluxTrident> {

    private static final ResourceLocation CHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    // private static final ResourceLocation EMPOWERED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private final TridentModel model;

    public ThrownFluxTridentRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
        this.model = new TridentModel(ctx.getModelSet().bakeLayer(ModelLayers.TRIDENT));
    }

    public void render(ThrownFluxTrident p_116111_, float p_116112_, float p_116113_, PoseStack poseStack, MultiBufferSource p_116115_, int p_116116_) {

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(p_116113_, p_116111_.yRotO, p_116111_.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(p_116113_, p_116111_.xRotO, p_116111_.getXRot()) + 90.0F));
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(p_116115_, this.model.renderType(this.getTextureLocation(p_116111_)), false, p_116111_.isFoil());
        this.model.renderToBuffer(poseStack, vertexconsumer, p_116116_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(p_116111_, p_116112_, p_116113_, poseStack, p_116115_, p_116116_);
    }

    public ResourceLocation getTextureLocation(ThrownFluxTrident trident) {

        //return trident.isEmpowered() ? EMPOWERED : CHARGED;
        return CHARGED;
    }

}
