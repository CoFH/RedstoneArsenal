package cofh.redstonearsenal.client.renderer;

import cofh.redstonearsenal.item.FluxTridentItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;

public class FluxTridentBEWLR extends BlockEntityWithoutLevelRenderer {

    public static final FluxTridentBEWLR INSTANCE = new FluxTridentBEWLR(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    private static final ResourceLocation UNCHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private static final ResourceLocation CHARGED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    private static final ResourceLocation EMPOWERED = new ResourceLocation(ID_REDSTONE_ARSENAL + ":textures/entity/flux_trident.png");
    protected TridentModel tridentModel;

    public FluxTridentBEWLR(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    public void onResourceManagerReload(ResourceManager p_172555_) {
        this.tridentModel = new TridentModel(this.entityModelSet.bakeLayer(ModelLayers.TRIDENT)); //TODO Hek: AT modelSet
    }

    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack, MultiBufferSource buffer, int p_239207_5_, int p_239207_6_) {

        if (stack.getItem() instanceof FluxTridentItem) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            VertexConsumer ivertexbuilder1 = ItemRenderer.getFoilBufferDirect(buffer, tridentModel.renderType(getTextureLocation(stack)), false, stack.hasFoil());
            tridentModel.renderToBuffer(matrixStack, ivertexbuilder1, p_239207_5_, p_239207_6_, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        } else {
            super.renderByItem(stack, transformType, matrixStack, buffer, p_239207_5_, p_239207_6_);
        }
    }

    public ResourceLocation getTextureLocation(ItemStack stack) {

        FluxTridentItem trident = (FluxTridentItem) stack.getItem();
        if (trident.getEnergyStored(stack) > 0) {
            return trident.isEmpowered(stack) ? EMPOWERED : CHARGED;
        }
        return UNCHARGED;
    }

}
